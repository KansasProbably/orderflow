package io.github.kansasprobably.orderflow.user.mapper;

import io.github.kansasprobably.orderflow.user.User;
import io.github.kansasprobably.orderflow.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
