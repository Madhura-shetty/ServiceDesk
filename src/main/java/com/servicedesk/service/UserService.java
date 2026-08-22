package com.servicedesk.service;

import com.servicedesk.dto.UserCreateRequest;
import com.servicedesk.dto.UserResponse;
import com.servicedesk.entity.Role;
import com.servicedesk.entity.User;
import com.servicedesk.exception.InvalidRequestException;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserCreateRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new InvalidRequestException("A user with this email already exists");
        });

        User user = new User(request.getName(), request.getEmail(), request.getRole());
        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return new UserResponse(findUserEntityById(id));
    }

    public List<UserResponse> getAgents() {
        return userRepository.findByRole(Role.SUPPORT_AGENT).stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
