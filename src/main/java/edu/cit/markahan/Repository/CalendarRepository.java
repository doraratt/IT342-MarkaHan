package edu.cit.markahan.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.markahan.Entity.CalendarEntity;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Integer> {

    List<CalendarEntity> findByUser_UserId(int userId);

    List<CalendarEntity> findByDate(LocalDate date);

    List<CalendarEntity> findByUser_UserIdAndDateBetween(int userId, LocalDate startDate, LocalDate endDate);

}
