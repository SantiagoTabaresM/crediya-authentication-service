package co.com.pragma.api.mapper;


import co.com.pragma.api.dto.AuthDTO;
import co.com.pragma.model.auth.Auth;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthDTOMapper {

    AuthDTO toAuthDTO(Auth auth);


}
