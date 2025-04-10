import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

function Grades() {
  const { user } = useUser();
  const [selectedSection, setSelectedSection] = useState('');
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
  const [students, setStudents] = useState([]);
  const [grades, setGrades] = useState([]);
  const [error, setError] = useState('');

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    if (user) {
      const fetchStudents = async () => {
        try {
          const response = await axios.get(`http://localhost:8080/api/student/getStudentsByUser?userId=${user.userId}`);
          console.log('Fetched students:', response.data);
          setStudents(response.data);
          const uniqueSections = [...new Set(response.data.map(s => s.section))].sort();
          setSelectedSection(uniqueSections[0] || '');
          setError('');
        } catch (error) {
          setError('Error fetching students: ' + (error.response?.data || error.message));
          console.error('Fetch students error:', error);
        }
      };

      const fetchGrades = async () => {
        try {
          const response = await axios.get(`http://localhost:8080/api/grade/getGradesByUser?userId=${user.userId}`);
          console.log('Fetched grades:', response.data);
          setGrades(response.data);
          setError('');
        } catch (error) {
          setError('Error fetching grades: ' + (error.response?.data || error.message));
          console.error('Fetch grades error:', error);
        }
      };

      fetchStudents();
      fetchGrades();
    }
  }, [user]);

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

  const handleViewGrades = async (student) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/grade/getGradesByUser?userId=${user.userId}`);
      console.log('Fetched grades for student view:', response.data);
      const studentGrades = response.data.filter(g => g.student?.studentId === student.studentId);
      const subjects = studentGrades.length > 0 ? studentGrades.map(g => ({
        name: g.subjectName,
        grade: g.finalGrade,
        remarks: g.remarks
      })) : [
        { name: 'Filipino', grade: '--', remarks: 'Not Set' },
        { name: 'English', grade: '--', remarks: 'Not Set' },
        { name: 'Mathematics', grade: '--', remarks: 'Not Set' },
        { name: 'Science', grade: '--', remarks: 'Not Set' },
        { name: 'AP', grade: '--', remarks: 'Not Set' },
        { name: 'ESP', grade: '--', remarks: 'Not Set' },
        { name: 'MAPEH', grade: '--', remarks: 'Not Set' },
        { name: 'Computer', grade: '--', remarks: 'Not Set' },
      ];

      setGrades(response.data); // Update global grades state
      setSelectedStudent({
        ...student,
        year: '2425',
        subjects
      });
      setError('');
    } catch (error) {
      setError('Error fetching grades: ' + (error.response?.data || error.message));
      console.error('Fetch grades error:', error);
    }
  };

  const handleGradeChange = (subjectName, newValue) => {
    const numValue = newValue === '--' ? '--' : parseInt(newValue) || 0;
    setEditedGrades(prev => ({
      ...prev,
      [subjectName]: {
        grade: numValue,
        remarks: numValue === '--' ? 'Not Set' : numValue >= 75 ? 'Passed' : 'Failed'
      }
    }));
  };

  const handleStartEdit = () => {
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

  const handleConfirmEdit = async () => {
    if (!user) {
      setError('User not logged in');
      return;
    }
    if (!selectedStudent) {
      setError('No student selected');
      return;
    }

    try {
      const updatedGrades = [];
      for (const [subjectName, data] of Object.entries(editedGrades)) {
        if (data.grade === '--') continue;

        const existingGrade = grades.find(g => 
          g.student?.studentId === selectedStudent.studentId && 
          g.subjectName === subjectName
        );

        const gradeData = {
          student: { studentId: selectedStudent.studentId },
          user: { userId: user.userId },
          subjectName,
          finalGrade: data.grade,
          remarks: data.remarks
        };

        if (existingGrade) {
          const response = await axios.put(
            `http://localhost:8080/api/grade/putGrade/${existingGrade.gradeId}`,
            gradeData
          );
          updatedGrades.push(response.data);
        } else {
          const response = await axios.post(
            'http://localhost:8080/api/grade/postGrade',
            gradeData
          );
          updatedGrades.push(response.data);
        }
      }

      // Fetch the latest grades after saving
      const response = await axios.get(`http://localhost:8080/api/grade/getGradesByUser?userId=${user.userId}`);
      setGrades(response.data);
      setSelectedStudent({
        ...selectedStudent,
        year: '2425',
        subjects: updatedGrades.length > 0 ? updatedGrades.map(g => ({
          name: g.subjectName,
          grade: g.finalGrade,
          remarks: g.remarks
        })) : selectedStudent.subjects
      });
      setIsEditing(false);
      setEditedGrades(null);
      setOpenConfirmModal(false);
      setError('');
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.response?.data || error.message;
      setError('Error saving grades: ' + errorMessage);
      console.error('Full error details:', error.response || error);
    }
  };

  const handleBack = () => {
    setSelectedStudent(null);
    setIsEditing(false);
    setEditedGrades(null);
    setError('');
  };

  if (selectedStudent) {
    const generalAverage = selectedStudent.subjects.reduce((acc, subject) => {
      const grade = editedGrades ? editedGrades[subject.name].grade : subject.grade;
      return grade === '--' ? acc : acc + parseInt(grade);
    }, 0) / selectedStudent.subjects.filter(s => s.grade !== '--').length || 0;

    return (
      <Box sx={{ width: '100%', p: 4 }}>
        {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          sx={{
            color: '#0D5CAB',
            textTransform: 'none',
            mb: 3,
            '&:hover': { backgroundColor: 'transparent', textDecoration: 'underline' },
          }}
        >
          Back to Grades
        </Button>

        <Box sx={{ mb: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              <Typography variant="h5">{selectedStudent.firstName} {selectedStudent.lastName}</Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Typography variant="body2" sx={{ mb: 0.5 }}>School Year</Typography>
                <Box sx={{ backgroundColor: '#0D5CAB', color: 'white', px: 3, py: 1, borderRadius: 1 }}>
                  <Typography>{selectedStudent.year}</Typography>
                </Box>
              </Box>
            </Box>
            {!isEditing && (
              <Button
                variant="contained"
                onClick={handleStartEdit}
                sx={{
                  backgroundColor: '#0D5CAB',
                  '&:hover': { backgroundColor: '#094a8f' },
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

        <Box sx={{ mb: 4, backgroundColor: 'white', borderRadius: 2, overflow: 'hidden' }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 100px 100px', gap: 2, p: 2, backgroundColor: '#f8f9fa' }}>
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
                  type="text"
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
                sx={{ color: (editedGrades ? editedGrades[subject.name].grade : subject.grade) >= 75 ? '#4CAF50' : '#f44336' }}
              >
                {editedGrades ? editedGrades[subject.name].remarks : subject.remarks}
              </Typography>
            </Box>
          ))}
        </Box>

        <Box sx={{ width: '100%', display: 'flex', justifyContent: 'center', position: 'relative', mb: 4, mt: 2 }}>
          <Box sx={{ width: '100%', maxWidth: '800px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box sx={{ width: '120px' }} />
            <Box sx={{ backgroundColor: '#f8f9fa', px: 4, py: 2, borderRadius: 1, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Typography>General Average:</Typography>
              <Typography fontWeight="bold">{generalAverage ? generalAverage.toFixed(0) : '--'}</Typography>
            </Box>
            {isEditing ? (
              <Button
                variant="contained"
                onClick={() => setOpenConfirmModal(true)}
                sx={{
                  backgroundColor: '#4CAF50',
                  '&:hover': { backgroundColor: '#45a049' },
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
                  '&:hover': { backgroundColor: '#094a8f' },
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
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">Edit Grade</Typography>
              <IconButton onClick={() => setOpenConfirmModal(false)} size="small">
                <CloseIcon />
              </IconButton>
            </Box>
            <Typography sx={{ mb: 3 }}>Are you sure you want to edit this grade?</Typography>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
              <Button
                variant="contained"
                onClick={handleConfirmEdit}
                sx={{
                  backgroundColor: '#4CAF50',
                  '&:hover': { backgroundColor: '#45a049' },
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
                  '&:hover': { backgroundColor: '#757575' },
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

  const sections = [...new Set(students.map(student => student.section))].sort();

  return (
    <Box sx={{ width: '100%', p: 3 }}>
      {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}
      <Box sx={{ mb: 4 }}>
        <Typography sx={{ mb: 2 }}>Sections</Typography>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
                  '&:hover': { backgroundColor: selectedSection === section ? '#0D5CAB' : '#f5f5f5' },
                  '&:first-of-type': { borderTopLeftRadius: '4px', borderBottomLeftRadius: '4px' },
                  '&:last-child': { borderTopRightRadius: '4px', borderBottomRightRadius: '4px' },
                }}
              >
                {section}
              </Button>
            ))}
          </Box>
          <Button
            variant="contained"
            onClick={() => setOpenFilterModal(true)}
            sx={{
              backgroundColor: '#0D5CAB',
              '&:hover': { backgroundColor: '#094a8f' },
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

      <Box sx={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden', width: '100%' }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr 120px', backgroundColor: '#f0f0f0', padding: '12px 24px' }}>
          <Typography fontWeight="500">Students</Typography>
          <Typography fontWeight="500">Remarks</Typography>
          <Typography fontWeight="500"></Typography>
        </Box>

        {students.length > 0 ? (
          students
            .filter(student => student.section === selectedSection)
            .map((student, index) => {
              const studentGrades = grades.filter(g => g.student?.studentId === student.studentId);
              const overallRemark = studentGrades.length > 0
                ? studentGrades.every(g => g.finalGrade >= 75) ? 'Passed' : 'Failed'
                : '--';

              return (
                <Box
                  key={student.studentId}
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: '1fr 1fr 120px',
                    padding: '16px 24px',
                    backgroundColor: index % 2 === 0 ? '#f0f0f0' : 'white',
                    '&:hover': { backgroundColor: '#f5f5f5' },
                  }}
                >
                  <Typography>{student.firstName} {student.lastName}</Typography>
                  <Typography>{overallRemark}</Typography>
                  <Button
                    onClick={() => handleViewGrades(student)}
                    sx={{
                      color: '#0D5CAB',
                      textTransform: 'none',
                      justifyContent: 'flex-end',
                      '&:hover': { backgroundColor: 'transparent', textDecoration: 'underline' },
                    }}
                  >
                    View Grades
                  </Button>
                </Box>
              );
            })
        ) : (
          <Box sx={{ padding: '16px 24px' }}>
            <Typography>No students available.</Typography>
          </Box>
        )}
      </Box>

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
                '&:hover': { backgroundColor: '#094a8f' },
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