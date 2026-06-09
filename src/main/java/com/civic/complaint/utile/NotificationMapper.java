package com.civic.complaint.utile;

import com.civic.complaint.dto.response.NotificationResponse;
import com.civic.complaint.model.Notification;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
