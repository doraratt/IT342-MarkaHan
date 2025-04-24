import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography'; 
import NotFoundImage from '../assets/404.svg'; // Ensure 404.svg is in the correct directory

const NotFound = () => {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        textAlign: 'left',
        backgroundColor: '#d6e1f7',
      }}
    >
      {/* SVG Image */}
      <Box
        component="img"
        src={NotFoundImage}
        alt="404 Not Found"
        sx={{
          maxWidth: '250px',
          marginRight: '200px',
          marginBottom: 4,
        }}
      />

      {/* Text Content */}
      <Box>
        <Typography
          variant="h1"
          sx={{
            fontWeight: 'bold',
            fontSize: '8rem',
            color: '#1f295a',
            fontFamily: 'Arial'
          }}
        >
          404
        </Typography>

        <Typography
          variant="body1"
          sx={{
            fontWeight: 'bold',
            fontSize: '2rem',
            color: '#1f295a',
            marginBottom: 1,
            fontFamily: 'Arial'
          }}
        >
          Looks like you are lost.
        </Typography>

        <Typography
          variant="body1"
          sx={{
            fontSize: '1.2rem',
            color: '#555',
            marginBottom: 4,
            marginRight: 4,
            fontFamily: 'Arial'
          }}
        >
          The page you are looking for is not available.
        </Typography>
      </Box>
    </Box>
  );
};

export default NotFound;
