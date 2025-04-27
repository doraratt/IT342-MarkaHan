import { Box, Button, Grid } from "@mui/material";
import Typography from "@mui/material/Typography";
import React from "react";
import { Link as RouterLink } from "react-router-dom";
import { Element, Link, scroller } from "react-scroll";
import Slider from "react-slick";
import "slick-carousel/slick/slick-theme.css";
import "slick-carousel/slick/slick.css";
import logo from "../assets/logo2.png";

//gifs
import dashboard from "../assets/Dashboard.png";

//imgs
import calendar from "../assets/LandingCalendar.png";
import journal from "../assets/LandingJournal.png";
import attendance from "../assets/LandingAttendance.png";
import studentlist from "../assets/LandingStudentList.png";
import grade from "../assets/LandingGrades.png"

import ArrowDropDownCircleIcon from "@mui/icons-material/ArrowDropDownCircle";
import KeyboardDoubleArrowUpIcon from "@mui/icons-material/KeyboardDoubleArrowUp";

const carouselItems = [
  {
    title: "Attendance",
    description: 
      "Track and manage student attendance with ease.",
    image: attendance,
  },
  {
    title: "Grades",
    description:
      "View and manage student grades efficiently.",
    image: grade,
  },
  {
    title: "Students List",
    description:
      "Access and organize your list of students effortlessly.",
    image: studentlist,
  },
  {
    title: "Calendar",
    description:
      "Plan your schedule and never miss a deadline with our task calendar.",
    image: calendar,
  },
  {
    title: "Journal",
    description:
      "Write and organize your thoughts and notes like a digital diary.",
    image: journal,
  },
];

