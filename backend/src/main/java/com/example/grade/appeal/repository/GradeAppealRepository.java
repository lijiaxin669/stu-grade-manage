package com.example.grade.appeal.repository;

import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.appeal.domain.GradeAppeal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GradeAppealRepository extends JpaRepository<GradeAppeal, Long> {

    boolean existsByGradeIdAndStatusNot(Long gradeId, AppealStatus excludedStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM GradeAppeal a WHERE a.id = :id")
    Optional<GradeAppeal> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT a FROM GradeAppeal a WHERE a.studentId = :studentId ORDER BY a.createdAt DESC")
    List<GradeAppeal> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM GradeAppeal a WHERE a.courseId IN :courseIds AND a.status IN :statuses ORDER BY a.createdAt DESC")
    List<GradeAppeal> findByCourseIdInAndStatusIn(@Param("courseIds") List<Long> courseIds,
                                                   @Param("statuses") List<AppealStatus> statuses);

    @Query("SELECT a FROM GradeAppeal a WHERE a.status = :status ORDER BY a.createdAt DESC")
    List<GradeAppeal> findByStatus(@Param("status") AppealStatus status);

    @Query("SELECT a FROM GradeAppeal a WHERE a.status = :status AND a.updatedAt < :cutoff")
    List<GradeAppeal> findByStatusAndUpdatedAtBefore(@Param("status") AppealStatus status,
                                                      @Param("cutoff") LocalDateTime cutoff);
}
