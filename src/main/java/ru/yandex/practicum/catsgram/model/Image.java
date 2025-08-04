package ru.yandex.practicum.catsgram.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Image {

    private Long id;

    private long postId;

    private String originalFileName;

    private String filePath;
}
