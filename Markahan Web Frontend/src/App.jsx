import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import SignUp from './components/Signup';
import DashboardLayout from './components/DashboardLayout';
import Grades from './components/Grades';
import Attendance from './components/Attendance';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/dashboard" element={<DashboardLayout />} />
        <Route path="grades" element={<Grades />} />
        <Route path="/attendace" element={<Attendance/>}/>
      </Routes>
    </Router>
  );
}

export default App;