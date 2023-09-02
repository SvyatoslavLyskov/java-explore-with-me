package ru.practicum.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.dto.UserDto;
import ru.practicum.utils.ObjectMapper;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(UserDto dto) {
        User user = ObjectMapper.toUser(dto);
        User savedUser = userRepository.save(user);
        log.info("Создан пользователь {}.", savedUser);
        return ObjectMapper.toUserDto(savedUser);
    }

    public List<UserDto> findAllUsers(Optional<List<Long>> ids, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (ids.isEmpty()) {
            Page<User> users = userRepository.findAll(pageRequest);
            log.info("Список пользователей получен.");
            return ObjectMapper.toUserDtoList(users);
        } else {
            List<User> userList = userRepository.findByIdInOrderByIdAsc(ids.get(), pageRequest);
            log.info("Список пользователей по id успешно получен.");
            return ObjectMapper.toUserDtoList(userList);
        }
    }

    @Transactional
    public void removeUserById(Long id) {
        checkUserAvailability(id);
        userRepository.deleteById(id);
        log.info("Пользователь {} успешно удален.", id);
    }

    public void checkUserAvailability(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь", id);
        }
    }
}