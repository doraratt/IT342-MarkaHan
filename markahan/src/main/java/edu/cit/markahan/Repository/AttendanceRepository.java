package edu.cit.markahan.Repository;

import edu.cit.markahan.Entity.AttendanceEntity;
import edu.cit.markahan.Entity.StudentEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Integer> {
    AttendanceEntity findByStudentAndDate(StudentEntity student, LocalDate localDate);
    List<AttendanceEntity> findByUserUserId(int userId); // New method to filter by userId
}