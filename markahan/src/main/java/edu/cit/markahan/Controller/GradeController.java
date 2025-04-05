package edu.cit.markahan.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.markahan.Entity.GradeEntity;
import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.StudentRepository;
import edu.cit.markahan.Repository.UserRepository;
import edu.cit.markahan.Service.GradeService;

@RestController
@RequestMapping("/api/grade")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private GradeService gradeService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/postGrade")
    public ResponseEntity<GradeEntity> postGrade(@RequestBody GradeEntity grade) {
        // Retrieve the student based on student_id
        StudentEntity student = studentRepository.findById(grade.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Retrieve the user based on user_id from the request
        UserEntity user = userRepository.findById(grade.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set the student and user for the grade record
        grade.setStudent(student);
        grade.setUser(user);
        
        GradeEntity createdGrade = gradeService.postGrade(grade);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
    }

    @GetMapping("/getAllGrades")
    public List<GradeEntity> getAllGrades() {
        return gradeService.getAllGrades();
    }

    @PutMapping("/putGrade/{id}")
    public ResponseEntity<GradeEntity> putGrade(@PathVariable int id, @RequestBody GradeEntity newGradeDetails) {
        GradeEntity updatedGrade = gradeService.putGrade(id, newGradeDetails);
        return ResponseEntity.ok(updatedGrade);
    }

    @DeleteMapping("/deleteGrade/{id}")
    public String deleteGrade(@PathVariable int id) {
        return gradeService.deleteGrade(id);
    }
}