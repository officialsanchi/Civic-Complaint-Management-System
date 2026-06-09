package com.civic.complaint.dto.response;

import com.civic.complaint.enums.ComplaintStatus;
import com.civic.complaint.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintResponse {

    private Long id;
    private String title;
    private String description;
    private ComplaintStatus status;
    private String category;
    private UserResponse reporter;
    private Location location;
    private List<String> imageUrls;
    private int upvotes;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}
