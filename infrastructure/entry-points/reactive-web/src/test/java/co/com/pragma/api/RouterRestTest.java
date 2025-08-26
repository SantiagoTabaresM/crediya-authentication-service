package co.com.pragma.api;

import co.com.pragma.api.config.UserPath;
import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.api.mapper.UserDTOMapperImpl;
import co.com.pragma.api.validation.ReactiveValidator;
import co.com.pragma.api.validation.UserValidator;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.IUserUseCase;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, UserHandler.class})
@EnableConfigurationProperties(UserPath.class)
@Import({UserDTOMapperImpl.class, UserValidator.class, ReactiveValidator.class})
@ImportAutoConfiguration(exclude = {ReactiveSecurityAutoConfiguration.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private IUserUseCase userUseCase;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private UserDTOMapper userDTOMapper;


    private final String USERS = "/api/v1/users";


    private final User userOne = User.builder()
            .id(1L)
            .name("name1")
            .lastName("lastName1")
            .document("12345")
            .birthDate(LocalDate.now())
            .phone("4543543")
            .email("email1@gmail.com")
            .address("address21")
            .baseSalary(1000)
            .build();

    private final User userTwo = User.builder()
            .id(2L)
            .name("name2")
            .lastName("lastName2")
            .document("1543534")
            .birthDate(LocalDate.now())
            .phone("31231312")
            .email("email2@gmail.com")
            .address("address2")
            .baseSalary(2000)
            .build();



    @Test
    void shouldGetAllUsers() {

        when(userUseCase.getAllUsers()).thenReturn(Flux.just(userOne, userTwo));


        webTestClient.get()
                .uri(USERS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .hasSize(2)
                .value(users -> {
                    Assertions.assertThat(users).isNotEmpty();
                });

    }

    @Test
    void shouldGetUserById() {
        Long id = 1L;

        when(userUseCase.getUserById(id)).thenReturn(Mono.just(userOne));

        webTestClient.get()
                .uri(USERS + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(response -> Assertions.assertThat(response.id()).isEqualTo(id));
    }

    @Test
    void shouldPostSaveUser() {
           when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.just(userOne));
           when(userUseCase.existsByEmail(any(String.class))).thenReturn(Mono.just(false));

           webTestClient.post()
                .uri(USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDTOMapper.toUserDTO(userOne))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(saved -> Assertions.assertThat(saved.name()).isEqualTo("name1"));
    }

    @Test
    void shouldPutUpdateUser() {
        User user = User.builder()
                .id(1L)
                .name("name1")
                .lastName("lastName1")
                .document("12345")
                .birthDate(LocalDate.now())
                .phone("4543543")
                .email("email1@gmail.com")
                .address("address21")
                .baseSalary(1000)
                .build();

        when(userUseCase.updateUser(any(User.class))).thenReturn(Mono.just(user));

        webTestClient.put()
                .uri(USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .value(updated -> Assertions.assertThat(updated.name()).isEqualTo(userOne.getName()));
    }

    @Test
    void shouldDeleteUser() {
        Long id = 1L;
        when(userUseCase.deleteUser(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(USERS + "/" + id)
                .exchange()
                .expectStatus().isNoContent();
    }
}
