package edu.cit.markahan.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query; // Add this import
import org.springframework.data.repository.query.Param; // Add this import
import edu.cit.markahan.Entity.GradeEntity;
import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<GradeEntity, Integer> {
    @Query("SELECT g FROM GradeEntity g JOIN FETCH g.student JOIN FETCH g.user WHERE g.user.userId = :userId")
    List<GradeEntity> findByUserUserIdWithFetch(@Param("userId") int userId);

    // Existing method (for reference)
    List<GradeEntity> findByUserUserId(@Param("userId") int userId);

    // Other methods (e.g., for getGradesByStudentId)
    @Query("SELECT g FROM GradeEntity g JOIN FETCH g.student JOIN FETCH g.user WHERE g.student.studentId = :studentId")
    GradeEntity findByStudentStudentIdWithFetch(@Param("studentId") int studentId);
}