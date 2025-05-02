import { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import axios from 'axios';
import { useUser } from '../UserContext';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'; // Fallback for local dev

function ArchivedStudentsPage() {
  const { user } = useUser();
  const [students, setStudents] = useState([]);
  const [selectedSection, setSelectedSection] = useState('');
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [openConfirmUnarchiveModal, setOpenConfirUnarchiveModal] = useState(false);
  const [openConfirmDeleteModal, setOpenConfirmDeleteModal] = useState(false);
  const [error, setError] = useState('');

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    const fetchArchivedStudents = async () => {
      try {
        const response = await axios.get(`${API_URL}/api/student/getStudentsByUser?userId=${user.userId}`);
        const mappedStudents = response.data.map(student => ({
          studentId: student.studentId,
          firstName: student.firstName,
          lastName: student.lastName,
          gender: student.gender,
          section: student.section,
          gradeLevel: student.gradeLevel,
          isArchived: student.archived,
        }));
        const archivedStudents = mappedStudents.filter(student => student.isArchived);
        setStudents(archivedStudents);
        const uniqueSections = [...new Set(archivedStudents.map(s => s.section))].sort();
        setSelectedSection(uniqueSections[0] || '');
      } catch (error) {
        console.error('Error fetching archived students:', error.response?.data || error.message);
        setError('Failed to fetch archived students');
      }
    };
    fetchArchivedStudents();
  }, [user]);

  const handleUnarchive = (student) => {
    setSelectedStudent(student);
    setOpenConfirmUnarchiveModal(true);
  };

  const handleFinalUnarchive = async () => {
    if (!selectedStudent?.studentId) {
      setError('No student selected');
      return;
    }
    try {
      const updatedStudentData = {
        studentId: selectedStudent.studentId,
        firstName: selectedStudent.firstName,
        lastName: selectedStudent.lastName,
        gender: selectedStudent.gender,
        section: selectedStudent.section,
        gradeLevel: selectedStudent.gradeLevel,
        archived: false,
        user: { userId: user.userId }
      };
      const response = await axios.put(
        `${API_URL}/api/student/update/${selectedStudent.studentId}`,
        updatedStudentData
      );
      const updatedStudentResponse = {
        studentId: response.data.studentId,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        gender: response.data.gender,
        section: response.data.section,
        gradeLevel: response.data.gradeLevel,
        isArchived: response.data.archived,
      };
      const updatedStudents = students.filter(student => student.studentId !== selectedStudent.studentId);
      setStudents(updatedStudents);
      const uniqueSections = [...new Set(updatedStudents.map(s => s.section))].sort();
      if (!uniqueSections.includes(selectedSection) || !selectedSection) {
        setSelectedSection(uniqueSections[0] || '');
      }
      setOpenConfirmUnarchiveModal(false);
      setSelectedStudent(null);
    } catch (error) {
      setError('Error unarchiving student: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = (student) => {
    setSelectedStudent(student);
    setOpenConfirmDeleteModal(true);
  };

  const handleFinalDelete = async () => {
    if (!selectedStudent?.studentId) {
      setError('No student selected');
      return;
    }
    try {
      await axios.delete(`${API_URL}/api/student/delete/${selectedStudent.studentId}`);
      const updatedStudents = students.filter(student => student.studentId !== selectedStudent.studentId);
      setStudents(updatedStudents);
      const uniqueSections = [...new Set(updatedStudents.map(s => s.section))].sort();
      if (!uniqueSections.includes(selectedSection) || !selectedSection) {
        setSelectedSection(uniqueSections[0] || '');
      }
      setOpenConfirmDeleteModal(false);
      setSelectedStudent(null);
    } catch (error) {
      setError('Error deleting student: ' + (error.response?.data?.message || error.message));
    }
  };

  const sections = [...new Set(students.map(student => student.section))].sort();
  const filteredStudents = students.filter(student => student.section === selectedSection);
  const maleStudents = filteredStudents
    .filter(student => student.gender === 'Male')
    .sort((a, b) => `${a.lastName}, ${a.firstName}`.toLowerCase().localeCompare(`${b.lastName}, ${b.firstName}`.toLowerCase()));
  const femaleStudents = filteredStudents
    .filter(student => student.gender === 'Female')
    .sort((a, b) => `${a.lastName}, ${a.firstName}`.toLowerCase().localeCompare(`${b.lastName}, ${b.firstName}`.toLowerCase()));

  return (
    <Box sx={{ width: '95%', p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mb: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ mb: 2 }}>Archived Students</Typography>
          <Typography sx={{ mb: 2 }}>Sections</Typography>
          <Box sx={{ display: 'flex', gap: '2px', width: 'fit-content' }}>
            {sections.length > 0 ? (
              sections.map((section) => (
                <Button
                  key={section}
                  onClick={() => setSelectedSection(section)}
                  sx={{
                    minWidth: '80px',
                    backgroundColor: selectedSection === section ? '#1f295a' : '#fff',
                    color: selectedSection === section ? '#fff' : '#000',
                    borderRadius: 0,
                    px: 3,
                    '&:hover': { backgroundColor: selectedSection === section ? '#1f295a' : '#f5f5f5' },
                    '&:first-of-type': { borderTopLeftRadius: '4px', borderBottomLeftRadius: '4px' },
                    '&:last-child': { borderTopRightRadius: '4px', borderBottomRightRadius: '4px' },
                  }}
                >
                  {section}
                </Button>
              ))
            ) : (
              <Typography>No sections available</Typography>
            )}
          </Box>
        </Box>
      </Box>

      {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}

      <Box sx={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden', width: '100%', minWidth: '1000px' }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: '3fr 1fr 1fr 2fr', backgroundColor: '#f8f9fa', padding: '16px 24px', borderBottom: '1px solid #e0e0e0' }}>
          <Typography fontWeight="bold">Students</Typography>
          <Typography fontWeight="bold">Section</Typography>
          <Typography fontWeight="bold">Grade Level</Typography>
          <Typography fontWeight="bold">Actions</Typography>
        </Box>

        {maleStudents.length > 0 && (
          <>
            <Box sx={{ backgroundColor: '#e0e0e0', padding: '8px 24px' }}>
              <Typography fontWeight="bold">Male Students</Typography>
            </Box>
            {maleStudents.map((student, index) => (
              <Box
                key={student.studentId}
                sx={{
                  display: 'grid',
                  gridTemplateColumns: '3fr 1fr 1fr 2fr',
                  padding: '16px 24px',
                  backgroundColor: index % 2 === 0 ? '#f8f9fa' : 'white',
                  borderBottom: '1px solid #e0e0e0',
                }}
              >
                <Typography>{`${student.lastName}, ${student.firstName}`}</Typography>
                <Typography>{student.section}</Typography>
                <Typography>{student.gradeLevel}</Typography>
                <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleUnarchive(student)}
                    sx={{ backgroundColor: '#4CAF50', '&:hover': { backgroundColor: '#45a049' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Unarchive
                  </Button>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleDelete(student)}
                    sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Delete
                  </Button>
                </Box>
              </Box>
            ))}
          </>
        )}

        {femaleStudents.length > 0 && (
          <>
            <Box sx={{ backgroundColor: '#e0e0e0', padding: '8px 24px' }}>
              <Typography fontWeight="bold">Female Students</Typography>
            </Box>
            {femaleStudents.map((student, index) => (
              <Box
                key={student.studentId}
                sx={{
                  display: 'grid',
                  gridTemplateColumns: '3fr 1fr 1fr 2fr',
                  padding: '16px 24px',
                  backgroundColor: index % 2 === 0 ? '#f8f9fa' : 'white',
                  borderBottom: '1px solid #e0e0e0',
                  '&:last-child': { borderBottom: 'none' },
                }}
              >
                <Typography>{`${student.lastName}, ${student.firstName}`}</Typography>
                <Typography>{student.section}</Typography>
                <Typography>{student.gradeLevel}</Typography>
                <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleUnarchive(student)}
                    sx={{ backgroundColor: '#4CAF50', '&:hover': { backgroundColor: '#45a049' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Unarchive
                  </Button>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleDelete(student)}
                    sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Delete
                  </Button>
                </Box>
              </Box>
            ))}
          </>
        )}

        {maleStudents.length === 0 && femaleStudents.length === 0 && (
          <Box sx={{ padding: '16px 24px', textAlign: 'center' }}>
            <Typography>No archived students found in this section.</Typography>
          </Box>
        )}
      </Box>

      <Modal open={openConfirmUnarchiveModal} onClose={() => setOpenConfirmUnarchiveModal(false)}>
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
            <Typography variant="h6">Unarchive Student</Typography>
            <IconButton onClick={() => setOpenConfirmUnarchiveModal(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          <Typography sx={{ mb: 3 }}>
            Are you sure you want to unarchive this student?
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleFinalUnarchive}
              sx={{ backgroundColor: '#4CAF50', '&:hover': { backgroundColor: '#45a049' }, textTransform: 'none' }}
            >
              Confirm
            </Button>
            <Button
              variant="contained"
              onClick={() => setOpenConfirmUnarchiveModal(false)}
              sx={{ backgroundColor: '#9e9e9e', '&:hover': { backgroundColor: '#757575' }, textTransform: 'none' }}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>

      <Modal open={openConfirmDeleteModal} onClose={() => setOpenConfirmDeleteModal(false)}>
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
            <Typography variant="h6">Delete Student</Typography>
            <IconButton onClick={() => setOpenConfirmDeleteModal(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          <Typography sx={{ mb: 3 }}>
            Are you sure you want to permanently delete this student? This action cannot be undone.
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleFinalDelete}
              sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' }, textTransform: 'none' }}
            >
              Confirm
            </Button>
            <Button
              variant="contained"
              onClick={() => setOpenConfirmDeleteModal(false)}
              sx={{ backgroundColor: '#9e9e9e', '&:hover': { backgroundColor: '#757575' }, textTransform: 'none' }}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>
    </Box>
  );
}

export default ArchivedStudentsPage;