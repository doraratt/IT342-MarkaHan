import React, { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

export default function NumberOfStudents() {
  const { user } = useUser();
  const [maleCount, setMaleCount] = useState(0);
  const [femaleCount, setFemaleCount] = useState(0);
  const [sections, setSections] = useState([]);
  const [selectedSectionIndex, setSelectedSectionIndex] = useState(0);
  const [students, setStudents] = useState([]);

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    if (user) {
      const fetchStudents = async () => {
        try {
          const response = await axios.get(`http://localhost:8080/api/student/getStudentsByUser?userId=${user.userId}`);
          const fetchedStudents = response.data;
          setStudents(fetchedStudents);
          const uniqueSections = [...new Set(fetchedStudents.map(s => s.section))].sort();
          setSections(uniqueSections);
          if (uniqueSections.length > 0) {
            updateCounts(fetchedStudents, uniqueSections[0]);
          }
        } catch (error) {
          console.error('Error fetching students:', error.response?.data || error.message);
        }
      };
      fetchStudents();
    }
  }, [user]);

  const updateCounts = (studentsData, section) => {
    const filteredStudents = studentsData.filter(student => student.section === section);
    const males = filteredStudents.filter(student => student.gender === 'Male').length;
    const females = filteredStudents.filter(student => student.gender === 'Female').length;
    setMaleCount(males);
    setFemaleCount(females);
  };

  const handlePreviousSection = () => {
    const prevIndex = (selectedSectionIndex - 1 + sections.length) % sections.length;
    setSelectedSectionIndex(prevIndex);
    updateCounts(students, sections[prevIndex]);
  };

  const handleNextSection = () => {
    const nextIndex = (selectedSectionIndex + 1) % sections.length;
    setSelectedSectionIndex(nextIndex);
    updateCounts(students, sections[nextIndex]);
  };

  return (
    <Box sx={{ 
      width: '100%',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      padding: 2,
    }}>
      {sections.length > 0 ? (
        <>
          <Box sx={{ 
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            width: '100%',
            mb: 2,
          }}>
            <IconButton 
              onClick={handlePreviousSection}
              disabled={sections.length <= 1}
              sx={{ 
                color: '#1f295a',
                mr: 1,
              }}
            >
              <ChevronLeftIcon />
            </IconButton>
            <Typography 
              variant="h6" 
              sx={{ 
                color: '#1f295a',
                fontWeight: 'bold',
                mx: 1,
              }}
            >
              Section: {sections[selectedSectionIndex]}
            </Typography>
            <IconButton 
              onClick={handleNextSection}
              disabled={sections.length <= 1}
              sx={{ 
                color: '#1f295a',
                ml: 1,
              }}
            >
              <ChevronRightIcon />
            </IconButton>
          </Box>
          <Box sx={{ 
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'flex-start',
            width: '100%',
          }}>
            <Typography 
              variant="body1" 
              sx={{ 
                color: '#1f295a', // Blue for Male
                fontWeight: 'bold', 
                mb: 0.5 
              }}
            >
              Male: {maleCount}
            </Typography>
            <Typography 
              variant="body1" 
              sx={{ 
                color: '#d32f2f', // Red for Female
                fontWeight: 'bold', 
                mb: 0.5 
              }}
            >
              Female: {femaleCount}
            </Typography>
            <Typography 
              variant="body1" 
              sx={{ 
                color: '#1f295a', // Keep Total as the app's primary color
                fontWeight: 'bold' 
              }}
            >
              Total: {maleCount + femaleCount}
            </Typography>
          </Box>
        </>
      ) : (
        <Typography variant="body1" sx={{ color: '#1f295a', fontWeight: 'bold' }}>
          No sections available
        </Typography>
      )}
    </Box>
  );
}