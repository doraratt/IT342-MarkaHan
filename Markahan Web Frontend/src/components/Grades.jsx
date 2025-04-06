import { useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

function Grades() {
  const [selectedSection, setSelectedSection] = useState('G1');
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [openFilterModal, setOpenFilterModal] = useState(false);
  const [filterData, setFilterData] = useState({
    firstName: '',
    lastName: '',
    section: ''
  });
  const [isEditing, setIsEditing] = useState(false);
  const [openConfirmModal, setOpenConfirmModal] = useState(false);
  const [editedGrades, setEditedGrades] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFilterData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFilter = () => {
    console.log('Filtering with:', filterData);
    setOpenFilterModal(false);
  };

  const students = [
    { id: 1, name: 'Gaylord Tuwid', remarks: 'Passed' },
    { id: 2, name: 'Kally Vhangon', remarks: 'Passed' },
    { id: 3, name: 'Bugart Batongbakal Jr.', remarks: 'Passed' },
    { id: 4, name: 'Wy Lee Guo', remarks: 'Passed' },
    { id: 5, name: 'Raoul Philipi', remarks: 'Passed' },
    { id: 6, name: 'Balmond Alucard', remarks: 'Passed' },
    { id: 7, name: 'Bessie Cooper', remarks: 'Passed' },
  ];

  const sections = ['G1', 'G2', 'G3', 'G4', 'G5'];

  // Function to handle viewing a student's grades
  const handleViewGrades = (student) => {
    setSelectedStudent({
      ...student,
      year: '2425',
      subjects: [
        { name: 'Filipino', grade: 75, remarks: 'Passed' },
        { name: 'English', grade: 75, remarks: 'Passed' },
        { name: 'Mathematics', grade: 75, remarks: 'Passed' },
        { name: 'Science', grade: 75, remarks: 'Passed' },
        { name: 'AP', grade: 75, remarks: 'Passed' },
        { name: 'ESP', grade: 75, remarks: 'Passed' },
        { name: 'MAPEH', grade: 75, remarks: 'Passed' },
        { name: 'Computer', grade: 75, remarks: 'Passed' },
      ]
    });
  };

  // Function to handle grade changes
  const handleGradeChange = (subjectName, newValue) => {
    const numValue = parseInt(newValue) || 0;
    setEditedGrades(prev => ({
      ...prev,
      [subjectName]: {
        grade: numValue,
        remarks: numValue >= 75 ? 'Passed' : 'Failed'
      }
    }));
  };

  // Function to start editing
  const handleStartEdit = () => {
    // Initialize editedGrades with current grades
    const initialGrades = {};
    selectedStudent.subjects.forEach(subject => {
      initialGrades[subject.name] = {
        grade: subject.grade,
        remarks: subject.remarks
      };
    });
    setEditedGrades(initialGrades);
    setIsEditing(true);
  };

  // Function to handle final confirmation
  const handleConfirmEdit = () => {
    // Here you would typically save the changes to your backend
    console.log('Saving edited grades:', editedGrades);
    setIsEditing(false);
    setEditedGrades(null);
    setOpenConfirmModal(false);
  };

  // If a student is selected, show their grades
  if (selectedStudent) {
    const generalAverage = selectedStudent.subjects.reduce((acc, subject) => {
      const grade = editedGrades ? editedGrades[subject.name].grade : subject.grade;
      return acc + grade;
    }, 0) / selectedStudent.subjects.length;

    return (
      <Box sx={{ width: '100%', p: 4 }}>
        {/* Back Button */}
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => setSelectedStudent(null)}
          sx={{
            color: '#0D5CAB',
            textTransform: 'none',
            mb: 3,
            '&:hover': {
              backgroundColor: 'transparent',
              textDecoration: 'underline',
            },
          }}
        >
          Back to Grades
        </Button>

        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center', 
            mb: 2 
          }}>
            {/* Left side with Student Name and School Year */}
            <Box sx={{ 
              display: 'flex', 
              alignItems: 'center',
              gap: 4 // Spacing between name and school year
            }}>
              <Typography variant="h5">{selectedStudent.name}</Typography>
              
              {/* School Year Box */}
              <Box sx={{ 
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center'
              }}>
                <Typography variant="body2" sx={{ mb: 0.5 }}>
                  School Year
                </Typography>
                <Box sx={{ 
                  backgroundColor: '#0D5CAB',
                  color: 'white',
                  px: 3,
                  py: 1,
                  borderRadius: 1
                }}>
                  <Typography>{selectedStudent.year}</Typography>
                </Box>
              </Box>
            </Box>

            {/* Edit Grade Button */}
            {!isEditing && (
              <Button
                variant="contained"
                onClick={handleStartEdit}
                sx={{
                  backgroundColor: '#0D5CAB',
                  '&:hover': {
                    backgroundColor: '#094a8f',
                  },
                  textTransform: 'none',
                  px: 3,
                  py: 1
                }}
              >
                Edit Grade
              </Button>
            )}
          </Box>
        </Box>

        {/* Learning Areas Table */}
        <Box sx={{ mb: 4, backgroundColor: 'white', borderRadius: 2, overflow: 'hidden' }}>
          <Box sx={{ 
            display: 'grid',
            gridTemplateColumns: '1fr 100px 100px',
            gap: 2,
            p: 2,
            backgroundColor: '#f8f9fa',
          }}>
            <Typography fontWeight="bold">Learning Areas</Typography>
            <Typography fontWeight="bold" align="center">Final Grade</Typography>
            <Typography fontWeight="bold" align="center">Remarks</Typography>
          </Box>

          {selectedStudent.subjects.map((subject, index) => (
            <Box
              key={subject.name}
              sx={{
                display: 'grid',
                gridTemplateColumns: '1fr 100px 100px',
                gap: 2,
                p: 2,
                backgroundColor: index % 2 === 0 ? '#f8f9fa' : 'white',
                borderTop: '1px solid #eee'
              }}
            >
              <Typography>{subject.name}</Typography>
              {isEditing ? (
                <TextField
                  size="small"
                  type="number"
                  defaultValue={editedGrades[subject.name].grade}
                  onChange={(e) => handleGradeChange(subject.name, e.target.value)}
                  inputProps={{ min: 0, max: 100 }}
                  sx={{ width: '80px', justifySelf: 'center' }}
                />
              ) : (
                <Typography align="center">
                  {editedGrades ? editedGrades[subject.name].grade : subject.grade}
                </Typography>
              )}
              <Typography 
                align="center" 
                sx={{ 
                  color: (editedGrades ? editedGrades[subject.name].grade : subject.grade) >= 75 ? '#4CAF50' : '#f44336'
                }}
              >
                {editedGrades ? editedGrades[subject.name].remarks : subject.remarks}
              </Typography>
            </Box>
          ))}
        </Box>

        {/* General Average and Submit/Confirm Button */}
        <Box sx={{ 
          width: '100%',
          display: 'flex',
          justifyContent: 'center',
          position: 'relative',
          mb: 4,
          mt: 2
        }}>
          <Box sx={{
            width: '100%',
            maxWidth: '800px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <Box sx={{ width: '120px' }} />
            <Box sx={{
              backgroundColor: '#f8f9fa',
              px: 4,
              py: 2,
              borderRadius: 1,
              display: 'flex',
              alignItems: 'center',
              gap: 2
            }}>
              <Typography>General Average:</Typography>
              <Typography fontWeight="bold">{generalAverage.toFixed(0)}</Typography>
            </Box>
            {isEditing ? (
              <Button
                variant="contained"
                onClick={() => setOpenConfirmModal(true)}
                sx={{
                  backgroundColor: '#4CAF50',
                  '&:hover': {
                    backgroundColor: '#45a049',
                  },
                  textTransform: 'none',
                  px: 4,
                  minWidth: '120px'
                }}
              >
                Confirm
              </Button>
            ) : (
              <Button
                variant="contained"
                sx={{
                  backgroundColor: '#0D5CAB',
                  '&:hover': {
                    backgroundColor: '#094a8f',
                  },
                  textTransform: 'none',
                  px: 4,
                  minWidth: '120px'
                }}
              >
                Submit
              </Button>
            )}
          </Box>
        </Box>

        {/* Confirmation Modal */}
        <Modal
          open={openConfirmModal}
          onClose={() => setOpenConfirmModal(false)}
        >
          <Box sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: 400,
            bgcolor: 'background.paper',
            borderRadius: 2,
            p: 3,
          }}>
            <Box sx={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center', 
              mb: 2 
            }}>
              <Typography variant="h6">Edit Grade</Typography>
              <IconButton 
                onClick={() => setOpenConfirmModal(false)}
                size="small"
              >
                <CloseIcon />
              </IconButton>
            </Box>
            
            <Typography sx={{ mb: 3 }}>
              Are you sure you want to edit this grade?
            </Typography>

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
              <Button
                variant="contained"
                onClick={handleConfirmEdit}
                sx={{
                  backgroundColor: '#4CAF50',
                  '&:hover': {
                    backgroundColor: '#45a049',
                  },
                  textTransform: 'none',
                }}
              >
                Confirm
              </Button>
              <Button
                onClick={() => setOpenConfirmModal(false)}
                sx={{
                  backgroundColor: '#9e9e9e',
                  color: 'white',
                  '&:hover': {
                    backgroundColor: '#757575',
                  },
                  textTransform: 'none',
                }}
              >
                Cancel
              </Button>
            </Box>
          </Box>
        </Modal>
      </Box>
    );
  }

  // If no student is selected, show the grades list
  return (
    <Box sx={{ width: '100%', p: 3 }}>
      {/* Sections Header */}
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

          {/* Filter Grade Button */}
          <Button
            variant="contained"
            onClick={() => setOpenFilterModal(true)}
            sx={{
              backgroundColor: '#0D5CAB',
              '&:hover': {
                backgroundColor: '#094a8f',
              },
              textTransform: 'none',
              borderRadius: '4px',
              px: 3,
              ml: 2
            }}
          >
            Filter Grade
          </Button>
        </Box>
      </Box>

      {/* Grades Table */}
      <Box sx={{ 
        backgroundColor: 'white',
        borderRadius: '8px',
        overflow: 'hidden',
        width: '100%',
      }}>
        {/* Table Header */}
        <Box sx={{ 
          display: 'grid',
          gridTemplateColumns: '1fr 1fr 120px',
          backgroundColor: '#f0f0f0',
          padding: '12px 24px',
        }}>
          <Typography fontWeight="500">Students</Typography>
          <Typography fontWeight="500">Remarks</Typography>
          <Typography fontWeight="500"></Typography>
        </Box>

        {/* Students List */}
        {students.map((student, index) => (
          <Box
            key={student.id}
            sx={{
              display: 'grid',
              gridTemplateColumns: '1fr 1fr 120px',
              padding: '16px 24px',
              backgroundColor: index % 2 === 0 ? '#f0f0f0' : 'white',
              '&:hover': {
                backgroundColor: '#f5f5f5',
              },
            }}
          >
            <Typography>{student.name}</Typography>
            <Typography>{student.remarks}</Typography>
            <Button
              onClick={() => handleViewGrades(student)}
              sx={{
                color: '#0D5CAB',
                textTransform: 'none',
                justifyContent: 'flex-end',
                '&:hover': {
                  backgroundColor: 'transparent',
                  textDecoration: 'underline',
                },
              }}
            >
              View Grades
            </Button>
          </Box>
        ))}
      </Box>

      {/* Filter Modal */}
      <Modal
        open={openFilterModal}
        onClose={() => setOpenFilterModal(false)}
        aria-labelledby="filter-modal"
      >
        <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 400,
          bgcolor: 'background.paper',
          borderRadius: 2,
          p: 3,
        }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h6">Filter Grade</Typography>
            <IconButton onClick={() => setOpenFilterModal(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Box>
              <Typography variant="body2" sx={{ mb: 1 }}>Last Name</Typography>
              <TextField
                name="lastName"
                value={filterData.lastName}
                onChange={handleInputChange}
                fullWidth
                size="small"
                placeholder="Enter last name"
              />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1 }}>First Name</Typography>
              <TextField
                name="firstName"
                value={filterData.firstName}
                onChange={handleInputChange}
                fullWidth
                size="small"
                placeholder="Enter first name"
              />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ mb: 1 }}>Section</Typography>
              <TextField
                name="section"
                value={filterData.section}
                onChange={handleInputChange}
                fullWidth
                size="small"
                placeholder="Enter section"
              />
            </Box>
            <Button
              variant="contained"
              onClick={handleFilter}
              sx={{
                backgroundColor: '#0D5CAB',
                '&:hover': {
                  backgroundColor: '#094a8f',
                },
                textTransform: 'none',
                mt: 2
              }}
            >
              Apply Filter
            </Button>
          </Box>
        </Box>
      </Modal>
    </Box>
  );
}

export default Grades;
