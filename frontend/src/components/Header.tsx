
import {
    NotificationsNoneOutlined,
    SearchOutlined,
} from "@mui/icons-material";

export default function Header() {
    return (
        <header className="topbar">
            <div className="breadcrumb">
                <span>Workspace</span>
                <span className="breadcrumb-divider">/</span>
                <strong>Dashboard</strong>
            </div>

            <div className="topbar-actions">
                <label className="global-search">
                    <SearchOutlined />
                    <input placeholder="Search anything..." />
                    <span className="shortcut">Ctrl K</span>
                </label>

                <button
                    className="icon-button"
                    type="button"
                    aria-label="Notifications"
                >
                    <NotificationsNoneOutlined />
                </button>
            </div>
        </header>
    );
}
