package com.example.social_network_authorization.mapper;

import com.example.social_network_authorization.entity.User;
import com.example.social_network_authorization.web.model.RegistrationDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@DecoratedWith(UserMapperDelegate.class)
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User registrationDtoToUser(RegistrationDto registrationDto);
}
