package edu.cit.markahan.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.markahan.Entity.CalendarEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.UserRepository;
import edu.cit.markahan.Service.CalendarService;

@RestController
@RequestMapping("/api/eventcalendar")
@CrossOrigin(origins = "*")
public class CalendarController {

    @Autowired
    private CalendarService eventCalendarService;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/addEventCal")
    public ResponseEntity<CalendarEntity> addTask(@RequestBody CalendarEntity taskCalendar) {
        if (taskCalendar.getUser() == null || taskCalendar.getUser().getUserId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); 
        }

        UserEntity user = userRepo.findById(taskCalendar.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + taskCalendar.getUser().getUserId()));
        
        taskCalendar.setUser(user);
        CalendarEntity savedTask = eventCalendarService.addTask(taskCalendar);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @GetMapping("/getEventByUser")
    public List<CalendarEntity> getTasksByUser(@RequestParam int userId) {
        return eventCalendarService.getTasksByUserId(userId);
    }

    @GetMapping("/getAllEvent")
    public List<CalendarEntity> getAllTasks() {
        return eventCalendarService.getAllTasks();
    }

    @GetMapping("/getEventsCalByDate")
    public List<CalendarEntity> getTasksByDate(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return eventCalendarService.getTasksByDate(parsedDate);
    }

    @GetMapping("/getEventsByDateRange")
    public ResponseEntity<List<CalendarEntity>> getTasksByDateRange(
            @RequestParam("userId") int userId, 
            @RequestParam("startDate") LocalDate startDate, 
            @RequestParam("endDate") LocalDate endDate) {
        List<CalendarEntity> tasks = eventCalendarService.getTasksByDateRange(userId, startDate, endDate);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PutMapping("/updateEventCal/{calendarId}")
    public ResponseEntity<?> updateTask(@PathVariable int calendarId, @RequestBody CalendarEntity taskCalendar) {
        if (taskCalendar.getUser() != null && taskCalendar.getUser().getUserId() != 0) {
            try {
                UserEntity user = userRepo.findById(taskCalendar.getUser().getUserId())
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + taskCalendar.getUser().getUserId()));
                taskCalendar.setUser(user);
            } catch (NoSuchElementException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID not provided");
        }

        try {
            CalendarEntity updatedTask = eventCalendarService.updateTask(calendarId, taskCalendar);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteEventCal/{calendarId}")
    public ResponseEntity<String> deleteTask(@PathVariable int calendarId) {
        String result = eventCalendarService.deleteTask(calendarId);
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}