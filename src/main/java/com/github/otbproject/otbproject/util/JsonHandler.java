package com.github.otbproject.otbproject.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.otbproject.otbproject.App;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class JsonHandler {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private JsonHandler() {}

    // Returns empty Optional if can't read object
    public static <T> Optional<T> readValue(String path, Class<T> className) {
        try {
            T t = MAPPER.readValue(new File(path), className);
            Set<ConstraintViolation<T>> violations = VALIDATOR.validate(t);
            if (!violations.isEmpty()) {
                App.logger.warn("Missing required field(s) in file: " + path);
                return Optional.<T>empty();
            }
            return Optional.ofNullable(t);
        } catch (FileNotFoundException e) {
            App.logger.warn("File not found to parse as JSON: " + path);
        } catch (IOException e) {
            App.logger.catching(e);
        }
        return Optional.<T>empty();
    }

    // Logs exception if can't write object
    public static <T> boolean writeValue(String path, T object) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(path), object);
            return true;
        } catch (IOException e) {
            App.logger.catching(e);
            return false;
        }
    }
}
