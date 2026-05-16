package dev.cervantvfr.vacationsplease.api.controller;

import dev.cervantvfr.vacationsplease.application.service.LeaveRequestService;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;
import dev.cervantvfr.vacationsplease.domain.model.Employee;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.model.LeaveType;
import dev.cervantvfr.vacationsplease.application.exception.BusinessRuleViolationException;
import dev.cervantvfr.vacationsplease.application.exception.ResourceNotFoundException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = LeaveRequestController.class)
@Import(dev.cervantvfr.vacationsplease.api.error.GlobalExceptionHandler.class)
class LeaveRequestControllerTest {
    
    @Autowired MockMvc mockMvc;
    @Autowired LeaveRequestService leaveRequestService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public LeaveRequestService leaveRequestService() {
            return mock(LeaveRequestService.class);
        }
    }

    @WithMockUser
    @Test
    void submitLeaveRequest_returns201_whenValid() throws Exception {
        Employee employee = new Employee("john@test.com", "John", "Test");
        LeaveType leaveType = new LeaveType("PTO", "Paid Time Off", true);
        LeaveRequest saved = new LeaveRequest(
            employee,
            leaveType,
            LocalDate.of(2026, 7, 10),
            LocalDate.of(2026, 7, 12),
            LeaveRequestStatus.PENDING,
            "Trip"
        );
    
        doReturn(saved).when(leaveRequestService).submitRequest(anyLong(), anyString(), any(), any(), any());
        
        String json = """
                {
                    "employeeId": 1,
                    "leaveTypeCode": "PTO",
                    "startDate": "2026-07-10",
                    "endDate": "2026-07-12",
                    "reason": "Trip"
                }
                """;
        
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.leaveTypeCode").value("PTO"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @WithMockUser
    @Test
    void submitLeaveRequest_returns400_whenInvalid() throws Exception {
        String json = """
                {
                    "employeeId": null,
                    "leaveTypeCode": "",
                    "startDate": null,
                    "endDate": null
                }
                """;
        
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.employeeId").exists());
    }

    @WithMockUser
    @Test
    void submitLeaveRequest_returns409_whenBusinessRuleFails() throws Exception {
        doThrow(new BusinessRuleViolationException("Overlapping leave request already exists")).when(leaveRequestService).submitRequest(anyLong(), anyString(), any(), any(), any());

        String json = """
                {
                    "employeeId": 1,
                    "leaveTypeCode": "PTO",
                    "startDate": "2026-07-11",
                    "endDate": "2026-07-13",
                    "reason": "Trip"
                }
                """;

        mockMvc.perform(post("/api/leave-requests")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Overlapping leave request already exists"));
    }

    @WithMockUser
    @Test
    void submitLeaveRequest_returns404_whenEmployeeOrTypeMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found")).when(leaveRequestService).submitRequest(anyLong(), anyString(), any(), any(), any());

        String json = """
                {
                    "employeeId": 1,
                    "leaveTypeCode": "PTO",
                    "startDate": "2026-07-10",
                    "endDate": "2026-07-12",
                    "reason": "Trip"
                }
                """;

        mockMvc.perform(post("/api/leave-requests")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Employee not found"));
    }
}