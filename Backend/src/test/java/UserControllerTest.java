package org.example.controller;

import org.example.controller.UserController;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserRepository userRepository;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userController = new UserController();
        userController.userRepository = userRepository;
    }

    @Test
    void register_savesNewUser_whenEmailNotTaken() {
        User newUser = new User("new@stackfive.com", "pass123", "New User", "STAFF");

        when(userRepository.findByEmail("new@stackfive.com")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(newUser);

        ResponseEntity<?> response = userController.register(newUser);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        User existingUser = new User("test@stackfive.com", "password123", "Test User", "STAFF");

        when(userRepository.findByEmail("test@stackfive.com")).thenReturn(Optional.of(existingUser));

        ResponseEntity<?> response = userController.register(existingUser);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }
}
