import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CloseIcon from '@mui/icons-material/Close';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import axios from 'axios';
import { useUser } from '../UserContext';

// EventModal
const EventModal = ({ open, onClose, date, event, onSave, onChange, isEditMode, eventId }) => (
  <Modal open={open} onClose={onClose} aria-labelledby="event-modal">
    <Box sx={{
      position: 'absolute',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      width: 400,
      bgcolor: 'background.paper',
      borderRadius: 2,
      p: 4,
    }}>
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        mb: 2
      }}>
        <Typography variant="h6">
          {isEditMode ? 'Edit Event' : 'Add Event'} for {date ? new Date(date).toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
          }) : ''}
        </Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </Box>

      <TextField
        fullWidth
        label="Event Name"
        value={event}
        onChange={onChange}
        sx={{ mb: 3 }}
        autoComplete="off"
      />

      <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          onClick={onSave}
          sx={{
            backgroundColor: '#1f295a',
            '&:hover': { backgroundColor: '#4259c1' },
            textTransform: 'none',
            fontWeight: 'normal',
          }}
        >
          {isEditMode ? 'Update Event' : 'Add Event'}
        </Button>
        <Button
          variant="outlined"
          onClick={onClose}
          sx={{
            textTransform: 'none',
            fontWeight: 'normal',
            borderColor: '#1f295a',
            color: '#1f295a',
            '&:hover': {
              borderColor: '#4259c1',
              backgroundColor: 'transparent',
            },
          }}
        >
          Cancel
        </Button>
      </Box>
    </Box>
  </Modal>
);

