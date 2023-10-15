package ru.practicum.user.service;

import io.micrometer.core.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User user = userRepository.findByName(newUserRequest.getName());

        if (user != null && user.getName().equalsIgnoreCase(newUserRequest.getName())) {
            throw new ConflictException("Имя " + newUserRequest.getName() + " уже занято",
                    "Integrity constraint has been violated");
        }

        User savedUser = userRepository.save(UserMapper.dtoToEntity(newUserRequest));
        return UserMapper.entityToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(@Nullable List<Long> ids, Integer from, Integer size) {
        Pageable page = PageRequest.of((int) from / size, size);

        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(page).stream()
                    .map(UserMapper::entityToDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, page).stream()
                    .map(UserMapper::entityToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConflictException(
                        "Пользователь с ID " + userId + " не существует", "object not found"));
        userRepository.delete(user);
    }
}
