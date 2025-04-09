package edu.cit.markahan.Entity;

import java.time.LocalDate;

import edu.cit.markahan.Entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "Attendance")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int attendanceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "studentId", nullable = false)
    @JsonIgnoreProperties({"user", "attendanceRecords"})
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonIgnoreProperties({"students", "journals", "attendanceRecords", "grades"})
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // Present, Absent, Late

    public AttendanceEntity() {
        super();
    }

    public AttendanceEntity(int attendanceId, LocalDate date, String status) {
        super();
        this.attendanceId = attendanceId;
        this.date = date;
        this.status = status;
    }

    // ... existing code ...
} 