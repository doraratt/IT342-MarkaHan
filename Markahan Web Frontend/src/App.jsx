import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import SignUp from './components/Signup';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
      </Routes>
    </Router>
  );
}

export default App;
