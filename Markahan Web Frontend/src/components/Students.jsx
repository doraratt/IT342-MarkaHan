import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'; // Fallback for local dev

// AddStudentModal
const AddStudentModal = ({ open, onClose, onSubmit, data, onChange }) => (
  <Modal open={open} onClose={onClose}>
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
        <Typography variant="h6">Add Student</Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </Box>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <TextField
          name="firstName"
          label="First Name"
          value={data.firstName || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter first name"
          required
          variant="outlined"
          autoComplete="off"
        />
        <TextField
          name="lastName"
          label="Last Name"
          value={data.lastName || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter last name"
          required
          variant="outlined"
          autoComplete="off"
        />
        <FormControl fullWidth size="small" required>
          <InputLabel>Gender</InputLabel>
          <Select
            name="gender"
            label="Gender"
            value={data.gender || ''}
            onChange={onChange}
          >
            <MenuItem value="Male">Male</MenuItem>
            <MenuItem value="Female">Female</MenuItem>
          </Select>
        </FormControl>
        <TextField
          name="section"
          label="Section"
          value={data.section || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter section"
          required
          variant="outlined"
          autoComplete="off"
        />
        <FormControl fullWidth size="small" required>
          <InputLabel>Grade Level</InputLabel>
          <Select
            name="gradeLevel"
            label="Grade Level"
            value={data.gradeLevel || ''}
            onChange={onChange}
          >
            {[1, 2, 3, 4, 5, 6].map(grade => (
              <MenuItem key={grade} value={grade.toString()}>
                {grade}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <Button
          variant="contained"
          onClick={onSubmit}
          sx={{
            backgroundColor: '#0D5CAB',
            '&:hover': { backgroundColor: '#094a8f' },
            textTransform: 'none',
            mt: 2
          }}
        >
          Add Student
        </Button>
      </Box>
    </Box>
  </Modal>
);

// EditStudentModal
const EditStudentModal = ({ open, onClose, onSubmit, data, onChange }) => (
  <Modal open={open} onClose={onClose}>
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
        <Typography variant="h6">Edit Student</Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </Box>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <TextField
          name="firstName"
          label="First Name"
          value={data.firstName || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter first name"
          required
          variant="outlined"
          autoComplete="off"
        />
        <TextField
          name="lastName"
          label="Last Name"
          value={data.lastName || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter last name"
          required
          variant="outlined"
          autoComplete="off"
        />
        <FormControl fullWidth size="small" required>
          <InputLabel>Gender</InputLabel>
          <Select
            name="gender"
            label="Gender"
            value={data.gender || ''}
            onChange={onChange}
          >
            <MenuItem value="Male">Male</MenuItem>
            <MenuItem value="Female">Female</MenuItem>
          </Select>
        </FormControl>
        <TextField
          name="section"
          label="Section"
          value={data.section || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter section"
          required
          variant="outlined"
          autoComplete="off"
        />
        <FormControl fullWidth size="small" required>
          <InputLabel>Grade Level</InputLabel>
          <Select
            name="gradeLevel"
            label="Grade Level"
            value={data.gradeLevel || ''}
            onChange={onChange}
          >
            {[1, 2, 3, 4, 5, 6].map(grade => (
              <MenuItem key={grade} value={grade.toString()}>
                {grade}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <Button
          variant="contained"
          onClick={onSubmit}
          sx={{
            backgroundColor: '#0D5CAB',
            '&:hover': { backgroundColor: '#094a8f' },
            textTransform: 'none',
            mt: 2
          }}
        >
          Apply Edit
        </Button>
      </Box>
    </Box>
  </Modal>
);

// SearchStudentsModal
const SearchStudentsModal = ({ open, onClose, students, onSelectStudent }) => {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredStudents = students.filter(student =>
    !student.isArchived &&
    `${student.firstName} ${student.lastName}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  return (
    <Modal open={open} onClose={onClose}>
      <Box sx={{
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: 500,
        bgcolor: 'background.paper',
        borderRadius: 2,
        p: 3,
        maxHeight: '80vh',
        overflowY: 'auto',
      }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h6">Search Students</Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
        </IconButton>
        </Box>
        <TextField
          label="Search by Name"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          fullWidth
          size="small"
          placeholder="Enter student name"
          variant="outlined"
          sx={{ mb: 2 }}
          autoComplete="off"
        />
        <Box>
          {filteredStudents.length > 0 ? (
            filteredStudents.map((student) => (
              <Box
                key={student.studentId}
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  p: 1,
                  borderBottom: '1px solid #e0e0e0',
                  '&:hover': { backgroundColor: '#f5f5f5', cursor: 'pointer' },
                }}
                onClick={() => onSelectStudent(student)}
              >
                <Typography>{`${student.firstName} ${student.lastName}`}</Typography>
                <Typography sx={{ color: '#666' }}>{student.section}</Typography>
              </Box>
            ))
          ) : (
            <Typography>No students found</Typography>
          )}
        </Box>
      </Box>
    </Modal>
  );
};

// Students Component
function Students() {
  const { user } = useUser();
  const [selectedSection, setSelectedSection] = useState('');
  const [openAddModal, setOpenAddModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [openConfirmEditModal, setOpenConfirmEditModal] = useState(false);
  const [openConfirmArchiveModal, setOpenConfirmArchiveModal] = useState(false);
  const [openSearchModal, setOpenSearchModal] = useState(false);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [students, setStudents] = useState([]);
  const [newStudent, setNewStudent] = useState({
    firstName: '',
    lastName: '',
    gender: '',
    section: '',
    gradeLevel: '',
    isArchived: false
  });
  const [editingStudent, setEditingStudent] = useState({
    id: null,
    firstName: '',
    lastName: '',
    gender: '',
    section: '',
    gradeLevel: '',
    isArchived: false
  });
  const [error, setError] = useState('');

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    if (user) {
      const fetchStudents = async () => {
        try {
          const response = await axios.get(`${API_URL}/api/student/getStudentsByUser?userId=${user.userId}`);
          // Map the response data to ensure correct field names
          const mappedStudents = response.data.map(student => ({
            studentId: student.studentId,
            firstName: student.firstName,
            lastName: student.lastName,
            gender: student.gender,
            section: student.section,
            gradeLevel: student.gradeLevel,
            isArchived: student.archived, // Map 'archived' from backend to 'isArchived'
          }));
          // Filter out archived students for the main list
          const nonArchivedStudents = mappedStudents.filter(student => !student.isArchived);
          setStudents(nonArchivedStudents);
          const uniqueSections = [...new Set(nonArchivedStudents.map(s => s.section))].sort();
          setSelectedSection(uniqueSections[0] || '');
        } catch (error) {
          console.error('Error fetching students:', error.response?.data || error.message);
          setError('Failed to fetch students');
        }
      };
      fetchStudents();
    }
  }, [user]);

  const updateSections = (updatedStudents) => {
    const uniqueSections = [...new Set(updatedStudents.filter(s => !s.isArchived).map(s => s.section))].sort();
    if (!uniqueSections.includes(selectedSection) || !selectedSection) {
      setSelectedSection(uniqueSections[0] || '');
    }
  };

  const handleOpenAdd = () => setOpenAddModal(true);
  const handleCloseAdd = () => {
    setOpenAddModal(false);
    setNewStudent({ firstName: '', lastName: '', gender: '', section: '', gradeLevel: '', isArchived: false });
    setError('');
  };
  const handleOpenEdit = (student) => {
    setEditingStudent({
      id: student.studentId,
      firstName: student.firstName,
      lastName: student.lastName,
      gender: student.gender,
      section: student.section,
      gradeLevel: student.gradeLevel,
      isArchived: student.isArchived
    });
    setOpenEditModal(true);
  };
  const handleCloseEdit = () => {
    setOpenEditModal(false);
    setEditingStudent({ id: null, firstName: '', lastName: '', gender: '', section: '', gradeLevel: '', isArchived: false });
    setError('');
  };
  const handleOpenSearch = () => setOpenSearchModal(true);
  const handleCloseSearch = () => setOpenSearchModal(false);

  const handleAddStudent = async () => {
    if (!newStudent.firstName || !newStudent.lastName || !newStudent.gender || !newStudent.section || !newStudent.gradeLevel) {
      setError('All fields are required');
      return;
    }
    const studentData = { ...newStudent, user: { userId: user.userId } };
    try {
      const response = await axios.post(`${API_URL}/api/student/add`, studentData);
      const newStudentData = {
        studentId: response.data.studentId,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        gender: response.data.gender,
        section: response.data.section,
        gradeLevel: response.data.gradeLevel,
        isArchived: response.data.archived, // Map 'archived' to 'isArchived'
      };
      const updatedStudents = [...students, newStudentData];
      setStudents(updatedStudents);
      updateSections(updatedStudents);
      handleCloseAdd();
    } catch (error) {
      setError('Error adding student: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleConfirmEdit = () => {
    setOpenConfirmEditModal(true);
    setOpenEditModal(false);
  };

  const handleFinalEdit = async () => {
    if (!editingStudent.id || !editingStudent.firstName || !editingStudent.lastName || !editingStudent.gender || !editingStudent.section || !editingStudent.gradeLevel) {
      setError('All fields are required');
      return;
    }
    const studentData = { ...editingStudent, user: { userId: user.userId } };
    try {
      const response = await axios.put(
        `${API_URL}/api/student/update/${editingStudent.id}`,
        studentData
      );
      const updatedStudentData = {
        studentId: response.data.studentId,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        gender: response.data.gender,
        section: response.data.section,
        gradeLevel: response.data.gradeLevel,
        isArchived: response.data.archived, // Map 'archived' to 'isArchived'
      };
      const updatedStudents = students.map(student => (student.studentId === editingStudent.id ? updatedStudentData : student));
      setStudents(updatedStudents.filter(s => !s.isArchived)); // Ensure archived students are filtered out
      updateSections(updatedStudents);
      setOpenConfirmEditModal(false);
      setEditingStudent({ id: null, firstName: '', lastName: '', gender: '', section: '', gradeLevel: '', isArchived: false });
    } catch (error) {
      setError('Error updating student: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleArchive = (student) => {
    setSelectedStudent(student);
    setOpenConfirmArchiveModal(true);
  };

  const handleFinalArchive = async () => {
    if (!selectedStudent?.studentId) {
      setError('No student selected');
      return;
    }
    try {
      // Use the update endpoint to set isArchived to true
      const updatedStudentData = {
        studentId: selectedStudent.studentId,
        firstName: selectedStudent.firstName,
        lastName: selectedStudent.lastName,
        gender: selectedStudent.gender,
        section: selectedStudent.section,
        gradeLevel: selectedStudent.gradeLevel,
        archived: true, // Backend expects 'archived'
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
        isArchived: response.data.archived, // Map 'archived' to 'isArchived'
      };
      // Remove the archived student from the list
      const updatedStudents = students.filter(student => student.studentId !== selectedStudent.studentId);
      setStudents(updatedStudents);
      updateSections(updatedStudents);
      setOpenConfirmArchiveModal(false);
      setSelectedStudent(null);
    } catch (error) {
      setError('Error archiving student: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleSelectStudent = (student) => {
    setSelectedSection(student.section);
    handleCloseSearch();
  };

  const handleAddInputChange = (e) => {
    const { name, value } = e.target;
    setNewStudent(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleEditInputChange = (e) => {
    const { name, value } = e.target;
    setEditingStudent(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Calculate sections dynamically
  const sections = [...new Set(students.map(student => student.section))].sort();

  // Filter and sort students by section and gender
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
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            onClick={handleOpenAdd}
            sx={{
              backgroundColor: '#0D5CAB',
              '&:hover': { backgroundColor: '#0A4A89' },
              textTransform: 'none',
              minWidth: '120px',
              borderRadius: '4px',
            }}
          >
            Add Student
          </Button>
          <Button
            variant="contained"
            onClick={handleOpenSearch}
            sx={{
              backgroundColor: '#0D5CAB',
              '&:hover': { backgroundColor: '#0A4A89' },
              textTransform: 'none',
              minWidth: '120px',
              borderRadius: '4px',
            }}
          >
            Search Students
          </Button>
        </Box>
      </Box>

      {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}

      <AddStudentModal
        open={openAddModal}
        onClose={handleCloseAdd}
        onSubmit={handleAddStudent}
        data={newStudent}
        onChange={handleAddInputChange}
      />

      <EditStudentModal
        open={openEditModal}
        onClose={handleCloseEdit}
        onSubmit={handleConfirmEdit}
        data={editingStudent}
        onChange={handleEditInputChange}
      />

      <SearchStudentsModal
        open={openSearchModal}
        onClose={handleCloseSearch}
        onSelectStudent={handleSelectStudent}
        students={students}
      />

      <Modal open={openConfirmEditModal} onClose={() => setOpenConfirmEditModal(false)}>
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
            <Typography variant="h6">Confirm Edit</Typography>
            <IconButton onClick={() => setOpenConfirmEditModal(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          <Typography sx={{ mb: 3 }}>
            Are you sure you want to edit this student's detail?
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleFinalEdit}
              sx={{ backgroundColor: '#4CAF50', '&:hover': { backgroundColor: '#45a049' }, textTransform: 'none' }}
            >
              Confirm
            </Button>
            <Button
              variant="contained"
              onClick={() => setOpenConfirmEditModal(false)}
              sx={{ backgroundColor: '#9e9e9e', '&:hover': { backgroundColor: '#757575' }, textTransform: 'none' }}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>

      <Modal open={openConfirmArchiveModal} onClose={() => setOpenConfirmArchiveModal(false)}>
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
            <Typography variant="h6">Move to Archive</Typography>
            <IconButton onClick={() => setOpenConfirmArchiveModal(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          <Typography sx={{ mb: 3 }}>
            Are you sure you want to move this student to the Archive?
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleFinalArchive}
              sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' }, textTransform: 'none' }}
            >
              Confirm
            </Button>
            <Button
              variant="contained"
              onClick={() => setOpenConfirmArchiveModal(false)}
              sx={{ backgroundColor: '#9e9e9e', '&:hover': { backgroundColor: '#757575' }, textTransform: 'none' }}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>

      <Box sx={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden', width: '100%', minWidth: '1000px' }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: '3fr 1fr 1fr 2fr', backgroundColor: '#f8f9fa', padding: '16px 24px', borderBottom: '1px solid #e0e0e0' }}>
          <Typography fontWeight="bold">Students</Typography>
          <Typography fontWeight="bold">Section</Typography>
          <Typography fontWeight="bold">Grade Level</Typography>
          <Typography fontWeight="bold">Actions</Typography>
        </Box>

        {/* Male Students Section */}
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
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleOpenEdit(student)}
                    sx={{ backgroundColor: '#4CAF50', '&:hover': { backgroundColor: '#45a049' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleArchive(student)}
                    sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Archive
                  </Button>
                </Box>
              </Box>
            ))}
          </>
        )}

        {/* Female Students Section */}
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
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleOpenEdit(student)}
                    sx={{ backgroundColor: '#4CAF50', '&:hover': { backgroundColor: '#45a049' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleArchive(student)}
                    sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' }, minWidth: '80px', textTransform: 'none' }}
                  >
                    Archive
                  </Button>
                </Box>
              </Box>
            ))}
          </>
        )}

        {/* Display message if no students in the section */}
        {maleStudents.length === 0 && femaleStudents.length === 0 && (
          <Box sx={{ padding: '16px 24px', textAlign: 'center' }}>
            <Typography>No students found in this section.</Typography>
          </Box>
        )}
      </Box>
    </Box>
  );
}

export default Students;