package com.app.venus.modules.user.interfaces.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.user.application.UserService;
import com.app.venus.modules.user.interfaces.dto.request.UpdateCurrentUserRequest;
import com.app.venus.modules.user.interfaces.dto.response.UserResponse;
import com.app.venus.shared.web.ApiPaths;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/me")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse getCurrentUser() {
        return UserResponse.from(userService.getCurrentUser());
    }

    @PatchMapping
    public UserResponse updateCurrentUser(@Valid @RequestBody UpdateCurrentUserRequest request) {
        return UserResponse.from(userService.updateCurrentUser(request.fullName(), request.avatarUrl()));
    }
}
