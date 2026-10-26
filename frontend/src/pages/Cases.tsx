import { useCallback, useEffect, useState } from "react";
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
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Stack,
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
import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import {
    createCase,
    deleteCase,
    getCases,
    updateCase,
    type CaseRecord,
    type CaseRequest,
    type CaseStatus,
} from "../services/caseService";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

interface Option {
    id: number;
    name: string;
    active?: boolean;
}

const statuses: CaseStatus[] = [
    "INCOMPLETE",
    "REPORT_NOT_FOUND",
    "REPORT_NOT_VERIFIED",
    "REPORT_COMPLETED",
    "REPORT_SAVED",
];

const emptyForm: CaseRequest = {
    accessionNumber: "",
    patientName: "",
    patientDob: null,
    patientGender: "",
    history: "",
    technique: "",
    bodyPartId: 0,
    modality: "",
    radiologistId: 0,
    status: "INCOMPLETE",
    report: "",
};

function statusLabel(status: CaseStatus) {
    return status.replaceAll("_", " ");
}

function statusColor(
    status: CaseStatus,
): "default" | "warning" | "info" | "success" {
    switch (status) {
        case "REPORT_COMPLETED":
        case "REPORT_SAVED":
            return "success";
        case "REPORT_NOT_FOUND":
            return "warning";
        case "REPORT_NOT_VERIFIED":
            return "info";
        default:
            return "default";
    }
}

