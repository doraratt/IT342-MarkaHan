import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import LogoutIcon from "@mui/icons-material/Logout";
import UserIcon from "@mui/icons-material/Person";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Divider from "@mui/material/Divider";
import Modal from "@mui/material/Modal";
import Typography from "@mui/material/Typography";
import PropTypes from "prop-types";
import * as React from "react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import PeopleIcon from '@mui/icons-material/People';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import GradingIcon from '@mui/icons-material/Grading';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import DashboardIcon from '@mui/icons-material/Dashboard';
import { useUser } from '../UserContext';
import logo1 from "../assets/logo2.png"

import Journal from './Journal';
import Calendar from './Calendar';
import Students from './Students';
import Grades from './Grades';
import Attendance from './Attendance';
import Dashboard from "./Dashboard";

const NAVIGATION = [
  {
    segment: "dashboard",
    title: "Dashboard",
    icon: <DashboardIcon />,
  },
  {
    segment: "calendar",
    title: "Calendar",
    icon: <CalendarMonthIcon />,
  },
  {
    segment: "students",
    title: "Students List",
    icon: <PeopleIcon />,
  },
  {
    segment: "attendance",
    title: "Attendance",
    icon: <HowToRegIcon />,
  },
  {
    segment: "grades",
    title: "Grades",
    icon: <GradingIcon />,
  },
  {
    segment: "journal",
    title: "Journal",
    icon: <MenuBookIcon />,
  },
];

function DemoPageContent({ activeSegment, user }) {
  const location = window.location.pathname;
  const studentIdMatch = location.match(/\/grades\/(\d+)/);
  const studentId = studentIdMatch ? studentIdMatch[1] : null;

  let content;
  
  switch (activeSegment) {
    case "dashboard":
      content = (
        <Box sx={{ width: "100%", padding: 0, margin: 0 }}>
          <Typography variant="h3" paddingTop={5} color="#134B70">
            Welcome to MarkaHan!
            <Typography variant="h6">
              {user ? `Welcome, Teacher ${user.firstName}!` : "Dashboard Coming Soon"}
            </Typography>
          </Typography>
          <Box
            sx={{
              width: "100%",
              marginTop: 2,
              boxSizing: "border-box",
              padding: 0,
            }}
          >
            <Dashboard user={user} />
          </Box>
        </Box>
      );
      break;
    case "calendar":
      content = <Calendar user={user} />;
      break;
    case "students":
      content = <Students user={user} />;
      break;
    case "attendance":
      content = <Attendance user={user} />;
      break;
    case "grades":
      content = <Grades user={user} />;
      break;
    case "journal":
      content = <Journal user={user} />;
      break;
    default:
      content = <Typography variant="h6">Select a segment to view content.</Typography>;
  }

  return (
    <Box sx={{
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      textAlign: "left",
      flexGrow: 1,
      overflowY: "hidden",
      minHeight: "100%",
      width: "100%",
    }}>
      {content}
    </Box>
  );
}

function Sidebar({ onNavigate, activeSegment, user }) {
  return (
    <Box sx={{
      maxWidth: "320px",
      minWidth: "320px",
      height: "97vh",
      borderRight: "1px solid #ccc",
      padding: 2,
      marginRight: "20px",
      borderRadius: "0px 25px 25px 0px",
      backgroundColor: "#1f295a",
      display: "flex",
      flexDirection: "column",
      justifyContent: "space-between",
    }}>
      <Box>
        <Box sx={{
          justifyContent: "center",
          alignItems: "center",
          padding: 3,
          cursor: "pointer",
        }} onClick={() => onNavigate("dashboard")}>
          <img src={logo1} alt="Logo" style={{ height: '150px', marginBottom: '20px', alignSelf: 'center' }} />
          <Box sx={{ padding: 1,
                  backgroundColor: "#6577C5",
                  borderRadius: "8px",
          }}>
          <Typography variant="h6" sx={{color: "#eeeeee", marginLeft: 2}}>
            {user ? `Welcome, Teacher ${user.firstName}!` : "Dashboard Coming Soon"}
          </Typography>
        </Box>
        </Box>

        {NAVIGATION.map((item) => (
          <Box
            key={item.segment}
            sx={{
              marginTop: 1,
              display: "flex",
              alignItems: "center",
              cursor: "pointer",
              padding: 2,
              borderRadius: "4px",
              backgroundColor: activeSegment === item.segment ? "#4259c1" : "transparent",
              color: activeSegment === item.segment ? "#eeeeee" : "#5163aa",
              "&:hover": { backgroundColor: "#6577C5", color: "#eeeeee" },
            }}
            onClick={() => onNavigate(item.segment)}
          >
            <Box sx={{ display: "flex", alignItems: "center", marginRight: 1, marginLeft: 4 }}>
              {item.icon}
            </Box>
            <Typography variant="body1" sx={{ marginLeft: 3, fontWeight: "bold" }}>
              {item.title}
            </Typography>
          </Box>
        ))}
      </Box>

      <Box
        sx={{
          marginTop: "auto",
          display: "flex",
          alignItems: "center",
          cursor: "pointer",
          padding: 2,
          borderRadius: "4px",
          color: "#5163aa",
          "&:hover": { backgroundColor: "#6577C5", color: "#eeeeee" },
        }}
        onClick={() => onNavigate("logout")}
      >
        <Box sx={{ display: "flex", alignItems: "center", marginRight: 1, marginLeft: 4 }}>
          <LogoutIcon />
        </Box>
        <Typography variant="body1" sx={{ marginLeft: 3, fontWeight: "bold" }}>
          Logout
        </Typography>
      </Box>
    </Box>
  );
}

