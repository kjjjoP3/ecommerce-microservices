package com.ecommerce.user.controller;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController @RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }
    @GetMapping("/{id}") public UserResponse get(@PathVariable Long id) {
        UserProfile profile = userService.getById(id);
        return new UserResponse(profile.getId(), profile.getEmail(), profile.getFullName());
    }
}
