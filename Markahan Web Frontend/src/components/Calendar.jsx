import { useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CloseIcon from '@mui/icons-material/Close';

function Calendar() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDates, setSelectedDates] = useState([]);
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null);
  const [events, setEvents] = useState({}); // Store events by date
  const [newEvent, setNewEvent] = useState('');

  // Helper functions
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

  // Helper function to format date consistently
  const formatDateString = (year, month, day) => {
    return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  };

  // Navigation handlers
  const handlePrevMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1));
  };

  const handleNextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1));
  };

  // Date selection handler
  const handleDateClick = (day) => {
    const dateString = formatDateString(
      currentDate.getFullYear(),
      currentDate.getMonth(),
      day
    );
    setSelectedDate(dateString);
    setEventModalOpen(true);
    setNewEvent(events[dateString] || '');
  };

  const handleAddEvent = () => {
    if (newEvent.trim()) {
      setEvents(prev => ({
        ...prev,
        [selectedDate]: newEvent.trim()
      }));
    } else {
      const updatedEvents = { ...events };
      delete updatedEvents[selectedDate];
      setEvents(updatedEvents);
    }
    setEventModalOpen(false);
    setNewEvent('');
  };

  return (
    <Box sx={{ 
      padding: 4,
      maxWidth: '800px',
      width: '100%',
      margin: '0 auto',
    }}>
      {/* Calendar Header */}
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

      {/* Calendar Grid */}
      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: 'repeat(7, 1fr)',
        gap: 2,
        justifyItems: 'center',
      }}>
        {/* Week day headers */}
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

        {/* Calendar days */}
        {Array.from({ length: 42 }, (_, i) => {
          const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
          const firstDay = date.getDay();
          const day = i - firstDay + 1;
          const currentMonth = date.getMonth();
          const isCurrentMonth = day > 0 && day <= getDaysInMonth(currentDate);
          
          // Create date string using our consistent formatter
          const dateString = formatDateString(
            currentDate.getFullYear(),
            currentMonth,
            day
          );
          
          const hasEvent = events[dateString];
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

      {/* Event Modal */}
      <Modal
        open={eventModalOpen}
        onClose={() => setEventModalOpen(false)}
        aria-labelledby="event-modal"
      >
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
              Add Event for {selectedDate ? new Date(selectedDate).toLocaleDateString('en-US', {
                month: 'long',
                day: 'numeric',
                year: 'numeric'
              }) : ''}
            </Typography>
            <IconButton 
              onClick={() => setEventModalOpen(false)}
              size="small"
            >
              <CloseIcon />
            </IconButton>
          </Box>

          <TextField
            fullWidth
            label="Event Name"
            value={newEvent}
            onChange={(e) => setNewEvent(e.target.value)}
            sx={{ mb: 3 }}
          />

          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleAddEvent}
              sx={{
                backgroundColor: '#1f295a',
                '&:hover': {
                  backgroundColor: '#4259c1',
                },
                textTransform: 'none',
                fontWeight: 'normal',
              }}
            >
              Save event
            </Button>
            <Button
              variant="outlined"
              onClick={() => setEventModalOpen(false)}
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
    </Box>
  );
}

export default Calendar;