function DashboardLayout() {
  const navigate = useNavigate();
  const [activeSegment, setActiveSegment] = useState(() => {
    return localStorage.getItem("activeSegment") || "dashboard";
  });
  const [isModalOpen, setModalOpen] = useState(false);
  const { user, setUser } = useUser();

  // Redirect to login if no user is present
  useEffect(() => {
    if (!user) {
      navigate("/");
    }
  }, [user, navigate]);

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem("activeSegment");
    navigate("/");
  };

  const handleNavigation = (segment) => {
    if (segment === "logout") {
      setModalOpen(true);
    } else {
      setActiveSegment(segment);
      localStorage.setItem("activeSegment", segment);
    }
  };

  const LogoutConfirmationModal = ({ open, onClose, onConfirm }) => {
    return (
      <Modal open={open} onClose={onClose}>
        <Box sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          bgcolor: "background.paper",
          boxShadow: 1,
          p: 1,
          borderRadius: 2,
          width: "400px",
        }}>
          <DialogTitle>Confirm Logout</DialogTitle>
          <Divider />
          <DialogContent>
            <Typography variant="body1">
              Are you sure you want to log out?
            </Typography>
          </DialogContent>
          <DialogActions sx={{ display: "flex", justifyContent: "flex-end" }}>
            <Button
              variant="outlined"
              onClick={onConfirm}
              sx={{
                borderColor: "red",
                color: "red",
                textTransform: "none",
                marginLeft: 2,
              }}
            >
              Logout
            </Button>
            <Button
              variant="text"
              onClick={onClose}
              sx={{ textTransform: "none" }}
            >
              Cancel
            </Button>
          </DialogActions>
        </Box>
      </Modal>
    );
  };

  return (
    <Box sx={{
      display: "flex",
      height: "100vh",
      backgroundColor: "#d6e1f7",
      overflow: "hidden",
      width: "100%",
    }}>
      <Sidebar onNavigate={handleNavigation} activeSegment={activeSegment} user={user} />
      <Box sx={{
        flexGrow: 1,
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
      }}>
        <Box sx={{
          display: "flex",
          alignItems: "center",
          padding: 2,
          justifyContent: "space-between",
          marginTop: 1,
          width: "95%",
        }}>
          <Typography variant="h4" sx={{
            marginBottom: "16px",
            margin: "0 10px",
            fontWeight: "bold",
            color: "#1f295a",
            minWidth: "220px",
          }}>
            {activeSegment.charAt(0).toUpperCase() + activeSegment.slice(1).replace("-", " ")}
          </Typography>

          <Box sx={{
            display: "flex",
            alignItems: "center",
            flexGrow: 1,
            justifyContent: "center",
          }}>
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Typography variant="body1" sx={{ marginRight: 2, marginLeft: 3, color: "#134B70" }}>
                {new Date().toLocaleDateString(undefined, {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                })}
              </Typography>
              <CalendarTodayIcon sx={{ marginRight: 15, color: "#134B70" }} />
            </Box>
          </Box>

          <Box sx={{ display: "flex", alignItems: "center", cursor: "pointer" }}>
            <UserIcon />
            <Typography variant="body1" sx={{ marginLeft: 1 }}>
              {user ? `${user.firstName} ${user.lastName}` : "User"}
            </Typography>
          </Box>
        </Box>

        <Box sx={{ flexGrow: 1, overflow: "auto" }}>
          <DemoPageContent activeSegment={activeSegment} user={user} />
        </Box>
      </Box>
      <LogoutConfirmationModal
        open={isModalOpen}
        onClose={() => setModalOpen(false)}
        onConfirm={handleLogout}
      />
    </Box>
  );
}

DashboardLayout.propTypes = {
  window: PropTypes.func,
};

export default DashboardLayout;