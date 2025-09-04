package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.*;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    UserDTO toUserDTO(User user);


    CreateUserDTO toCreateUserDTO(User user);

    User toUser(CreateUserDTO createUserDTO);

    UpdateUserDTO toUpdateUserDTO(User user);

    User updateUserDTOtoUser(UpdateUserDTO updateUserDTO);

    User toUser(LoginDTO loginDTO);

    UserBasicInfDTO toUserBasicInfDTO(User user);

}
