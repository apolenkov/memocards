package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserSettingsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserSettingsJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSettingsJpaAdapter Tests")
class UserSettingsJpaAdapterTest {

    @Mock
    private UserSettingsJpaRepository repo;

    private UserSettingsJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserSettingsJpaAdapter(repo);
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {
        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // Given
            Class<UserSettingsJpaAdapter> clazz = UserSettingsJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.context.annotation.Profile.class))
                    .isTrue();
            org.springframework.context.annotation.Profile profile =
                    clazz.getAnnotation(org.springframework.context.annotation.Profile.class);
            assertThat(profile.value()).contains("dev", "jpa", "prod");
        }

        @Test
        @DisplayName("Should be annotated with Repository")
        void shouldBeAnnotatedWithRepository() {
            // Given
            Class<UserSettingsJpaAdapter> clazz = UserSettingsJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.stereotype.Repository.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Find Preferred Locale Code Tests")
    class FindPreferredLocaleCodeTests {
        @Test
        @DisplayName("FindPreferredLocaleCode should return locale code when settings exist")
        void findPreferredLocaleCodeShouldReturnLocaleCodeWhenSettingsExist() {
            // Given
            long userId = 1L;
            String expectedLocale = "ru";
            UserSettingsEntity entity = new UserSettingsEntity();
            entity.setUserId(userId);
            entity.setPreferredLocaleCode(expectedLocale);

            when(repo.findByUserId(userId)).thenReturn(Optional.of(entity));

            // When
            Optional<String> result = adapter.findPreferredLocaleCode(userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedLocale);
            verify(repo).findByUserId(userId);
        }

        @Test
        @DisplayName("FindPreferredLocaleCode should return empty when settings do not exist")
        void findPreferredLocaleCodeShouldReturnEmptyWhenSettingsDoNotExist() {
            // Given
            long userId = 1L;
            when(repo.findByUserId(userId)).thenReturn(Optional.empty());

            // When
            Optional<String> result = adapter.findPreferredLocaleCode(userId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findByUserId(userId);
        }

        @Test
        @DisplayName("FindPreferredLocaleCode should return empty when locale code is null")
        void findPreferredLocaleCodeShouldReturnEmptyWhenLocaleCodeIsNull() {
            // Given
            long userId = 1L;
            UserSettingsEntity entity = new UserSettingsEntity();
            entity.setUserId(userId);
            entity.setPreferredLocaleCode(null);

            when(repo.findByUserId(userId)).thenReturn(Optional.of(entity));

            // When
            Optional<String> result = adapter.findPreferredLocaleCode(userId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Save Preferred Locale Code Tests")
    class SavePreferredLocaleCodeTests {
        @Test
        @DisplayName("SavePreferredLocaleCode should update existing settings")
        void savePreferredLocaleCodeShouldUpdateExistingSettings() {
            // Given
            long userId = 1L;
            String localeCode = "es";
            UserSettingsEntity existingEntity = new UserSettingsEntity();
            existingEntity.setUserId(userId);
            existingEntity.setPreferredLocaleCode("en");

            when(repo.findByUserId(userId)).thenReturn(Optional.of(existingEntity));
            when(repo.save(any(UserSettingsEntity.class))).thenReturn(existingEntity);

            // When
            adapter.savePreferredLocaleCode(userId, localeCode);

            // Then
            assertThat(existingEntity.getPreferredLocaleCode()).isEqualTo(localeCode);
            verify(repo).findByUserId(userId);
            verify(repo).save(existingEntity);
        }

        @Test
        @DisplayName("SavePreferredLocaleCode should create new settings when none exist")
        void savePreferredLocaleCodeShouldCreateNewSettingsWhenNoneExist() {
            // Given
            long userId = 1L;
            String localeCode = "fr";

            when(repo.findByUserId(userId)).thenReturn(Optional.empty());
            when(repo.save(any(UserSettingsEntity.class))).thenReturn(new UserSettingsEntity());

            // When
            adapter.savePreferredLocaleCode(userId, localeCode);

            // Then
            verify(repo).findByUserId(userId);
            verify(repo).save(any(UserSettingsEntity.class));
        }

        @Test
        @DisplayName("SavePreferredLocaleCode should handle null locale code")
        void savePreferredLocaleCodeShouldHandleNullLocaleCode() {
            // Given
            long userId = 1L;
            String localeCode = null;

            when(repo.findByUserId(userId)).thenReturn(Optional.empty());
            when(repo.save(any(UserSettingsEntity.class))).thenReturn(new UserSettingsEntity());

            // When
            adapter.savePreferredLocaleCode(userId, localeCode);

            // Then
            verify(repo).findByUserId(userId);
            verify(repo).save(any(UserSettingsEntity.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle very large user IDs")
        void shouldHandleVeryLargeUserIDs() {
            // Given
            long largeUserId = Long.MAX_VALUE;
            when(repo.findByUserId(largeUserId)).thenReturn(Optional.empty());

            // When
            Optional<String> result = adapter.findPreferredLocaleCode(largeUserId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findByUserId(largeUserId);
        }

        @Test
        @DisplayName("Should handle very long locale codes")
        void shouldHandleVeryLongLocaleCodes() {
            // Given
            long userId = 1L;
            String longLocaleCode = "a".repeat(100);

            when(repo.findByUserId(userId)).thenReturn(Optional.empty());
            when(repo.save(any(UserSettingsEntity.class))).thenReturn(new UserSettingsEntity());

            // When
            adapter.savePreferredLocaleCode(userId, longLocaleCode);

            // Then
            verify(repo).findByUserId(userId);
            verify(repo).save(any(UserSettingsEntity.class));
        }

        @Test
        @DisplayName("Should handle special characters in locale codes")
        void shouldHandleSpecialCharactersInLocaleCodes() {
            // Given
            long userId = 1L;
            String specialLocaleCode = "ru-RU_Петербург";

            when(repo.findByUserId(userId)).thenReturn(Optional.empty());
            when(repo.save(any(UserSettingsEntity.class))).thenReturn(new UserSettingsEntity());

            // When
            adapter.savePreferredLocaleCode(userId, specialLocaleCode);

            // Then
            verify(repo).findByUserId(userId);
            verify(repo).save(any(UserSettingsEntity.class));
        }

        @Test
        @DisplayName("Should handle empty locale codes")
        void shouldHandleEmptyLocaleCodes() {
            // Given
            long userId = 1L;
            String emptyLocaleCode = "";

            when(repo.findByUserId(userId)).thenReturn(Optional.empty());
            when(repo.save(any(UserSettingsEntity.class))).thenReturn(new UserSettingsEntity());

            // When
            adapter.savePreferredLocaleCode(userId, emptyLocaleCode);

            // Then
            verify(repo).findByUserId(userId);
            verify(repo).save(any(UserSettingsEntity.class));
        }
    }
}
