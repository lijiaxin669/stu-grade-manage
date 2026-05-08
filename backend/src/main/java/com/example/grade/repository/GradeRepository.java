package com.example.grade.repository;

import com.example.grade.domain.Grade;
import com.example.grade.dto.grade.GradeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    boolean existsByEnrollmentId(Long enrollmentId);

    boolean existsByEnrollment_Course_Id(Long courseId);

    Optional<Grade> findByEnrollmentId(Long enrollmentId);

    // Teacher view: grades for their courses
    @Query("SELECT new com.example.grade.dto.grade.GradeDto(" +
           "g.id, s.id, s.realName, s.username, c.name, g.score, c.semester, g.updatedAt) " +
           "FROM Grade g " +
           "JOIN g.enrollment e " +
           "JOIN e.student s " +
           "JOIN e.course c " +
           "WHERE (:teacherId IS NULL OR c.teacher.id = :teacherId) " +
           "AND (:courseId IS NULL OR c.id = :courseId)")
    List<GradeDto> findByTeacher(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    // Student view: own grades
    @Query("SELECT new com.example.grade.dto.grade.GradeDto(" +
           "g.id, s.id, s.realName, s.username, c.name, g.score, c.semester, g.updatedAt) " +
           "FROM Grade g " +
           "JOIN g.enrollment e " +
           "JOIN e.student s " +
           "JOIN e.course c " +
           "WHERE s.id = :studentId")
    List<GradeDto> findByStudent(@Param("studentId") Long studentId);

    @Query("SELECT AVG(g.score) as avgScore, COUNT(g) as totalCount, MAX(g.score) as maxScore, MIN(g.score) as minScore " +
           "FROM Grade g JOIN g.enrollment e WHERE e.course.id = :courseId")
    com.example.grade.dto.grade.GradeStatsProjection getStatistics(@Param("courseId") Long courseId);

    @Query("SELECT new com.example.grade.dto.grade.GradeDto(" +
           "g.id, s.id, s.realName, s.username, c.name, g.score, c.semester, g.updatedAt) " +
           "FROM Grade g " +
           "JOIN g.enrollment e " +
           "JOIN e.student s " +
           "JOIN e.course c " +
           "WHERE c.id = :courseId")
    org.springframework.data.domain.Page<GradeDto> findByCourse(@Param("courseId") Long courseId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(g) FROM Grade g JOIN g.enrollment e WHERE e.course.id = :courseId AND g.score >= :passLine")
    Long countPassedStudents(@Param("courseId") Long courseId, @Param("passLine") java.math.BigDecimal passLine);
}
