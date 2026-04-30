package com.ecommerce.user.service.impl;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.repository.UserProfileRepository;
import com.ecommerce.user.service.UserService;
import org.springframework.stereotype.Service;
@Service
public class UserServiceImpl implements UserService {
    private final UserProfileRepository repository;
    public UserServiceImpl(UserProfileRepository repository) { this.repository = repository; }
    public UserProfile getById(Long id) { return repository.findById(id).orElseThrow(); }
}
