package com.civic.complaint.controller;

import com.civic.complaint.dto.request.ComplaintRequest;
import com.civic.complaint.dto.response.ApiResponse;
import com.civic.complaint.dto.response.ComplaintResponse;
import com.civic.complaint.model.User;
import com.civic.complaint.security.UserPrincipal;
import com.civic.complaint.service.impl.ComplaintServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintServiceImpl complaintService;

    @PostMapping
    public ResponseEntity<ComplaintResponse> create(
            @Valid @RequestBody ComplaintRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.createComplaint(request, user));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ComplaintResponse>> mine(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {

        User user = principal.getUser();
        return ResponseEntity.ok(complaintService.getUserComplaints(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getById(id));
    }


    @GetMapping("/search")
    public ResponseEntity<Page<ComplaintResponse>> search(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(complaintService.searchComplaints(q, pageable));
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<ComplaintResponse> upvote(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.upvote(id));
    }
}
