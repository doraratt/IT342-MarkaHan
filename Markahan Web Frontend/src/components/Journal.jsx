import { useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Checkbox from '@mui/material/Checkbox';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import Paper from '@mui/material/Paper';
import Button from '@mui/material/Button';
import AddIcon from "@mui/icons-material/Add";
import Modal from "@mui/material/Modal";
import TextField from "@mui/material/TextField";
import CloseIcon from "@mui/icons-material/Close";

function Journal() {
  const [entries, setEntries] = useState([
    {
      id: 1,
      content: 'Lorem ipsum dolor amet, consectetur adipiscing elit. Lectus blandit magna tempor vel sprient. Aliquet nulla venenatis neque, malesuada blandit. Suspendisse lectus blandit, viverra mattis inceptos sodales, amet venenatis amet. Justo leo varius mi suspendisse porta id egestas consula. Proin sociosqu nibh ut hendrerit vivamus fringilla. Elit sprient mauris morbi risus laoreet mescetur porttitor mus non.',
      date: '2024-10-31'
    },
    {
      id: 2,
      content: 'Lorem ipsum dolor amet, consectetur adipiscing elit. Lectus blandit magna tempor vel sprient. Aliquet nulla venenatis neque, malesuada blandit. Suspendisse lectus blandit, viverra mattis inceptos sodales, amet venenatis amet.',
      date: 'Oct. 31, 2024 12:05PM'
    },
    {
      id: 3,
      content: 'Lorem ipsum dolor amet, consectetur adipiscing elit. Lectus blandit magna tempor vel sprient. Aliquet nulla venenatis neque, malesuada blandit.',
      date: 'Oct. 31, 2024 12:05PM'
    },
  ]);

  // States for add/edit modals
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [newEntry, setNewEntry] = useState({
    content: '',
    date: new Date().toISOString().split('T')[0]
  });
  const [editingEntry, setEditingEntry] = useState(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [entryToDelete, setEntryToDelete] = useState(null);

  // Add Entry Handlers
  const handleAddOpen = () => setAddModalOpen(true);
  const handleAddClose = () => {
    setAddModalOpen(false);
    setNewEntry({ content: '', date: new Date().toISOString().split('T')[0] });
  };

  const handleAddEntry = () => {
    const entry = {
      id: Date.now(), // Simple way to generate unique id
      ...newEntry
    };
    setEntries([...entries, entry]);
    handleAddClose();
  };

  // Edit Entry Handlers
  const handleEditOpen = (entry) => {
    setEditingEntry(entry);
    setEditModalOpen(true);
  };

  const handleEditClose = () => {
    setEditModalOpen(false);
    setEditingEntry(null);
  };

  const handleEditEntry = () => {
    if (!editingEntry) return;
    
    setEntries(entries.map(entry => 
      entry.id === editingEntry.id ? editingEntry : entry
    ));
    handleEditClose();
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

  const handleDeleteEntry = () => {
    if (!entryToDelete) return;
    setEntries(entries.filter(entry => entry.id !== entryToDelete.id));
    handleDeleteClose();
  };

  // Modal Component for both Add and Edit
  const EntryModal = ({ open, onClose, title, entry, onSave, buttonText }) => (
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
          onChange={(e) => title === 'Edit Journal Entry' 
            ? setEditingEntry({ ...editingEntry, content: e.target.value })
            : setNewEntry({ ...newEntry, content: e.target.value })
          }
          sx={{ mb: 3 }}
        />

        <Typography variant="subtitle1" sx={{ mb: 1 }}>
          Date
        </Typography>
        <TextField
          type="date"
          fullWidth
          value={entry.date}
          onChange={(e) => title === 'Edit Journal Entry'
            ? setEditingEntry({ ...editingEntry, date: e.target.value })
            : setNewEntry({ ...newEntry, date: e.target.value })
          }
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

  // Delete Confirmation Modal Component
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

  return (
    <Box sx={{ 
      padding: 3,
      width: '100%',
      maxWidth: '1200px',
      margin: '0 auto'
    }}>
      {/* Add Journal Entry Button */}
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

      {/* Add Modal */}
      <EntryModal
        open={addModalOpen}
        onClose={handleAddClose}
        title="Add Journal Entry"
        entry={newEntry}
        onSave={handleAddEntry}
        buttonText="Add Journal Entry"
      />

      {/* Edit Modal */}
      <EntryModal
        open={editModalOpen}
        onClose={handleEditClose}
        title="Edit Journal Entry"
        entry={editingEntry || { content: '', date: '' }}
        onSave={handleEditEntry}
        buttonText="Edit Journal Entry"
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmationModal
        open={deleteModalOpen}
        onClose={handleDeleteClose}
        onConfirm={handleDeleteEntry}
      />

      {/* Journal Entries List */}
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
          <Checkbox 
            sx={{
              mt: 1,
              '& .MuiSvgIcon-root': {
                fontSize: 20,
              }
            }}
          />
          
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
              Due: {entry.date}
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
