import React from 'react';
import ReactDOM from 'react-dom/client';
import { Navigate, Route, Routes } from 'react-router-dom';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import AppShell from './components/AppShell.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/LoginPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import MealLogPage from './pages/MealLogPage.jsx';
import HistoryPage from './pages/HistoryPage.jsx';
import AchievementsPage from './pages/AchievementsPage.jsx';
import ReportsPage from './pages/ReportsPage.jsx';
import AdminUsersPage from './pages/AdminUsersPage.jsx';
import AdminImportPlanPage from './pages/AdminImportPlanPage.jsx';
import AdminManualPlanPage from './pages/AdminManualPlanPage.jsx';
import './styles.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="registro" element={<MealLogPage />} />
            <Route path="historico" element={<HistoryPage />} />
            <Route path="conquistas" element={<AchievementsPage />} />
            <Route path="relatorios" element={<ReportsPage />} />
            <Route path="admin/usuarios" element={<AdminUsersPage />} />
            <Route path="admin/importar-plano" element={<AdminImportPlanPage />} />
            <Route path="admin/plano-manual" element={<AdminManualPlanPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
);
