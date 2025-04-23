import { useState } from 'react';
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
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import GoogleButton from './GoogleButton';
import axios from 'axios'; // Added axios import
import { useUser } from '../UserContext'; // Added UserContext import
import { AppProvider } from '@toolpad/core/AppProvider';

function CustomFirstNameField({ onChange }) {
  return (
    <FormControl sx={{ my: 1 }} fullWidth variant="outlined">
      <InputLabel size="small" htmlFor="outlined-adornment-firstname">
        First Name
      </InputLabel>
      <OutlinedInput
        id="outlined-adornment-firstname"
        label="First Name"
        name="firstName"
        type="text"
        size="small"
        required
        onChange={onChange}
      />
    </FormControl>
  );
}

function CustomLastNameField({ onChange }) {
  return (
    <FormControl sx={{ my: 1 }} fullWidth variant="outlined">
      <InputLabel size="small" htmlFor="outlined-adornment-lastname">
        Last Name
      </InputLabel>
      <OutlinedInput
        id="outlined-adornment-lastname"
        label="Last Name"
        name="lastName"
        type="text"
        size="small"
        required
        onChange={onChange}
      />
    </FormControl>
  );
}

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

function CustomPasswordField({ onChange, name, label }) {
  const [showPassword, setShowPassword] = useState(false);

  const handleClickShowPassword = () => setShowPassword((show) => !show);
  const handleMouseDownPassword = (event) => {
    event.preventDefault();
  };

  return (
    <FormControl sx={{ my: 1 }} fullWidth variant="outlined">
      <InputLabel size="small" htmlFor={`outlined-adornment-${name}`}>
        {label}
      </InputLabel>
      <OutlinedInput
        id={`outlined-adornment-${name}`}
        type={showPassword ? 'text' : 'password'}
        name={name}
        size="small"
        required
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
              {showPassword ? <VisibilityOff fontSize="inherit" /> : <Visibility fontSize="inherit" />}
            </IconButton>
          </InputAdornment>
        }
        label={label}
        autoComplete={name === 'password' ? 'new-password' : 'off'}
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
      Sign Up
    </Button>
  );
}

function SignInLink() {
  return (
    <RouterLink to="/login" style={{ textAlign: 'center', textDecoration: 'none' }}>
      <Typography>
        <span style={{ color: '#333' }}>Already have an account?</span>{' '}
        <span style={{ color: '#4259c1' }}>Sign in</span>
      </Typography>
    </RouterLink>
  );
}

const SignUp = () => {
  const theme = useTheme();
  const navigate = useNavigate(); // Added navigation
  const { setUser } = useUser(); // Added user context
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'confirmPassword') {
      setConfirmPassword(value);
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Password validation
    if (formData.password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    const passwordValidation = /^(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    if (!passwordValidation.test(formData.password)) {
      setError('Password must be at least 8 characters long and contain at least one special character');
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/user/signup', formData); // Updated to use axios
      console.log('Response data:', response.data);
      setUser(response.data); // Set user in context
      setSuccess('Account created successfully!');
      setError(null);
      
      // Clear form
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
      });
      setConfirmPassword('');
      
      navigate('/dashboard'); // Redirect to home page
    } catch (err) {
      console.error('Signup error:', err);
      if (err.response) {
        setError('Signup failed: ' + (err.response.data?.message || 'Server error'));
      } else {
        setError('Signup failed: ' + err.message);
      }
      setSuccess(null);
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
        backgroundImage: 'linear-gradient(45deg, #1f295a, #4259c1)',
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
        <Box sx={{ display: 'flex', flexDirection: 'column', paddingBottom: 5, paddingLeft: 5, paddingRight: 5 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
            <Typography variant="h4" align="left" sx={{ color: '#1f295a', fontWeight: 'bold' }}>
              Welcome User
            </Typography>
            <Typography align="left" sx={{ mb: 2 }}>
              Create your account.
            </Typography>
          </Box>

          <CustomFirstNameField onChange={handleChange} />
          <CustomLastNameField onChange={handleChange} />
          <CustomEmailField onChange={handleChange} />
          <CustomPasswordField onChange={handleChange} name="password" label="Password" />
          <CustomPasswordField onChange={handleChange} name="confirmPassword" label="Confirm Password" />
          
          {error && <Typography color="error" align="center">{error}</Typography>}
          {success && <Typography color="success.main" align="center">{success}</Typography>}
          
          <CustomButton />
          <GoogleButton />
          <SignInLink />
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
          backgroundColor: '#1f295a',
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        <Typography variant="h4" sx={{ color: 'white', mb: 2 }}>
          Markahan
        </Typography>
      </Box>
    </Box>
    </AppProvider>
  );
};

export default SignUp;