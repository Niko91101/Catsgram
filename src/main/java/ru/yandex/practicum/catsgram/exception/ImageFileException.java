package ru.yandex.practicum.catsgram.exception;

import java.io.IOException;

public class ImageFileException extends RuntimeException {
    public ImageFileException(String message, IOException e) {
        super(message);
    }
}
