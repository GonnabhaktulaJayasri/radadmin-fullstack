
import { useCallback, useEffect, useState } from "react";
import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    Paper,
    Snackbar,
    Switch,
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
    AddOutlined,
    DeleteOutlined,
    EditOutlined,
    RefreshOutlined,
} from "@mui/icons-material";
import {
    createBodyPart,
    deleteBodyPart,
    getBodyParts,
    updateBodyPart,
    type BodyPart,
} from "../services/bodyPartService";

export default function BodyParts() {
    const [items, setItems] = useState<BodyPart[]>([]);
    const [search, setSearch] = useState("");
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");
    const [dialogOpen, setDialogOpen] = useState(false);
    const [editing, setEditing] = useState<BodyPart | null>(null);
    const [name, setName] = useState("");
    const [active, setActive] = useState(true);
    const [saving, setSaving] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<BodyPart | null>(null);

    const loadItems = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const result = await getBodyParts({
                search,
                page,
                size: rowsPerPage,
                sortBy: "name",
                direction: "asc",
            });

            setItems(result.content);
            setTotal(result.totalElements);
        } catch (err) {
            setError(
                err instanceof Error ? err.message : "Unable to load body parts",
            );
        } finally {
            setLoading(false);
        }
    }, [search, page, rowsPerPage]);

    useEffect(() => {
        void loadItems();
    }, [loadItems]);

    function openCreate() {
        setEditing(null);
        setName("");
        setActive(true);
        setDialogOpen(true);
    }

    function openEdit(item: BodyPart) {
        setEditing(item);
        setName(item.name);
        setActive(item.active);
        setDialogOpen(true);
    }

    async function handleSave() {
        const trimmedName = name.trim();

        if (!trimmedName) {
            setError("Body part name is required.");
            return;
        }

        setSaving(true);
        setError("");

        try {
            if (editing) {
                await updateBodyPart(editing.id, {
                    name: trimmedName,
                    active,
                });
                setNotice("Body part updated successfully.");
            } else {
                await createBodyPart({
                    name: trimmedName,
                    active,
                });
                setPage(0);
                setNotice("Body part created successfully.");
            }

            setDialogOpen(false);
            await loadItems();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to save body part");
        } finally {
            setSaving(false);
        }
    }

    async function handleDelete() {
        if (!deleteTarget) return;

        try {
            await deleteBodyPart(deleteTarget.id);
            setDeleteTarget(null);
            setNotice("Body part deleted successfully.");

            if (items.length === 1 && page > 0) {
                setPage((current) => current - 1);
            } else {
                await loadItems();
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to delete body part");
            setDeleteTarget(null);
        }
    }

    return (
        <div className="page-content">
            <div className="page-heading">
                <div>
                    <h1>Body Parts</h1>
                    <p className="muted">
                        Manage the body parts used in radiology reporting.
                    </p>
                </div>

                <Button
                    variant="contained"
                    startIcon={<AddOutlined />}
                    onClick={openCreate}
                >
                    Add Body Part
                </Button>
            </div>

            <Paper variant="outlined" sx={{ p: 2.5, borderRadius: 3 }}>
                <div
                    style={{
                        display: "flex",
                        gap: 12,
                        alignItems: "center",
                        marginBottom: 20,
                    }}
                >
                    <TextField
                        size="small"
                        label="Search body parts"
                        placeholder="Enter a name"
                        value={search}
                        onChange={(event) => {
                            setSearch(event.target.value);
                            setPage(0);
                        }}
                        sx={{ width: 300, maxWidth: "100%" }}
                    />

                    <IconButton
                        aria-label="Refresh body parts"
                        onClick={() => void loadItems()}
                    >
                        <RefreshOutlined />
                    </IconButton>

                    <Typography sx={{ ml: "auto" }} variant="body2" color="text.secondary">
                        {total} records
                    </Typography>
                </div>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
                        {error}
                    </Alert>
                )}

                <TableContainer>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Body Part</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell>Created At</TableCell>
                                <TableCell align="right">Actions</TableCell>
                            </TableRow>
                        </TableHead>

                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                                        <CircularProgress size={28} />
                                    </TableCell>
                                </TableRow>
                            ) : items.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={4} align="center" sx={{ py: 5 }}>
                                        No body parts found.
                                    </TableCell>
                                </TableRow>
                            ) : (
                                items.map((item) => (
                                    <TableRow hover key={item.id}>
                                        <TableCell>
                                            <Typography sx={{ fontWeight: 600 }}>
                                                {item.name}
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary">
                                                ID: {item.id}
                                            </Typography>
                                        </TableCell>

                                        <TableCell>
                                            <span className={`status-pill ${item.active ? "status-active" : "status-inactive"}`}>
                                                {item.active ? "Active" : "Inactive"}
                                            </span>
                                        </TableCell>

                                        <TableCell>
                                            {new Date(item.createdAt).toLocaleDateString()}
                                        </TableCell>

                                        <TableCell align="right">
                                            <IconButton
                                                aria-label={`Edit ${item.name}`}
                                                onClick={() => openEdit(item)}
                                            >
                                                <EditOutlined />
                                            </IconButton>
                                            <IconButton
                                                aria-label={`Delete ${item.name}`}
                                                color="error"
                                                onClick={() => setDeleteTarget(item)}
                                            >
                                                <DeleteOutlined />
                                            </IconButton>
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
                    onPageChange={(_, nextPage) => setPage(nextPage)}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={(event) => {
                        setRowsPerPage(Number(event.target.value));
                        setPage(0);
                    }}
                    rowsPerPageOptions={[5, 10, 25, 50]}
                />
            </Paper>

            <Dialog open={dialogOpen} onClose={() => !saving && setDialogOpen(false)} fullWidth maxWidth="sm">
                <DialogTitle>{editing ? "Edit Body Part" : "Add Body Part"}</DialogTitle>

                <DialogContent>
                    <TextField
                        autoFocus
                        fullWidth
                        required
                        margin="normal"
                        label="Body part name"
                        placeholder="e.g. CERVICAL SPINE"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                        slotProps={{
                            htmlInput: {
                                maxLength: 100,
                            },
                        }}
                        helperText="Enter a unique body part name."
                    />

                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <Switch
                            checked={active}
                            onChange={(event) => setActive(event.target.checked)}
                        />
                        <Typography variant="body2">
                            {active ? "Active" : "Inactive"}
                        </Typography>
                    </div>
                </DialogContent>

                <DialogActions sx={{ p: 2.5 }}>
                    <Button onClick={() => setDialogOpen(false)} disabled={saving}>
                        Cancel
                    </Button>
                    <Button
                        variant="contained"
                        onClick={() => void handleSave()}
                        disabled={saving || !name.trim()}
                    >
                        {saving ? "Saving..." : editing ? "Save Changes" : "Create"}
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog
                open={Boolean(deleteTarget)}
                onClose={() => setDeleteTarget(null)}
            >
                <DialogTitle>Delete body part?</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete{" "}
                    <strong>{deleteTarget?.name}</strong>?
                </DialogContent>
                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={() => setDeleteTarget(null)}>Cancel</Button>
                    <Button color="error" variant="contained" onClick={() => void handleDelete()}>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar
                open={Boolean(notice)}
                autoHideDuration={3000}
                onClose={() => setNotice("")}
                message={notice}
            />
        </div>
    );
}
