package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserRepositoryAdapterTest {

    private UserReactiveRepository repository;
    private ObjectMapper mapper;
    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(UserReactiveRepository.class);
        mapper = Mockito.mock(ObjectMapper.class);
        adapter = new UserRepositoryAdapter(repository, mapper);
    }

    @Test
    void save_ShouldReturnUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Test User");

        // mock mapeos
        when(mapper.map(any(User.class), eq(UserEntity.class))).thenReturn(entity);
        when(mapper.map(any(UserEntity.class), eq(User.class))).thenReturn(user);

        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(user))
                .expectNextMatches(savedUser -> savedUser.getName().equals("Test User"))
                .verifyComplete();

        verify(repository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void findAll_ShouldReturnUsers() {
        UserEntity entity1 = new UserEntity();
        entity1.setId(1L);
        entity1.setName("User1");

        UserEntity entity2 = new UserEntity();
        entity2.setId(2L);
        entity2.setName("User2");

        User user1 = new User();
        user1.setId(1L);
        user1.setName("User1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User2");

        when(repository.findAll()).thenReturn(Flux.just(entity1, entity2));
        when(mapper.map(entity1, User.class)).thenReturn(user1);
        when(mapper.map(entity2, User.class)).thenReturn(user2);

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(u -> u.getName().equals("User1"))
                .expectNextMatches(u -> u.getName().equals("User2"))
                .verifyComplete();

        verify(repository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Found User");

        User user = new User();
        user.setId(1L);
        user.setName("Found User");

        when(repository.findById(1L)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(user);

        StepVerifier.create(adapter.findById(1L))
                .expectNextMatches(u -> u.getName().equals("Found User"))
                .verifyComplete();

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void deleteById_ShouldComplete() {
        when(repository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(1L))
                .verifyComplete();

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        when(repository.existsByEmail("test@mail.com")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByEmail("test@mail.com"))
                .expectNext(true)
                .verifyComplete();

        verify(repository, times(1)).existsByEmail("test@mail.com");
    }

    @Test
    void existsByDocumentAndEmail_ShouldReturnFalse_WhenNotExists() {
        when(repository.existsByDocumentAndEmail("123", "test@mail.com"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsByDocumentAndEmail("123", "test@mail.com"))
                .expectNext(false)
                .verifyComplete();

        verify(repository, times(1))
                .existsByDocumentAndEmail("123", "test@mail.com");
    }
}
