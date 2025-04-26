import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormControl from '@mui/material/FormControl';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

function Attendance() {
  const { user } = useUser();
  const [selectedSection, setSelectedSection] = useState('');
  const [selectedView, setSelectedView] = useState('mark');
  const [filterModalOpen, setFilterModalOpen] = useState(false);
  const [markAttendanceModal, setMarkAttendanceModal] = useState(false);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [attendanceStatus, setAttendanceStatus] = useState('present');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [filterData, setFilterData] = useState({
    firstName: '',
    lastName: '',
    section: ''
  });
  const [students, setStudents] = useState([]);
  const [originalStudents, setOriginalStudents] = useState([]);
  const [attendanceRecords, setAttendanceRecords] = useState([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  const fetchStudents = async () => {
    setIsLoading(true);
    try {
      const response = await axios.get(`http://localhost:8080/api/student/getStudentsByUser?userId=${user.userId}`);
      setStudents(response.data);
      setOriginalStudents(response.data);
      const uniqueSections = [...new Set(response.data.map(s => s.section))].sort();
      setSelectedSection(uniqueSections[0] || '');
      setError('');
    } catch (error) {
      setError('Error fetching students: ' + (error.response?.data || error.message));
      console.error('Fetch students error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchAttendance = async () => {
    setIsLoading(true);
    try {
      console.log('Fetching attendance for userId:', user.userId, 'month:', currentMonth + 1, 'year:', currentYear);
      const response = await axios.get(`http://localhost:8080/api/attendance/getAttendanceByUser?userId=${user.userId}`);
      console.log('Fetched attendance records:', response.data);
      const filteredRecords = response.data.filter(record => {
        const recordDate = new Date(record.date);
        return recordDate.getFullYear() === currentYear && recordDate.getMonth() === currentMonth;
      });
      console.log('Filtered attendance records:', filteredRecords);
      setAttendanceRecords(filteredRecords);
      setError('');
    } catch (error) {
      const errorMessage = error.response?.status === 500
        ? 'Server error: Unable to fetch attendance records. Please try again later or contact support.'
        : `Error fetching attendance: ${error.response?.data?.message || error.message}`;
      setError(errorMessage);
      console.error('Fetch attendance error:', error);
      console.error('Error details:', error.response?.data);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchStudents();
      fetchAttendance();
    }
  }, [user, currentMonth, currentYear]);

  const handleSaveAttendance = async () => {
    if (!user) {
      setError('User not logged in');
      return;
    }
    if (!selectedStudent) {
      setError('No student selected');
      return;
    }

    const attendanceDataToSend = {
      student: { studentId: selectedStudent.studentId },
      user: { userId: user.userId },
      date: selectedDate,
      status: attendanceStatus
    };

    try {
      const response = await axios.post('http://localhost:8080/api/attendance/postAttendance', attendanceDataToSend);
      setAttendanceRecords(prev => [
        ...prev.filter(r => r.date !== selectedDate || r.student.studentId !== selectedStudent.studentId),
        response.data
      ]);
      setMarkAttendanceModal(false);
      setSelectedStudent(null);
      setAttendanceStatus('present');
      setError('');
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.response?.data || error.message;
      setError('Error saving attendance: ' + errorMessage);
      console.error('Full error details:', error.response || error);
    }
  };

  const getStatusColor = (status) => {
    switch (status?.charAt(0).toUpperCase()) {
      case 'P':
        return { bgcolor: '#90EE90', color: '#000' };
      case 'L':
        return { bgcolor: '#FFD700', color: '#000' };
      case 'A':
        return { bgcolor: '#FFB6C1', color: '#000' };
      default:
        return { bgcolor: '#f5f5f5', color: '#666' };
    }
  };

  const sections = [...new Set(students.map(student => student.section))].sort();

  const getWeekdaysInMonth = (year, month) => {
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const weekdays = [];
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const dayOfWeek = date.getDay();
      if (dayOfWeek !== 0 && dayOfWeek !== 6) {
        weekdays.push(day);
      }
    }
    return weekdays;
  };

  const weekdays = getWeekdaysInMonth(currentYear, currentMonth);

  const renderMarkAttendanceView = () => (
    <Box sx={{ 
      backgroundColor: 'white',
      borderRadius: '8px',
      overflow: 'hidden',
      width: '100%',
    }}>
      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        backgroundColor: '#f0f0f0',
        padding: '12px 24px',
      }}>
        <Typography fontWeight="500">Students</Typography>
        <Typography fontWeight="500">Attendance</Typography>
      </Box>

      {students
        .filter(student => student.section === selectedSection)
        .map((student, index) => (
          <Box
            key={student.studentId}
            sx={{
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              padding: '16px 24px',
              backgroundColor: index % 2 === 0 ? '#f0f0f0' : 'white',
              '&:hover': {
                backgroundColor: '#f5f5f5',
              },
            }}
          >
            <Typography>{`${student.firstName} ${student.lastName}`}</Typography>
            <Button
              onClick={() => {
                setSelectedStudent(student);
                setMarkAttendanceModal(true);
              }}
              sx={{
                color: '#0D5CAB',
                textTransform: 'none',
                justifyContent: 'flex-start',
                '&:hover': {
                  backgroundColor: 'transparent',
                  textDecoration: 'underline',
                },
              }}
            >
              Mark Attendance
            </Button>
          </Box>
        ))}
    </Box>
  );

  const renderAttendanceTableView = () => {
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    const filteredStudents = students.filter(student => student.section === selectedSection);

    return (
      <Box sx={{ 
        backgroundColor: 'white',
        borderRadius: '8px',
        overflow: 'hidden',
        width: '100%',
        boxShadow: '0 1px 3px rgba(0,0,0,0.12)'
      }}>
        <Box sx={{ 
          display: 'flex',
          bgcolor: '#f0f0f0',
          borderBottom: '1px solid #ddd',
          p: 2
        }}>
          <Box sx={{ width: '200px', flexShrink: 0 }}>
            <Typography fontWeight="500">Student</Typography>
          </Box>
          <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
            <IconButton 
              size="small"
              onClick={() => {
                if (currentMonth === 0) {
                  setCurrentMonth(11);
                  setCurrentYear(prev => prev - 1);
                } else {
                  setCurrentMonth(prev => prev - 1);
                }
              }}
            >
              <ChevronLeftIcon />
            </IconButton>
            <Typography fontWeight="500">
              Month: {monthNames[currentMonth]} {currentYear}
            </Typography>
            <IconButton 
              size="small"
              onClick={() => {
                if (currentMonth === 11) {
                  setCurrentMonth(0);
                  setCurrentYear(prev => prev + 1);
                } else {
                  setCurrentMonth(prev => prev + 1);
                }
              }}
            >
              <ChevronRightIcon />
            </IconButton>
          </Box>
        </Box>

        <Box sx={{ 
          display: 'flex',
          bgcolor: '#f5f5f5',
          borderBottom: '1px solid #ddd'
        }}>
          <Box sx={{ width: '200px', p: 1, flexShrink: 0 }} />
          {weekdays.map(day => (
            <Box 
              key={day}
              sx={{ 
                width: '40px',
                textAlign: 'center',
                p: 1,
                borderRight: '1px solid #ddd'
              }}
            >
              <Typography variant="caption">{day}</Typography>
            </Box>
          ))}
        </Box>

        <Box sx={{ overflowX: 'auto' }}>
          {filteredStudents.map((student, index) => (
            <Box 
              key={student.studentId}
              sx={{ 
                display: 'flex',
                borderBottom: '1px solid #ddd',
                bgcolor: index % 2 === 0 ? '#fafafa' : 'white'
              }}
            >
              <Box sx={{ 
                width: '200px',
                p: 1,
                flexShrink: 0,
                display: 'flex',
                alignItems: 'center'
              }}>
                <Typography variant="body2">{`${student.firstName} ${student.lastName}`}</Typography>
              </Box>
              {weekdays.map(day => {
                const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                const record = attendanceRecords.find(r => {
                  if (!r.student) {
                    console.warn('Attendance record missing student field:', r);
                    return false;
                  }
                  return r.student.studentId === student.studentId && r.date === dateStr;
                });
                const status = record 
                  ? (record.status.charAt(0).toUpperCase() === 'P' ? 'P' : record.status.charAt(0).toUpperCase())
                  : '--';

                return (
                  <Box
                    key={dateStr}
                    sx={{
                      width: '40px',
                      p: 1,
                      textAlign: 'center',
                      borderRight: '1px solid #ddd',
                      ...getStatusColor(status)
                    }}
                  >
                    <Typography variant="body2">{status}</Typography>
                  </Box>
                );
              })}
            </Box>
          ))}
        </Box>

        <Box sx={{ 
          p: 2,
          borderTop: '1px solid #ddd',
          display: 'flex',
          gap: 2,
          bgcolor: '#f9f9f9'
        }}>
          <Typography variant="caption" color="text.secondary">Legend:</Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Box sx={{ ...getStatusColor('P'), px: 1, borderRadius: 1 }}>P</Box>
            <Box sx={{ ...getStatusColor('L'), px: 1, borderRadius: 1 }}>L</Box>
            <Box sx={{ ...getStatusColor('A'), px: 1, borderRadius: 1 }}>A</Box>
          </Box>
        </Box>
      </Box>
    );
  };

  return (
    <Box sx={{ width: '100%', p: 3 }}>
      {isLoading && <Typography>Loading...</Typography>}
      {error && (
        <Box sx={{ mb: 2 }}>
          <Typography color="error">{error}</Typography>
          <Button onClick={() => { fetchStudents(); fetchAttendance(); }}>Retry</Button>
        </Box>
      )}
      
      <Box sx={{ mb: 4 }}>
        <Typography sx={{ mb: 2 }}>Sections</Typography>
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <Box sx={{ display: 'flex', gap: '2px' }}>
            {sections.map((section) => (
              <Button
                key={section}
                onClick={() => setSelectedSection(section)}
                sx={{
                  minWidth: '80px',
                  backgroundColor: selectedSection === section ? '#0D5CAB' : '#fff',
                  color: selectedSection === section ? '#fff' : '#000',
                  borderRadius: 0,
                  px: 3,
                  '&:hover': {
                    backgroundColor: selectedSection === section ? '#0D5CAB' : '#f5f5f5',
                  },
                  '&:first-of-type': {
                    borderTopLeftRadius: '4px',
                    borderBottomLeftRadius: '4px',
                  },
                  '&:last-child': {
                    borderTopRightRadius: '4px',
                    borderBottomRightRadius: '4px',
                  },
                }}
              >
                {section}
              </Button>
            ))}
          </Box>

          <Box sx={{ display: 'flex', gap: 2 }}>
            <Box sx={{ display: 'flex', gap: '2px' }}>
              <Button
                onClick={() => setSelectedView('mark')}
                sx={{
                  minWidth: '140px',
                  backgroundColor: selectedView === 'mark' ? '#0D5CAB' : '#fff',
                  color: selectedView === 'mark' ? '#fff' : '#000',
                  borderRadius: 0,
                  px: 3,
                  '&:hover': {
                    backgroundColor: selectedView === 'mark' ? '#0D5CAB' : '#f5f5f5',
                  },
                  borderTopLeftRadius: '4px',
                  borderBottomLeftRadius: '4px',
                }}
              >
                Mark Attendance
              </Button>
              <Button
                onClick={() => setSelectedView('table')}
                sx={{
                  minWidth: '140px',
                  backgroundColor: selectedView === 'table' ? '#0D5CAB' : '#fff',
                  color: selectedView === 'table' ? '#fff' : '#000',
                  borderRadius: 0,
                  px: 3,
                  '&:hover': {
                    backgroundColor: selectedView === 'table' ? '#0D5CAB' : '#f5f5f5',
                  },
                  borderTopRightRadius: '4px',
                  borderBottomRightRadius: '4px',
                }}
              >
                Attendance Table
              </Button>
            </Box>

            <Button
              onClick={() => setFilterModalOpen(true)}
              sx={{
                minWidth: '140px',
                backgroundColor: '#fff',
                color: '#000',
                borderRadius: '4px',
                px: 3,
                '&:hover': {
                  backgroundColor: '#f5f5f5',
                },
              }}
            >
              Filter Attendance
            </Button>
          </Box>
        </Box>
      </Box>

      {selectedView === 'mark' ? renderMarkAttendanceView() : renderAttendanceTableView()}

      <Modal
        open={filterModalOpen}
        onClose={() => setFilterModalOpen(false)}
        aria-labelledby="filter-modal-title"
      >
        <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 400,
          bgcolor: 'background.paper',
          borderRadius: '8px',
          p: 3,
        }}>
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            mb: 3
          }}>
            <Typography variant="h6" component="h2">
              Filter Attendance
            </Typography>
            <IconButton 
              onClick={() => setFilterModalOpen(false)}
              size="small"
              sx={{ color: 'text.secondary' }}
            >
              <CloseIcon />
            </IconButton>
          </Box>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="First Name"
              fullWidth
              value={filterData.firstName}
              onChange={(e) => setFilterData({ ...filterData, firstName: e.target.value })}
            />
            <TextField
              label="Last Name"
              fullWidth
              value={filterData.lastName}
              onChange={(e) => setFilterData({ ...filterData, lastName: e.target.value })}
            />
            <TextField
              label="Section"
              fullWidth
              value={filterData.section}
              onChange={(e) => setFilterData({ ...filterData, section: e.target.value })}
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button
                onClick={() => {
                  setFilterModalOpen(false);
                  setStudents(originalStudents.filter(student =>
                    student.firstName.toLowerCase().includes(filterData.firstName.toLowerCase()) &&
                    student.lastName.toLowerCase().includes(filterData.lastName.toLowerCase()) &&
                    (filterData.section ? student.section === filterData.section : true)
                  ));
                }}
                sx={{
                  mt: 1,
                  backgroundColor: '#0B5394',
                  color: '#fff',
                  '&:hover': {
                    backgroundColor: '#0B5394',
                  },
                  textTransform: 'none',
                  flex: 1
                }}
              >
                Apply Filter
              </Button>
              <Button
                onClick={() => {
                  setFilterModalOpen(false);
                  setFilterData({ firstName: '', lastName: '', section: '' });
                  setStudents(originalStudents);
                }}
                sx={{
                  mt: 1,
                  backgroundColor: '#fff',
                  color: '#000',
                  border: '1px solid #0B5394',
                  '&:hover': {
                    backgroundColor: '#f5f5f5',
                  },
                  textTransform: 'none',
                  flex: 1
                }}
              >
                Reset Filter
              </Button>
            </Box>
          </Box>
        </Box>
      </Modal>

      <Modal
        open={markAttendanceModal}
        onClose={() => setMarkAttendanceModal(false)}
        aria-labelledby="mark-attendance-modal-title"
      >
        <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 400,
          bgcolor: 'background.paper',
          borderRadius: '8px',
          p: 3,
        }}>
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            mb: 3
          }}>
            <Typography variant="h6" component="h2">
              Mark Attendance
            </Typography>
            <IconButton 
              onClick={() => setMarkAttendanceModal(false)}
              size="small"
              sx={{ color: 'text.secondary' }}
            >
              <CloseIcon />
            </IconButton>
          </Box>

          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 500, mb: 2 }}>
              Student: {selectedStudent ? `${selectedStudent.firstName} ${selectedStudent.lastName}` : ''}
            </Typography>
            
            <TextField
              type="date"
              label="Date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              fullWidth
              sx={{ mb: 2 }}
              InputLabelProps={{
                shrink: true,
              }}
            />
          </Box>

          <FormControl>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              Attendance Status
            </Typography>
            <RadioGroup
              value={attendanceStatus}
              onChange={(e) => setAttendanceStatus(e.target.value)}
            >
              <FormControlLabel 
                value="present" 
                control={<Radio />} 
                label={
                  <Typography sx={{ color: 'success.main', fontWeight: 500 }}>
                    Present
                  </Typography>
                }
              />
              <FormControlLabel 
                value="late" 
                control={<Radio />} 
                label={
                  <Typography sx={{ color: 'warning.main', fontWeight: 500 }}>
                    Late
                  </Typography>
                }
              />
              <FormControlLabel 
                value="absent" 
                control={<Radio />} 
                label={
                  <Typography sx={{ color: 'error.main', fontWeight: 500 }}>
                    Absent
                  </Typography>
                }
              />
            </RadioGroup>
          </FormControl>

          <Button
            onClick={handleSaveAttendance}
            fullWidth
            sx={{
              mt: 3,
              backgroundColor: '#0B5394',
              color: '#fff',
              '&:hover': {
                backgroundColor: '#0B5394',
              },
              textTransform: 'none',
            }}
          >
            Save
          </Button>
        </Box>
      </Modal>
    </Box>
  );
}

export default Attendance;