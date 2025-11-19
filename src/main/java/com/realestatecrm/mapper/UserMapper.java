package com.realestatecrm.mapper;

import com.realestatecrm.dto.user.response.UserResponse;
import com.realestatecrm.entity.User;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for User entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps User entity to UserResponse DTO (record).
     */
    UserResponse toResponse(User user);
}
