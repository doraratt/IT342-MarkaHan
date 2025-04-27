import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

export default function JournalBoardView() {
  const { user } = useUser();
  const [entries, setEntries] = useState([]);

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
    }
  }, [user]);

  // Function to truncate text to 20-30 words and limit to one sentence
  const truncateText = (text) => {
    // Split into sentences and take the first one
    const sentences = text.split('.');
    const firstSentence = sentences[0].trim();

    // Split the first sentence into words
    const words = firstSentence.split(/\s+/);
    const maxWords = 25; // Target around 20-30 words
    if (words.length > maxWords) {
      return words.slice(0, maxWords).join(' ') + '...';
    }
    return firstSentence + (sentences.length > 1 ? '...' : '');
  };

  return (
    <Box sx={{ 
      width: '100%',
      height: '100%',
      overflow: 'hidden', // Prevent horizontal overflow
      padding: 1,
    }}>
      <Box sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 1,
        maxHeight: '100%',
        overflowY: 'auto', // Enable vertical scrolling
        overflowX: 'hidden', // Disable horizontal scrolling
        // Hide the vertical scrollbar while keeping it functional
        scrollbarWidth: 'none', // Firefox
        '&::-webkit-scrollbar': {
          display: 'none', // Chrome, Safari, Edge
        },
      }}>
        {entries.length === 0 ? (
          <Typography variant="body1" sx={{ color: '#666' }}>
            No journal entries found.
          </Typography>
        ) : (
          entries.map((entry) => (
            <Box
              key={entry.id}
              sx={{
                backgroundColor: '#f5f5f5',
                padding: '10px',
                borderRadius: '8px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
                },
              }}
            >
              <Typography
                variant="body1"
                sx={{
                  color: '#333',
                  mb: 1,
                  lineHeight: 1.6,
                  whiteSpace: 'normal', // Allow text to wrap
                  overflowWrap: 'break-word', // Ensure words break to fit
                  overflow: 'hidden', // Prevent any overflow
                }}
              >
                {truncateText(entry.content)}
              </Typography>
              <Typography
                variant="caption"
                sx={{
                  color: '#666',
                  display: 'block',
                  whiteSpace: 'normal', // Allow date to wrap if needed
                  overflowWrap: 'break-word',
                }}
              >
                Date: {entry.date}
              </Typography>
            </Box>
          ))
        )}
      </Box>
    </Box>
  );
}