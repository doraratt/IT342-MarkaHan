import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import SignUp from './components/Signup';
import DashboardLayout from './components/DashboardLayout';
import Grades from './components/Grades';
import Attendance from './components/Attendance';
import './App.css';
import { UserProvider } from './UserContext';
import OAuth2RedirectHandler from './components/OAuth2RedirectHandler';

function App() {
  return (
    <UserProvider> 
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/dashboard" element={<DashboardLayout />} />
        <Route path="grades" element={<Grades />} />
        <Route path="/attendace" element={<Attendance/>}/>
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
      </Routes>
    </Router>
    </UserProvider>
  );
}

export default App;