package edu.cit.markahan.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.markahan.Entity.JournalEntity;
import edu.cit.markahan.Repository.JournalRepository;

@Service
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    public JournalEntity postJournal(JournalEntity journal) {
        return journalRepository.save(journal);
    }

    public List<JournalEntity> getAllJournals() {
        return journalRepository.findAll();
    }

    public JournalEntity putJournal(int journalId, JournalEntity updatedJournal) {
        return journalRepository.findById(journalId).map(journal -> {
            journal.setEntry(updatedJournal.getEntry());
            journal.setDate(updatedJournal.getDate());
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
