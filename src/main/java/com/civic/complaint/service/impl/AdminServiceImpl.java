package com.civic.complaint.service.impl;

import com.civic.complaint.dto.response.AuthResponse;
import com.civic.complaint.dto.response.DashboardResponse;
import com.civic.complaint.exception.BadRequestException;
import com.civic.complaint.exception.ResourceNotFoundException;
import com.civic.complaint.enums.ComplaintStatus;
import com.civic.complaint.enums.Role;
import com.civic.complaint.model.User;
import com.civic.complaint.repository.ComplaintRepository;
import com.civic.complaint.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl  {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public DashboardResponse getDashboard() {
        Map<String, Long> byStatus = Arrays.stream(ComplaintStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        complaintRepository::countByStatus
                ));

        Map<String, Long> byCategory = complaintRepository
                .countGroupedByCategory()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        return DashboardResponse.builder()
                .totalComplaints(complaintRepository.count())
                .totalUsers(userRepository.count())
                .byStatus(byStatus)
                .byCategory(byCategory)
                .pendingComplaints(complaintRepository.countByStatus(ComplaintStatus.PENDING))
                .resolvedComplaints(complaintRepository.countByStatus(ComplaintStatus.RESOLVED))
                .build();
    }
}
