package com.civic.complaint.controller;

import com.civic.complaint.dto.request.StatusUpdateRequest;
import com.civic.complaint.dto.response.ApiResponse;
import com.civic.complaint.dto.response.AuthResponse;
import com.civic.complaint.dto.response.ComplaintResponse;
import com.civic.complaint.dto.response.DashboardResponse;
import com.civic.complaint.service.impl.AdminServiceImpl;
import com.civic.complaint.service.impl.ComplaintServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminServiceImpl adminService;
    private final ComplaintServiceImpl complaintService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @PatchMapping("/complaints/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request));
    }
}
