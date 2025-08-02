package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PostService {

    private final Map<Long, Post> posts = new LinkedHashMap<>();

    private final UserService userService;

    public Collection<Post> findAll(int size, String sort, int from) {

        SortOrder order = SortOrder.from(sort);

        Stream<Post> result = posts.values().stream();

        if (order == SortOrder.DESCENDING) {
            result = result.sorted(Comparator.comparing(Post::getPostDate).reversed());
        } else {
            result  = result.sorted(Comparator.comparing(Post::getPostDate));
        }

        return result
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        userService.findUserById(post.getAuthorId())
                        .orElseThrow(() -> new ConditionsNotMetException("Автор с id = " +
                                post.getAuthorId() + " не найден"));
        post.setId(getNextId());
        post.setPostDate(Instant.now());

        posts.put(post.getId(), post);

        return post;
    }

    public Post update(@RequestBody Post newPost) {

        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());

            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }

            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }

        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");

    }

    public Post findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("id должен быть передан");
        }

        return Optional.ofNullable(posts.get(id)).orElseThrow(
                () -> new ConditionsNotMetException("Поста с таким id нет")
        );
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }

}
