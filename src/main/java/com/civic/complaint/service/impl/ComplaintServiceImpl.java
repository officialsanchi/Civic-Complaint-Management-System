package com.civic.complaint.service.impl;

import com.civic.complaint.dto.request.ComplaintRequest;
import com.civic.complaint.dto.request.StatusUpdateRequest;
import com.civic.complaint.dto.response.ComplaintResponse;
import com.civic.complaint.enums.ComplaintStatus;
import com.civic.complaint.enums.NotificationType;
import com.civic.complaint.enums.Role;
import com.civic.complaint.exception.AccessDeniedException;
import com.civic.complaint.exception.ResourceNotFoundException;
import com.civic.complaint.model.*;
import com.civic.complaint.repository.ComplaintRepository;
import com.civic.complaint.utile.ComplaintMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComplaintServiceImpl {

    private final ComplaintRepository complaintRepository;
    private final NotificationServiceImpl notificationService;
    private final ComplaintMapper complaintMapper;

    public ComplaintResponse createComplaint(ComplaintRequest request, User reporter) {
        Complaint complaint = complaintMapper.toEntity(request);
        complaint.setReporter(reporter);

        // Map location from nested DTO
        Location location = Location.builder()
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .address(request.getLocation().getAddress())
                .ward(request.getLocation().getWard())
                .lga(request.getLocation().getLga())
                .state(request.getLocation().getState())
                .build();
        complaint.setLocation(location);

        return complaintMapper.toResponse(complaintRepository.save(complaint));
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getUserComplaints(User user, Pageable pageable) {
        return complaintRepository.findByReporter(user, pageable)
                .map(complaintMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> searchComplaints(String query, Pageable pageable) {
        return complaintRepository.search(query, pageable)
                .map(complaintMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getById(Long id) {
        return complaintRepository.findById(id)
                .map(complaintMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
    }

    public ComplaintResponse updateStatus(Long id, StatusUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));

        complaint.setStatus(request.getStatus());
        if (request.getStatus() == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        notificationService.sendNotification(
                complaint.getReporter(),
                String.format("Your complaint '%s' status updated to: %s",
                        complaint.getTitle(), request.getStatus()),
                NotificationType.STATUS_UPDATE
        );

        return complaintMapper.toResponse(saved);
    }

    public ComplaintResponse upvote(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
        complaint.setUpvotes(complaint.getUpvotes() + 1);
        return complaintMapper.toResponse(complaintRepository.save(complaint));
    }
}
