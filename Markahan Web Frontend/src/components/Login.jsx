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
import { Link as RouterLink } from 'react-router-dom';
import GoogleButton from './GoogleButton';

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
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Login attempt with:', formData);
  };

  return (
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
          <CustomPasswordField onChange={handleChange} name="password" label="Password" />
          <CustomButton />
          <GoogleButton />
          <SignUpLink />
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
        <Typography variant="h4" sx={{ color: 'white', mb: 2 }}>
          Your Logo Here
        </Typography>
      </Box>
    </Box>
  );
};

export default Login;
