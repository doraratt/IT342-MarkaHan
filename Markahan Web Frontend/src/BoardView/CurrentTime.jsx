import React, { useState, useEffect } from 'react';
import { Box, Typography } from '@mui/material';

export default function CurrentTime() {
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    // Cleanup interval on component unmount
    return () => clearInterval(timer);
  }, []);

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'flex-start',
        height: '95%',
        borderRadius: '15px',
        marginLeft: 2
      }}
    >

      <Typography
        variant="h4"
        sx={{
          color: '#30418e',
          fontWeight: 'bold',
          fontSize: '2.5rem', // Slightly smaller to fit in the analytics box
        }}
      >
        {currentTime.toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: true
        })}
      </Typography>
    </Box>
  );
}