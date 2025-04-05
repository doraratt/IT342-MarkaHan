package edu.cit.markahan.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.cit.markahan.Entity.AttendanceEntity;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Integer> {
    // Custom query methods (if necessary) can go here
}
