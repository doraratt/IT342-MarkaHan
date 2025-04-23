package edu.cit.markahan.Entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "Student")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int studentId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false)
    private String gradeLevel;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",referencedColumnName="userId",nullable=false)
    @JsonBackReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserEntity user;
    
 // Add OneToMany relationships
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AttendanceEntity> attendanceRecords = new ArrayList<>();
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GradeEntity> grades = new ArrayList<>();

    public StudentEntity() {
        super();
    }

    public StudentEntity(int studentId, String firstName, String lastName, String section, String gradeLevel) {
        super();
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.section = section;
        this.gradeLevel = gradeLevel;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
    
 // Add getters and setters for the new relationships
    public List<AttendanceEntity> getAttendanceRecords() {
        return attendanceRecords;
    }

    public void setAttendanceRecords(List<AttendanceEntity> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }
    
    public void addAttendanceRecord(AttendanceEntity attendance) {
        attendanceRecords.add(attendance);
        attendance.setStudent(this);
    }
    
    public void removeAttendanceRecord(AttendanceEntity attendance) {
        attendanceRecords.remove(attendance);
        attendance.setStudent(null);
    }
    
    public List<GradeEntity> getGrades() {
        return grades;
    }

    public void setGrades(List<GradeEntity> grades) {
        this.grades = grades;
    }
    
    public void addGrade(GradeEntity grade) {
        grades.add(grade);
        grade.setStudent(this);
    }
    
    public void removeGrade(GradeEntity grade) {
        grades.remove(grade);
        grade.setStudent(null);
    }
}