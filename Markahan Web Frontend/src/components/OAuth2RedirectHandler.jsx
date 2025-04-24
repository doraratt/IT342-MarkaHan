import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import CircularProgress from "@mui/material/CircularProgress"
import Box from "@mui/material/Box"
import Typography from "@mui/material/Typography"
import { useUser } from "../UserContext"

const OAuth2RedirectHandler = () => {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const navigate = useNavigate()
  const { setUser } = useUser()

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        console.log("Fetching user info after OAuth login...")
        const response = await fetch("http://localhost:8080/api/user/me", {
          credentials: "include", // Important for cookies
          headers: {
            Accept: "application/json",
          },
        })

        if (response.ok) {
          const userData = await response.json()
          console.log("User data received:", userData)

          // Store user data in context
          setUser(userData)

          // Also store in localStorage as backup
          localStorage.setItem("user", JSON.stringify(userData))

          // Redirect to dashboard
          navigate("/dashboard")
        } else {
          console.error("Failed to fetch user info:", await response.text())
          setError("Failed to fetch user information. Please try again.")
          setTimeout(() => navigate("/login"), 3000)
        }
      } catch (err) {
        console.error("Error during authentication:", err)
        setError("An error occurred during authentication. Please try again.")
        setTimeout(() => navigate("/login"), 3000)
      } finally {
        setLoading(false)
      }
    }

    fetchUserInfo()
  }, [navigate, setUser])

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
    )
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
          {error}
        </Typography>
        <Typography variant="body1">Redirecting to login page...</Typography>
      </Box>
    )
  }

  return null
}

export default OAuth2RedirectHandler