export default function Landing() {
  const settings = {
    dots: true,
    infinite: true,
    speed: 800,
    slidesToShow: 1,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
  };

  const scrollToNextSection = (sectionName) => {
    scroller.scrollTo(sectionName, {
      duration: 800,
      delay: 0,
      smooth: "easeInOutQuart",
    });
  };

  return (
    <>
      <Element name="section1">
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            minHeight: "100vh",
            alignItems: "center",
            backgroundImage: "linear-gradient(45deg, #4259c1, #1f295a)",
            position: "relative",
            px: { xs: 2, md: 4 },
          }}
        >
          <Grid container spacing={4} alignItems="center" justifyContent="center">
            <Grid item xs={12} md={6}>
              <Box
                sx={{
                  textAlign: "center",
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  justifyContent: "center",
                  height: "100%",
                  px: 2,
                }}
              >
                <img
                  src={logo}
                  alt="Logo"
                  style={{ width: "500px", height: "300px" }}
                />
                <Typography
                  variant="h5"
                  component="p"
                  sx={{ mt: 2, color: "#eeeeee" }}
                >
                  Start your Class Management journey with MarkaHan.
                </Typography>
                <Typography
                  variant="h5"
                  component="p"
                  sx={{ mb: 4, color: "#eeeeee" }}
                >
                  Login or Sign up now!
                </Typography>
                <Box
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    gap: 2,
                    width: { xs: "100%", sm: "50%" },
                  }}
                >
                  <Button
                    component={RouterLink}
                    to="/login"
                    variant="contained"
                    sx={{
                      backgroundColor: "#4259c1",
                      color: "#eeeeee",
                    }}
                    size="large"
                    fullWidth
                  >
                    Login
                  </Button>
                  <Button
                    component={RouterLink}
                    to="/signup"
                    variant="outlined"
                    sx={{
                      borderColor: "#eeeeee",
                      color: "#eeeeee",
                    }}
                    size="large"
                    fullWidth
                  >
                    Sign Up
                  </Button>
                </Box>
              </Box>
            </Grid>
            <Grid item xs={12} md={6}>
              <Slider
                {...settings}
                style={{
                  width: "100%",
                  maxWidth: "500px",
                  borderRadius: "8px",
                  mx: "auto",
                }}
              >
                {carouselItems.map((item, index) => (
                  <Box key={index} sx={{ padding: 2, textAlign: "center" }}>
                    <img
                      src={item.image}
                      alt={item.title}
                      style={{
                        width: "100%",
                        maxWidth: "300px",
                        borderRadius: "10px",
                        margin: "0 auto",
                      }}
                    />
                    <Typography
                      variant="h4"
                      component="h2"
                      sx={{ mt: 2, color: "#eeeeee" }}
                    >
                      {item.title}
                    </Typography>
                    <Typography
                      variant="body1"
                      sx={{ mt: 1, color: "#eeeeee" }}
                    >
                      {item.description}
                    </Typography>
                  </Box>
                ))}
              </Slider>
            </Grid>
          </Grid>
          <Box
            sx={{
              position: "absolute",
              zIndex: 2,
              bottom: 20,
              left: "50%",
              transform: "translateX(-50%)",
              cursor: "pointer",
              animation: "bounce 2s infinite",
              "@keyframes bounce": {
                "0%, 20%, 50%, 80%, 100%": {
                  transform: "translateY(0) translateX(-50%)",
                },
                "40%": {
                  transform: "translateY(-10px) translateX(-50%)",
                },
                "60%": {
                  transform: "translateY(-10px) translateX(-50%)",
                },
              },
            }}
            onClick={() => scrollToNextSection("section2")}
          >
            <Typography
              sx={{
                color: "#eeeeee",
                fontSize: "2rem",
                userSelect: "none",
              }}
            >
              <ArrowDropDownCircleIcon
                sx={{
                  opacity: 0.5,
                  transition: "opacity 0.3s ease",
                  "&:hover": {
                    opacity: 1,
                  },
                }}
              />
            </Typography>
          </Box>
        </Box>
      </Element>

      <Element name="section2">
        <Box
          sx={{
            minHeight: "100vh",
            height: "100%",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            background: "#D4EBF8",
            position: "relative",
            transition: "all 0.5s ease-in-out",
          }}
        >
          <Typography
            variant="h2"
            sx={{
              color: "#1c329b",
              mb: 4,
              textAlign: "center",
            }}
          >
            All Your Class Management Tools in One Place
          </Typography>
          <Typography
            variant="h5"
            sx={{
              color: "#1c329b",
              mb: 6,
              textAlign: "center",
            }}
          >
            Access everything you need from our intuitive dashboard. Manage your
            Class Attendance, Student Grades, Students List, and more.
          </Typography>
          <Box xs={12} md={8}>
            <img
              src={dashboard}
              alt="dashboard"
              style={{
                width: "100%",
                maxWidth: "800px",
                height: "auto",
                borderRadius: 16,
              }}
            />
          </Box>
          <Box
            sx={{
              position: "absolute",
              zIndex: 2,
              bottom: 20,
              left: "50%",
              transform: "translateX(-50%)",
              cursor: "pointer",
              animation: "bounce 2s infinite",
              "@keyframes bounce": {
                "0%, 20%, 50%, 80%, 100%": {
                  transform: "translateY(0) translateX(-50%)",
                },
                "40%": {
                  transform: "translateY(-10px) translateX(-50%)",
                },
                "60%": {
                  transform: "translateY(-10px) translateX(-50%)",
                },
              },
            }}
            onClick={() => scrollToNextSection("section1")}
          >
            <Typography
              sx={{
                color: "#334596",
                background: "white",
                borderRadius: "50%",
                userSelect: "none",
                padding: "2px 5px 0px 5px",
              }}
            >
              <KeyboardDoubleArrowUpIcon
                sx={{
                  opacity: 0.5,
                  transition: "opacity 0.3s ease",
                  "&:hover": {
                    opacity: 1,
                  },
                }}
              />
            </Typography>
          </Box>
        </Box>
      </Element>
    </>
  );
}