export default function Cases() {
    const [cases, setCases] = useState<CaseRecord[]>([]);
    const [bodyParts, setBodyParts] = useState<Option[]>([]);
    const [radiologists, setRadiologists] = useState<Option[]>([]);
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("");
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [total, setTotal] = useState(0);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [dialogOpen, setDialogOpen] = useState(false);
    const [editing, setEditing] = useState<CaseRecord | null>(null);
    const [form, setForm] = useState<CaseRequest>(emptyForm);

    const [deleteTarget, setDeleteTarget] = useState<CaseRecord | null>(null);

    const loadCases = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const result = await getCases({
                search,
                status: statusFilter,
                page,
                size: rowsPerPage,
                sortBy: "createdAt",
                direction: "desc",
            });

            setCases(result.content);
            setTotal(result.totalElements);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to load cases");
        } finally {
            setLoading(false);
        }
    }, [search, statusFilter, page, rowsPerPage]);

    useEffect(() => {
        void loadCases();
    }, [loadCases]);

    useEffect(() => {
        async function loadOptions() {
            try {
                const [bodyResponse, radiologistResponse] = await Promise.all([
                    fetch(`${API_BASE}/body-parts?size=100&sortBy=name&direction=asc`),
                    fetch(`${API_BASE}/radiologists?size=100&sortBy=name&direction=asc`),
                ]);

                if (!bodyResponse.ok || !radiologistResponse.ok) {
                    throw new Error("Could not load body parts or radiologists");
                }

                const [bodyData, radiologistData] = await Promise.all([
                    bodyResponse.json(),
                    radiologistResponse.json(),
                ]);

                setBodyParts(bodyData.content ?? []);
                setRadiologists(radiologistData.content ?? []);
            } catch (err) {
                setError(
                    err instanceof Error ? err.message : "Failed to load dropdown data",
                );
            }
        }

        void loadOptions();
    }, []);

    function openCreateDialog() {
        setEditing(null);
        setForm({ ...emptyForm });
        setError("");
        setDialogOpen(true);
    }

    function openEditDialog(item: CaseRecord) {
        setEditing(item);
        setForm({
            accessionNumber: item.accessionNumber,
            patientName: item.patientName,
            patientDob: item.patientDob,
            patientGender: item.patientGender ?? "",
            history: item.history ?? "",
            technique: item.technique ?? "",
            bodyPartId: item.bodyPartId,
            modality: item.modality ?? "",
            radiologistId: item.radiologistId,
            status: item.status,
            report: item.report ?? "",
        });
        setError("");
        setDialogOpen(true);
    }

    function setField<K extends keyof CaseRequest>(
        field: K,
        value: CaseRequest[K],
    ) {
        setForm((previous) => ({ ...previous, [field]: value }));
    }

    async function handleSave() {
        if (
            !form.accessionNumber.trim() ||
            !form.patientName.trim() ||
            !form.bodyPartId ||
            !form.radiologistId
        ) {
            setError(
                "Accession number, patient name, body part and radiologist are required.",
            );
            return;
        }

        setSaving(true);
        setError("");
        setSuccess("");

        const payload: CaseRequest = {
            ...form,
            accessionNumber: form.accessionNumber.trim(),
            patientName: form.patientName.trim(),
            patientDob: form.patientDob || null,
        };

        try {
            if (editing) {
                await updateCase(editing.id, payload);
                setSuccess("Case updated successfully.");
            } else {
                await createCase(payload);
                setSuccess("Case created successfully.");
            }

            setDialogOpen(false);
            await loadCases();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Could not save case");
        } finally {
            setSaving(false);
        }
    }

    async function handleDelete() {
        if (!deleteTarget) return;

        setError("");
        setSuccess("");

        try {
            await deleteCase(deleteTarget.id);
            setDeleteTarget(null);
            setSuccess("Case deleted successfully.");

            if (cases.length === 1 && page > 0) {
                setPage((current) => current - 1);
            } else {
                await loadCases();
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : "Could not delete case");
        }
    }

    return (
        <Box sx={{ p: { xs: 2, md: 3 } }}>
            <Stack
                direction={{ xs: "column", sm: "row" }}
                spacing={2}
                sx={{
                    mb: 3,
                    justifyContent: "space-between",
                    alignItems: { xs: "stretch", sm: "center" },
                }}
            >
                <Box>
                    <Typography variant="h5" sx={{ fontWeight: 700 }}>
                        Cases
                    </Typography>
                    <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                        Manage patient cases and radiology reporting workflow.
                    </Typography>
                </Box>

                <Button
                    variant="contained"
                    startIcon={<AddOutlinedIcon />}
                    onClick={openCreateDialog}
                >
                    Add case
                </Button>
            </Stack>

            {error && (
                <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
                    {error}
                </Alert>
            )}

            {success && (
                <Alert
                    severity="success"
                    sx={{ mb: 2 }}
                    onClose={() => setSuccess("")}
                >
                    {success}
                </Alert>
            )}

            <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
                <Stack
                    direction={{ xs: "column", md: "row" }}
                    spacing={2}
                    sx={{ mb: 2 }}
                >
                    <TextField
                        fullWidth
                        size="small"
                        label="Search accession number or patient"
                        value={search}
                        onChange={(event) => {
                            setSearch(event.target.value);
                            setPage(0);
                        }}
                    />

                    <FormControl size="small" sx={{ minWidth: 220 }}>
                        <InputLabel id="case-status-filter-label">Status</InputLabel>
                        <Select
                            labelId="case-status-filter-label"
                            value={statusFilter}
                            label="Status"
                            onChange={(event) => {
                                setStatusFilter(event.target.value);
                                setPage(0);
                            }}
                        >
                            <MenuItem value="">All statuses</MenuItem>
                            {statuses.map((status) => (
                                <MenuItem key={status} value={status}>
                                    {statusLabel(status)}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Stack>

                <TableContainer>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Accession number</TableCell>
                                <TableCell>Patient</TableCell>
                                <TableCell>Body part</TableCell>
                                <TableCell>Modality</TableCell>
                                <TableCell>Radiologist</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell align="right">Actions</TableCell>
                            </TableRow>
                        </TableHead>

                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={7} align="center" sx={{ py: 5 }}>
                                        <CircularProgress size={28} />
                                    </TableCell>
                                </TableRow>
                            ) : cases.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} align="center" sx={{ py: 5 }}>
                                        No cases found.
                                    </TableCell>
                                </TableRow>
                            ) : (
                                cases.map((item) => (
                                    <TableRow key={item.id} hover>
                                        <TableCell>
                                            <Typography sx={{ fontWeight: 600 }}>
                                                {item.accessionNumber}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography sx={{ fontWeight: 500 }}>
                                                {item.patientName}
                                            </Typography>
                                            <Typography sx={{ variant: "caption", color: "text.secondary" }}>
                                                {item.patientGender || "Gender not specified"}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>{item.bodyPartName}</TableCell>
                                        <TableCell>{item.modality || "—"}</TableCell>
                                        <TableCell>{item.radiologistName}</TableCell>
                                        <TableCell>
                                            <Chip
                                                size="small"
                                                label={statusLabel(item.status)}
                                                color={statusColor(item.status)}
                                                variant="outlined"
                                            />
                                        </TableCell>
                                        <TableCell align="right">
                                            <Button
                                                size="small"
                                                aria-label={`Edit ${item.accessionNumber}`}
                                                onClick={() => openEditDialog(item)}
                                            >
                                                <EditOutlinedIcon fontSize="small" />
                                            </Button>
                                            <Button
                                                size="small"
                                                color="error"
                                                aria-label={`Delete ${item.accessionNumber}`}
                                                onClick={() => setDeleteTarget(item)}
                                            >
                                                <DeleteOutlineOutlinedIcon fontSize="small" />
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>

                <TablePagination
                    component="div"
                    count={total}
                    page={page}
                    rowsPerPage={rowsPerPage}
                    rowsPerPageOptions={[5, 10, 25, 50]}
                    onPageChange={(_, nextPage) => setPage(nextPage)}
                    onRowsPerPageChange={(event) => {
                        setRowsPerPage(Number(event.target.value));
                        setPage(0);
                    }}
                />
            </Paper>

            <Dialog
                open={dialogOpen}
                onClose={() => !saving && setDialogOpen(false)}
                fullWidth
                maxWidth="md"
            >
                <DialogTitle>{editing ? "Edit case" : "Add case"}</DialogTitle>

                <DialogContent dividers>
                    <Stack spacing={2} sx={{ pt: 1 }}>
                        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                            <TextField
                                fullWidth
                                required
                                label="Accession number"
                                value={form.accessionNumber}
                                onChange={(e) => setField("accessionNumber", e.target.value)}
                                slotProps={{
                                    htmlInput: {
                                        maxLength: 100,
                                    },
                                }}
                            />
                            <TextField
                                fullWidth
                                required
                                label="Patient name"
                                value={form.patientName}
                                onChange={(e) => setField("patientName", e.target.value)}
                                slotProps={{
                                    htmlInput: {
                                        maxLength: 150,
                                    },
                                }}
                            />
                        </Stack>

                        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                            <TextField
                                fullWidth
                                label="Date of birth"
                                type="date"
                                value={form.patientDob ?? ""}
                                onChange={(e) =>
                                    setField("patientDob", e.target.value || null)
                                }
                                slotProps={{ inputLabel: { shrink: true } }}
                            />
                            <FormControl fullWidth>
                                <InputLabel id="patient-gender-label">Gender</InputLabel>
                                <Select
                                    labelId="patient-gender-label"
                                    label="Gender"
                                    value={form.patientGender}
                                    onChange={(e) => setField("patientGender", e.target.value)}
                                >
                                    <MenuItem value="">Not specified</MenuItem>
                                    <MenuItem value="Female">Female</MenuItem>
                                    <MenuItem value="Male">Male</MenuItem>
                                    <MenuItem value="Other">Other</MenuItem>
                                </Select>
                            </FormControl>
                        </Stack>

                        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                            <FormControl fullWidth required>
                                <InputLabel id="body-part-label">Body part</InputLabel>
                                <Select
                                    labelId="body-part-label"
                                    label="Body part"
                                    value={form.bodyPartId || ""}
                                    onChange={(e) => setField("bodyPartId", Number(e.target.value))}
                                >
                                    {bodyParts.filter((option) => option.active !== false).map((option) => (
                                        <MenuItem key={option.id} value={option.id}>
                                            {option.name}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>

                            <TextField
                                fullWidth
                                label="Modality"
                                value={form.modality}
                                onChange={(e) => setField("modality", e.target.value)}
                                placeholder="X-RAY, CT, MRI"
                            />
                        </Stack>

                        <FormControl fullWidth required>
                            <InputLabel id="radiologist-label">Radiologist</InputLabel>
                            <Select
                                labelId="radiologist-label"
                                label="Radiologist"
                                value={form.radiologistId || ""}
                                onChange={(e) =>
                                    setField("radiologistId", Number(e.target.value))
                                }
                            >
                                {radiologists.filter((option) => option.active !== false).map((option) => (
                                    <MenuItem key={option.id} value={option.id}>
                                        {option.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <FormControl fullWidth>
                            <InputLabel id="case-status-label">Status</InputLabel>
                            <Select
                                labelId="case-status-label"
                                label="Status"
                                value={form.status}
                                onChange={(e) =>
                                    setField("status", e.target.value as CaseStatus)
                                }
                            >
                                {statuses.map((status) => (
                                    <MenuItem key={status} value={status}>
                                        {statusLabel(status)}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <TextField
                            fullWidth
                            multiline
                            minRows={2}
                            label="Clinical history"
                            value={form.history}
                            onChange={(e) => setField("history", e.target.value)}
                        />

                        <TextField
                            fullWidth
                            multiline
                            minRows={2}
                            label="Technique"
                            value={form.technique}
                            onChange={(e) => setField("technique", e.target.value)}
                        />

                        <TextField
                            fullWidth
                            multiline
                            minRows={3}
                            label="Report"
                            value={form.report}
                            onChange={(e) => setField("report", e.target.value)}
                        />
                    </Stack>
                </DialogContent>

                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={() => setDialogOpen(false)} disabled={saving}>
                        Cancel
                    </Button>
                    <Button
                        variant="contained"
                        onClick={handleSave}
                        disabled={saving}
                    >
                        {saving ? "Saving..." : editing ? "Save changes" : "Create case"}
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog
                open={Boolean(deleteTarget)}
                onClose={() => setDeleteTarget(null)}
            >
                <DialogTitle>Delete case?</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete case{" "}
                    <strong>{deleteTarget?.accessionNumber}</strong>?
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteTarget(null)}>Cancel</Button>
                    <Button color="error" variant="contained" onClick={handleDelete}>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}