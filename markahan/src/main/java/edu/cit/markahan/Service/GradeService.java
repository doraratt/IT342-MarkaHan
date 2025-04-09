package edu.cit.markahan.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.markahan.Entity.GradeEntity;
import edu.cit.markahan.Entity.StudentEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.GradeRepository;
import edu.cit.markahan.Repository.StudentRepository;
import edu.cit.markahan.Repository.UserRepository;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;

    public GradeEntity postGrade(GradeEntity grade) {
        StudentEntity student = studentRepository.findById(grade.getStudent().getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        grade.setStudent(student);

        UserEntity user = userRepository.findById(grade.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        grade.setUser(user);
        
        return gradeRepository.save(grade);
    }

    @Transactional(readOnly = true)
    public List<GradeEntity> getAllGrades() {
        return gradeRepository.findAll().stream()
            .filter(grade -> grade.getStudent() != null)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GradeEntity> getGradesByUserId(int userId) {
        return gradeRepository.findByUserUserId(userId).stream()
            .filter(grade -> grade.getStudent() != null)
            .collect(Collectors.toList());
    }

    public GradeEntity putGrade(int gradeId, GradeEntity newGradeDetails) {
        GradeEntity grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
        
        if (newGradeDetails.getStudent() != null) {
            StudentEntity student = studentRepository.findById(newGradeDetails.getStudent().getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            grade.setStudent(student);
        }
        
        if (newGradeDetails.getUser() != null) {
            UserEntity user = userRepository.findById(newGradeDetails.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            grade.setUser(user);
        }
        
        grade.setSubjectName(newGradeDetails.getSubjectName());
        grade.setFinalGrade(newGradeDetails.getFinalGrade());
        grade.setRemarks(newGradeDetails.getRemarks());
        
        return gradeRepository.save(grade);
    }

    public String deleteGrade(int gradeId) {
        if (gradeRepository.existsById(gradeId)) {
            gradeRepository.deleteById(gradeId);
            return "Grade successfully deleted.";
        } else {
            return "Grade not found.";
        }
    }
}