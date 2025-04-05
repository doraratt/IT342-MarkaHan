import { useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';

function Students() {
  const [selectedSection, setSelectedSection] = useState('G1');
  const [openAddModal, setOpenAddModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [openConfirmEditModal, setOpenConfirmEditModal] = useState(false);
  const [openConfirmArchiveModal, setOpenConfirmArchiveModal] = useState(false);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [newStudent, setNewStudent] = useState({
    firstName: '',
    lastName: '',
    section: '',
    gradeLevel: ''
  });
  const [editingStudent, setEditingStudent] = useState({
    firstName: '',
    lastName: '',
    section: '',
    gradeLevel: ''
  });

  const handleOpenAdd = () => setOpenAddModal(true);
  const handleCloseAdd = () => setOpenAddModal(false);
  const handleOpenEdit = (student) => {
    // Split the name into first and last name
    const [firstName, lastName] = student.name.split(' ');
    setEditingStudent({
      firstName,
      lastName,
      section: student.section,
      gradeLevel: student.gradeLevel
    });
    setOpenEditModal(true);
  };
  const handleCloseEdit = () => setOpenEditModal(false);

  const handleInputChange = (e, isEdit = false) => {
    const { name, value } = e.target;
    if (isEdit) {
      setEditingStudent(prev => ({
        ...prev,
        [name]: value
      }));
    } else {
      setNewStudent(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleAddStudent = () => {
    // Add student logic here
    console.log('New student:', newStudent);
    handleCloseAdd();
    setNewStudent({
      firstName: '',
      lastName: '',
      section: '',
      gradeLevel: ''
    });
  };

  const handleConfirmEdit = () => {
    setOpenConfirmEditModal(true);
    setOpenEditModal(false);
  };

  const handleFinalEdit = () => {
    console.log('Final edit confirmed:', editingStudent);
    setOpenConfirmEditModal(false);
    setEditingStudent({
      firstName: '',
      lastName: '',
      section: '',
      gradeLevel: ''
    });
  };

  const handleArchive = (student) => {
    setSelectedStudent(student);
    setOpenConfirmArchiveModal(true);
  };

  const handleFinalArchive = () => {
    console.log('Archive confirmed for:', selectedStudent);
    setOpenConfirmArchiveModal(false);
    setSelectedStudent(null);
  };

  // Sample student data - replace with your actual data source later
  const students = [
    { id: 1, name: 'Gaylord Tuwid', section: 'G1', gradeLevel: 4 },
    { id: 2, name: 'Kally Vhangon', section: 'G1', gradeLevel: 4 },
    { id: 3, name: 'Bugart Batongbakal Jr.', section: 'G1', gradeLevel: 4 },
    { id: 4, name: 'Wy Lee Guo', section: 'G1', gradeLevel: 4 },
    { id: 5, name: 'Raoul Philipi', section: 'G1', gradeLevel: 4 },
    { id: 6, name: 'Balmond Alucard', section: 'G1', gradeLevel: 4 },
    { id: 7, name: 'Bessie Cooper', section: 'G1', gradeLevel: 4 },
  ];

  const sections = ['G1', 'G2', 'G3', 'G4', 'G5'];

  // Modal content component to reduce duplication
  const ModalContent = ({ title, data, onChange, onSubmit, isEdit }) => (
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
        <Typography variant="h6">{title}</Typography>
        <IconButton onClick={isEdit ? handleCloseEdit : handleCloseAdd} size="small">
          <CloseIcon />
        </IconButton>
      </Box>
      
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <Box>
          <Typography variant="body2" sx={{ mb: 1 }}>First Name</Typography>
          <TextField
            name="firstName"
            value={data.firstName}
            onChange={(e) => onChange(e, isEdit)}
            fullWidth
            size="small"
          />
        </Box>
        <Box>
          <Typography variant="body2" sx={{ mb: 1 }}>Last Name</Typography>
          <TextField
            name="lastName"
            value={data.lastName}
            onChange={(e) => onChange(e, isEdit)}
            fullWidth
            size="small"
          />
        </Box>
        <Box>
          <Typography variant="body2" sx={{ mb: 1 }}>Section</Typography>
          <TextField
            name="section"
            value={data.section}
            onChange={(e) => onChange(e, isEdit)}
            fullWidth
            size="small"
          />
        </Box>
        <Box>
          <Typography variant="body2" sx={{ mb: 1 }}>Grade Level</Typography>
          <TextField
            name="gradeLevel"
            value={data.gradeLevel}
            onChange={(e) => onChange(e, isEdit)}
            fullWidth
            size="small"
          />
        </Box>
        <Button
          variant="contained"
          onClick={onSubmit}
          sx={{
            backgroundColor: '#0D5CAB',
            '&:hover': {
              backgroundColor: '#0A4A89',
            },
            textTransform: 'none',
            mt: 2
          }}
        >
          {isEdit ? 'Apply Edit' : 'Add Student'}
        </Button>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ 
      width: '95%',
      p: 3
    }}>
      {/* Header Container */}
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'space-between',
        alignItems: 'flex-end',
        mb: 2
      }}>
        {/* Left side - Sections */}
        <Box>
          <Typography sx={{ mb: 2 }}>Sections</Typography>
          <Box sx={{ display: 'flex', gap: '2px', width: 'fit-content' }}>
            {sections.map((section) => (
              <Button
                key={section}
                onClick={() => setSelectedSection(section)}
                sx={{
                  minWidth: '80px',
                  backgroundColor: selectedSection === section ? '#1f295a' : '#fff',
                  color: selectedSection === section ? '#fff' : '#000',
                  borderRadius: 0,
                  px: 3,
                  '&:hover': {
                    backgroundColor: selectedSection === section ? '#1f295a' : '#f5f5f5',
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
        </Box>

        {/* Right side - Action Buttons */}
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            onClick={handleOpenAdd}
            sx={{
              backgroundColor: '#0D5CAB',
              '&:hover': {
                backgroundColor: '#0A4A89',
              },
              textTransform: 'none',
              minWidth: '120px',
              borderRadius: '4px',
            }}
          >
            Add Student
          </Button>
          <Button
            variant="contained"
            sx={{
              backgroundColor: '#0D5CAB',
              '&:hover': {
                backgroundColor: '#0A4A89',
              },
              textTransform: 'none',
              minWidth: '120px',
              borderRadius: '4px',
            }}
          >
            Filter List
          </Button>
        </Box>
      </Box>

      {/* Add Student Modal */}
      <Modal open={openAddModal} onClose={handleCloseAdd}>
        <ModalContent 
          title="Add Student"
          data={newStudent}
          onChange={handleInputChange}
          onSubmit={handleAddStudent}
          isEdit={false}
        />
      </Modal>

      {/* Edit Student Modal */}
      <Modal open={openEditModal} onClose={handleCloseEdit}>
        <ModalContent 
          title="Edit Student"
          data={editingStudent}
          onChange={handleInputChange}
          onSubmit={handleConfirmEdit}
          isEdit={true}
        />
      </Modal>

      {/* Confirm Edit Modal */}
      <Modal 
        open={openConfirmEditModal} 
        onClose={() => setOpenConfirmEditModal(false)}
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
              sx={{
                backgroundColor: '#4CAF50',
                '&:hover': { backgroundColor: '#45a049' },
                textTransform: 'none',
              }}
            >
              Confirm
            </Button>
            <Button
              variant="contained"
              onClick={() => setOpenConfirmEditModal(false)}
              sx={{
                backgroundColor: '#9e9e9e',
                '&:hover': { backgroundColor: '#757575' },
                textTransform: 'none',
              }}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>

      {/* Confirm Archive Modal */}
      <Modal 
        open={openConfirmArchiveModal} 
        onClose={() => setOpenConfirmArchiveModal(false)}
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
            <Typography variant="h6">Move to Archive</Typography>
            <IconButton onClick={() => setOpenConfirmArchiveModal(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
          
          <Typography sx={{ mb: 3 }}>
            Are you sure you want to move this student in the Archive?
          </Typography>

          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleFinalArchive}
              sx={{
                backgroundColor: '#f44336',
                '&:hover': { backgroundColor: '#d32f2f' },
                textTransform: 'none',
              }}
            >
              Confirm
            </Button>
            <Button
              variant="contained"
              onClick={() => setOpenConfirmArchiveModal(false)}
              sx={{
                backgroundColor: '#9e9e9e',
                '&:hover': { backgroundColor: '#757575' },
                textTransform: 'none',
              }}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>

      {/* Table Container */}
      <Box sx={{ 
        backgroundColor: 'white',
        borderRadius: '8px',
        overflow: 'hidden',
        width: '100%',
        minWidth: '1000px',
      }}>
        {/* Table Header */}
        <Box sx={{ 
          display: 'grid',
          gridTemplateColumns: '3fr 1fr 1fr 2fr',
          backgroundColor: '#f8f9fa',
          padding: '16px 24px',
          borderBottom: '1px solid #e0e0e0',
        }}>
          <Typography fontWeight="bold">Students</Typography>
          <Typography fontWeight="bold">Section</Typography>
          <Typography fontWeight="bold">Grade Level</Typography>
          <Typography fontWeight="bold">Actions</Typography>
        </Box>

        {/* Students List */}
        {students.map((student, index) => (
          <Box
            key={student.id}
            sx={{
              display: 'grid',
              gridTemplateColumns: '3fr 1fr 1fr 2fr',
              padding: '16px 24px',
              backgroundColor: index % 2 === 0 ? '#f8f9fa' : 'white',
              borderBottom: '1px solid #e0e0e0',
              '&:last-child': {
                borderBottom: 'none',
              },
            }}
          >
            <Typography>{student.name}</Typography>
            <Typography>{student.section}</Typography>
            <Typography>{student.gradeLevel}</Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                size="small"
                onClick={() => {
                  handleOpenEdit(student);
                }}
                sx={{
                  backgroundColor: '#4CAF50',
                  '&:hover': { backgroundColor: '#45a049' },
                  minWidth: '80px',
                  textTransform: 'none',
                }}
              >
                Edit
              </Button>
              <Button
                variant="contained"
                size="small"
                onClick={() => handleArchive(student)}
                sx={{
                  backgroundColor: '#f44336',
                  '&:hover': { backgroundColor: '#d32f2f' },
                  minWidth: '80px',
                  textTransform: 'none',
                }}
              >
                Archive
              </Button>
            </Box>
          </Box>
        ))}
      </Box>
    </Box>
  );
}

export default Students;
