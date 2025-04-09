import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import googleLogo from '../assets/Google-Symbol.png';

const GoogleButton = () => {
  return (
    <>
      <Box sx={{ display: 'flex', alignItems: 'center', my: 2 }}>
        <Box sx={{ flex: 1, height: '1px', backgroundColor: '#e0e0e0' }} />
        <Typography sx={{ mx: 2, color: '#666', fontSize: '0.875rem' }}>or</Typography>
        <Box sx={{ flex: 1, height: '1px', backgroundColor: '#e0e0e0' }} />
      </Box>
      <Button
        fullWidth
        variant="outlined"
        sx={{
          py: 1,
          border: '1px solid #e0e0e0',
          color: '#000',
          backgroundColor: '#fff',
          textTransform: 'none',
          '&:hover': {
            backgroundColor: '#f8f8f8',
            border: '1px solid #e0e0e0',
          },
          mb: 3
        }}
        startIcon={
          <img
            src={googleLogo}
            alt="Google logo"
            style={{ height: '18px' }}
          />
        }
      >
        Google
      </Button>
    </>
  );
};

export default GoogleButton; 