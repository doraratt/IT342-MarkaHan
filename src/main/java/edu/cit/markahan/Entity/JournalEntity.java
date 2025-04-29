package edu.cit.markahan.Entity;

import java.time.LocalDate;

import edu.cit.markahan.Entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "journals")
public class JournalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int journalId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserEntity user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String entry;

    @Column(nullable = false)
    private LocalDate date;

    public JournalEntity() {}

    public JournalEntity(int journalId, UserEntity user, String entry, LocalDate date) {
        this.journalId = journalId;
        this.user = user;
        this.entry = entry;
        this.date = date;
    }

    public int getJournalId() {
        return journalId;
    }

    public void setJournalId(int journalId) {
        this.journalId = journalId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}