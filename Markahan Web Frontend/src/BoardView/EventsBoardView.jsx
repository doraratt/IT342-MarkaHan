import React, { useEffect, useState } from 'react';
import Box from '@mui/joy/Box';
import Typography from '@mui/material/Typography';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'; // Fallback for local dev

export default function EventBoardView() {
  const { user } = useUser();
  const [events, setEvents] = useState([]);
  const [error, setError] = useState(null);

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    if (user) {
      const fetchEvents = async () => {
        try {
          const response = await axios.get(`${API_URL}/api/eventcalendar/getEventByUser?userId=${user.userId}`);
          const parsedEvents = response.data.map(event => ({
            calendarId: event.calendarId,
            title: event.eventDescription, // Rename eventDescription to title
            date: formatDateString(new Date(event.date))
          }));
          
          // Sort events by date ASC
          parsedEvents.sort((a, b) => new Date(a.date) - new Date(b.date));
          setEvents(parsedEvents);
          setError(null); // Clear any previous errors
        } catch (error) {
          console.error('Error fetching events:', error.response?.data || error.message);
          setError('Failed to load events. Please check if the server is running or the API endpoint is correct.');
        }
      };
      fetchEvents();
    }
  }, [user]);

  const formatDateString = (date) => {
    const utcDate = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    return utcDate.toISOString().split('T')[0];
  };

  const formatDateWithDay = (date) => {
    const options = { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(date).toLocaleDateString('en-US', options);
  };

  return (
    <Box sx={{ 
      width: '100%',
      height: '100%',
    }}>
      <Box sx={{ 
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        maxHeight: '80vh',
        padding: 2,
        '&::-webkit-scrollbar': {
          width: '8px',
        },
        '&::-webkit-scrollbar-track': {
          background: '#f1f1f1',
          borderRadius: '4px',
        },
        '&::-webkit-scrollbar-thumb': {
          background: '#4259c1',
          borderRadius: '4px',
        },
      }}>
        {error ? (
          <Typography variant="body1" sx={{ color: '#666' }}>
            {error}
          </Typography>
        ) : events.length === 0 ? (
          <Typography variant="body1" sx={{ color: '#666' }}>
            No events found.
          </Typography>
        ) : (
          events.map((event) => (
            <Box
              key={event.calendarId}
              sx={{
                padding: 3,
                borderRadius: '8px',
                backgroundColor: '#dde5f8',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
                },
              }}
            >
              <Typography
                variant="subtitle1"
                sx={{
                  color: '#1f295a',
                  fontWeight: 'bold',
                  marginBottom: 1
                }}
              >
                {formatDateWithDay(event.date)}
              </Typography>
              <Typography
                sx={{
                  color: '#1f295a',
                }}
              >
                {event.title}
              </Typography>
            </Box>
          ))
        )}
      </Box>
    </Box>
  );
}