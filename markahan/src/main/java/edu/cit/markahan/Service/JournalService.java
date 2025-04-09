package edu.cit.markahan.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.markahan.Entity.JournalEntity;
import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.JournalRepository;
import edu.cit.markahan.Repository.UserRepository;

@Service
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserRepository userRepository; // Added to associate user with journal

    public JournalEntity postJournal(JournalEntity journal) {
        UserEntity user = userRepository.findById(journal.getUser().getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + journal.getUser().getUserId()));
        journal.setUser(user);
        return journalRepository.save(journal);
    }

    public List<JournalEntity> getAllJournals() {
        return journalRepository.findAll();
    }

    // New method to get journals by user ID
    public List<JournalEntity> getJournalsByUserId(int userId) {
        return journalRepository.findByUserUserId(userId);
    }

    public JournalEntity putJournal(int journalId, JournalEntity updatedJournal) {
        return journalRepository.findById(journalId).map(journal -> {
            journal.setEntry(updatedJournal.getEntry());
            journal.setDate(updatedJournal.getDate());
            // User is not updated here to prevent changing ownership
            return journalRepository.save(journal);
        }).orElseThrow(() -> new RuntimeException("Journal not found"));
    }

    public String deleteJournal(int journalId) {
        if (journalRepository.existsById(journalId)) {
            journalRepository.deleteById(journalId);
            return "Journal successfully deleted.";
        } else {
            return "Journal not found.";
        }
    }
}