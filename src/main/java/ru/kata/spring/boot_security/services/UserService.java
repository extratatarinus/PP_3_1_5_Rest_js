package ru.kata.spring.boot_security.services;

import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.configs.BCryptCoder;
import ru.kata.spring.boot_security.entities.Role;
import ru.kata.spring.boot_security.entities.User;
import ru.kata.spring.boot_security.repositories.RoleRepository;
import ru.kata.spring.boot_security.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserServiceInt, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        Optional<User> userFromDb = userRepository.findById(id);
        return userFromDb.orElse(new User());
    }

    @Override
    @Transactional
    public boolean saveUser(User user) {
        Optional<User> currentUser = findByEmail(user.getEmail());
        if (currentUser.isPresent()) {
            return false;
        }
        user.setPassword(BCryptCoder.passwordEncoder().encode(user.getPassword()));
        try {
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("\nСохранение не удалось. Возможно имя пользователя уже существует в базе\n");
            return false;
        }
        return true;
    }

    @Transactional
    public boolean updateUser(User user) {
        Optional<User> existingUserOpt = userRepository.findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            return false;
        }

        User existingUser = existingUserOpt.get();

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        } else if (!user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(BCryptCoder.passwordEncoder().encode(user.getPassword()));
        }

        if (!user.getEmail().equals(existingUser.getEmail())) {
            Optional<User> currentUser = findByEmail(user.getEmail());
            if (currentUser.isPresent() && !currentUser.get().getId().equals(user.getId())) {
                return false;
            }
        }

        try {
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("\nСохранение не удалось. Возможно имя пользователя уже существует в базе\n");
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void saveRole(Role role) {
        roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRole() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not Found!!!");
        }
        User user = optionalUser.get();
        Hibernate.initialize(user.getRoles());
        return user;
    }

    public Role findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

}
