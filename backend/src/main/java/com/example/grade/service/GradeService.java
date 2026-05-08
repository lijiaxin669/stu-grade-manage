package com.example.grade.service;

import com.example.grade.common.ErrorCode;
import com.example.grade.domain.*;
import com.example.grade.dto.grade.CourseStatisticsDto;
import com.example.grade.dto.grade.GradeDto;
import com.example.grade.dto.grade.GradeCreateDto;
import com.example.grade.dto.grade.GradeUpdateDto;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.*;
import com.example.grade.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public void createGrade(CustomUserDetails operator, GradeCreateDto dto) {
        boolean isTeacher = operator.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Course course = courseRepository.findById(dto.getCourseId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "课程不存在"));

        if (!course.getTeacher().getId().equals(operator.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "学生未选修该课程"));

        if (gradeRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "成绩已存在，请使用更新接口");
        }

        Grade grade = new Grade();
        grade.setEnrollment(enrollment);
        grade.setScore(java.math.BigDecimal.valueOf(dto.getScore()));
        if (dto.getType() != null && !dto.getType().isBlank()) grade.setType(dto.getType());
        gradeRepository.save(grade);
    }

    @Transactional
    public void updateGrade(CustomUserDetails operator, Long gradeId, GradeUpdateDto dto) {
        boolean isTeacher = operator.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Grade grade = gradeRepository.findById(gradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!grade.getEnrollment().getCourse().getTeacher().getId().equals(operator.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        grade.setScore(java.math.BigDecimal.valueOf(dto.getScore()));
        if (dto.getType() != null && !dto.getType().isBlank()) grade.setType(dto.getType());
        grade.setUpdatedAt(java.time.LocalDateTime.now());
        gradeRepository.save(grade);
    }

    @Transactional
    public void deleteGrade(CustomUserDetails operator, Long gradeId) {
        boolean isTeacher = operator.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Grade grade = gradeRepository.findById(gradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!grade.getEnrollment().getCourse().getTeacher().getId().equals(operator.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        gradeRepository.delete(grade);
    }

    public List<GradeDto> listGrades(Long userId, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities, Long courseId) {
        boolean isTeacher = authorities.contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        boolean isSuperAdmin = authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        
        if (isSuperAdmin) {
             // Admin sees all grades (optionally filtered by courseId)
             return gradeRepository.findByTeacher(null, courseId);
        }

        if (isTeacher) {
            return gradeRepository.findByTeacher(userId, courseId);
        }
        
        return gradeRepository.findByStudent(userId);
    }
    
    public CourseStatisticsDto getStats(Long courseId, Long requestUserId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // Permission: If not Admin, must be Teacher of this course
        if (!isAdmin && !course.getTeacher().getId().equals(requestUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        com.example.grade.dto.grade.GradeStatsProjection stats = gradeRepository.getStatistics(courseId);
        
        if (stats.getTotalCount() == 0) {
            return new CourseStatisticsDto(courseId, course.getName(), 0.0, 0.0, 0L, 0L, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO);
        }

        java.math.BigDecimal passLine = java.math.BigDecimal.valueOf(60);
        Long passed = gradeRepository.countPassedStudents(courseId, passLine);
        
        double passRate = (double) passed / stats.getTotalCount();
        
        // Round avg to 2 decimal places
        Double avg = stats.getAvgScore() != null ? 
                     java.math.BigDecimal.valueOf(stats.getAvgScore())
                         .setScale(2, java.math.RoundingMode.HALF_UP).doubleValue() 
                     : 0.0;
        
        // Round passRate to 2 decimal places implies percentage usually, but let's keep 0-1 float or round it.
        passRate = java.math.BigDecimal.valueOf(passRate)
                     .setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();

        return new CourseStatisticsDto(
            courseId, 
            course.getName(), 
            avg, 
            passRate, 
            stats.getTotalCount(), 
            passed,
            stats.getMaxScore(),
            stats.getMinScore()
        );
    }

    public void exportGrades(Long courseId, Long userId, boolean isAdmin, jakarta.servlet.http.HttpServletResponse response) {
        // 1. Check Course & Permission
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!isAdmin && !course.getTeacher().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 2. Setup Response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "Grades_" + course.getName() + "_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        // 3. Create Workbook (SXSSF for handling large data with sliding window)
        // Window size of 100 rows in memory, rest flushed to disk
        // Window size of 100 rows in memory, rest flushed to disk
        try (org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("成绩表");
            
            // Header
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "用户名", "成绩", "录入时间"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 4. Batch Query & Write
            int pageSize = 1000;
            int pageNo = 0;
            int rowNum = 1;
            
            while (true) {
                org.springframework.data.domain.Page<GradeDto> page = gradeRepository.findByCourse(
                    courseId, 
                    org.springframework.data.domain.PageRequest.of(pageNo, pageSize)
                );
                
                if (page.isEmpty()) break;

                for (GradeDto dto : page.getContent()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(dto.getStudentId());
                    row.createCell(1).setCellValue(dto.getStudentName());
                    row.createCell(2).setCellValue(dto.getStudentUsername());
                    // Handle BigDecimal score
                    if (dto.getScore() != null) {
                        row.createCell(3).setCellValue(dto.getScore().doubleValue());
                    }
                    if (dto.getGradedAt() != null) {
                        row.createCell(4).setCellValue(dto.getGradedAt().toString());
                    }
                }
                
                if (!page.hasNext()) break;
                pageNo++;
            }

            // Write to stream
            workbook.write(response.getOutputStream());
            
            // Dispose temporary files
            workbook.dispose();
            
        } catch (java.io.IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
