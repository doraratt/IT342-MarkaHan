package edu.cit.markahan.Repository;

import org.hibernate.mapping.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.cit.markahan.Entity.JournalEntity;

@Repository
public interface JournalRepository extends JpaRepository<JournalEntity, Integer> {
    List findByUserUserId(int userId);
}
