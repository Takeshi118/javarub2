package com.example.photoauth.controller;


import com.example.photoauth.model.AppUser;
import com.example.photoauth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam String username,
                                               @RequestParam MultipartFile photo) {
        try {
            // Определяем абсолютный путь для папки uploads
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadFolder = new File(uploadDir);

            // Создаём папку uploads, если она не существует
            if (!uploadFolder.exists()) {
                boolean isCreated = uploadFolder.mkdir();
                if (!isCreated) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create upload directory");
                }
            }

            // Формируем путь для сохранения файла
            String photoPath = uploadDir + photo.getOriginalFilename();
            File destinationFile = new File(photoPath);

            // Сохраняем файл
            photo.transferTo(destinationFile);

            // Сохраняем данные пользователя в базе данных
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPhotoPath(photoPath);
            userRepository.save(user);


            return ResponseEntity.ok("User registered successfully");
        } catch (IOException e) {
            e.printStackTrace(); // Логируем исключение в консоль
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving photo");
        } catch (Exception e) {
            e.printStackTrace(); // Ловим любые другие исключения
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String photoName) {
        var user = userRepository.findByUsername(photoName);
        if (user.isPresent()) {
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid photo name");
    }
}

