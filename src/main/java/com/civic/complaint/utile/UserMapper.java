package com.civic.complaint.utile;

import com.civic.complaint.dto.response.UserResponse;
import com.civic.complaint.model.User;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    List<UserResponse> toResponseList(List<User> users);
}
