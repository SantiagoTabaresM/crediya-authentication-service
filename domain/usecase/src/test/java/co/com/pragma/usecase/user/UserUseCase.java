package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.user.exception.BussinesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUseCase userUseCase;

    private User validUser;
    private User invalidUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .id(1L)
                .name("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .baseSalary(5000000)
                .build();

        invalidUser = User.builder()
                .name("") // Nombre vacío
                .lastName("D") // Apellido muy corto
                .email("invalid-email") // Email inválido
                .baseSalary(-1000) // Salario negativo
                .build();
    }





    @Test
    void testSaveUser_WithValidUser_ShouldSaveSuccessfully() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.save(validUser)).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void validateCreateUser_shouldFail_whenInvalidUser() {
        // when & then
        StepVerifier.create(userUseCase.saveUser(invalidUser))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("name");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("last_name");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("email");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("base_salary");

                })
                .verify();
    }


    @Test
    void saveUser_shouldThrowBussinesException_whenEmailAlreadyExists() {

        // Simular que el email ya existe
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(true));

        // Act
        Mono<User> result = userUseCase.saveUser(validUser);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("email");
                })
                .verify();

        // Verify
        verify(userRepository, times(1)).existsByEmail(validUser.getEmail());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }




    @Test
    void testUpdateUser_WithValidUser_ShouldUpdateSuccessfully() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.save(validUser)).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.updateUser(validUser))
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void validateUpdateUser_shouldFail_whenInvalidUser() {
        // when & then
        StepVerifier.create(userUseCase.updateUser(invalidUser))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("name");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("last_name");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("email");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("base_salary");

                })
                .verify();
    }


    @Test
    void updateUser_shouldThrowBussinesException_whenEmailAlreadyExists() {
        // Simular que el email ya existe
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(true));

        // Act
        Mono<User> result = userUseCase.updateUser(validUser);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("email");
                })
                .verify();

        // Verify
        verify(userRepository, times(1)).existsByEmail(validUser.getEmail());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }



    @Test
    void testGetUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.getUserById(1L))
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void testGetUserById_WhenUserNotExists_ShouldReturnEmpty() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.getUserById(999L))
                .verifyComplete();
    }

    @Test
    void testDeleteUser_ShouldCompleteSuccessfully() {
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.deleteUser(1L))
                .verifyComplete();
    }


}