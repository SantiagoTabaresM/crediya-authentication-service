package co.com.pragma.usecase.user;

import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.utils.gateways.Logger;
import co.com.pragma.model.utils.gateways.PasswordEncoderPort;
import co.com.pragma.model.utils.gateways.TxOperational;
import co.com.pragma.usecase.user.exception.BussinesException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersUseCaseTest {


    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;


    @Mock
    private Logger logger;

    @Mock
    private TxOperational txOperational;

    @Mock
    private PasswordEncoderPort passwordEncoder;

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
                .document("123456")
                .roleId(1)
                .password("12345")
                .baseSalary(5000000)
                .build();

        invalidUser = User.builder()
                .name("") // Nombre vacío
                .lastName("D") // Apellido muy corto
                .email("invalid-email") // Email inválido
                .roleId(2)
                .baseSalary(-1000) // Salario negativo
                .build();



    }


    @Test
    void testSaveUser_WithValidUser_ShouldSaveSuccessfully() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.existsByDocument(validUser.getDocument())).thenReturn(Mono.just(false));
        when(roleRepository.existsById(validUser.getRoleId())).thenReturn(Mono.just(true));
        when(passwordEncoder.encode(validUser.getPassword())).thenReturn(Mono.just(validUser.getPassword()));


        when(userRepository.save(any(User.class))).thenReturn(Mono.just(validUser));
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });

        StepVerifier.create(userUseCase.saveUser(validUser))
              .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void validateCreateUser_shouldFail_whenInvalidUser() {
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
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
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
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
    void saveUser_shouldThrowBussinesException_whenDocumentAlreadyExists() {
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
        // Simular que el email ya existe
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));

        when(userRepository.existsByDocument(validUser.getDocument())).thenReturn(Mono.just(true));

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
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("document");
                })
                .verify();

        // Verify
        verify(userRepository, times(1)).existsByDocument(validUser.getDocument());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }


    @Test
    void saveUser_shouldThrowBussinesException_whenRoleNotExists() {
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
        // Simular que el email ya existe
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.existsByDocument(validUser.getDocument())).thenReturn(Mono.just(false));
        when(roleRepository.existsById(validUser.getRoleId())).thenReturn(Mono.just(false));

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
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("role");
                })
                .verify();

        // Verify
        verify(roleRepository, times(1)).existsById(validUser.getRoleId());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }




    @Test
    void testUpdateUser_WithValidUser_ShouldUpdateSuccessfully() {
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.save(validUser)).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.updateUser(validUser))
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void validateUpdateUser_shouldFail_whenInvalidUser() {
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
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
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
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
    void existsByDocumentAndEmail_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsByDocumentAndEmail(validUser.getDocument(), validUser.getEmail()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.existsByDocumentAndEmail(validUser.getDocument(), validUser.getEmail()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByDocumentAndEmail_ShouldReturnFalse_WhenUserDoesNotExist() {
        when(userRepository.existsByDocumentAndEmail(validUser.getDocument(), validUser.getEmail()))
                .thenReturn(Mono.just(false));

        StepVerifier.create(userUseCase.existsByDocumentAndEmail(validUser.getDocument(), validUser.getEmail()))
                .expectNext(false)
                .verifyComplete();
    }

    // -------- getUsersByDocuments --------

    @Test
    void getUsersByDocuments_ShouldReturnUsers_WhenFound() {

        User validUser2 = User.builder()
                .id(3L)
                .name("Juan")
                .lastName("Doe")
                .email("juan.doe@email.com")
                .document("654321")
                .roleId(2)
                .password("3244")
                .baseSalary(500000)
                .build();

        when(userRepository.findByDocument(anyList()))
                .thenReturn(Flux.just(validUser, validUser2));

        StepVerifier.create(userUseCase.getUsersByDocuments(List.of("123456", "654321")))
                .expectNext(validUser)
                .expectNext(validUser2)
                .verifyComplete();
    }

    @Test
    void getUsersByDocuments_ShouldReturnEmpty_WhenNoUsersFound() {
        when(userRepository.findByDocument(anyList()))
                .thenReturn(Flux.empty());

        StepVerifier.create(userUseCase.getUsersByDocuments(List.of("999999")))
                .verifyComplete();
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
