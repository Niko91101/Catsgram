package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.ImageFileException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Image;
import ru.yandex.practicum.catsgram.model.ImageData;
import ru.yandex.practicum.catsgram.model.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final PostService postService;

    private final Map<Long, Image> images = new HashMap<>();

    private final String imageDirectory = "./justImages";

    public List<Image> getPostImages(long postId) {

        if (postId <= 0) {
            throw new ParameterNotValidException("postId", "Значение должно быть больше 0");
        }
        List<Image> result = images.values()
                .stream()
                .filter(image -> image.getPostId() == postId)
                .toList();

        if (result.isEmpty()) {
            throw new NotFoundException("Изображения для поста с id = " + postId + "не найдены");
        }

        return result;
    }

    public List<Image> saveImages(long postId, List<MultipartFile> files) {

        if (postId <= 0) {
            throw new ParameterNotValidException("postId", "значение должно быть больше 0");
        }

        if (files == null || files.isEmpty()) {
            throw new ConditionsNotMetException("необходимо загрузить хотя бы один файл!");
        }
        return files.stream()
                .map(file -> saveImage(postId, file))
                .collect(Collectors.toList());
    }

    public ImageData getImageData(long imageId) {
        if (!images.containsKey(imageId)) {
            throw new ConditionsNotMetException("Изображения с id = " + imageId + " не найдено");
        }

        Image image = images.get(imageId);

        byte[] data = loadFile(image);

        return new ImageData(data, image.getOriginalFileName());


    }

    private byte[] loadFile(Image image) {
        Path path = Paths.get(image.getFilePath());

        if (Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new ImageFileException("Ошибка чтения файла " + image.getId()
                        + ", name:" + image.getOriginalFileName(), e);
            }
        } else {
            throw new NotFoundException("Файл не найден. Id: " + image.getId()
                    + ", name: " + image.getOriginalFileName());
        }
    }

    private Image saveImage(long postId, MultipartFile file) {
        Post post = postService.findById(postId);

        Path filePath = saveFile(file, post);

        long imageId = getNextId();

        Image image = new Image();

        image.setId(imageId);
        image.setPostId(postId);
        image.setFilePath(filePath.toString());
        image.setOriginalFileName(file.getOriginalFilename());

        images.put(image.getId(), image);

        return image;
    }

    private Path saveFile(MultipartFile file, Post post) {
        try {
            String uniqueFileName = String.format("%d.%s", Instant.now().toEpochMilli(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename()));

            Path uploadFile = Paths.get(imageDirectory, String.valueOf(post.getAuthorId()), post.getId().toString());
            Path filePath = uploadFile.resolve(uniqueFileName);

            if (!Files.exists(filePath)) {
                Files.createDirectories(uploadFile);
            }

            file.transferTo(filePath);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getNextId() {
        long currentMaxId = images.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}
