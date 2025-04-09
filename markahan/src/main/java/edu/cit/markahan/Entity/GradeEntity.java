package edu.cit.markahan.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "Grade")
public class GradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gradeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "studentId", nullable = false)
    @JsonBackReference
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonIgnoreProperties({"students", "journals", "attendanceRecords", "grades", "hibernateLazyInitializer", "handler"})
    private UserEntity user;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private double finalGrade;

    @Column
    private String remarks;

    public GradeEntity() {
        super();
    }

    public GradeEntity(int gradeId, String subjectName, double finalGrade, String remarks) {
        super();
        this.gradeId = gradeId;
        this.subjectName = subjectName;
        this.finalGrade = finalGrade;
        this.remarks = remarks;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public double getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(double finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    // Add getters and setters for student and user
    public StudentEntity getStudent() {
        return student;
    }

    public void setStudent(StudentEntity student) {
        this.student = student;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}