import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import Paper from '@mui/material/Paper';
import Button from '@mui/material/Button';
import AddIcon from "@mui/icons-material/Add";
import Modal from "@mui/material/Modal";
import TextField from "@mui/material/TextField";
import CloseIcon from "@mui/icons-material/Close";
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

// EntryModal component (unchanged)
const EntryModal = ({ open, onClose, title, entry, onSave, buttonText, onContentChange, onDateChange }) => (
  <Modal
    open={open}
    onClose={onClose}
    aria-labelledby={`${title.toLowerCase()}-journal-modal`}
  >
    <Box sx={{
      position: 'absolute',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      width: 400,
      bgcolor: 'background.paper',
      borderRadius: 2,
      boxShadow: 24,
      p: 4,
    }}>
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        mb: 2
      }}>
        <Typography variant="h6" component="h2">
          {title}
        </Typography>
        <IconButton 
          onClick={onClose}
          size="small"
          sx={{ color: 'text.secondary' }}
        >
          <CloseIcon />
        </IconButton>
      </Box>

      <Typography variant="subtitle1" sx={{ mb: 1 }}>
        Entry
      </Typography>
      <TextField
        multiline
        rows={4}
        fullWidth
        value={entry.content}
        onChange={onContentChange}
        sx={{ mb: 3 }}
        autoComplete="off"
      />

      <Typography variant="subtitle1" sx={{ mb: 1 }}>
        Date
      </Typography>
      <TextField
        type="date"
        fullWidth
        value={entry.date}
        onChange={onDateChange}
        sx={{ mb: 3 }}
      />

      <Button
        fullWidth
        variant="contained"
        onClick={onSave}
        sx={{
          backgroundColor: '#1f295a',
          '&:hover': {
            backgroundColor: '#4259c1',
          },
          textTransform: 'none',
        }}
      >
        {buttonText}
      </Button>
    </Box>
  </Modal>
);

// DeleteConfirmationModal component (unchanged)
const DeleteConfirmationModal = ({ open, onClose, onConfirm }) => (
  <Modal
    open={open}
    onClose={onClose}
    aria-labelledby="delete-journal-modal"
  >
    <Box sx={{
      position: 'absolute',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      width: 400,
      bgcolor: 'background.paper',
      borderRadius: 2,
      boxShadow: 24,
      p: 3,
    }}>
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        mb: 2
      }}>
        <Typography variant="h6" component="h2">
          Delete Journal Entry
        </Typography>
        <IconButton 
          onClick={onClose}
          size="small"
          sx={{ color: 'text.secondary' }}
        >
          <CloseIcon />
        </IconButton>
      </Box>

      <Typography variant="body1" sx={{ mb: 3 }}>
        Are you sure you want to delete this journal entry?
      </Typography>

      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'flex-end',
        gap: 1
      }}>
        <Button
          variant="contained"
          onClick={onConfirm}
          sx={{
            backgroundColor: '#dc3545',
            '&:hover': {
              backgroundColor: '#c82333',
            },
            textTransform: 'none',
          }}
        >
          Delete
        </Button>
        <Button
          variant="contained"
          onClick={onClose}
          sx={{
            backgroundColor: '#6c757d',
            '&:hover': {
              backgroundColor: '#5a6268',
            },
            textTransform: 'none',
          }}
        >
          Cancel
        </Button>
      </Box>
    </Box>
  </Modal>
);

