package edu.cit.markahan.Repository;

import edu.cit.markahan.Entity.AttendanceEntity;
import edu.cit.markahan.Entity.StudentEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Integer> {
    @Query("SELECT a FROM AttendanceEntity a JOIN FETCH a.student JOIN FETCH a.user WHERE a.student = :student AND a.date = :date")
    AttendanceEntity findByStudentAndDateWithFetch(@Param("student") StudentEntity student, @Param("date") LocalDate date);

    @Query("SELECT a FROM AttendanceEntity a JOIN FETCH a.student JOIN FETCH a.user WHERE a.user.userId = :userId")
    List<AttendanceEntity> findByUserUserIdWithFetch(@Param("userId") int userId);

    // Existing methods (for reference)
    AttendanceEntity findByStudentAndDate(StudentEntity student, LocalDate localDate);
    List<AttendanceEntity> findByUserUserId(int userId);

    @Query("SELECT a FROM AttendanceEntity a JOIN FETCH a.student JOIN FETCH a.user")
    List<AttendanceEntity> findAllWithFetch();
}