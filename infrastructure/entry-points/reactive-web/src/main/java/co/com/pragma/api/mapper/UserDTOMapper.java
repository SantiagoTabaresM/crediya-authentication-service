package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.CreateUserDTO;
import co.com.pragma.api.dto.UpdateUserDTO;
import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    UserDTO toUserDTO(User user);


    CreateUserDTO toCreateUserDTO(User user);

    User toUser(CreateUserDTO createUserDTO);

    UpdateUserDTO toUpdateUserDTO(User user);

    User updateUserDTOtoUser(UpdateUserDTO updateUserDTO);

}
