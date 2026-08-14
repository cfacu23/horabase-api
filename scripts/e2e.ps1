param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminDocument = "45678901",
    [string]$AdminPassword = "AdminDemo123!",
    [string]$AdminFinalPassword = "AdminReady123!",
    [string]$EmployeePassword = "EmpleadoDemo123!",
    [string]$EmployeeFinalPassword = "EmpleadoReady123!",
    [string]$DatabaseUser = "horabase",
    [string]$DatabaseName = "horabase"
)

$ErrorActionPreference = "Stop"
$runId = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()

function Invoke-HoraBase {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body,
        [string]$Token,
        [hashtable]$Headers = @{}
    )
    $requestHeaders = @{} + $Headers
    if ($Token) { $requestHeaders.Authorization = "Bearer $Token" }
    $parameters = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        Headers = $requestHeaders
        ContentType = "application/json"
    }
    if ($null -ne $Body) { $parameters.Body = ($Body | ConvertTo-Json -Depth 8 -Compress) }
    Invoke-RestMethod @parameters
}

function Assert-Status {
    param([scriptblock]$Action, [int]$Expected, [string]$Label)
    try {
        & $Action | Out-Null
        throw "$Label no devolvio HTTP $Expected"
    } catch {
        $response = $_.Exception.Response
        if ($null -eq $response -or [int]$response.StatusCode -ne $Expected) { throw }
        Write-Host "[OK] $Label -> HTTP $Expected"
    }
}

Write-Host "[1/12] Salud y login administrador"
$health = Invoke-HoraBase GET "/api/health" $null ""
if ($health.status -ne "UP") { throw "Backend no saludable" }
$adminLogin = Invoke-HoraBase POST "/api/auth/login" @{ document = $AdminDocument; password = $AdminPassword } ""
$adminToken = $adminLogin.accessToken
$businessId = [long]$adminLogin.user.businessId
if ($adminLogin.user.mustChangePassword) {
    Invoke-HoraBase POST "/api/auth/change-password" @{ currentPassword = $AdminPassword; newPassword = $AdminFinalPassword } $adminToken | Out-Null
    $AdminPassword = $AdminFinalPassword
    Write-Host "[OK] Contrasena inicial del administrador reemplazada"
}

Write-Host "[2/12] Sector, empleado y cambio obligatorio de contrasena"
$sector = Invoke-HoraBase POST "/api/businesses/$businessId/sectors" @{ name = "Operaciones $runId"; description = "Sector E2E" } $adminToken
$employeeDocument = "7$($runId.Substring([Math]::Max(0, $runId.Length - 7)))"
$employee = Invoke-HoraBase POST "/api/businesses/$businessId/employees" @{
    firstName = "E2E"; lastName = "Empleado"; document = $employeeDocument
    email = "e2e-$runId@horabase.local"; temporaryPassword = $EmployeePassword
    sectorId = $sector.id; hireDate = [DateTime]::UtcNow.ToString("yyyy-MM-dd")
    hourlyRate = 300; overtimeHourlyRate = 450; weeklyHours = 40
} $adminToken
$employeeLogin = Invoke-HoraBase POST "/api/auth/login" @{ businessId = $businessId; document = $employeeDocument; password = $EmployeePassword } ""
$employeeToken = $employeeLogin.accessToken
Assert-Status { Invoke-HoraBase GET "/api/me/profile" $null $employeeToken } 403 "Bloqueo por contrasena temporal"
Invoke-HoraBase POST "/api/auth/change-password" @{ currentPassword = $EmployeePassword; newPassword = $EmployeeFinalPassword } $employeeToken | Out-Null
$employeeLogin = Invoke-HoraBase POST "/api/auth/login" @{ businessId = $businessId; document = $employeeDocument; password = $EmployeeFinalPassword } ""
$employeeToken = $employeeLogin.accessToken

Write-Host "[3/12] Turno y calendarios"
$shiftStart = [DateTimeOffset]::Now.AddDays(1)
$shiftStart = $shiftStart.AddHours(9 - $shiftStart.Hour).AddMinutes(-$shiftStart.Minute).AddSeconds(-$shiftStart.Second).AddMilliseconds(-$shiftStart.Millisecond)
$shiftEnd = $shiftStart.AddHours(8)
$shift = Invoke-HoraBase POST "/api/businesses/$businessId/shifts" @{
    employeeId = $employee.id; sectorId = $sector.id
    startsAt = $shiftStart.ToString("o"); endsAt = $shiftEnd.ToString("o")
    breakMinutes = 30; notes = "Turno E2E"
} $adminToken
$calendar = Invoke-HoraBase GET "/api/me/calendar?from=$([uri]::EscapeDataString($shiftStart.AddDays(-1).ToString('o')))&to=$([uri]::EscapeDataString($shiftEnd.AddDays(1).ToString('o')))" $null $employeeToken
if (@($calendar).Count -lt 1) { throw "El turno no aparecio en el calendario del empleado" }

Write-Host "[4/12] Terminal, entrada duplicada y salida"
$terminal = Invoke-HoraBase POST "/api/businesses/$businessId/terminals" @{ name = "Terminal E2E"; identifier = "e2e-$runId" } $adminToken
$terminalHeaders = @{ "X-Terminal-Id" = $terminal.terminal.identifier; "X-Terminal-Secret" = $terminal.secret }
$attendance = Invoke-HoraBase POST "/api/terminal/businesses/$businessId/check-in" @{ employeeId = $employee.id } "" $terminalHeaders
Assert-Status { Invoke-HoraBase POST "/api/terminal/businesses/$businessId/check-in" @{ employeeId = $employee.id } "" $terminalHeaders } 409 "Doble check-in"
$attendance = Invoke-HoraBase POST "/api/terminal/businesses/$businessId/check-out" @{ employeeId = $employee.id } "" $terminalHeaders
if ($attendance.status -ne "CLOSED") { throw "La asistencia no quedo cerrada" }

