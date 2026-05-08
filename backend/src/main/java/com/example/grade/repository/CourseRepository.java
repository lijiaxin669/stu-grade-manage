package com.example.grade.repository;

import com.example.grade.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    
    // 检查重名 (name + semester)
    boolean existsByNameAndSemester(String name, String semester);

    Optional<Course> findByNameAndSemester(String name, String semester);

    // 教师查看自己的课程
    List<Course> findByTeacherId(Long teacherId);

    boolean existsByTeacherId(Long teacherId);
}
