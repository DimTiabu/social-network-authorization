package ru.skillbox.social_network_authorization.mapper;

import ru.skillbox.social_network_authorization.entity.User;
import ru.skillbox.social_network_authorization.dto.RegistrationDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@DecoratedWith(UserMapperDelegate.class)
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User registrationDtoToUser(RegistrationDto registrationDto);
}
