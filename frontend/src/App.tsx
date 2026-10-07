import { useState } from "react";

import Sidebar, { type PageName } from "./components/Sidebar";
import Header from "./components/Header";

import Dashboard from "./pages/Dashboard";
import BodyParts from "./pages/BodyParts";
import Radiologists from "./pages/Radiologists";

import "./App.css";

function App() {
  const [activePage, setActivePage] =
    useState<PageName>("Dashboard");

  const renderPage = () => {
    switch (activePage) {
      case "Body Parts":
        return <BodyParts />;

      case "Radiologists":
        return <Radiologists />;

      case "Dashboard":
        return <Dashboard />;

      case "Cases":
      case "Analytics":
      case "Settings":
        return (
          <div>
            <h2>{activePage}</h2>
            <p>Coming soon.</p>
          </div>
        );

      default:
        return <Dashboard />;
    }
  };

  return (
    <div className="app">
      <Sidebar
        activePage={activePage}
        onNavigate={setActivePage}
      />

      <div className="main-content">
        <Header />

        <main className="page-content">
          {renderPage()}
        </main>
      </div>
    </div>
  );
}

export default App;