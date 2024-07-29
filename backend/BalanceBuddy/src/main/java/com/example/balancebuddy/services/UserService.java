package com.example.balancebuddy.services;

import com.example.balancebuddy.entities.MyUser;
import com.example.balancebuddy.entities.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService implements UserDetailsManager {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Transactional
    public List<MyUser> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM MyUser u", MyUser.class).getResultList();
    }

    @Transactional
    public MyUser createUser(MyUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        entityManager.persist(user);
        return user;
    }

    @Transactional
    public Optional<MyUser> findByID(Integer id) {
        return Optional.ofNullable(entityManager.find(MyUser.class, id));
    }

    @Transactional
    public Optional<MyUser> findByEmail(String email) {
        TypedQuery<MyUser> query = entityManager.createQuery("SELECT u FROM MyUser u WHERE u.email = :email", MyUser.class);
        query.setParameter("email", email);
        List<MyUser> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        findByEmail(email).ifPresent(entityManager::remove);
    }

    @Transactional
    public MyUser updateUser(MyUser user) {
        MyUser existingUser = entityManager.find(MyUser.class, user.getUserID());

        if (existingUser != null) {
            existingUser.setFirstname(user.getFirstname());
            existingUser.setLastname(user.getLastname());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            entityManager.merge(existingUser);
            return existingUser;
        } else {
            throw new IllegalArgumentException("User not found!");
        }
    }

    @Transactional
    public void myChangePassword(String email, String oldPassword, String newPassword) throws Exception {
        Optional<MyUser> userOptional = findByEmail(email);

        // If the user exists in the database, continue changing the passwords
        if (userOptional.isPresent()) {
            MyUser user = userOptional.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                if (oldPassword.equals(newPassword)) {
                    throw new Exception("New password cannot be the same as the old password");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                entityManager.merge(user);
            } else {
                throw new IllegalArgumentException("Old password is incorrect!");
            }
        } else {
            throw new UsernameNotFoundException("User not found!");
        }
    }

    public boolean userExists(String email) {
        return findByEmail(email).isPresent();
    }

    public MyUser loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<MyUser> userOptional = findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }
        return userOptional.get();
    }

    @Override
    public void createUser(UserDetails userDetails) {
        MyUser myUser = (MyUser) userDetails;
        createUser(myUser);
    }

    @Override
    public void updateUser(UserDetails userDetails) {
        MyUser myUser = (MyUser) userDetails;
        updateUser(myUser);
    }

    @Override
    public void deleteUser(String username) {
        deleteUserByEmail(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
    }

}
