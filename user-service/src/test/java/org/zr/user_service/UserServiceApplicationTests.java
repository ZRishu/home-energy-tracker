package org.zr.user_service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zr.user_service.domain.entity.User;
import org.zr.user_service.repository.UserRepository;

@Slf4j
@SpringBootTest
class UserServiceApplicationTests {

    public static final int NUMBER_OF_USERS = 10;
    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Disabled
    @Test
    void createUser() {
        for (int i = 1; i <= NUMBER_OF_USERS; i++) {
            User user = User.builder()
                    .name("user" + i)
                    .surname("surname" + i)
                    .email("user" + i + "example.com")
                    .address(i + "Example street")
                    .alerting(i % 2 == 0)
                    .energyAlertingThreshold(1000.0 + i)
                    .build();
            userRepository.save(user);
        }
        log.info("user repository populated successfully");
    }

}
