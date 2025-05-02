import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import googleLogo from '../assets/Google-Symbol.png';

const GoogleButton = () => {
  const handleGoogleLogin = () => {
    console.log("Initiating Google OAuth2 redirect");
    try {
      // Use the standard OAuth2 authorization endpoint
      window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    } catch (err) {
      console.error("Redirect failed:", err);
      alert("Failed to initiate Google login. Please try again.");
    }
  };

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
        onClick={handleGoogleLogin}
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
            src={googleLogo || "/placeholder.svg"}
            alt="Google logo"
            style={{ height: '18px' }}
          />
        }
      >
        Sign in with Google
      </Button>
    </>
  );
};

export default GoogleButton;