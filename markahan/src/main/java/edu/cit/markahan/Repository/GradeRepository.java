package edu.cit.markahan.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.cit.markahan.Entity.GradeEntity;
import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<GradeEntity, Integer> {
    List<GradeEntity> findByUserUserId(int userId); // Fetch grades by userId
}