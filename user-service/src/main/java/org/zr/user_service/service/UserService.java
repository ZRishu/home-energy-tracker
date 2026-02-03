package org.zr.user_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zr.user_service.domain.dto.UserDto;
import org.zr.user_service.domain.entity.User;
import org.zr.user_service.repository.UserRepository;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(UserDto input) {
        final User createdUser = User.builder()
                .name(input.name())
                .surname(input.surname())
                .email(input.email())
                .address(input.address())
                .alerting(input.alerting())
                .energyAlertingThreshold(input.energyAlertingThreshold())
                .build();

        User savedUser = userRepository.save(createdUser);
        return toDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public void updateUser(Long id, UserDto input) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.setName(input.name());
        user.setSurname(input.surname());
        user.setEmail(input.email());
        user.setAddress(input.address());
        user.setAlerting(input.alerting());
        user.setEnergyAlertingThreshold(input.energyAlertingThreshold());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        userRepository.delete(user);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }
}

