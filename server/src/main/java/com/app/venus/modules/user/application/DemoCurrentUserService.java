package com.app.venus.modules.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.shared.domain.Role;

@Service
public class DemoCurrentUserService {
    public static final String DEMO_DRIVER_ID = "usr_demo_driver";

    private final UserRepository userRepository;

    public DemoCurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String currentDriverId() {
        return DEMO_DRIVER_ID;
    }

    @Transactional
    public User currentDriver() {
        return userRepository.findById(DEMO_DRIVER_ID)
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        DEMO_DRIVER_ID,
                        "Demo Driver",
                        "driver@volzen.test",
                        Role.DRIVER,
                        "https://cdn.volzen.vn/avatars/usr_demo_driver.jpg")));
    }
}
