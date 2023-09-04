package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
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

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(UserDto dto) {
        User user = ObjectMapper.toUser(dto);
        User savedUser = userRepository.save(user);
        log.info("Создан пользователь {}.", savedUser);
        return ObjectMapper.toUserDto(savedUser);
    }

    public List<UserDto> findAllUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (ids == null) {
            Page<User> users = userRepository.findAll(pageRequest);
            log.info("Список пользователей получен.");
            return ObjectMapper.toUserDtoList(users);
        } else {
            List<User> userList = userRepository.findByIdInOrderByIdAsc(ids, pageRequest);
            log.info("Список пользователей по идентификатору получен.");
            return ObjectMapper.toUserDtoList(userList);
        }
    }

    @Transactional
    public void removeUserById(Long id) {
        checkUserAvailability(userRepository, id);
        userRepository.deleteById(id);
        log.info("Пользователь {} удален.", id);
    }

    public static void checkUserAvailability(UserRepository repository, Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Пользователь", id);
        }
    }
}
