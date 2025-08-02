package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }


    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Email должен быть указан");
        }

        if (users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new DuplicatedDataException("Этот Email уже используется");
        }

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User oldUser = users.get(newUser.getId());

        if (users.containsKey(newUser.getId())) {

            if (users.values().stream()
                    .anyMatch(user -> user.getEmail().equals(newUser.getEmail()))) {
                throw new DuplicatedDataException("Этот email уже используется");
            }

            if (!(newUser.getEmail() == null)) {
                oldUser.setEmail(newUser.getEmail());
            }

            if (!(newUser.getUsername() == null)) {
                oldUser.setUsername(newUser.getUsername());
            }

            if (!(newUser.getPassword() == null)) {
                oldUser.setPassword(newUser.getPassword());
            }

            return oldUser;
        }

        throw new ConditionsNotMetException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public User findById(Long id) {

        if (id == null) {
            throw  new ConditionsNotMetException("id должен быть передан");
        }

    return Optional.ofNullable(users.get(id)).orElseThrow(
            () -> new ConditionsNotMetException("Пользователя с таки id нет")
    );
    }

    private long getNextId() {
        long maxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++maxId;
    }


}
