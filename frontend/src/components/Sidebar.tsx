import {
  DashboardOutlined,
  AccessibilityNewOutlined,
  DescriptionOutlined,
  PeopleOutlined,
  BarChartOutlined,
  SettingsOutlined,
} from "@mui/icons-material";

export type PageName =
  | "Dashboard"
  | "Body Parts"
  | "Cases"
  | "Radiologists"
  | "Analytics"
  | "Settings";

const menuItems = [
  { label: "Dashboard" as const, icon: <DashboardOutlined /> },
  { label: "Body Parts" as const, icon: <AccessibilityNewOutlined /> },
  { label: "Cases" as const, icon: <DescriptionOutlined /> },
  { label: "Radiologists" as const, icon: <PeopleOutlined /> },
  { label: "Analytics" as const, icon: <BarChartOutlined /> },
  { label: "Settings" as const, icon: <SettingsOutlined /> },
];

interface SidebarProps {
  activePage: PageName;
  onNavigate: (page: PageName) => void;
}

export default function Sidebar({ activePage, onNavigate }: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">R</div>
        <div>
          <h2>RadAdmin</h2>
          <span>Reporting Platform</span>
        </div>
      </div>

      <p className="nav-heading">WORKSPACE</p>

      <nav className="nav-menu">
        {menuItems.map((item) => (
          <button
            className={`nav-item ${activePage === item.label ? "active" : ""}`}
            key={item.label}
            type="button"
            onClick={() => onNavigate(item.label)}
          >
            {item.icon}
            <span>{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="avatar">JA</div>
        <div>
          <strong>Admin User</strong>
          <span>Administrator</span>
        </div>
      </div>
    </aside>
  );
}