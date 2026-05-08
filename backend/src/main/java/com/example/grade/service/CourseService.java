package com.example.grade.service;

import com.example.grade.common.ErrorCode;
import com.example.grade.domain.Course;
import com.example.grade.domain.User;
import com.example.grade.dto.course.CourseCreateDto;
import com.example.grade.dto.course.CourseDto;
import com.example.grade.dto.course.CourseUpdateDto;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.CourseRepository;
import com.example.grade.repository.UserRepository;
import com.example.grade.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

	    private final CourseRepository courseRepository;
	    private final UserRepository userRepository;
	    private final com.example.grade.repository.GradeRepository gradeRepository;
	    private final com.example.grade.repository.EnrollmentRepository enrollmentRepository;

		    @Transactional(readOnly = true)
		    public List<CourseDto> listCourses(CustomUserDetails user, Long studentId) {
		        boolean isSuperAdmin = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
		        boolean isTeacher = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
		        boolean isStudent = !isSuperAdmin && !isTeacher;

		        // 如果传入了 studentId，只返回该学生已选的课程
		        if (studentId != null) {
		            // Student can only query their own enrollments
		            if (isStudent && !studentId.equals(user.getUserId())) {
		                throw new BusinessException(ErrorCode.FORBIDDEN);
		            }
		            List<com.example.grade.domain.Enrollment> enrollments = enrollmentRepository.findByStudentIdWithCourse(studentId);
		            return enrollments.stream()
		                    .map(e -> toDto(e.getCourse()))
		                    .collect(Collectors.toList());
		        }

		        // Student: default to "my courses" when no studentId is provided
		        if (isStudent) {
		            List<com.example.grade.domain.Enrollment> enrollments = enrollmentRepository.findByStudentIdWithCourse(user.getUserId());
		            return enrollments.stream()
		                    .map(e -> toDto(e.getCourse()))
		                    .collect(Collectors.toList());
		        }

		        List<Course> courses;
	        if (isSuperAdmin || !isTeacher) { 
	            // 超级管理员看所有，学生(非Teacher)看所有(可选/已选逻辑可在此扩展)
	            // 简单实现：学生由于看"可选"，所以也是看所有
	            courses = courseRepository.findAll();
	        } else {
	            // 教师只看自己的
	            courses = courseRepository.findByTeacherId(user.getUserId());
        }

        return courses.stream().map(this::toDto).collect(Collectors.toList());
    }

	    @Transactional
	    public void createCourse(CustomUserDetails currentUser, CourseCreateDto dto) {
	        if (courseRepository.existsByNameAndSemester(dto.getName(), dto.getSemester())) {
	            throw new BusinessException(2004, "该学期已存在同名课程");
	        }

        Course course = new Course();
        BeanUtils.copyProperties(dto, course);

	        Long teacherId = dto.getTeacherId();
	        boolean isSuperAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
	        boolean isTeacher = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
	        
	        // 如果是教师创建，强制设为自己
	        if (isTeacher && !isSuperAdmin) {
	            teacherId = currentUser.getUserId();
	        } else {
	            // 超级管理员：必须指定教师ID
	            if (teacherId == null) {
	                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "创建课程必须指定教师");
	            }
	        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        
        // 可选：校验该用户是否真的是教师角色 (略)
        
        course.setTeacher(teacher);
        courseRepository.save(course);
    }

	    @Transactional
	    public void updateCourse(CustomUserDetails currentUser, Long courseId, CourseUpdateDto dto) {
	        Course course = courseRepository.findById(courseId)
	                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

	        // 校验权限：教师只能改自己的
	        boolean isSuperAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
	        boolean isTeacher = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));

	        if (!isSuperAdmin && isTeacher) {
	            if (!course.getTeacher().getId().equals(currentUser.getUserId())) {
	                throw new BusinessException(ErrorCode.FORBIDDEN);
	            }
	            // 教师不可转让课程
            if (dto.getTeacherId() != null && !dto.getTeacherId().equals(course.getTeacher().getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "教师无权转让课程");
            }
        }

        if (StringUtils.hasText(dto.getName())) course.setName(dto.getName());
        if (StringUtils.hasText(dto.getDescription())) course.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getSemester())) course.setSemester(dto.getSemester());
        if (dto.getCredits() != null) course.setCredits(dto.getCredits());

	        // 超级管理员可变更教师
	        if (isSuperAdmin && dto.getTeacherId() != null) {
	            User teacher = userRepository.findById(dto.getTeacherId())
	                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
	            course.setTeacher(teacher);
	        }

        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

	    @Transactional
	    public void deleteCourse(CustomUserDetails currentUser, Long courseId) {
	        Course course = courseRepository.findById(courseId)
	                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

	        boolean isSuperAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
	        boolean isTeacher = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
	        if (!isSuperAdmin && isTeacher && !course.getTeacher().getId().equals(currentUser.getUserId())) {
	            throw new BusinessException(ErrorCode.FORBIDDEN);
	        }

        // 校验是否有关联成绩
        if (gradeRepository.existsByEnrollment_Course_Id(courseId)) {
            throw new BusinessException(409, "该课程已有关联成绩记录，无法删除");
        }
        
        // 软删除建议实现，这里演示物理删除
        courseRepository.delete(course);
    }

    private CourseDto toDto(Course course) {
        CourseDto dto = new CourseDto();
        BeanUtils.copyProperties(course, dto);
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherName(course.getTeacher().getRealName());
        }
        return dto;
    }
}