Write-Host "[5/12] Consulta de asistencia e incidencia"
$attendanceFrom = [DateTimeOffset]::Now.AddDays(-1).ToString("o")
$attendanceTo = [DateTimeOffset]::Now.AddDays(1).ToString("o")
$attendanceList = Invoke-HoraBase GET "/api/businesses/$businessId/attendances?from=$([uri]::EscapeDataString($attendanceFrom))&to=$([uri]::EscapeDataString($attendanceTo))&employeeId=$($employee.id)" $null $adminToken
if (@($attendanceList).Count -lt 1) { throw "No se encontro la asistencia" }
$incident = Invoke-HoraBase POST "/api/businesses/$businessId/incidents" @{
    employeeId = $employee.id; attendanceId = $attendance.id
    incidentDate = [DateTime]::UtcNow.ToString("yyyy-MM-dd")
    type = "LATE_ARRIVAL"; detectedMinutes = 10; notes = "Incidencia E2E"
} $adminToken
$incident = Invoke-HoraBase POST "/api/businesses/$businessId/incidents/$($incident.id)/resolve" @{ status = "JUSTIFIED"; notes = "Justificada E2E" } $adminToken

Write-Host "[6/12] Horas extra: alta, aprobacion y pago"
$overtime = Invoke-HoraBase POST "/api/businesses/$businessId/overtime" @{
    employeeId = $employee.id; attendanceId = $attendance.id
    workDate = [DateTime]::UtcNow.ToString("yyyy-MM-dd"); detectedMinutes = 60; notes = "Extra E2E"
} $adminToken
$overtime = Invoke-HoraBase POST "/api/businesses/$businessId/overtime/$($overtime.id)/approve" @{ approvedMinutes = 60; notes = "Aprobada E2E" } $adminToken
$overtime = Invoke-HoraBase POST "/api/businesses/$businessId/overtime/$($overtime.id)/pay" @{ paidAmount = $overtime.approvedAmount; paidAt = [DateTimeOffset]::Now.ToString("o"); notes = "Pago E2E" } $adminToken
if ($overtime.status -ne "PAID") { throw "La hora extra no quedo pagada" }

Write-Host "[7/12] Solicitud del empleado y resolucion administrativa"
$request = Invoke-HoraBase POST "/api/me/requests" @{ type = "DAY_OFF"; title = "Solicitud E2E"; description = "Prueba automatizada"; relatedDate = [DateTime]::UtcNow.AddDays(7).ToString("yyyy-MM-dd") } $employeeToken
$request = Invoke-HoraBase POST "/api/businesses/$businessId/requests/$($request.id)/approve" @{ response = "Aprobada en E2E" } $adminToken
$myRequests = Invoke-HoraBase GET "/api/me/requests" $null $employeeToken
if (-not (@($myRequests) | Where-Object { $_.id -eq $request.id -and $_.status -eq "APPROVED" })) { throw "El empleado no ve la resolucion" }

Write-Host "[8/12] Autorizacion por rol"
Assert-Status { Invoke-HoraBase GET "/api/businesses/$businessId/dashboard" $null $employeeToken } 403 "Empleado en endpoint administrativo"

Write-Host "[9/12] Segundo comercio e aislamiento"
$taxId = "99$runId"
$sql = "INSERT INTO businesses (name,tax_id,active,created_at,updated_at) VALUES ('Comercio aislamiento E2E','$taxId',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) RETURNING id;"
$secondOutput = docker compose exec -T postgres psql -U $DatabaseUser -d $DatabaseName -Atc $sql
if ($LASTEXITCODE -ne 0) { throw "No se pudo crear el segundo comercio para la prueba" }
$secondBusinessId = [long](@($secondOutput) | Where-Object { $_ -match '^\d+$' } | Select-Object -First 1)
Assert-Status { Invoke-HoraBase GET "/api/businesses/$secondBusinessId/dashboard" $null $adminToken } 403 "Acceso cross-business"

Write-Host "[10/12] Endpoints de autoservicio"
Invoke-HoraBase GET "/api/me/profile" $null $employeeToken | Out-Null
Invoke-HoraBase GET "/api/me/incidents?from=$([DateTime]::UtcNow.AddMonths(-1).ToString('yyyy-MM-dd'))&to=$([DateTime]::UtcNow.AddDays(1).ToString('yyyy-MM-dd'))" $null $employeeToken | Out-Null
Invoke-HoraBase GET "/api/me/overtime?from=$([DateTime]::UtcNow.AddMonths(-1).ToString('yyyy-MM-dd'))&to=$([DateTime]::UtcNow.AddDays(1).ToString('yyyy-MM-dd'))" $null $employeeToken | Out-Null

Write-Host "[11/12] Validacion de resultados"
if ($incident.status -ne "JUSTIFIED" -or $shift.status -ne "SCHEDULED") { throw "Estados finales inesperados" }

Write-Host "[12/12] E2E COMPLETO" -ForegroundColor Green
Write-Host "Comercio: $businessId | Empleado: $($employee.id) | Asistencia: $($attendance.id) | Segundo comercio: $secondBusinessId"
