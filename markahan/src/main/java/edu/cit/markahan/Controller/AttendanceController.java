package edu.cit.markahan.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.markahan.Entity.AttendanceEntity;
import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.StudentRepository;
import edu.cit.markahan.Repository.UserRepository;
import edu.cit.markahan.Service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/postAttendance")
    public ResponseEntity<AttendanceEntity> postAttendance(@RequestBody AttendanceEntity attendance) {
        StudentEntity student = studentRepository.findById(attendance.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        UserEntity user = userRepository.findById(attendance.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        attendance.setStudent(student);
        attendance.setUser(user);

        AttendanceEntity createdAttendance = attendanceService.postAttendance(attendance);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAttendance);
    }

    @GetMapping("/getAllAttendance")
    public List<AttendanceEntity> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }

    // New endpoint to get attendance by user ID
    @GetMapping("/getAttendanceByUser")
    public ResponseEntity<List<AttendanceEntity>> getAttendanceByUser(@RequestParam int userId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByUserId(userId));
    }

    @GetMapping("/getAttendanceByUserAndMonth")
    public ResponseEntity<List<AttendanceEntity>> getAttendanceByUserAndMonth(
        @RequestParam int userId,
        @RequestParam int month,
        @RequestParam int year
    ) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: must be between 1 and 12");
        }
        if (year < 2000 || year > 9999) {
            throw new IllegalArgumentException("Invalid year");
        }
        List<AttendanceEntity> attendance = attendanceService.getAttendanceByUserIdAndMonthYear(userId, month, year);
        return ResponseEntity.ok(attendance);
    }

    @PutMapping("/putAttendance/{id}")
    public ResponseEntity<AttendanceEntity> putAttendance(@PathVariable int id, @RequestBody AttendanceEntity newAttendanceDetails) {
        AttendanceEntity updatedAttendance = attendanceService.putAttendance(id, newAttendanceDetails);
        return ResponseEntity.ok(updatedAttendance);
    }

    @DeleteMapping("/deleteAttendance/{id}")
    public String deleteAttendance(@PathVariable int id) {
        return attendanceService.deleteAttendance(id);
    }
}