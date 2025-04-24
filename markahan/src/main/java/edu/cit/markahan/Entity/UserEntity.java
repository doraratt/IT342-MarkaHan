package edu.cit.markahan.Entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="User")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int userId;

    @Column(nullable=false)
    private String firstName;

    @Column(nullable=false)
    private String lastName;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String password;
    
    @Column
    private String oauthId;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonIgnore
    private List<StudentEntity> students = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonIgnore
    private List<JournalEntity> journals = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonIgnore
    private List<AttendanceEntity> attendanceRecords = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<GradeEntity> grades = new ArrayList<>();

    public UserEntity() {
        super();
    }

    public UserEntity(int userId, String firstName, String lastName, String email, String password,
            List<StudentEntity> students, List<JournalEntity> journals,
            List<AttendanceEntity> attendanceRecords, List<GradeEntity> grades) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.students = students;
        this.journals = journals;
        this.attendanceRecords = attendanceRecords;
        this.grades = grades;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }
    
    // Add getters and setters for collections if needed
    public List<StudentEntity> getStudents() {
        return students;
    }

    public void setStudents(List<StudentEntity> students) {
        this.students = students;
    }

    public List<JournalEntity> getJournals() {
        return journals;
    }

    public void setJournals(List<JournalEntity> journals) {
        this.journals = journals;
    }

    public List<AttendanceEntity> getAttendanceRecords() {
        return attendanceRecords;
    }

    public void setAttendanceRecords(List<AttendanceEntity> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    public List<GradeEntity> getGrades() {
        return grades;
    }

    public void setGrades(List<GradeEntity> grades) {
        this.grades = grades;
    }
    
    // Helper methods for managing relationships
    public void addStudent(StudentEntity student) {
        students.add(student);
        student.setUser(this);
    }
    
    public void removeStudent(StudentEntity student) {
        students.remove(student);
        student.setUser(null);
    }
    
    public void addAttendanceRecord(AttendanceEntity attendance) {
        attendanceRecords.add(attendance);
        attendance.setUser(this);
    }
    
    public void removeAttendanceRecord(AttendanceEntity attendance) {
        attendanceRecords.remove(attendance);
        attendance.setUser(null);
    }
    
    public void addGrade(GradeEntity grade) {
        grades.add(grade);
        grade.setUser(this);
    }
    
    public void removeGrade(GradeEntity grade) {
        grades.remove(grade);
        grade.setUser(null);
    }
    
    
}