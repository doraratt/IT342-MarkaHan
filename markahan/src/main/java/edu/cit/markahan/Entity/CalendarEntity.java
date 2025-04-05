package edu.cit.markahan.Entity;

import java.time.LocalDate;


import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Calendar")
public class CalendarEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int calendarId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonBackReference
    private UserEntity user;

    @Column(name = "event_description", nullable = false) 
    private String eventDescription;

    @Column(nullable = false)
    private LocalDate date;

    public CalendarEntity() {
        super();
    }

    public CalendarEntity(int calendarId, UserEntity user, String eventDescription, LocalDate date) {
        super();
        this.calendarId = calendarId;
        this.user = user;
        this.eventDescription = eventDescription;
        this.date = date;
    }

    public int getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(int calendarId) {
        this.calendarId = calendarId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
