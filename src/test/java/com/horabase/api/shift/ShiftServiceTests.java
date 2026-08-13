package com.horabase.api.shift;

import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.sector.Sector;
import com.horabase.api.sector.SectorRepository;
import com.horabase.api.shift.dto.CreateShiftRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTests {

    @Mock ShiftRepository shifts;
    @Mock BusinessRepository businesses;
    @Mock EmployeeRepository employees;
    @Mock SectorRepository sectors;

    private ShiftService service;
    private Business business;
    private Employee employee;
    private Sector sector;
    private OffsetDateTime startsAt;

    @BeforeEach
    void setUp() {
        service = new ShiftService(shifts, businesses, employees, sectors);
        business = new Business();
        ReflectionTestUtils.setField(business, "id", 1L);
        business.setActive(true);
        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", 2L);
        employee.setBusiness(business);
        employee.setActive(true);
        sector = new Sector();
        ReflectionTestUtils.setField(sector, "id", 3L);
        sector.setBusiness(business);
        sector.setActive(true);
        startsAt = OffsetDateTime.parse("2026-08-13T09:00:00Z");
    }

    @Test
    void rejectsEmployeeFromAnotherBusiness() {
        when(businesses.findById(1L)).thenReturn(Optional.of(business));
        when(employees.findByIdAndBusiness_Id(2L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, request(startsAt.plusHours(8))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("empleado");
    }

    @Test
    void rejectsOverlappingShift() {
        stubResources();
        when(shifts.existsByEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusNot(
                2L, startsAt.plusHours(8), startsAt, ShiftStatus.CANCELLED
        )).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, request(startsAt.plusHours(8))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("otro turno");
    }

    @Test
    void rejectsShiftWhoseEndIsNotAfterStart() {
        stubResources();

        assertThatThrownBy(() -> service.create(1L, request(startsAt)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("salida");
    }

    private void stubResources() {
        when(businesses.findById(1L)).thenReturn(Optional.of(business));
        when(employees.findByIdAndBusiness_Id(2L, 1L))
                .thenReturn(Optional.of(employee));
        when(sectors.findByIdAndBusiness_Id(3L, 1L))
                .thenReturn(Optional.of(sector));
    }

    private CreateShiftRequest request(OffsetDateTime endsAt) {
        return new CreateShiftRequest(
                2L, 3L, startsAt, endsAt, 30, null
        );
    }
}
