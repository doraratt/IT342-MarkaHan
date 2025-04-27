import { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Legend, Tooltip } from 'recharts';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

function AttendanceBoardView() {
  const { user } = useUser();
  const [attendanceData, setAttendanceData] = useState([
    { name: 'Present', value: 0, color: '#00FF00' },
    { name: 'Late', value: 0, color: '#FFFF00' },
    { name: 'Absent', value: 0, color: '#FF0000' },
  ]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sections, setSections] = useState([]);
  const [selectedSectionIndex, setSelectedSectionIndex] = useState(0);
  const [students, setStudents] = useState([]);
  const [allAttendanceRecords, setAllAttendanceRecords] = useState([]);

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  // Fetch students to get sections
  const fetchStudents = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/student/getStudentsByUser?userId=${user.userId}`);
      const fetchedStudents = response.data;
      setStudents(fetchedStudents);
      const uniqueSections = [...new Set(fetchedStudents.map(s => s.section))].sort();
      setSections(uniqueSections);
    } catch (error) {
      console.error('Error fetching students:', error.response?.data || error.message);
    }
  };

  // Fetch all attendance records
  const fetchTodayAttendance = async () => {
    setIsLoading(true);
    try {
      const today = new Date().toISOString().split('T')[0];
      const response = await axios.get(
        `http://localhost:8080/api/attendance/getAttendanceByUser?userId=${user.userId}`
      );

      const todayRecords = response.data.filter(record => record.date === today);
      setAllAttendanceRecords(todayRecords);

      // Initial counts for the first section (or all if no sections)
      updateAttendanceData(todayRecords, sections.length > 0 ? sections[0] : null);
      setError('');
    } catch (error) {
      const errorMessage = error.response?.status === 500
        ? 'Server error: Unable to fetch attendance records.'
        : `Error fetching attendance: ${error.response?.data?.message || error.message}`;
      setError(errorMessage);
      console.error('Fetch attendance error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // Update attendance data based on selected section
  const updateAttendanceData = (records, section) => {
    let filteredRecords = records;
    if (section) {
      const sectionStudentIds = students
        .filter(student => student.section === section)
        .map(student => student.studentId);
      filteredRecords = records.filter(record => sectionStudentIds.includes(record.student.studentId));
    }

    const counts = {
      Present: 0,
      Late: 0,
      Absent: 0,
    };

    filteredRecords.forEach(record => {
      const status = record.status.charAt(0).toUpperCase() + record.status.slice(1).toLowerCase();
      if (status in counts) {
        counts[status]++;
      }
    });

    const updatedData = [
      { name: 'Present', value: counts.Present || 1, color: '#69ea60' },
      { name: 'Late', value: counts.Late || 1, color: '#f2d151' },
      { name: 'Absent', value: counts.Absent || 1, color: '#f16135' },
    ];

    setAttendanceData(updatedData);
  };

  const handlePreviousSection = () => {
    const prevIndex = (selectedSectionIndex - 1 + sections.length) % sections.length;
    setSelectedSectionIndex(prevIndex);
    updateAttendanceData(allAttendanceRecords, sections[prevIndex]);
  };

  const handleNextSection = () => {
    const nextIndex = (selectedSectionIndex + 1) % sections.length;
    setSelectedSectionIndex(nextIndex);
    updateAttendanceData(allAttendanceRecords, sections[nextIndex]);
  };

  useEffect(() => {
    if (user) {
      fetchStudents();
      fetchTodayAttendance();
    }
  }, [user]);

  useEffect(() => {
    if (sections.length > 0 && allAttendanceRecords.length > 0) {
      updateAttendanceData(allAttendanceRecords, sections[selectedSectionIndex]);
    }
  }, [sections, allAttendanceRecords]);

  const totalRecords = attendanceData.reduce((sum, entry) => sum + entry.value, 0);
  const isPlaceholderData = totalRecords === 3 && attendanceData.every(entry => entry.value === 1);

  return (
    <Box
      sx={{
        width: '100%',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
      }}
    >
      {sections.length > 0 && (
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            width: '100%',
            mb: 1,
          }}
        >
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
              fontSize: '14px',
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
      )}
      {isLoading && (
        <Typography variant="caption" color="#1f295a">
          Loading...
        </Typography>
      )}
      {error && (
        <Typography variant="caption" color="error" sx={{ mb: 1 }}>
          {error}
        </Typography>
      )}
      {!isLoading && !error && (
        <Box style={{ display: 'flex', alignItems: 'center' }}>
          <PieChart width={180} height={160}>
            <Pie
              data={attendanceData}
              cx="50%"
              cy="45%"
              outerRadius={55}
              dataKey="value"
              label={false}
            >
              {attendanceData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={isPlaceholderData ? '#e0e0e0' : entry.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                backgroundColor: '#fff',
                border: '1px solid #e0e0e0',
                borderRadius: '4px',
                fontSize: '14px',
                fontWeight: 500,
                color: '#1f295a',
              }}
            />
            <Legend
              layout="horizontal"
              align="center"
              verticalAlign="bottom"
              iconSize={10}
              formatter={(value) => (
                <Typography
                  component="span"
                  variant="body2"
                  sx={{
                    color: '#1f295a',
                    fontSize: '12px',
                    fontWeight: 'bold',
                  }}
                >
                  {value}
                </Typography>
              )}
            />
          </PieChart>
        </Box>
      )}
    </Box>
  );
}

export default AttendanceBoardView;