import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

function Grades() {
  const { user } = useUser();
  const [selectedSection, setSelectedSection] = useState('');
  const [openGradesModal, setOpenGradesModal] = useState(false);
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
          console.log('Fetched grades:', response.data); // Debug log
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
    setOpenFilterModal(false);
  };

  const handleViewGrades = (student) => {
    console.log('Viewing grades for student ID:', student.studentId);
    const studentGrade = grades.find(g => {
      if (!g.student) {
        console.warn('Grade record missing student field:', g);
        return false;
      }
      return g.student.studentId === student.studentId;
    });
    console.log('Student grade found:', studentGrade);
    if (!studentGrade) {
      setError('No grades have been recorded for this student yet. Click "Edit Grade" to add grades.');
    } else {
      setError('');
    }
    const subjects = [
      { name: 'Filipino', grade: studentGrade?.filipino || '--', remarks: studentGrade?.filipino ? (studentGrade.filipino >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'English', grade: studentGrade?.english || '--', remarks: studentGrade?.english ? (studentGrade.english >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'Mathematics', grade: studentGrade?.mathematics || '--', remarks: studentGrade?.mathematics ? (studentGrade.mathematics >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'Science', grade: studentGrade?.science || '--', remarks: studentGrade?.science ? (studentGrade.science >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'AP', grade: studentGrade?.ap || '--', remarks: studentGrade?.ap ? (studentGrade.ap >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'ESP', grade: studentGrade?.esp || '--', remarks: studentGrade?.esp ? (studentGrade.esp >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'MAPEH', grade: studentGrade?.mapeh || '--', remarks: studentGrade?.mapeh ? (studentGrade.mapeh >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
      { name: 'Computer', grade: studentGrade?.computer || '--', remarks: studentGrade?.computer ? (studentGrade.computer >= 75 ? 'Passed' : 'Failed') : 'Not Set' },
    ];
  
    setSelectedStudent({
      ...student,
      year: '2425',
      subjects,
      gradeId: studentGrade?.gradeId
    });
    setOpenGradesModal(true);
  };

  const handleGradeChange = (subjectName, newValue) => {
    const numValue = newValue === '--' ? '--' : parseFloat(newValue) || 0;
    setEditedGrades(prev => ({
      ...prev,
      [subjectName.toLowerCase()]: {
        grade: numValue,
        remarks: numValue === '--' ? 'Not Set' : numValue >= 75 ? 'Passed' : 'Failed'
      }
    }));
  };

  const handleStartEdit = () => {
    const initialGrades = {
      filipino: { grade: selectedStudent.subjects.find(s => s.name === 'Filipino').grade, remarks: selectedStudent.subjects.find(s => s.name === 'Filipino').remarks },
      english: { grade: selectedStudent.subjects.find(s => s.name === 'English').grade, remarks: selectedStudent.subjects.find(s => s.name === 'English').remarks },
      mathematics: { grade: selectedStudent.subjects.find(s => s.name === 'Mathematics').grade, remarks: selectedStudent.subjects.find(s => s.name === 'Mathematics').remarks },
      science: { grade: selectedStudent.subjects.find(s => s.name === 'Science').grade, remarks: selectedStudent.subjects.find(s => s.name === 'Science').remarks },
      ap: { grade: selectedStudent.subjects.find(s => s.name === 'AP').grade, remarks: selectedStudent.subjects.find(s => s.name === 'AP').remarks },
      esp: { grade: selectedStudent.subjects.find(s => s.name === 'ESP').grade, remarks: selectedStudent.subjects.find(s => s.name === 'ESP').remarks },
      mapeh: { grade: selectedStudent.subjects.find(s => s.name === 'MAPEH').grade, remarks: selectedStudent.subjects.find(s => s.name === 'MAPEH').remarks },
      computer: { grade: selectedStudent.subjects.find(s => s.name === 'Computer').grade, remarks: selectedStudent.subjects.find(s => s.name === 'Computer').remarks },
    };
    setEditedGrades(initialGrades);
    setIsEditing(true);
  };

  const handleConfirmEdit = async () => {
    if (!user || !selectedStudent) {
      setError('User or student not selected');
      return;
    }

    try {
      const gradeData = {
        student: { studentId: selectedStudent.studentId },
        user: { userId: user.userId },
        filipino: editedGrades.filipino.grade === '--' ? 0 : editedGrades.filipino.grade,
        english: editedGrades.english.grade === '--' ? 0 : editedGrades.english.grade,
        mathematics: editedGrades.mathematics.grade === '--' ? 0 : editedGrades.mathematics.grade,
        science: editedGrades.science.grade === '--' ? 0 : editedGrades.science.grade,
        ap: editedGrades.ap.grade === '--' ? 0 : editedGrades.ap.grade,
        esp: editedGrades.esp.grade === '--' ? 0 : editedGrades.esp.grade,
        mapeh: editedGrades.mapeh.grade === '--' ? 0 : editedGrades.mapeh.grade,
        computer: editedGrades.computer.grade === '--' ? 0 : editedGrades.computer.grade,
        remarks: editedGrades.filipino.grade >= 75 && editedGrades.english.grade >= 75 && 
                 editedGrades.mathematics.grade >= 75 && editedGrades.science.grade >= 75 &&
                 editedGrades.ap.grade >= 75 && editedGrades.esp.grade >= 75 &&
                 editedGrades.mapeh.grade >= 75 && editedGrades.computer.grade >= 75 ? 'Passed' : 'Failed'
      };

      let response;
      if (selectedStudent.gradeId) {
        response = await axios.put(
          `http://localhost:8080/api/grade/putGrade/${selectedStudent.gradeId}`,
          gradeData
        );
      } else {
        response = await axios.post(
          'http://localhost:8080/api/grade/postGrade',
          gradeData
        );
      }

      const updatedGrades = await axios.get(`http://localhost:8080/api/grade/getGradesByUser?userId=${user.userId}`);
      setGrades(updatedGrades.data);
      
      const updatedSubjects = [
        { name: 'Filipino', grade: response.data.filipino, remarks: response.data.filipino >= 75 ? 'Passed' : 'Failed' },
        { name: 'English', grade: response.data.english, remarks: response.data.english >= 75 ? 'Passed' : 'Failed' },
        { name: 'Mathematics', grade: response.data.mathematics, remarks: response.data.mathematics >= 75 ? 'Passed' : 'Failed' },
        { name: 'Science', grade: response.data.science, remarks: response.data.science >= 75 ? 'Passed' : 'Failed' },
        { name: 'AP', grade: response.data.ap, remarks: response.data.ap >= 75 ? 'Passed' : 'Failed' },
        { name: 'ESP', grade: response.data.esp, remarks: response.data.esp >= 75 ? 'Passed' : 'Failed' },
        { name: 'MAPEH', grade: response.data.mapeh, remarks: response.data.mapeh >= 75 ? 'Passed' : 'Failed' },
        { name: 'Computer', grade: response.data.computer, remarks: response.data.computer >= 75 ? 'Passed' : 'Failed' },
      ];

      setSelectedStudent({
        ...selectedStudent,
        subjects: updatedSubjects,
        gradeId: response.data.gradeId
      });
      
      setIsEditing(false);
      setEditedGrades(null);
      setOpenConfirmModal(false);
      setError('');
    } catch (error) {
      setError('Error saving grades: ' + (error.response?.data || error.message));
      console.error('Save grades error:', error);
    }
  };

  const sections = [...new Set(students.map(student => student.section))].sort();

  const renderGradesContent = () => {
    const generalAverage = selectedStudent?.subjects.reduce((acc, subject) => {
      const grade = editedGrades ? editedGrades[subject.name.toLowerCase()]?.grade : subject.grade;
      return grade === '--' ? acc : acc + parseFloat(grade);
    }, 0) / selectedStudent.subjects.filter(s => s.grade !== '--').length || 0;

    return (
      <Box sx={{ width: '100%', p: 4 }}>
        {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}
        
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
                  defaultValue={editedGrades[subject.name.toLowerCase()].grade}
                  onChange={(e) => handleGradeChange(subject.name, e.target.value)}
                  inputProps={{ min: 0, max: 100 }}
                  sx={{ width: '80px', justifySelf: 'center' }}
                />
              ) : (
                <Typography align="center">
                  {editedGrades ? editedGrades[subject.name.toLowerCase()].grade : subject.grade}
                </Typography>
              )}
              <Typography 
                align="center" 
                sx={{ color: (editedGrades ? editedGrades[subject.name.toLowerCase()].grade : subject.grade) >= 75 ? '#4CAF50' : '#f44336' }}
              >
                {editedGrades ? editedGrades[subject.name.toLowerCase()].remarks : subject.remarks}
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
      </Box>
    );
  };

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
              const studentGrade = grades.find(g => g.student?.studentId === student.studentId);
              const overallRemark = studentGrade ? 
                (studentGrade.filipino >= 75 && studentGrade.english >= 75 && 
                 studentGrade.mathematics >= 75 && studentGrade.science >= 75 &&
                 studentGrade.ap >= 75 && studentGrade.esp >= 75 &&
                 studentGrade.mapeh >= 75 && studentGrade.computer >= 75 ? 'Passed' : 'Failed') : '--';

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

      <Modal
        open={openGradesModal}
        onClose={() => {
          setOpenGradesModal(false);
          setSelectedStudent(null);
          setIsEditing(false);
          setEditedGrades(null);
        }}
      >
        <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: '90%',
          maxWidth: 800,
          bgcolor: 'background.paper',
          borderRadius: 2,
          p: 3,
          maxHeight: '90vh',
          overflowY: 'auto'
        }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h6">Student Grades</Typography>
            <IconButton 
              onClick={() => {
                setOpenGradesModal(false);
                setSelectedStudent(null);
                setIsEditing(false);
                setEditedGrades(null);
              }} 
              size="small"
            >
              <CloseIcon />
            </IconButton>
          </Box>
          {selectedStudent && renderGradesContent()}
        </Box>
      </Modal>

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

export default Grades;