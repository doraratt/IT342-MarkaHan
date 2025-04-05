package edu.cit.markahan.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public StudentEntity addStudent(StudentEntity student) {
        return studentRepository.save(student);
    }

    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    public StudentEntity updateStudent(int studentId, StudentEntity newStudentDetails) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setFirstName(newStudentDetails.getFirstName());
        student.setLastName(newStudentDetails.getLastName());
        student.setSection(newStudentDetails.getSection());
        student.setGradeLevel(newStudentDetails.getGradeLevel());

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
