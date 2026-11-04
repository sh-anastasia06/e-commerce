package io.github.shanastasia06.userservice.user.repository;

import io.github.shanastasia06.userservice.TestcontainersConfiguration;
import io.github.shanastasia06.userservice.user.model.Role;
import io.github.shanastasia06.userservice.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

// starts only the jpa slice of an app
// every test runs in inside transaction that is rolled back at the end
@DataJpaTest
@Import(TestcontainersConfiguration.class)
// use configured datasource (testcontainers)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void save_newUser_setsIdRoleAndCreatedAtInDatabase() {
        // Arrange
        // saveAndFlush makes INSERT in moment (doesn't wait for commit)
        User saved = userRepository.saveAndFlush(newUser("anna@example.com"));
        UUID id = saved.getId();

        // Act
        // get data from db, not from cache
        entityManager.clear();
        User loaded = userRepository.findById(id).orElseThrow();

        // Assert
        assertThat(loaded.getId()).isEqualTo(id);
        assertThat(loaded.getRole()).isEqualTo(Role.USER);
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        // Arrange
        userRepository.saveAndFlush(newUser("anna@example.com"));

        //Act
        entityManager.clear();
        Optional<User> result = userRepository.findByEmail("anna@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Anna");
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrueOnlyForSavedEmail() {
        // Arrange
        userRepository.saveAndFlush(newUser("anna@example.com"));

        // Assert
        assertThat(userRepository.existsByEmail("anna@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void save_duplicateEmail_throwsException() {
        userRepository.saveAndFlush(newUser("anna@example.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("anna@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User newUser(String email) {
        return User.builder()
                .email(email)
                .passwordHash("hash")
                .firstName("Anna")
                .lastName("Smith")
                .build();
    }
}
