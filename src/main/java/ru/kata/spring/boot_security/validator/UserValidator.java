package ru.kata.spring.boot_security.validator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ru.kata.spring.boot_security.entities.User;
import ru.kata.spring.boot_security.services.UserService;

import java.util.Optional;

@Component
public class UserValidator implements Validator {

    private final UserService userService;

    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "", "Поле обязательно для заполнения");
        if (user.getEmail().length() < 3 || user.getEmail().length() > 32) {
            errors.rejectValue("email", "", "Email должен быть длиной от 3 до 32 символов");
        }

        Optional<User> optionalUser = userService.findByEmail(user.getEmail());
        if (optionalUser.isPresent() && !optionalUser.get().getId().equals(user.getId())) {
            errors.rejectValue("email", "", "Пользователь с такой почтой уже существует");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "", "Поле не может быть пустым");
        if (user.getPassword() == null || user.getPassword().length() < 4 || user.getPassword().length() > 32) {
            errors.rejectValue("password", "", "Пароль должен быть от 4 до 32 символов");
        }

        if (user.getAge() < 0) {
            errors.rejectValue("age", "", "Возраст не может быть отрицательным");
        }
    }

    public void validateOnReg(Object o, Errors errors) {
        User user = (User) o;

        validate(o, errors);

        if (user.getPasswordConfirm() == null || !user.getPasswordConfirm().equals(user.getPassword())) {
            errors.rejectValue("passwordConfirm", "", "Пароли не совпадают");
        }
    }

    public void checkRoles(User user, Authentication auth) {
        if (user.getRoles().isEmpty() &&
                user.getEmail().equals(auth.getName()) &&
                auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            user.addRole(userService.findRoleByName("ROLE_ADMIN"));
        } else if (user.getRoles().isEmpty()) {
            user.addRole(userService.findRoleByName("ROLE_USER"));
        }
    }
}
