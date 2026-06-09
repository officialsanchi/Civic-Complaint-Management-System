package com.civic.complaint.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalComplaints;
    private long totalUsers;
    private Map<String, Long> byStatus;
    private Map<String, Long> byCategory;
    private long pendingComplaints;
    private long resolvedComplaints;
}
