package com.app.venus.modules.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.user.domain.User;

@Service
public class UserService {
    private final DemoCurrentUserService demoCurrentUserService;

    public UserService(DemoCurrentUserService demoCurrentUserService) {
        this.demoCurrentUserService = demoCurrentUserService;
    }

    @Transactional
    public User getCurrentUser() {
        return demoCurrentUserService.currentDriver();
    }

    @Transactional
    public User updateCurrentUser(String fullName, String avatarUrl) {
        User user = demoCurrentUserService.currentDriver();
        user.updateProfile(fullName, avatarUrl);
        return user;
    }
}
