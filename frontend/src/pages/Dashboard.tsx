
import {
    AssignmentOutlined,
    CheckCircleOutlined,
    GroupsOutlined,
    PendingActionsOutlined,
} from "@mui/icons-material";

const stats = [
    {
        label: "Total Cases",
        value: "1,284",
        note: "Sample dashboard data",
        icon: <AssignmentOutlined />,
    },
    {
        label: "Completed Reports",
        value: "1,106",
        note: "Sample dashboard data",
        icon: <CheckCircleOutlined />,
    },
    {
        label: "Pending Reports",
        value: "128",
        note: "Sample dashboard data",
        icon: <PendingActionsOutlined />,
    },
    {
        label: "Radiologists",
        value: "24",
        note: "Sample dashboard data",
        icon: <GroupsOutlined />,
    },
];

export default function Dashboard() {
    return (
        <div className="page-content">
            <div className="page-heading">
                <div>
                    <p className="eyebrow">OVERVIEW</p>
                    <h1>Dashboard</h1>
                    <p className="muted">
                        Welcome back. Here is your reporting workspace.
                    </p>
                </div>
            </div>

            <section className="stats-grid">
                {stats.map((stat) => (
                    <article className="stat-card" key={stat.label}>
                        <div className="stat-card-top">
                            <span>{stat.label}</span>
                            <span className="stat-icon">{stat.icon}</span>
                        </div>
                        <strong className="stat-value">{stat.value}</strong>
                        <span className="stat-note">{stat.note}</span>
                    </article>
                ))}
            </section>

            <section className="panel welcome-panel">
                <div>
                    <p className="eyebrow">GETTING STARTED</p>
                    <h2>Your reporting workspace</h2>
                    <p className="muted">
                        Manage master data, organize reporting workflows, and
                        monitor activity from one place.
                    </p>
                </div>
                <div className="welcome-mark">R</div>
            </section>
        </div>
    );
}
