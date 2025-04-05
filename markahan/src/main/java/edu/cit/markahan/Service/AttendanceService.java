package edu.cit.markahan.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.markahan.Entity.AttendanceEntity;
import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.AttendanceRepository;
import edu.cit.markahan.Repository.StudentRepository;
import edu.cit.markahan.Repository.UserRepository;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    public AttendanceEntity postAttendance(AttendanceEntity attendance) {
        // Ensure the student exists
        StudentEntity student = studentRepository.findById(attendance.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        attendance.setStudent(student);

        // Ensure the user exists
        UserEntity user = userRepository.findById(attendance.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        attendance.setUser(user);

        return attendanceRepository.save(attendance);
    }

    public AttendanceEntity putAttendance(int attendanceId, AttendanceEntity newAttendanceDetails) {
        AttendanceEntity attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        // Update student
        if (newAttendanceDetails.getStudent() != null) {
            StudentEntity student = studentRepository.findById(newAttendanceDetails.getStudent().getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            attendance.setStudent(student);
        }

        // Update user
        if (newAttendanceDetails.getUser() != null) {
            UserEntity user = userRepository.findById(newAttendanceDetails.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            attendance.setUser(user);
        }

        attendance.setDate(newAttendanceDetails.getDate());
        attendance.setStatus(newAttendanceDetails.getStatus());

        return attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceEntity> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public String deleteAttendance(int attendanceId) {
        if (attendanceRepository.existsById(attendanceId)) {
            attendanceRepository.deleteById(attendanceId);
            return "Attendance successfully deleted.";
        } else {
            return "Attendance not found.";
        }
    }
}