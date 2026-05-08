package com.example.grade.appeal.repository;

import com.example.grade.appeal.domain.Appeal;
import com.example.grade.appeal.domain.AppealStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, Long> {

    boolean existsByGradeIdAndStatusNot(Long gradeId, AppealStatus status);

    List<Appeal> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT a FROM Appeal a WHERE a.courseId IN :courseIds AND a.status IN :statuses ORDER BY a.createdAt DESC")
    List<Appeal> findByCourseIdInAndStatusIn(@Param("courseIds") List<Long> courseIds, 
                                              @Param("statuses") List<AppealStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appeal a WHERE a.id = :id")
    Optional<Appeal> findByIdWithLock(@Param("id") Long id);

    List<Appeal> findByStatusOrderByCreatedAtAsc(AppealStatus status);

    @Query("SELECT a FROM Appeal a WHERE a.status = :status AND a.updatedAt < :before")
    List<Appeal> findByStatusAndUpdatedAtBefore(@Param("status") AppealStatus status, 
                                                 @Param("before") LocalDateTime before);
}
