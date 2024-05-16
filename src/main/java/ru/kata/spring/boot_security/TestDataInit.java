package ru.kata.spring.boot_security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.entities.Role;
import ru.kata.spring.boot_security.entities.User;
import ru.kata.spring.boot_security.services.UserService;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component
public class TestDataInit {

    private final UserService userService;


    @Autowired
    public TestDataInit(UserService userService) {
        this.userService = userService;

    }

    @PostConstruct
    public void init() {
        final Role ROLE_USER = new Role("ROLE_USER");
        final Role ROLE_ADMIN = new Role("ROLE_ADMIN");
        userService.saveRole(new Role("ROLE_GUEST"));

        userService.saveRole(ROLE_USER);
        userService.saveRole(ROLE_ADMIN);


        userService.saveUser(new User("Жмышенко", "Валерий", 54, "admin@mail.com", "admin",
                new HashSet<>(Set.of(ROLE_ADMIN))));

        userService.saveUser(new User("Аюбджони", "Рабджазода", 13, "user@mail.com", "user",
                new HashSet<>(Set.of(ROLE_USER))));
    }
}