package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.LoginDTO;
import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.model.auth.Auth;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginDTOMapper {

    Auth toAuth(LoginDTO loginDto);


}
