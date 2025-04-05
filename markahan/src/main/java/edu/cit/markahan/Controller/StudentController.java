package edu.cit.markahan.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Service.StudentService;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/add")
    public ResponseEntity<StudentEntity> addStudent(@RequestBody StudentEntity student) {
        return ResponseEntity.ok(studentService.addStudent(student));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StudentEntity>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<StudentEntity> updateStudent(@PathVariable int id, @RequestBody StudentEntity updatedStudent) {
        return ResponseEntity.ok(studentService.updateStudent(id, updatedStudent));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable int id) {
        return ResponseEntity.ok(studentService.deleteStudent(id));
    }
}