// ConfirmEditEventModal
const ConfirmEditEventModal = ({ open, onClose, onConfirm, eventDescription }) => (
  <Modal open={open} onClose={onClose}>
    <Box sx={{
      position: 'absolute',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      width: 400,
      bgcolor: 'background.paper',
      borderRadius: 2,
      p: 3,
    }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Confirm Edit</Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </Box>
      <Typography sx={{ mb: 3 }}>
        Are you sure you want to edit the event "{eventDescription}"?
      </Typography>
      <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          onClick={onConfirm}
          sx={{ 
            backgroundColor: '#4CAF50', 
            '&:hover': { backgroundColor: '#45a049' }, 
            textTransform: 'none' 
          }}
        >
          Confirm
        </Button>
        <Button
          variant="contained"
          onClick={onClose}
          sx={{ 
            backgroundColor: '#9e9e9e', 
            '&:hover': { backgroundColor: '#757575' }, 
            textTransform: 'none' 
          }}
        >
          Cancel
        </Button>
      </Box>
    </Box>
  </Modal>
);

// ConfirmDeleteEventModal
const ConfirmDeleteEventModal = ({ open, onClose, onConfirm, eventDescription }) => (
  <Modal open={open} onClose={onClose}>
    <Box sx={{
      position: 'absolute',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      width: 400,
      bgcolor: 'background.paper',
      borderRadius: 2,
      p: 3,
    }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Confirm Delete</Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </Box>
      <Typography sx={{ mb: 3 }}>
        Are you sure you want to delete the event "{eventDescription}"?
      </Typography>
      <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          onClick={onConfirm}
          sx={{ 
            backgroundColor: '#f44336', 
            '&:hover': { backgroundColor: '#d32f2f' }, 
            textTransform: 'none' 
          }}
        >
          Confirm
        </Button>
        <Button
          variant="contained"
          onClick={onClose}
          sx={{ 
            backgroundColor: '#9e9e9e', 
            '&:hover': { backgroundColor: '#757575' }, 
            textTransform: 'none' 
          }}
        >
          Cancel
        </Button>
      </Box>
    </Box>
  </Modal>
);

function Calendar() {
  const { user } = useUser();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [confirmEditModalOpen, setConfirmEditModalOpen] = useState(false);
  const [confirmDeleteModalOpen, setConfirmDeleteModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null);
  const [events, setEvents] = useState({});
  const [newEvent, setNewEvent] = useState('');
  const [error, setError] = useState('');
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingEventId, setEditingEventId] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);

  const getDaysInMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  const getFirstDayOfMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  };

  const formatMonth = (date) => {
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return `${monthNames[date.getMonth()]} ${date.getFullYear()}`;
  };

  const formatDateString = (year, month, day) => {
    return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  };

  useEffect(() => {
    if (user) {
      fetchEvents();
    }
  }, [user]);

  const fetchEvents = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/eventcalendar/getEventByUser?userId=${user.userId}`);
      const eventsMap = response.data.reduce((acc, event) => {
        const date = event.date;
        if (!acc[date]) {
          acc[date] = [];
        }
        acc[date].push({ calendarId: event.calendarId, description: event.eventDescription });
        return acc;
      }, {});
      setEvents(eventsMap);
      setError('');
    } catch (error) {
      setError('Error fetching events: ' + (error.response?.data || error.message));
      console.error('Fetch error:', error);
    }
  };

  const handlePrevMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1));
  };

  const handleNextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1));
  };

  const handleDateClick = (day) => {
    const dateString = formatDateString(
      currentDate.getFullYear(),
      currentDate.getMonth(),
      day
    );
    setSelectedDate(dateString);
    setNewEvent('');
    setIsEditMode(false);
    setEditingEventId(null);
    setEventModalOpen(true);
  };

  const handleAddEvent = async () => {
    if (!user) {
      setError('User not logged in');
      return;
    }
    if (!newEvent.trim()) {
      setError('Event description cannot be empty');
      return;
    }

    const eventData = {
      eventDescription: newEvent.trim(),
      date: selectedDate,
      user: { userId: user.userId }
    };

    try {
      const response = await axios.post('http://localhost:8080/api/eventcalendar/addEventCal', eventData);
      setEvents(prev => ({
        ...prev,
        [selectedDate]: [
          ...(prev[selectedDate] || []),
          { calendarId: response.data.calendarId, description: response.data.eventDescription }
        ]
      }));
      setEventModalOpen(false);
      setNewEvent('');
      setError('');
    } catch (error) {
      setError('Error adding event: ' + (error.response?.data || error.message));
      console.error('Add error:', error);
    }
  };

  const handleUpdateEvent = async () => {
    if (!user) {
      setError('User not logged in');
      return;
    }
    if (!newEvent.trim()) {
      setError('Event description cannot be empty');
      return;
    }

    const eventData = {
      calendarId: editingEventId,
      eventDescription: newEvent.trim(),
      date: selectedDate,
      user: { userId: user.userId }
    };

    try {
      const response = await axios.put(
        `http://localhost:8080/api/eventcalendar/updateEventCal/${editingEventId}`,
        eventData,
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );

      setEvents(prev => {
        const updated = { ...prev };
        const eventIndex = updated[selectedDate].findIndex(e => e.calendarId === editingEventId);
        updated[selectedDate][eventIndex] = { 
          calendarId: editingEventId, 
          description: newEvent.trim() 
        };
        return updated;
      });
      
      setConfirmEditModalOpen(false);
      setEventModalOpen(false);
      setNewEvent('');
      setIsEditMode(false);
      setEditingEventId(null);
      setSelectedEvent(null);
      setError('');
    } catch (error) {
      console.error('Update error details:', {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data,
        config: error.config
      });
      setError('Error updating event: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleEditEvent = (event) => {
    setSelectedDate(event.date);
    setNewEvent(event.description);
    setIsEditMode(true);
    setEditingEventId(event.calendarId);
    setEventModalOpen(true);
  };

  const handleConfirmEdit = () => {
    setSelectedEvent({ 
      calendarId: editingEventId, 
      description: newEvent, 
      date: selectedDate 
    });
    setEventModalOpen(false);
    setConfirmEditModalOpen(true);
  };

  const handleDeleteEvent = async () => {
    try {
      await axios.delete(`http://localhost:8080/api/eventcalendar/deleteEventCal/${selectedEvent.calendarId}`);
      setEvents(prev => {
        const updated = { ...prev };
        updated[selectedEvent.date] = updated[selectedEvent.date].filter(event => event.calendarId !== selectedEvent.calendarId);
        if (updated[selectedEvent.date].length === 0) {
          delete updated[selectedEvent.date];
        }
        return updated;
      });
      setConfirmDeleteModalOpen(false);
      setSelectedEvent(null);
      setError('');
    } catch (error) {
      setError('Error deleting event: ' + (error.response?.data || error.message));
      console.error('Delete error:', error);
    }
  };

  const handleConfirmDelete = (event) => {
    setSelectedEvent(event);
    setConfirmDeleteModalOpen(true);
  };

  const handleSave = () => {
    if (isEditMode) {
      handleConfirmEdit();
    } else {
      handleAddEvent();
    }
  };

  const eventsList = Object.entries(events)
    .flatMap(([date, eventArray]) => eventArray.map(event => ({ date, ...event })))
    .sort((a, b) => new Date(a.date) - new Date(b.date));

  return (
    <Box sx={{ 
      padding: 4,
      maxWidth: '800px',
      width: '100%',
      margin: '0 auto',
    }}>
      {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}

      <Box sx={{ 
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        mb: 4,
      }}>
        <IconButton onClick={handlePrevMonth} size="large">
          <ChevronLeftIcon />
        </IconButton>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          {formatMonth(currentDate)}
        </Typography>
        <IconButton onClick={handleNextMonth} size="large">
          <ChevronRightIcon />
        </IconButton>
      </Box>

      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: 'repeat(7, 1fr)',
        gap: 2,
        justifyItems: 'center',
      }}>
        {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(day => (
          <Typography
            key={`header-${day}`}
            sx={{
              width: '60px',
              height: '60px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.1rem',
              fontWeight: 500,
              color: '#666',
            }}
          >
            {day}
          </Typography>
        ))}

        {Array.from({ length: 42 }, (_, i) => {
          const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
          const firstDay = date.getDay();
          const day = i - firstDay + 1;
          const currentMonth = date.getMonth();
          const isCurrentMonth = day > 0 && day <= getDaysInMonth(currentDate);
          
          const dateString = formatDateString(
            currentDate.getFullYear(),
            currentMonth,
            day
          );
          
          const hasEvent = events[dateString]?.length > 0;
          const isToday = new Date().toDateString() === new Date(currentDate.getFullYear(), currentMonth, day).toDateString();

          return (
            <Box
              key={i}
              onClick={() => isCurrentMonth && handleDateClick(day)}
              sx={{
                width: '60px',
                height: '60px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: isCurrentMonth ? 'pointer' : 'default',
                borderRadius: '30px',
                backgroundColor: hasEvent ? '#e3f2fd' : 
                                isToday ? '#e6f3ff' : 
                                'transparent',
                '&:hover': isCurrentMonth ? {
                  backgroundColor: '#f0f0f0',
                } : {},
                position: 'relative',
              }}
            >
              <Typography
                sx={{
                  fontSize: '1.2rem',
                  color: !isCurrentMonth ? '#ccc' : 'black',
                }}
              >
                {isCurrentMonth ? day : 
                 day <= 0 ? getDaysInMonth(new Date(date.getFullYear(), date.getMonth() - 1)) + day :
                 day - getDaysInMonth(currentDate)}
              </Typography>
              {hasEvent && (
                <Box
                  sx={{
                    width: '4px',
                    height: '4px',
                    borderRadius: '50%',
                    backgroundColor: '#1f295a',
                    position: 'absolute',
                    bottom: '8px',
                  }}
                />
              )}
            </Box>
          );
        })}
      </Box>

      <Box sx={{ mt: 4 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>Events</Typography>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Date</TableCell>
                <TableCell>Event Description</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {eventsList.length > 0 ? (
                eventsList.map((event, index) => (
                  <TableRow key={index}>
                    <TableCell>{new Date(event.date).toLocaleDateString('en-US', {
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric'
                    })}</TableCell>
                    <TableCell>{event.description}</TableCell>
                    <TableCell align="right">
                      <IconButton
                        size="small"
                        onClick={() => handleEditEvent(event)}
                        sx={{ mr: 1 }}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleConfirmDelete(event)}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={3} align="center">
                    No events found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>

      <EventModal
        open={eventModalOpen}
        onClose={() => {
          setEventModalOpen(false);
          setIsEditMode(false);
          setEditingEventId(null);
        }}
        date={selectedDate}
        event={newEvent}
        onSave={handleSave}
        onChange={(e) => setNewEvent(e.target.value)}
        isEditMode={isEditMode}
        eventId={editingEventId}
      />

      <ConfirmEditEventModal
        open={confirmEditModalOpen}
        onClose={() => setConfirmEditModalOpen(false)}
        onConfirm={handleUpdateEvent}
        eventDescription={selectedEvent?.description || ''}
      />

      <ConfirmDeleteEventModal
        open={confirmDeleteModalOpen}
        onClose={() => setConfirmDeleteModalOpen(false)}
        onConfirm={handleDeleteEvent}
        eventDescription={selectedEvent?.description || ''}
      />
    </Box>
  );
}

export default Calendar;