import { useState } from 'react';
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

function Attendance() {
  const [selectedSection, setSelectedSection] = useState('G1');
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

  const students = [
    { id: 1, name: 'Gaylord Tuwid' },
    { id: 2, name: 'Kally Vhangon' },
    { id: 3, name: 'Bugart Batongbakal Jr.' },
    { id: 4, name: 'Wy Lee Guo' },
    { id: 5, name: 'Raoul Philipi' },
    { id: 6, name: 'Balmond Alucard' },
    { id: 7, name: 'Bessie Cooper' },
  ];

  // Sample attendance data structure for future reference (commented out)
  /*
  const attendanceData = {
    'Gaylord Tuwid': ['P', 'P', 'P', 'L', 'P', 'A', 'P', 'P', 'A', 'L', 'P', 'P'],
    'Kally Vhangon': ['P', 'P', 'P', 'P', 'P', 'A', 'P', 'P', 'A', 'P', 'P', 'P'],
    // ... other students
  };
  */

  // Empty attendance data for initial state
  const emptyAttendanceData = {};
  students.forEach(student => {
    emptyAttendanceData[student.name] = Array(12).fill('--');
  });

  // Keep the status color function for future reference
  const getStatusColor = (status) => {
    switch (status) {
      case 'P':
        return { bgcolor: '#90EE90', color: '#000' }; // Light green
      case 'L':
        return { bgcolor: '#FFD700', color: '#000' }; // Gold
      case 'A':
        return { bgcolor: '#FFB6C1', color: '#000' }; // Light red
      default:
        return { bgcolor: '#f5f5f5', color: '#666' }; // Gray for empty state
    }
  };

  const sections = ['G1', 'G2', 'G3', 'G4', 'G5'];
  const days = [1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 15, 16];

  // Render the mark attendance view
  const renderMarkAttendanceView = () => (
    <Box sx={{ 
      backgroundColor: 'white',
      borderRadius: '8px',
      overflow: 'hidden',
      width: '100%',
    }}>
      {/* Table Header */}
      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        backgroundColor: '#f0f0f0',
        padding: '12px 24px',
      }}>
        <Typography fontWeight="500">Students</Typography>
        <Typography fontWeight="500">Attendance</Typography>
      </Box>

      {/* Students List */}
      {students.map((student, index) => (
        <Box
          key={student.id}
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
          <Typography>{student.name}</Typography>
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

  // Modify the renderAttendanceTableView function
  const renderAttendanceTableView = () => (
    <Box sx={{ 
      backgroundColor: 'white',
      borderRadius: '8px',
      overflow: 'hidden',
      width: '100%',
      boxShadow: '0 1px 3px rgba(0,0,0,0.12)'
    }}>
      {/* Header Row */}
      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: '250px repeat(12, 1fr)',
        bgcolor: '#f0f0f0',
        borderBottom: '1px solid #ddd',
      }}>
        <Box sx={{ p: 2, borderRight: '1px solid #ddd' }}>
          <Typography fontWeight="500">Students</Typography>
        </Box>
        <Box sx={{ 
          gridColumn: '2 / -1',
          p: 2,
          display: 'flex',
          alignItems: 'center',
          gap: 1
        }}>
          <IconButton size="small">
            <ChevronLeftIcon />
          </IconButton>
          <Typography fontWeight="500">Month: June</Typography>
          <IconButton size="small">
            <ChevronRightIcon />
          </IconButton>
        </Box>
      </Box>

      {/* Days Row */}
      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: '250px repeat(12, 1fr)',
        bgcolor: '#f0f0f0',
        borderBottom: '1px solid #ddd',
      }}>
        <Box sx={{ p: 2, borderRight: '1px solid #ddd' }}></Box>
        {days.map((day) => (
          <Box key={day} sx={{ 
            p: 2, 
            textAlign: 'center',
            borderRight: '1px solid #ddd'
          }}>
            <Typography>{day}</Typography>
          </Box>
        ))}
      </Box>

      {/* Student Rows */}
      {students.map((student) => (
        <Box key={student.id} sx={{ 
          display: 'grid',
          gridTemplateColumns: '250px repeat(12, 1fr)',
          borderBottom: '1px solid #ddd',
          '&:nth-of-type(odd)': {
            bgcolor: '#f9f9f9'
          }
        }}>
          <Box sx={{ 
            p: 2, 
            borderRight: '1px solid #ddd',
            display: 'flex',
            alignItems: 'center'
          }}>
            <Typography>{student.name}</Typography>
          </Box>
          {emptyAttendanceData[student.name].map((status, dayIndex) => (
            <Box 
              key={dayIndex}
              sx={{ 
                p: 2,
                textAlign: 'center',
                borderRight: '1px solid #ddd',
                ...getStatusColor(status),
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <Typography sx={{ 
                color: '#666',
                fontSize: '14px'
              }}>
                --
              </Typography>
            </Box>
          ))}
        </Box>
      ))}

      {/* Legend for future reference */}
      <Box sx={{ 
        p: 2, 
        borderTop: '1px solid #ddd',
        display: 'flex',
        gap: 3,
        bgcolor: '#f9f9f9'
      }}>
        <Typography sx={{ fontSize: '14px', color: '#666' }}>
          Legend (for future implementation):
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Box sx={{ 
            bgcolor: '#90EE90', 
            px: 1, 
            borderRadius: 1,
            fontSize: '14px'
          }}>P - Present</Box>
          <Box sx={{ 
            bgcolor: '#FFD700', 
            px: 1, 
            borderRadius: 1,
            fontSize: '14px'
          }}>L - Late</Box>
          <Box sx={{ 
            bgcolor: '#FFB6C1', 
            px: 1, 
            borderRadius: 1,
            fontSize: '14px'
          }}>A - Absent</Box>
        </Box>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ width: '100%', p: 3 }}>
      {/* Header with Sections and View Buttons */}
      <Box sx={{ mb: 4 }}>
        <Typography sx={{ mb: 2 }}>Sections</Typography>
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          {/* Sections Buttons */}
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

          {/* View Selection and Filter Buttons */}
          <Box sx={{ display: 'flex', gap: 2 }}>
            {/* View Selection Buttons */}
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

            {/* Filter Button - Separated */}
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

      {/* Render the appropriate view */}
      {selectedView === 'mark' ? renderMarkAttendanceView() : renderAttendanceTableView()}

      {/* Filter Modal */}
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
            <Button
              onClick={() => {
                setFilterModalOpen(false);
              }}
              sx={{
                mt: 1,
                backgroundColor: '#0B5394',
                color: '#fff',
                '&:hover': {
                  backgroundColor: '#0B5394',
                },
                textTransform: 'none',
              }}
            >
              Apply Filter
            </Button>
          </Box>
        </Box>
      </Modal>

      {/* Mark Attendance Modal */}
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
              Student: {selectedStudent?.name}
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
            onClick={() => setMarkAttendanceModal(false)}
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
