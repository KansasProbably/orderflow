package io.github.kansasprobably.orderflow.user;

import io.github.kansasprobably.orderflow.user.dto.UserCreateRequest;
import io.github.kansasprobably.orderflow.user.exception.UserAlreadyExistsException;
import io.github.kansasprobably.orderflow.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWithEncodedPassword() {
        UserCreateRequest request =  new UserCreateRequest(
                "admin@example.com",
                "password",
                UserRole.ADMIN
        );

        when(userRepository.existsByEmail("admin@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("$2a$10$encoded-password");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPasswordHash())
                .isEqualTo("$2a$10$encoded-password");

        assertThat(savedUser.getPasswordHash())
                .isNotEqualTo("password");

        verify(passwordEncoder)
                .encode("password");
    }

    @Test
    void shouldNormalizeEmailBeforeSaving() {
        UserCreateRequest request =  new UserCreateRequest(
                " adMIn@exAMPle.com ",
                "password",
                UserRole.ADMIN
        );

        when(userRepository.existsByEmail("admin@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("$2a$10$encoded-password");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo("admin@example.com");

        verify(userRepository)
                .existsByEmail("admin@example.com");
    }

    @Test
    void shouldThrowThenEmailAlreadyExists() {
        UserCreateRequest request =  new UserCreateRequest(
                "admin@example.com",
                "password",
                UserRole.ADMIN
        );

        when(userRepository.existsByEmail("admin@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .saveAndFlush(any(User.class));

        verify(userMapper, never())
                .toResponse(any(User.class));
    }
}
