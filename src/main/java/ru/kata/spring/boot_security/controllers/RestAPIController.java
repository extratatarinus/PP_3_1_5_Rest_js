package ru.kata.spring.boot_security.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.entities.Role;
import ru.kata.spring.boot_security.entities.User;
import ru.kata.spring.boot_security.services.UserService;
import ru.kata.spring.boot_security.validator.UserValidator;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RestAPIController {

    private final UserService userService;
    private final UserValidator userValidator;

    @Autowired
    public RestAPIController(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @GetMapping("/user/getCurrentUser")
    public ResponseEntity<User> getCurrentUser(Authentication auth) {
        String email = auth.getName();
        Optional<User> optionalUser = userService.findByEmail(email);
        User authUser = optionalUser.orElse(null);
        return ResponseEntity.ok(authUser);
    }

    @GetMapping("/admin/getUsers")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PostMapping("/admin/add")
    public ResponseEntity<?> add(@RequestBody @Valid User user, BindingResult bindingResult, Authentication auth) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        if (!userService.saveUser(user)) {
            bindingResult.rejectValue("email", "", "Пользователь с таким email уже существует");
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        userValidator.checkRoles(user, auth);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/admin/update")
    public ResponseEntity<User> update(@RequestBody User user, Authentication auth) {
        userValidator.checkRoles(user, auth);
        if (!userService.updateUser(user)) {
            return ResponseEntity.badRequest().body(user);
        }
        return ResponseEntity.ok(userService.findUserById(user.getId()));
    }


    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/admin/findAllRoles")
    public ResponseEntity<List<Role>> findAllRoles() {
        return ResponseEntity.ok(userService.getAllRole());
    }
}
