package com.neuramatch.user.service;

import com.neuramatch.user.dto.UserDTO;
import com.neuramatch.user.entity.User;
import com.neuramatch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserDTO.fromEntity(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getActiveUsers() {
        return userRepository.findAllActiveUsers().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUserStatus(Long id, User.UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setStatus(status);
        user = userRepository.save(user);

        log.info("Updated status for user {}: {}", user.getEmail(), status);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);

        log.info("Soft deleted user: {}", user.getEmail());
    }

    public long countUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
}
