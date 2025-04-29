import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AccountCircle from '@mui/icons-material/AccountCircle';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import FormControl from '@mui/material/FormControl';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import InputLabel from '@mui/material/InputLabel';
import OutlinedInput from '@mui/material/OutlinedInput';
import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import { Link as RouterLink } from 'react-router-dom';
import axios from 'axios';
import { useUser } from '../UserContext'; // Ensure path is correct
import GoogleButton from './GoogleButton';
import { AppProvider } from '@toolpad/core/AppProvider';
import logo2 from "../assets/logo1.png";
import logo1 from "../assets/logo2.png"

function CustomEmailField({ onChange }) {
  return (
    <FormControl sx={{ my: 1 }} fullWidth variant="outlined">
      <InputLabel size="small" htmlFor="outlined-adornment-email">
        Email
      </InputLabel>
      <OutlinedInput
        id="outlined-adornment-email"
        label="Email"
        name="email"
        type="email"
        size="small"
        required
        onChange={onChange}
        endAdornment={
          <InputAdornment position="end">
            <AccountCircle />
          </InputAdornment>
        }
      />
    </FormControl>
  );
}

function CustomPasswordField({ onChange }) {
  const [showPassword, setShowPassword] = useState(false);

  const handleClickShowPassword = () => setShowPassword((show) => !show);
  const handleMouseDownPassword = (event) => {
    event.preventDefault();
  };

  return (
    <FormControl sx={{ my: 1 }} fullWidth variant="outlined">
      <InputLabel size="small" htmlFor="outlined-adornment-password">
        Password
      </InputLabel>
      <OutlinedInput
        id="outlined-adornment-password"
        type={showPassword ? 'text' : 'password'}
        name="password"
        size="small"
        onChange={onChange}
        endAdornment={
          <InputAdornment position="end">
            <IconButton
              aria-label="toggle password visibility"
              onClick={handleClickShowPassword}
              onMouseDown={handleMouseDownPassword}
              edge="end"
              size="small"
            >
              {showPassword ? (
                <VisibilityOff fontSize="inherit" />
              ) : (
                <Visibility fontSize="inherit" />
              )}
            </IconButton>
          </InputAdornment>
        }
        label="Password"
      />
    </FormControl>
  );
}

function CustomButton() {
  return (
    <Button
      type="submit"
      variant="contained"
      size="large"
      disableElevation
      fullWidth
      sx={{ my: 2, backgroundColor: '#4259c1' }}
    >
      Sign In
    </Button>
  );
}

function SignUpLink() {
  return (
    <RouterLink to="/signup" style={{ textAlign: 'center', textDecoration: 'none' }}>
      <Typography>
        <span style={{ color: '#333' }}>Not registered yet?</span>{' '}
        <span style={{ color: '#4259c1' }}>Create an account</span>
      </Typography>
    </RouterLink>
  );
}

const Login = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { setUser } = useUser();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); // Reset error
    setSuccess(''); // Reset success

    try {
      const response = await axios.post('http://localhost:8080/api/user/login', {
        email: formData.email, // Changed from username to email to match UserEntity
        password: formData.password,
      });
      setUser(response.data); // Save user in context
      setSuccess('Login successful!');
      setError('');
      console.log('Login successful:', response.data);
      navigate('/dashboard');
    } catch (err) {
      setError('Login failed: ' + (err.response?.status === 401 ? 'Invalid credentials' : 'Unknown error'));
      setSuccess('');
      console.error('Login error:', err);
    }
  };

  return (
    <AppProvider theme={theme}>
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundImage: 'linear-gradient(45deg, #4259c1, #1f295a)',
      }}
    >
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          display: 'flex',
          flexDirection: 'column',
          maxWidth: 500,
          minWidth: 450,
          height: 670,
          padding: 4,
          borderRadius: '12px 0 0 12px',
          backgroundColor: '#d6e1f7',
        }}
      >
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            paddingBottom: 5,
            paddingLeft: 5,
            paddingRight: 5,
          }}
        >
          <img src={logo2} alt="Logo" style={{ height: '130px', marginBottom: '20px', alignSelf: 'center' }} />
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              mb: 4,
            }}
          >
            <Typography variant="h4" align="left" sx={{ color: '#1f295a', fontWeight: 'bold' }}>
              Welcome back
            </Typography>
            <Typography align="left" sx={{ mb: 2 }}>
              We are happy to see you again.
            </Typography>
          </Box>

          <CustomEmailField onChange={handleChange} />
          <CustomPasswordField onChange={handleChange} />
          <CustomButton />
          <GoogleButton />
          <SignUpLink />

          {error && <Typography color="error" sx={{ mt: 2 }} align="center">{error}</Typography>}
          {success && <Typography color="primary" sx={{ mt: 2 }} align="center">{success}</Typography>}
        </Box>
      </Box>

      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          maxWidth: 700,
          minWidth: 450,
          height: 670,
          padding: 4,
          borderRadius: '0px 12px 12px 0px',
          backgroundColor: '#4259c1',
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
      <img src={logo1} alt="Logo" style={{ height: '180px', marginBottom: '20px', alignSelf: 'center' }} />
      </Box>
    </Box>
    </AppProvider>
  );
};

export default Login;