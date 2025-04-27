import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import MenuItem from '@mui/material/MenuItem'; // Add for dropdown
import Select from '@mui/material/Select'; // Add for dropdown
import FormControl from '@mui/material/FormControl'; // Add for dropdown
import InputLabel from '@mui/material/InputLabel'; // Add for dropdown
import axios from 'axios';
import { useUser } from '../UserContext';
import { Navigate } from 'react-router-dom';

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
        <TextField
          name="gradeLevel"
          label="Grade Level"
          value={data.gradeLevel || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter grade level"
          required
          variant="outlined"
          autoComplete="off"
        />
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
        <TextField
          name="gradeLevel"
          label="Grade Level"
          value={data.gradeLevel || ''}
          onChange={onChange}
          fullWidth
          size="small"
          placeholder="Enter grade level"
          required
          variant="outlined"
          autoComplete="off"
        />
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

// SearchStudentsModal (unchanged)
const SearchStudentsModal = ({ open, onClose, students, onSelectStudent }) => {
  const [searchTerm, setSearchTerm] = useState('');
  
  const filteredStudents = students.filter(student => 
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
    gender: '', // Added gender field
    section: '',
    gradeLevel: ''
  });
  const [editingStudent, setEditingStudent] = useState({
    id: null,
    firstName: '',
    lastName: '',
    gender: '', // Added gender field
    section: '',
    gradeLevel: ''
  });
  const [error, setError] = useState('');

  if (!user) {
    return <Navigate to="/404" replace />;
  }

  useEffect(() => {
    if (user) {
      const fetchStudents = async () => {
        try {
          const response = await axios.get(`http://localhost:8080/api/student/getStudentsByUser?userId=${user.userId}`);
          setStudents(response.data);
          const uniqueSections = [...new Set(response.data.map(s => s.section))].sort();
          setSelectedSection(uniqueSections[0] || '');
        } catch (error) {
          console.error('Error fetching students:', error.response?.data || error.message);
        }
      };

      fetchStudents();
    } else {
      console.log('No user logged in');
    }
  }, [user]);

  const handleOpenAdd = () => setOpenAddModal(true);
  const handleCloseAdd = () => {
    setOpenAddModal(false);
    setNewStudent({ firstName: '', lastName: '', gender: '', section: '', gradeLevel: '' });
  };
  const handleOpenEdit = (student) => {
    setEditingStudent({
      id: student.studentId,
      firstName: student.firstName,
      lastName: student.lastName,
      gender: student.gender, // Include gender
      section: student.section,
      gradeLevel: student.gradeLevel
    });
    setOpenEditModal(true);
  };
  const handleCloseEdit = () => {
    setOpenEditModal(false);
    setEditingStudent({ id: null, firstName: '', lastName: '', gender: '', section: '', gradeLevel: '' });
  };
  const handleOpenSearch = () => setOpenSearchModal(true);
  const handleCloseSearch = () => setOpenSearchModal(false);

  const handleAddStudent = async () => {
    if (!newStudent.firstName || !newStudent.lastName || !newStudent.gender || !newStudent.section || !newStudent.gradeLevel) {
      setError('All fields are required to add a student');
      return;
    }
    if (!user) {
      setError('User not logged in');
      return;
    }

    const studentData = { ...newStudent, user: { userId: user.userId } };
    try {
      console.log('Adding student:', studentData);
      const response = await axios.post('http://localhost:8080/api/student/add', studentData);
      setStudents(prevStudents => [...prevStudents, response.data]);
      const uniqueSections = [...new Set([...students, response.data].map(s => s.section))].sort();
      if (!selectedSection || !students.some(s => s.section === selectedSection)) {
        setSelectedSection(uniqueSections[0] || '');
      }
      handleCloseAdd();
      setError('');
    } catch (error) {
      setError('Error adding student: ' + (error.response?.data?.message || error.response?.data || error.message));
      console.error('Add error:', error.response?.data, error);
    }
  };

  const handleConfirmEdit = () => {
    setOpenConfirmEditModal(true);
    setOpenEditModal(false);
  };

  const handleFinalEdit = async () => {
    if (!editingStudent.id || !editingStudent.firstName || !editingStudent.lastName || !editingStudent.gender || !editingStudent.section || !editingStudent.gradeLevel) {
      setError('All fields are required to update a student');
      return;
    }
    if (!user) {
      setError('User not logged in');
      return;
    }

    const studentData = { ...editingStudent, user: { userId: user.userId } };
    try {
      const response = await axios.put(
        `http://localhost:8080/api/student/update/${editingStudent.id}`,
        studentData
      );
      setStudents(prevStudents =>
        prevStudents.map(student => (student.studentId === editingStudent.id ? response.data : student))
      );
      const updatedStudents = students.map(student => 
        student.studentId === editingStudent.id ? response.data : student
      );
      const uniqueSections = [...new Set(updatedStudents.map(s => s.section))].sort();
      if (!updatedStudents.some(s => s.section === selectedSection)) {
        setSelectedSection(uniqueSections[0] || '');
      }
      setOpenConfirmEditModal(false);
      setEditingStudent({ id: null, firstName: '', lastName: '', gender: '', section: '', gradeLevel: '' });
      setError('');
    } catch (error) {
      setError('Error updating student: ' + (error.response?.data?.message || error.response?.data || error.message));
      console.error('Edit error:', error.response?.data, error);
    }
  };

  const handleArchive = (student) => {
    setSelectedStudent(student);
    setOpenConfirmArchiveModal(true);
  };

  const handleFinalArchive = async () => {
    if (!selectedStudent?.studentId) return;
    try {
      await axios.delete(`http://localhost:8080/api/student/delete/${selectedStudent.studentId}`);
      const updatedStudents = students.filter(student => student.studentId !== selectedStudent.studentId);
      setStudents(updatedStudents);
      const uniqueSections = [...new Set(updatedStudents.map(s => s.section))].sort();
      if (!updatedStudents.some(s => s.section === selectedSection)) {
        setSelectedSection(uniqueSections[0] || '');
      }
      setOpenConfirmArchiveModal(false);
      setSelectedStudent(null);
      setError('');
    } catch (error) {
      setError('Error archiving student: ' + (error.response?.data || error.message));
      console.error('Archive error:', error);
    }
  };

  const handleSelectStudent = (student) => {
    setSelectedSection(student.section);
    handleCloseSearch();
  };

  const sections = [...new Set(students.map(student => student.section))].sort();

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

  // Filter and sort students by section and gender
  const filteredStudents = students.filter(student => student.section === selectedSection);
  const maleStudents = filteredStudents
    .filter(student => student.gender === 'Male')
    .sort((a, b) => {
      const nameA = `${a.lastName}, ${a.firstName}`.toLowerCase();
      const nameB = `${b.lastName}, ${b.firstName}`.toLowerCase();
      return nameA.localeCompare(nameB);
    });
  const femaleStudents = filteredStudents
    .filter(student => student.gender === 'Female')
    .sort((a, b) => {
      const nameA = `${a.lastName}, ${a.firstName}`.toLowerCase();
      const nameB = `${b.lastName}, ${b.firstName}`.toLowerCase();
      return nameA.localeCompare(nameB);
    });

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
        students={students}
        onSelectStudent={handleSelectStudent}
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