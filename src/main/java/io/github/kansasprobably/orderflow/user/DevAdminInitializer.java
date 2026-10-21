package io.github.kansasprobably.orderflow.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bootstrap.admin.email}")
    private String email;

    @Value("${bootstrap.admin.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        User admin = new User(
                normalizedEmail,
                passwordEncoder.encode(password),
                UserRole.ADMIN
        );

        userRepository.save(admin);

    }
}
