import { useEffect, useState } from "react";
import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    MenuItem,
    Paper,
    Snackbar,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    TextField,
    Typography,
} from "@mui/material";

import {
    Add,
    Delete,
    Edit,
    Search,
} from "@mui/icons-material";

import {
    createRadiologist,
    deleteRadiologist,
    getRadiologists,
    updateRadiologist,
    type Radiologist,
    type RadiologistRequest,
} from "../services/radiologistService";

import "./Radiologists.css";

interface FormData {
    name: string;
    email: string;
    phone: string;
    specialization: string;
    active: boolean;
}

const initialFormData: FormData = {
    name: "",
    email: "",
    phone: "",
    specialization: "",
    active: true,
};

function Radiologists() {
    const [radiologists, setRadiologists] = useState<Radiologist[]>([]);
    const [loading, setLoading] = useState(true);

    const [search, setSearch] = useState("");
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [totalElements, setTotalElements] = useState(0);

    const [sortBy, setSortBy] = useState("name");
    const [direction, setDirection] = useState<"asc" | "desc">("asc");

    const [dialogOpen, setDialogOpen] = useState(false);
    const [editingRadiologist, setEditingRadiologist] =
        useState<Radiologist | null>(null);

    const [formData, setFormData] =
        useState<FormData>(initialFormData);

    const [formErrors, setFormErrors] = useState<
        Record<string, string>
    >({});

    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [radiologistToDelete, setRadiologistToDelete] =
        useState<Radiologist | null>(null);

    const [saving, setSaving] = useState(false);
    const [deleting, setDeleting] = useState(false);

    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const loadRadiologists = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getRadiologists(
                search,
                page,
                rowsPerPage,
                sortBy,
                direction
            );

            setRadiologists(data.content);
            setTotalElements(data.totalElements);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Failed to load radiologists"
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRadiologists();
    }, [page, rowsPerPage, sortBy, direction]);

    const handleSearch = () => {
        setPage(0);
        loadRadiologists();
    };

    const openAddDialog = () => {
        setEditingRadiologist(null);
        setFormData(initialFormData);
        setFormErrors({});
        setDialogOpen(true);
    };

    const openEditDialog = (radiologist: Radiologist) => {
        setEditingRadiologist(radiologist);

        setFormData({
            name: radiologist.name,
            email: radiologist.email,
            phone: radiologist.phone || "",
            specialization: radiologist.specialization || "",
            active: radiologist.active,
        });

        setFormErrors({});
        setDialogOpen(true);
    };

    const closeDialog = () => {
        if (saving) return;

        setDialogOpen(false);
        setEditingRadiologist(null);
        setFormData(initialFormData);
        setFormErrors({});
    };

    const handleChange = (
        field: keyof FormData,
        value: string | boolean
    ) => {
        setFormData((previous) => ({
            ...previous,
            [field]: value,
        }));

        setFormErrors((previous) => ({
            ...previous,
            [field]: "",
        }));
    };

    const validateForm = (): boolean => {
        const errors: Record<string, string> = {};

        if (!formData.name.trim()) {
            errors.name = "Radiologist name is required";
        } else if (formData.name.trim().length > 150) {
            errors.name = "Name cannot exceed 150 characters";
        }

        if (!formData.email.trim()) {
            errors.email = "Email is required";
        } else if (
            !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())
        ) {
            errors.email = "Email must be valid";
        }

        if (formData.phone.length > 20) {
            errors.phone = "Phone cannot exceed 20 characters";
        }

        if (formData.specialization.length > 150) {
            errors.specialization =
                "Specialization cannot exceed 150 characters";
        }

        setFormErrors(errors);

        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async () => {
        if (!validateForm()) {
            return;
        }

        const request: RadiologistRequest = {
            name: formData.name.trim(),
            email: formData.email.trim(),
            phone: formData.phone.trim() || undefined,
            specialization:
                formData.specialization.trim() || undefined,
            active: formData.active,
        };

        try {
            setSaving(true);
            setError("");

            if (editingRadiologist) {
                await updateRadiologist(
                    editingRadiologist.id,
                    request
                );

                setSuccessMessage(
                    "Radiologist updated successfully"
                );
            } else {
                await createRadiologist(request);

                setSuccessMessage(
                    "Radiologist created successfully"
                );
            }

            closeDialog();
            await loadRadiologists();
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Failed to save radiologist"
            );
        } finally {
            setSaving(false);
        }
    };

    const openDeleteDialog = (radiologist: Radiologist) => {
        setRadiologistToDelete(radiologist);
        setDeleteDialogOpen(true);
    };

    const closeDeleteDialog = () => {
        if (deleting) return;

        setDeleteDialogOpen(false);
        setRadiologistToDelete(null);
    };

    const handleDelete = async () => {
        if (!radiologistToDelete) return;

        try {
            setDeleting(true);
            setError("");

            await deleteRadiologist(radiologistToDelete.id);

            setSuccessMessage(
                "Radiologist deleted successfully"
            );

            closeDeleteDialog();

            if (radiologists.length === 1 && page > 0) {
                setPage((previous) => previous - 1);
            } else {
                await loadRadiologists();
            }
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Failed to delete radiologist"
            );
        } finally {
            setDeleting(false);
        }
    };

    const handleSort = (field: string) => {
        if (sortBy === field) {
            setDirection((previous) =>
                previous === "asc" ? "desc" : "asc"
            );
        } else {
            setSortBy(field);
            setDirection("asc");
        }

        setPage(0);
    };

    return (
        <Box className="radiologists-page">
            {/* Header */}
            <Box className="radiologists-header">
                <Box>
                    <Typography className="radiologists-title">
                        Radiologists
                    </Typography>

                    <Typography className="radiologists-subtitle">
                        Manage radiologists and their specializations.
                    </Typography>
                </Box>

                <Button
                    className="radiologists-add-button"
                    variant="contained"
                    startIcon={<Add />}
                    onClick={openAddDialog}
                >
                    Add Radiologist
                </Button>
            </Box>

            {/* Search */}
            <Paper className="radiologists-card">
                <Box className="radiologists-toolbar">
                    <TextField
                        className="radiologists-search"
                        size="small"
                        placeholder="Search radiologists"
                        value={search}
                        onChange={(event) =>
                            setSearch(event.target.value)
                        }
                        onKeyDown={(event) => {
                            if (event.key === "Enter") {
                                handleSearch();
                            }
                        }}
                    />

                    <Button
                        className="radiologists-search-button"
                        variant="contained"
                        onClick={handleSearch}
                    >
                        <Search fontSize="small" />
                    </Button>
                </Box>
            </Paper>

            {/* Error */}
            {error && (
                <Alert
                    severity="error"
                    sx={{ mb: 2 }}
                    onClose={() => setError("")}
                >
                    {error}
                </Alert>
            )}

            {/* Table */}
            <Paper className="radiologists-card">
                <TableContainer>
                    <Table className="radiologists-table">
                        <TableHead>
                            <TableRow>
                                <TableCell
                                    sx={{ cursor: "pointer" }}
                                    onClick={() => handleSort("name")}
                                >
                                    <strong>Name</strong>
                                </TableCell>

                                <TableCell
                                    sx={{ cursor: "pointer" }}
                                    onClick={() => handleSort("email")}
                                >
                                    <strong>Email</strong>
                                </TableCell>

                                <TableCell>
                                    <strong>Phone</strong>
                                </TableCell>

                                <TableCell
                                    sx={{ cursor: "pointer" }}
                                    onClick={() =>
                                        handleSort("specialization")
                                    }
                                >
                                    <strong>Specialization</strong>
                                </TableCell>

                                <TableCell
                                    sx={{ cursor: "pointer" }}
                                    onClick={() => handleSort("active")}
                                >
                                    <strong>Status</strong>
                                </TableCell>

                                <TableCell align="right">
                                    <strong>Actions</strong>
                                </TableCell>
                            </TableRow>
                        </TableHead>

                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell
                                        colSpan={6}
                                        align="center"
                                        sx={{ py: 5 }}
                                    >
                                        <CircularProgress />
                                    </TableCell>
                                </TableRow>
                            ) : radiologists.length === 0 ? (
                                <TableRow>
                                    <TableCell
                                        colSpan={6}
                                        align="center"
                                        sx={{ py: 5 }}
                                    >
                                        <Typography color="text.secondary">
                                            No radiologists found
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                radiologists.map((radiologist) => (
                                    <TableRow key={radiologist.id} hover>
                                        <TableCell>
                                            <Typography className="radiologist-name">
                                                {radiologist.name}
                                            </Typography>
                                        </TableCell>

                                        <TableCell>
                                            <Typography className="radiologist-email">
                                                {radiologist.email}
                                            </Typography>
                                        </TableCell>

                                        <TableCell>
                                            <Typography className="radiologist-secondary">
                                                {radiologist.phone || "-"}
                                            </Typography>
                                        </TableCell>

                                        <TableCell>
                                            <Typography className="radiologist-secondary">
                                                {radiologist.specialization || "-"}
                                            </Typography>
                                        </TableCell>

                                        <TableCell>
                                            <Chip
                                                className={`radiologist-status ${radiologist.active
                                                    ? "radiologist-status-active"
                                                    : "radiologist-status-inactive"
                                                    }`}
                                                label={
                                                    radiologist.active
                                                        ? "Active"
                                                        : "Inactive"
                                                }
                                                size="small"
                                            />
                                        </TableCell>

                                        <TableCell align="right">
                                            <IconButton
                                                className="radiologist-action-button"
                                                color="primary"
                                                onClick={() =>
                                                    openEditDialog(radiologist)
                                                }
                                            >
                                                <Edit />
                                            </IconButton>

                                            <IconButton
                                                className="radiologist-action-button"
                                                color="error"
                                                onClick={() =>
                                                    openDeleteDialog(radiologist)
                                                }
                                            >
                                                <Delete />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>

                <TablePagination
                    className="radiologists-pagination"
                    component="div"
                    count={totalElements}
                    page={page}
                    onPageChange={(_, newPage) =>
                        setPage(newPage)
                    }
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={(event) => {
                        setRowsPerPage(
                            Number(event.target.value)
                        );
                        setPage(0);
                    }}
                    rowsPerPageOptions={[5, 10, 25, 50]}
                />
            </Paper>

            {/* Add/Edit Dialog */}
            <Dialog
                className="radiologist-dialog"
                open={dialogOpen}
                onClose={closeDialog}
                fullWidth
                maxWidth="sm"
            >
                <DialogTitle className="radiologist-dialog-title">
                    {editingRadiologist
                        ? "Edit Radiologist"
                        : "Add Radiologist"}
                </DialogTitle>

                <DialogContent className="radiologist-dialog-content">
                    <TextField
                        fullWidth
                        required
                        label="Name"
                        value={formData.name}
                        onChange={(event) =>
                            handleChange("name", event.target.value)
                        }
                        error={Boolean(formErrors.name)}
                        helperText={formErrors.name}
                        margin="normal"
                        slotProps={{
                            htmlInput: {
                                maxLength: 150,
                            },
                        }}
                    />

                    <TextField
                        fullWidth
                        required
                        label="Email"
                        type="email"
                        value={formData.email}
                        onChange={(event) =>
                            handleChange("email", event.target.value)
                        }
                        error={Boolean(formErrors.email)}
                        helperText={formErrors.email}
                        margin="normal"
                        slotProps={{
                            htmlInput: {
                                maxLength: 150,
                            },
                        }}
                    />

                    <TextField
                        fullWidth
                        label="Phone"
                        value={formData.phone}
                        onChange={(event) =>
                            handleChange("phone", event.target.value)
                        }
                        error={Boolean(formErrors.phone)}
                        helperText={formErrors.phone}
                        margin="normal"
                        slotProps={{
                            htmlInput: {
                                maxLength: 20,
                            },
                        }}
                    />

                    <TextField
                        fullWidth
                        label="Specialization"
                        value={formData.specialization}
                        onChange={(event) =>
                            handleChange(
                                "specialization",
                                event.target.value
                            )
                        }
                        error={Boolean(formErrors.specialization)}
                        helperText={formErrors.specialization}
                        margin="normal"
                        slotProps={{
                            htmlInput: {
                                maxLength: 150,
                            },
                        }}
                    />

                    <TextField
                        select
                        fullWidth
                        label="Status"
                        value={formData.active ? "true" : "false"}
                        onChange={(event) =>
                            handleChange(
                                "active",
                                event.target.value === "true"
                            )
                        }
                        margin="normal"
                    >
                        <MenuItem value="true">Active</MenuItem>
                        <MenuItem value="false">Inactive</MenuItem>
                    </TextField>
                </DialogContent>

                <DialogActions className="radiologist-dialog-actions">
                    <Button
                        onClick={closeDialog}
                        disabled={saving}
                    >
                        Cancel
                    </Button>

                    <Button
                        variant="contained"
                        onClick={handleSubmit}
                        disabled={saving}
                    >
                        {saving ? (
                            <CircularProgress size={22} />
                        ) : editingRadiologist ? (
                            "Update"
                        ) : (
                            "Create"
                        )}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Confirmation */}
            <Dialog
              className="radiologist-dialog"
                open={deleteDialogOpen}
                onClose={closeDeleteDialog}
            >
                <DialogTitle>
                    Delete Radiologist
                </DialogTitle>

                <DialogContent>
                    <Typography>
                        Are you sure you want to delete{" "}
                        <strong>
                            {radiologistToDelete?.name}
                        </strong>
                        ?
                    </Typography>
                </DialogContent>

                <DialogActions>
                    <Button
                        onClick={closeDeleteDialog}
                        disabled={deleting}
                    >
                        Cancel
                    </Button>

                    <Button
                        color="error"
                        variant="contained"
                        onClick={handleDelete}
                        disabled={deleting}
                    >
                        {deleting ? (
                            <CircularProgress size={22} />
                        ) : (
                            "Delete"
                        )}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Success Snackbar */}
            <Snackbar
                open={Boolean(successMessage)}
                autoHideDuration={3000}
                onClose={() => setSuccessMessage("")}
                message={successMessage}
            />
        </Box>
    );
}

export default Radiologists;