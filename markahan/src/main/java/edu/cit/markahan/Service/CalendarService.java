package edu.cit.markahan.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.markahan.Entity.CalendarEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.CalendarRepository;
import edu.cit.markahan.Repository.UserRepository;

@Service
public class CalendarService {

    @Autowired
    private CalendarRepository eventCalendarRepo;

    @Autowired
    private UserRepository userRepo;

    public List<CalendarEntity> getTasksByUserId(int userId) {
        return eventCalendarRepo.findByUser_UserId(userId);
    }

    public CalendarEntity addTask(CalendarEntity eventCalendar) {
        UserEntity user = userRepo.findById(eventCalendar.getUser().getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + eventCalendar.getUser().getUserId()));
        eventCalendar.setUser(user);
        return eventCalendarRepo.save(eventCalendar);
    }

    public List<CalendarEntity> getAllTasks() {
        return eventCalendarRepo.findAll();
    }

    public CalendarEntity updateTask(int calendarId, CalendarEntity newTaskDetails) {
        CalendarEntity taskCalendar = eventCalendarRepo.findById(calendarId)
            .orElseThrow(() -> new NoSuchElementException("EventCalendar " + calendarId + " not found!"));
    
        if (newTaskDetails.getUser() != null && newTaskDetails.getUser().getUserId() != 0) {
            UserEntity user = userRepo.findById(newTaskDetails.getUser().getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + newTaskDetails.getUser().getUserId()));
            taskCalendar.setUser(user);
        }
    
        taskCalendar.setDate(newTaskDetails.getDate());
        taskCalendar.setEventDescription(newTaskDetails.getEventDescription());
        return eventCalendarRepo.save(taskCalendar);
    }

    public String deleteTask(int calendarId) {
        if (eventCalendarRepo.existsById(calendarId)) {
        	eventCalendarRepo.deleteById(calendarId);
            return "EventCalendar successfully deleted.";
        }
        return "EventCalendar " + calendarId + " not found!";
    }

    public List<CalendarEntity> getTasksByDate(LocalDate date) {
        return eventCalendarRepo.findByDate(date);
    }

    public List<CalendarEntity> getTasksByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return eventCalendarRepo.findByUser_UserIdAndDateBetween(userId, startDate, endDate);
    }
}
