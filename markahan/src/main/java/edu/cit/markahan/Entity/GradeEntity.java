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

    // Individual subject grades
    @Column
    private double filipino;

    @Column
    private double english;

    @Column
    private double mathematics;

    @Column
    private double science;

    @Column
    private double ap;

    @Column
    private double esp;

    @Column
    private double mapeh;

    @Column
    private double computer;

    @Column(nullable = false)
    private double finalGrade;

    @Column
    private String remarks;

    public GradeEntity() {
        super();
    }

    public GradeEntity(int gradeId, double filipino, double english, double mathematics, 
                      double science, double ap, double esp, double mapeh, 
                      double computer, double finalGrade, String remarks) {
        super();
        this.gradeId = gradeId;
        this.filipino = filipino;
        this.english = english;
        this.mathematics = mathematics;
        this.science = science;
        this.ap = ap;
        this.esp = esp;
        this.mapeh = mapeh;
        this.computer = computer;
        this.finalGrade = finalGrade;
        this.remarks = remarks;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    // Getters and setters for individual subjects
    public double getFilipino() {
        return filipino;
    }

    public void setFilipino(double filipino) {
        this.filipino = filipino;
    }

    public double getEnglish() {
        return english;
    }

    public void setEnglish(double english) {
        this.english = english;
    }

    public double getMathematics() {
        return mathematics;
    }

    public void setMathematics(double mathematics) {
        this.mathematics = mathematics;
    }

    public double getScience() {
        return science;
    }

    public void setScience(double science) {
        this.science = science;
    }

    public double getAp() {
        return ap;
    }

    public void setAp(double ap) {
        this.ap = ap;
    }

    public double getEsp() {
        return esp;
    }

    public void setEsp(double esp) {
        this.esp = esp;
    }

    public double getMapeh() {
        return mapeh;
    }

    public void setMapeh(double mapeh) {
        this.mapeh = mapeh;
    }

    public double getComputer() {
        return computer;
    }

    public void setComputer(double computer) {
        this.computer = computer;
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
    
    // Helper method to calculate final grade based on all subjects
    public void calculateFinalGrade() {
        this.finalGrade = (filipino + english + mathematics + science + ap + esp + mapeh + computer) / 8.0;
    }
}