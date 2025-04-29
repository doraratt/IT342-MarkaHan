import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import CircularProgress from "@mui/material/CircularProgress";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import { useUser } from "../UserContext";

const OAuth2RedirectHandler = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { setUser } = useUser();

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        console.log("Fetching user info from /api/user/me after OAuth login...");
        
        // Add a small delay to ensure the session is properly established
        await new Promise(resolve => setTimeout(resolve, 500));
        
        const response = await fetch("http://localhost:8080/api/user/me", {
          method: "GET",
          credentials: "include", // Important for cookies (JSESSIONID)
          headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
          },
        });

        console.log("Response status:", response.status);
        
        if (response.ok) {
          const userData = await response.json();
          console.log("User data received:", userData);
          
          if (userData) {
            setUser(userData);
            localStorage.setItem("user", JSON.stringify(userData));
            navigate("/dashboard");
          } else {
            throw new Error("Empty user data received");
          }
        } else {
          let errorMessage;
          try {
            const errorData = await response.json();
            errorMessage = errorData.message || "Authentication failed";
          } catch (e) {
            errorMessage = await response.text() || `HTTP error: ${response.status}`;
          }
          
          console.error("Failed to fetch user info:", errorMessage);
          setError(errorMessage);
          setTimeout(() => navigate("/login"), 3000);
        }
      } catch (err) {
        console.error("Error during authentication:", err);
        setError(err.message || "An unexpected error occurred");
        setTimeout(() => navigate("/login"), 3000);
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, [navigate, setUser]);

  if (loading) {
    return (
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          height: "100vh",
        }}
      >
        <CircularProgress />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Completing authentication...
        </Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          height: "100vh",
        }}
      >
        <Typography variant="h6" color="error">
          Authentication Error: {error}
        </Typography>
        <Typography variant="body1">Redirecting to login page...</Typography>
      </Box>
    );
  }

  return null;
};

export default OAuth2RedirectHandler;