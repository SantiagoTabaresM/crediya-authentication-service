package co.com.pragma.usecase.auth;

import co.com.pragma.model.role.Role;
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
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

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
    private AuthUseCase authUseCase;

    private User userDb;

    @BeforeEach
    void setUp() {
        userDb = User.builder()
                .id(1L)
                .name("John")
                .lastName("Doe")
                .email("test@email.com")
                .document("123456")
                .roleId(1)
                .password("encodedPass")
                .baseSalary(5000000)
                .build();

    }

    // ---------- validateUser ----------

    @Test
    void validateUser_ShouldReturnUser_WhenCredentialsAreCorrect() {
        User loginUser = new User();
        loginUser.setEmail("test@email.com");
        loginUser.setPassword("rawPass");

        when(userRepository.findByEmail("test@email.com"))
                .thenReturn(Mono.just(userDb));

        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });

        when(passwordEncoder.matches("rawPass", "encodedPass"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(authUseCase.validateUser(loginUser))
                .expectNext(userDb)
                .verifyComplete();
    }

    @Test
    void validateUser_ShouldError_WhenUserNotFound() {
        User loginUser = new User();
        loginUser.setEmail("notfound@email.com");
        loginUser.setPassword("any");

        when(userRepository.findByEmail("notfound@email.com"))
                .thenReturn(Mono.empty());

        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });

        StepVerifier.create(authUseCase.validateUser(loginUser))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("email");
                })
                .verify();
    }

    @Test
    void validateUser_ShouldError_WhenPasswordIncorrect() {
        User loginUser = new User();
        loginUser.setEmail("test@email.com");
        loginUser.setPassword("wrong");

        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });


        when(userRepository.findByEmail("test@email.com"))
                .thenReturn(Mono.just(userDb));

        when(passwordEncoder.matches("wrong", "encodedPass"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(authUseCase.validateUser(loginUser))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("password");
                })
                .verify();
    }

    @Test
    void validateUser_ShouldError_WhenEmailInvalid() {
        User loginUser = new User();
        loginUser.setEmail("invalid-email");
        loginUser.setPassword("rawPass");

        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });

        StepVerifier.create(authUseCase.validateUser(loginUser))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("email");
                })
                .verify();
    }

    @Test
    void validateUser_ShouldError_WhenPasswordBlank() {
        User loginUser = new User();
        loginUser.setEmail("test@email.com");
        loginUser.setPassword("   "); // vacío
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<User>>) invocation.getArgument(0)).get();
        });
        StepVerifier.create(authUseCase.validateUser(loginUser))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("password");
                })
                .verify();
    }

    // ---------- getRoleInfo ----------

    @Test
    void getRoleInfo_ShouldReturnAuth_WhenRoleExists() {
        Role role = Role.builder()
                .roleId(1)
                .description("Administrator role")
                .name("ADMIN")
                .build();

        when(roleRepository.findById(1)).thenReturn(Mono.just(role));

        StepVerifier.create(authUseCase.getRoleInfo(userDb))
                .expectNextMatches(auth ->
                        auth.getDocument().equals("123456") &&
                                auth.getRole().equals("ADMIN")
                )
                .verifyComplete();
    }

    @Test
    void getRoleInfo_ShouldError_WhenRoleNotFound() {
        when(roleRepository.findById(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.getRoleInfo(userDb))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey("role");
                })
                .verify();
    }




}
