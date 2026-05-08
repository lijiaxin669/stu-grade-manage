package com.example.grade.service;

import com.example.grade.common.ErrorCode;
import com.example.grade.domain.Course;
import com.example.grade.domain.Enrollment;
import com.example.grade.domain.User;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.CourseRepository;
import com.example.grade.repository.EnrollmentRepository;
import com.example.grade.repository.GradeRepository;
import com.example.grade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;

    @Transactional
    public void addStudent(Long operatorId, Long courseId, Long studentId, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        boolean isSuperAdmin = authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        if (!isSuperAdmin) {
            throw new BusinessException(com.example.grade.common.ErrorCode.FORBIDDEN);
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(com.example.grade.common.ErrorCode.NOT_FOUND));

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(com.example.grade.common.ErrorCode.USER_NOT_EXIST));

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessException(com.example.grade.common.ErrorCode.PARAM_ERROR.getCode(), "该学生已在课程名单中");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void removeStudent(Long operatorId, Long courseId, Long studentId, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        boolean isSuperAdmin = authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        if (!isSuperAdmin) {
            throw new BusinessException(com.example.grade.common.ErrorCode.FORBIDDEN);
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(com.example.grade.common.ErrorCode.NOT_FOUND));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
            .orElseThrow(() -> new BusinessException(com.example.grade.common.ErrorCode.NOT_FOUND, "未找到课程名单记录"));

        if (gradeRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new BusinessException(com.example.grade.common.ErrorCode.FORBIDDEN.getCode(), "该学生已录入成绩，禁止移出名单");
        }

        enrollmentRepository.delete(enrollment);
    }

    public java.util.List<com.example.grade.dto.enrollment.EnrollmentDto> listByCourse(Long userId, Long courseId, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        boolean isSuperAdmin = authorities.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        
        // Check Course
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(com.example.grade.common.ErrorCode.NOT_FOUND));
            
        // Permission
        if (!isSuperAdmin && !course.getTeacher().getId().equals(userId)) {
            throw new BusinessException(com.example.grade.common.ErrorCode.FORBIDDEN);
        }
        
        return enrollmentRepository.findByCourseId(courseId).stream()
            .map(e -> new com.example.grade.dto.enrollment.EnrollmentDto(
                e.getId(), 
                e.getStudent().getId(), 
                e.getStudent().getRealName(), 
                e.getStudent().getUsername(),
                e.getCourse().getId()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
}
