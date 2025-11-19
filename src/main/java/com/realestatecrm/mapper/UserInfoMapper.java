package com.realestatecrm.mapper;

import com.realestatecrm.dto.auth.response.UserInfo;
import com.realestatecrm.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting User entity to UserInfo DTO.
 * Used primarily in authentication endpoints.
 */
@Mapper(componentModel = "spring")
public interface UserInfoMapper {

    /**
     * Maps User entity to UserInfo DTO.
     *
     * @param user the User entity
     * @return UserInfo DTO with authentication details
     */
    @Mapping(target = "id", expression = "java(user.getId().toString())")
    @Mapping(target = "roles", expression = "java(java.util.List.of(\"ROLE_\" + user.getRole().name()))")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "createdDate", expression = "java(user.getCreatedDate().toString())")
    @Mapping(target = "updatedDate", expression = "java(user.getUpdatedDate().toString())")
    UserInfo toUserInfo(User user);

    /**
     * Maps a list of User entities to a list of UserInfo DTOs.
     *
     * @param users list of User entities
     * @return list of UserInfo DTOs
     */
    List<UserInfo> toUserInfoList(List<User> users);
}
