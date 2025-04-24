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
        StudentEntity student = studentRepository.findById(grade.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        UserEntity user = userRepository.findById(grade.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        grade.setStudent(student);
        grade.setUser(user);
        
        // Calculate final grade before saving
        grade.calculateFinalGrade();
        
        GradeEntity createdGrade = gradeService.postGrade(grade);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
    }

    @GetMapping("/getAllGrades")
    public List<GradeEntity> getAllGrades() {
        return gradeService.getAllGrades();
    }

    @GetMapping("/getGradesByUser")
    public ResponseEntity<List<GradeEntity>> getGradesByUser(@RequestParam int userId) {
        return ResponseEntity.ok(gradeService.getGradesByUserId(userId));
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