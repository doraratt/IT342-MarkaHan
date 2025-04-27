package edu.cit.markahan.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.StudentRepository;
import edu.cit.markahan.Repository.UserRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    public StudentEntity addStudent(StudentEntity student) {
        UserEntity user = userRepository.findById(student.getUser().getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + student.getUser().getUserId()));
        student.setUser(user);
        return studentRepository.save(student);
    }

    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    // New method to get students by user ID
    public List<StudentEntity> getStudentsByUserId(int userId) {
        return studentRepository.findByUserUserId(userId);
    }

    public StudentEntity updateStudent(int studentId, StudentEntity newStudentDetails) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setFirstName(newStudentDetails.getFirstName());
        student.setLastName(newStudentDetails.getLastName());
        student.setSection(newStudentDetails.getSection());
        student.setGradeLevel(newStudentDetails.getGradeLevel());
        student.setGender(newStudentDetails.getGender());
        // User is not updated here to prevent changing ownership

        return studentRepository.save(student);
    }

    public String deleteStudent(int studentId) {
        if (studentRepository.existsById(studentId)) {
            studentRepository.deleteById(studentId);
            return "Student successfully deleted.";
        } else {
            return "Student not found.";
        }
    }
}