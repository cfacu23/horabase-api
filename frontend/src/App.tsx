import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { AppShell } from './layouts/AppShell'
import { ChangePasswordPage } from './pages/ChangePasswordPage'
import { KioskPage } from './pages/KioskPage'
import { LoginPage } from './pages/LoginPage'
import { AdminDashboard } from './pages/admin/AdminDashboard'
import { AdminRequestsPage } from './pages/admin/AdminRequestsPage'
import { AttendancesPage } from './pages/admin/AttendancesPage'
import { CalendarPage } from './pages/admin/CalendarPage'
import { EmployeeProfilePage } from './pages/admin/EmployeeProfilePage'
import { EmployeesPage } from './pages/admin/EmployeesPage'
import { IncidentsPage } from './pages/admin/IncidentsPage'
import { OvertimePage } from './pages/admin/OvertimePage'
import { SectorsPage } from './pages/admin/SectorsPage'
import { TerminalsPage } from './pages/admin/TerminalsPage'
import { EmployeeHomePage, MyAttendancesPage, MyIncidentsPage, MyOvertimePage, MyProfilePage, MyRequestsPage, MyShiftsPage } from './pages/employee/EmployeePages'

export default function App() {
  return <Routes><Route path="/login" element={<LoginPage />} /><Route path="/kiosk" element={<KioskPage />} /><Route element={<ProtectedRoute />}><Route path="/cambiar-contrasena" element={<ChangePasswordPage />} /></Route><Route element={<ProtectedRoute role="ADMIN" />}><Route element={<AppShell />}><Route path="/admin" element={<AdminDashboard />} /><Route path="/admin/empleados" element={<EmployeesPage />} /><Route path="/admin/empleados/:employeeId" element={<EmployeeProfilePage />} /><Route path="/admin/calendario" element={<CalendarPage />} /><Route path="/admin/asistencias" element={<AttendancesPage />} /><Route path="/admin/incidencias" element={<IncidentsPage />} /><Route path="/admin/horas-extra" element={<OvertimePage />} /><Route path="/admin/solicitudes" element={<AdminRequestsPage />} /><Route path="/admin/sectores" element={<SectorsPage />} /><Route path="/admin/terminales" element={<TerminalsPage />} /></Route></Route><Route element={<ProtectedRoute role="EMPLOYEE" />}><Route element={<AppShell />}><Route path="/mi" element={<EmployeeHomePage />} /><Route path="/mi/turnos" element={<MyShiftsPage />} /><Route path="/mi/asistencias" element={<MyAttendancesPage />} /><Route path="/mi/incidencias" element={<MyIncidentsPage />} /><Route path="/mi/horas-extra" element={<MyOvertimePage />} /><Route path="/mi/solicitudes" element={<MyRequestsPage />} /><Route path="/mi/perfil" element={<MyProfilePage />} /></Route></Route><Route path="*" element={<Navigate to="/login" replace />} /></Routes>
}
