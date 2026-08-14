export type Role = 'ADMIN' | 'EMPLOYEE'
export type Status = 'OPEN' | 'CLOSED' | 'CANCELLED' | 'SCHEDULED' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'PAID' | 'DETECTED' | 'CONFIRMED' | 'JUSTIFIED' | 'DISMISSED'

export interface AuthUser { accountId: number; businessId: number; employeeId: number | null; document: string; email: string; role: Role; mustChangePassword: boolean }
export interface LoginResponse { accessToken: string; tokenType: string; expiresAt: string; user: AuthUser }
export interface ApiError { status: number; message: string; errors?: Record<string, string> }
export interface Sector { id: number; businessId: number; name: string; description?: string; active: boolean }
export interface Employee { id: number; businessId: number; accountId: number; sectorId: number; sectorName: string; firstName: string; lastName: string; document: string; email: string; phone?: string; hireDate: string; hourlyRate: number; overtimeHourlyRate: number; weeklyHours: number; active: boolean; mustChangePassword: boolean }
export interface Shift { id: number; employeeId: number; employeeName: string; sectorId: number; sectorName: string; startsAt: string; endsAt: string; breakMinutes: number; plannedWorkMinutes: number; notes?: string; status: Status }
export interface Attendance { id: number; employeeId: number; employeeName: string; shiftId?: number; scheduledStartsAt?: string; scheduledEndsAt?: string; checkInAt: string; checkOutAt?: string; workedMinutes?: number; origin: 'TERMINAL' | 'MANUAL'; status: Status; notes?: string }
export interface Overtime { id: number; employeeId: number; employeeName: string; attendanceId?: number; workDate: string; detectedMinutes: number; approvedMinutes?: number; hourlyRateSnapshot?: number; approvedAmount?: number; status: Status; paidAt?: string; paidAmount?: number; notes?: string }
export interface EmployeeRequest { id: number; employeeId: number; employeeName: string; type: string; title: string; description: string; shiftId?: number; relatedDate?: string; status: Status; adminResponse?: string; resolvedAt?: string; createdAt: string }
export interface Incident { id: number; employeeId: number; employeeName: string; shiftId?: number; attendanceId?: number; incidentDate: string; type: string; status: Status; detectedMinutes?: number; notes?: string }
export interface Terminal { id: number; name: string; identifier: string; active: boolean; lastSeenAt?: string; createdAt: string }
export interface DashboardSummary { businessId: number; generatedAt: string; businessDate: string; activeEmployees: number; workingNow: number; upcomingShifts: number; lateArrivalsToday: number; absencesToday: number; pendingRequests: number; pendingOvertime: number }
export interface EmployeeProfile { employee: Employee; upcomingShifts: Shift[]; recentAttendances: Attendance[]; recentIncidents: Incident[]; recentOvertime: Overtime[]; requests: EmployeeRequest[] }
