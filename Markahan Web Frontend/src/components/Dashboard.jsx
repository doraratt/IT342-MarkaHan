import React from 'react';
import { Box, Typography } from '@mui/material';
import { useUser } from '../UserContext';
import JournalBoardView from '../BoardView/JournalBoardView';
import CurrentTime from '../BoardView/CurrentTime';
import NumberOfStudents from '../BoardView/NumberOfStudents';
import EventBoardView from '../BoardView/EventsBoardView';
import AttendanceBoardView from '../BoardView/AttendanceBoardView';

const Dashboard = () => {
  const { user } = useUser();

  return (
    <Box sx={{ width: '95%', marginLeft: 2, padding: 2, boxSizing: 'border-box' }}>
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(6, 1fr)',
          gridTemplateRows: 'repeat(6, 1fr)',
          gap: '8px',
          width: '98%',
          height: '600px', // Adjust height to fit the 6x6 grid proportionally
        }}
      >
        {/* Box 1 (Current Time) */}
        <Box
          sx={{
            gridColumn: 'span 2',
            gridRow: 'span 3',
            borderRadius: '15px',
            backgroundColor: '#fff9f2',
            padding: 2,
            overflow: 'hidden',
            border: '1px solid #e0e0e0',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1f295a', mb: 1 }}>
            Current Time
          </Typography>
          <Box sx={{ 
            flexGrow: 1,
            overflow: 'hidden',
            height: 'calc(100% - 28px)',
          }}>
            <CurrentTime />
          </Box>
        </Box>

        {/* Box 2 (Number of Students) */}
        <Box
          sx={{
            gridColumn: '3 / span 2',
            gridRow: 'span 3',
            borderRadius: '15px',
            backgroundColor: '#fff9f2',
            padding: 2,
            overflow: 'hidden',
            border: '1px solid #e0e0e0',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1f295a'}}>
            Number of Students
          </Typography>
          <Box sx={{ 
            flexGrow: 1,
            overflow: 'hidden',
            height: 'calc(100% - 28px)',
          }}>
            <NumberOfStudents />
          </Box>
        </Box>

        {/* Box 3 (Journal Entries) */}
        <Box
          sx={{
            gridColumn: '5 / span 2',
            gridRow: 'span 6',
            borderRadius: '15px',
            backgroundColor: '#fff9f2',
            padding: 2,
            overflow: 'hidden', // Prevent any overflow outside the box
            border: '1px solid #e0e0e0',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1f295a', mb: 1 }}>
            Journal Entries
          </Typography>
          <Box sx={{ 
            flexGrow: 1,
            overflowY: 'auto', // Enable vertical scrolling
            overflowX: 'hidden', // Disable horizontal scrolling
            height: 'calc(100% - 28px)',
            // Hide the vertical scrollbar while keeping it functional
            scrollbarWidth: 'none', // Firefox
            '&::-webkit-scrollbar': {
              display: 'none', // Chrome, Safari, Edge
            },
          }}>
            <JournalBoardView />
          </Box>
        </Box>

        {/* Box 4 (Events) */}
        <Box
          sx={{
            gridColumn: 'span 2',
            gridRow: '4 / span 3',
            borderRadius: '15px',
            backgroundColor: '#fff9f2',
            padding: 2,
            overflow: 'hidden',
            border: '1px solid #e0e0e0',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1f295a', mb: 1 }}>
            Events
          </Typography>
          <Box sx={{ 
            flexGrow: 1,
            overflowY: 'auto', // Enable vertical scrolling
            overflowX: 'hidden', // Disable horizontal scrolling
            height: 'calc(100% - 28px)',
            // Hide the vertical scrollbar while keeping it functional
            scrollbarWidth: 'none', // Firefox
            '&::-webkit-scrollbar': {
              display: 'none', // Chrome, Safari, Edge
            },
          }}>
            <EventBoardView />
          </Box>
        </Box>

        {/* Box 5 (Attendance Pie Chart) */}
        <Box
          sx={{
            gridColumn: '3 / span 2',
            gridRow: '4 / span 3',
            borderRadius: '15px',
            backgroundColor: '#fff9f2',
            padding: 2,
            overflow: 'hidden',
            border: '1px solid #e0e0e0',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1f295a', mb: 1 }}>
            Today's Attendance
          </Typography>
          <Box sx={{ 
            flexGrow: 1,
            overflow: 'hidden',
            height: 'calc(100% - 28px)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
          }}>
            <AttendanceBoardView />
          </Box>
        </Box>
      </Box>
    </Box>
  );
};

export default Dashboard;