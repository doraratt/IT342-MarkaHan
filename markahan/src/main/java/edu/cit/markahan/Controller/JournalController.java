package edu.cit.markahan.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.markahan.Entity.JournalEntity;
import edu.cit.markahan.Service.JournalService;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @PostMapping("/post")
    public ResponseEntity<JournalEntity> postJournal(@RequestBody JournalEntity journal) {
        return ResponseEntity.ok(journalService.postJournal(journal));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<JournalEntity>> getAllJournals() {
        return ResponseEntity.ok(journalService.getAllJournals());
    }

    // New endpoint to get journals by user ID
    @GetMapping("/getJournalsByUser")
    public ResponseEntity<List<JournalEntity>> getJournalsByUser(@RequestParam int userId) {
        return ResponseEntity.ok(journalService.getJournalsByUserId(userId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<JournalEntity> updateJournal(@PathVariable int id, @RequestBody JournalEntity updatedJournal) {
        return ResponseEntity.ok(journalService.putJournal(id, updatedJournal));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteJournal(@PathVariable int id) {
        return ResponseEntity.ok(journalService.deleteJournal(id));
    }
}