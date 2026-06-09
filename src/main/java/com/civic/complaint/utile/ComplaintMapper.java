package com.civic.complaint.utile;

import com.civic.complaint.dto.request.ComplaintRequest;
import com.civic.complaint.dto.response.ComplaintResponse;
import com.civic.complaint.model.Complaint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ComplaintMapper {
    ComplaintResponse toResponse(Complaint complaint);

    List<ComplaintResponse> toResponseList(List<Complaint> complaints);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "upvotes", constant = "0")
    @Mapping(target = "reporter", ignore = true)
    @Mapping(target = "reportedAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    Complaint toEntity(ComplaintRequest request);
}