function Journal() {
  const { user } = useUser();
  const [entries, setEntries] = useState([]);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [newEntry, setNewEntry] = useState({
    content: '',
    date: new Date().toISOString().split('T')[0]
  });
  const [editingEntry, setEditingEntry] = useState(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [entryToDelete, setEntryToDelete] = useState(null);
  const [error, setError] = useState('');

  // Redirect to 404 if no user is logged in
  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    if (user) {
      const fetchEntries = async () => {
        try {
          const response = await axios.get(`http://localhost:8080/api/journal/getJournalsByUser?userId=${user.userId}`);
          setEntries(response.data.map(entry => ({
            id: entry.journalId,
            content: entry.entry,
            date: entry.date
          })));
        } catch (error) {
          console.error('Error fetching journal entries:', error.response?.data || error.message);
        }
      };

      fetchEntries();
    } else {
      console.log('No user logged in');
    }
  }, [user]);

  // Add Entry Handlers
  const handleAddOpen = () => setAddModalOpen(true);
  const handleAddClose = () => {
    setAddModalOpen(false);
    setNewEntry({ content: '', date: new Date().toISOString().split('T')[0] });
  };

  const handleAddEntry = async () => {
    if (!newEntry.content || !newEntry.date) {
      setError('Content and date are required');
      return;
    }
    if (!user) {
      setError('User not logged in');
      return;
    }

    const entryData = {
      entry: newEntry.content,
      date: newEntry.date,
      user: { userId: user.userId }
    };

    try {
      const response = await axios.post('http://localhost:8080/api/journal/post', entryData);
      setEntries((prevEntries) => [...prevEntries, {
        id: response.data.journalId,
        content: response.data.entry,
        date: response.data.date
      }]);
      handleAddClose();
      setError('');
    } catch (error) {
      setError('Error adding journal entry: ' + (error.response?.data || error.message));
      console.error('Add error:', error);
    }
  };

  // Edit Entry Handlers
  const handleEditOpen = (entry) => {
    setEditingEntry({
      id: entry.id,
      content: entry.content,
      date: entry.date
    });
    setEditModalOpen(true);
  };

  const handleEditClose = () => {
    setEditModalOpen(false);
    setEditingEntry(null);
  };

  const handleEditEntry = async () => {
    if (!editingEntry || !editingEntry.content || !editingEntry.date) {
      setError('Content and date are required');
      return;
    }
    if (!user) {
      setError('User not logged in');
      return;
    }

    const entryData = {
      entry: editingEntry.content,
      date: editingEntry.date,
      user: { userId: user.userId }
    };

    try {
      const response = await axios.put(
        `http://localhost:8080/api/journal/update/${editingEntry.id}`,
        entryData
      );
      setEntries((prevEntries) => prevEntries.map((entry) =>
        entry.id === editingEntry.id ? {
          id: response.data.journalId,
          content: response.data.entry,
          date: response.data.date
        } : entry
      ));
      handleEditClose();
      setError('');
    } catch (error) {
      setError('Error updating journal entry: ' + (error.response?.data || error.message));
      console.error('Edit error:', error);
    }
  };

  // Delete Entry Handlers
  const handleDeleteOpen = (entry) => {
    setEntryToDelete(entry);
    setDeleteModalOpen(true);
  };

  const handleDeleteClose = () => {
    setDeleteModalOpen(false);
    setEntryToDelete(null);
  };

  const handleDeleteEntry = async () => {
    if (!entryToDelete) return;

    try {
      await axios.delete(`http://localhost:8080/api/journal/delete/${entryToDelete.id}`);
      setEntries((prevEntries) => prevEntries.filter((entry) => entry.id !== entryToDelete.id));
      handleDeleteClose();
      setError('');
    } catch (error) {
      setError('Error deleting journal entry: ' + (error.response?.data || error.message));
      console.error('Delete error:', error);
    }
  };

  // Handlers for EntryModal input changes
  const handleAddContentChange = (e) => setNewEntry({ ...newEntry, content: e.target.value });
  const handleAddDateChange = (e) => setNewEntry({ ...newEntry, date: e.target.value });
  const handleEditContentChange = (e) => setEditingEntry({ ...editingEntry, content: e.target.value });
  const handleEditDateChange = (e) => setEditingEntry({ ...editingEntry, date: e.target.value });

  return (
    <Box sx={{ 
      padding: 3,
      width: '100%',
      maxWidth: '1450px',
      margin: '0 auto'
    }}>
      {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}

      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'flex-end',
        mb: 3
      }}>
        <Button
          variant="text"
          startIcon={<AddIcon />}
          onClick={handleAddOpen}
          sx={{
            color: '#1f295a',
            textTransform: 'none',
            fontWeight: 'normal',
            '&:hover': {
              backgroundColor: 'transparent',
              color: '#4259c1',
            },
            padding: '4px 8px',
            minWidth: 0,
          }}
        >
          Journal Entry
        </Button>
      </Box>

      <EntryModal
        open={addModalOpen}
        onClose={handleAddClose}
        title="Add Journal Entry"
        entry={newEntry}
        onSave={handleAddEntry}
        buttonText="Add Journal Entry"
        onContentChange={handleAddContentChange}
        onDateChange={handleAddDateChange}
      />

      <EntryModal
        open={editModalOpen}
        onClose={handleEditClose}
        title="Edit Journal Entry"
        entry={editingEntry || { content: '', date: '' }}
        onSave={handleEditEntry}
        buttonText="Edit Journal Entry"
        onContentChange={handleEditContentChange}
        onDateChange={handleEditDateChange}
      />

      <DeleteConfirmationModal
        open={deleteModalOpen}
        onClose={handleDeleteClose}
        onConfirm={handleDeleteEntry}
      />

      {entries.map((entry) => (
        <Paper
          key={entry.id}
          elevation={0}
          sx={{
            p: 2,
            mb: 2,
            backgroundColor: 'white',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'flex-start',
            gap: 2,
          }}
        >
          <Box sx={{ flexGrow: 1 }}>
            <Typography
              variant="body1"
              sx={{
                color: '#333',
                mb: 1,
                lineHeight: 1.6,
              }}
            >
              {entry.content}
            </Typography>
            
            <Typography
              variant="caption"
              sx={{
                color: '#666',
                display: 'block',
                mt: 1
              }}
            >
              Date: {entry.date}
            </Typography>
          </Box>

          <Box sx={{ 
            display: 'flex', 
            gap: 1,
            alignItems: 'center'
          }}>
            <IconButton 
              size="small"
              onClick={() => handleEditOpen(entry)}
            >
              <EditIcon fontSize="small" />
            </IconButton>
            <IconButton 
              size="small"
              onClick={() => handleDeleteOpen(entry)}
            >
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Box>
        </Paper>
      ))}
    </Box>
  );
}

export default Journal;