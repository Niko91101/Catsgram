package ru.yandex.practicum.catsgram.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Builder
public class Post {

    private Long id;

    private long authorId;

    private String description;

    private Instant postDate;
}
