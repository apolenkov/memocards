package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.RoleAuditEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.RoleAuditJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleAuditJpaAdapter Tests")
class RoleAuditJpaAdapterTest {

    @Mock
    private RoleAuditJpaRepository repo;

    private RoleAuditJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RoleAuditJpaAdapter(repo);
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {
        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // Given
            Class<RoleAuditJpaAdapter> clazz = RoleAuditJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.context.annotation.Profile.class))
                    .isTrue();
            org.springframework.context.annotation.Profile profile =
                    clazz.getAnnotation(org.springframework.context.annotation.Profile.class);
            assertThat(profile.value()).contains("dev", "prod");
        }

        @Test
        @DisplayName("Should be annotated with Repository")
        void shouldBeAnnotatedWithRepository() {
            // Given
            Class<RoleAuditJpaAdapter> clazz = RoleAuditJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.stereotype.Repository.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Record Change Tests")
    class RecordChangeTests {
        @Test
        @DisplayName("RecordChange should save role audit entity with all fields")
        void recordChangeShouldSaveRoleAuditEntityWithAllFields() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER", "EDITOR");
            Set<String> rolesAfter = Set.of("USER", "ADMIN");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("RecordChange should handle empty roles before")
        void recordChangeShouldHandleEmptyRolesBefore() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of();
            Set<String> rolesAfter = Set.of("USER");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("RecordChange should handle empty roles after")
        void recordChangeShouldHandleEmptyRolesAfter() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER", "ADMIN");
            Set<String> rolesAfter = Set.of();
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("RecordChange should handle single role")
        void recordChangeShouldHandleSingleRole() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER");
            Set<String> rolesAfter = Set.of("ADMIN");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }
    }

    @Nested
    @DisplayName("List All Tests")
    class ListAllTests {
        @Test
        @DisplayName("ListAll should return all role audit records")
        void listAllShouldReturnAllRoleAuditRecords() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);

            RoleAuditEntity entity1 = createRoleAuditEntity("admin1@test.com", 1L, "USER,EDITOR", "USER,ADMIN", now);
            RoleAuditEntity entity2 = createRoleAuditEntity("admin2@test.com", 2L, "USER", "USER,EDITOR", yesterday);

            when(repo.findAll()).thenReturn(List.of(entity1, entity2));

            // When
            List<RoleAuditRepository.RoleAuditRecord> result = adapter.listAll();

            // Then
            assertThat(result).hasSize(2);

            RoleAuditRepository.RoleAuditRecord record1 = result.get(0);
            assertThat(record1.adminEmail()).isEqualTo("admin1@test.com");
            assertThat(record1.userId()).isEqualTo(1L);
            assertThat(record1.rolesBefore()).containsExactlyInAnyOrder("USER", "EDITOR");
            assertThat(record1.rolesAfter()).containsExactlyInAnyOrder("USER", "ADMIN");
            assertThat(record1.at()).isEqualTo(now);

            RoleAuditRepository.RoleAuditRecord record2 = result.get(1);
            assertThat(record2.adminEmail()).isEqualTo("admin2@test.com");
            assertThat(record2.userId()).isEqualTo(2L);
            assertThat(record2.rolesBefore()).containsExactlyInAnyOrder("USER");
            assertThat(record2.rolesAfter()).containsExactlyInAnyOrder("USER", "EDITOR");
            assertThat(record2.at()).isEqualTo(yesterday);

            verify(repo).findAll();
        }

        @Test
        @DisplayName("ListAll should return empty list when no records exist")
        void listAllShouldReturnEmptyListWhenNoRecordsExist() {
            // Given
            when(repo.findAll()).thenReturn(List.of());

            // When
            List<RoleAuditRepository.RoleAuditRecord> result = adapter.listAll();

            // Then
            assertThat(result).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("ListAll should handle empty roles strings")
        void listAllShouldHandleEmptyRolesStrings() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            RoleAuditEntity entity = createRoleAuditEntity("admin@test.com", 1L, "", "", now);

            when(repo.findAll()).thenReturn(List.of(entity));

            // When
            List<RoleAuditRepository.RoleAuditRecord> result = adapter.listAll();

            // Then
            assertThat(result).hasSize(1);
            RoleAuditRepository.RoleAuditRecord roleRecord = result.get(0);
            assertThat(roleRecord.rolesBefore()).isEmpty();
            assertThat(roleRecord.rolesAfter()).isEmpty();
            verify(repo).findAll();
        }

        @Test
        @DisplayName("ListAll should handle single role strings")
        void listAllShouldHandleSingleRoleStrings() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            RoleAuditEntity entity = createRoleAuditEntity("admin@test.com", 1L, "USER", "ADMIN", now);

            when(repo.findAll()).thenReturn(List.of(entity));

            // When
            List<RoleAuditRepository.RoleAuditRecord> result = adapter.listAll();

            // Then
            assertThat(result).hasSize(1);
            RoleAuditRepository.RoleAuditRecord roleRecord = result.get(0);
            assertThat(roleRecord.rolesBefore()).containsExactlyInAnyOrder("USER");
            assertThat(roleRecord.rolesAfter()).containsExactlyInAnyOrder("ADMIN");
            verify(repo).findAll();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle very large user IDs")
        void shouldHandleVeryLargeUserIDs() {
            // Given
            String adminEmail = "admin@test.com";
            long largeUserId = Long.MAX_VALUE;
            Set<String> rolesBefore = Set.of("USER");
            Set<String> rolesAfter = Set.of("ADMIN");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, largeUserId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("Should handle very long admin emails")
        void shouldHandleVeryLongAdminEmails() {
            // Given
            String longAdminEmail = "a".repeat(100) + "@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER");
            Set<String> rolesAfter = Set.of("ADMIN");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(longAdminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("Should handle very long role names")
        void shouldHandleVeryLongRoleNames() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            String longRole = "ROLE_" + "A".repeat(100);
            Set<String> rolesBefore = Set.of(longRole);
            Set<String> rolesAfter = Set.of("ADMIN");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("Should handle special characters in admin email")
        void shouldHandleSpecialCharactersInAdminEmail() {
            // Given
            String specialEmail = "admin+test@test-domain.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER");
            Set<String> rolesAfter = Set.of("ADMIN");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(specialEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("Should handle special characters in role names")
        void shouldHandleSpecialCharactersInRoleNames() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER_ROLE", "EDITOR_ROLE");
            Set<String> rolesAfter = Set.of("ADMIN_ROLE", "SUPER_USER_ROLE");
            LocalDateTime changedAt = LocalDateTime.now();

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }

        @Test
        @DisplayName("Should handle null timestamp")
        void shouldHandleNullTimestamp() {
            // Given
            String adminEmail = "admin@test.com";
            long userId = 1L;
            Set<String> rolesBefore = Set.of("USER");
            Set<String> rolesAfter = Set.of("ADMIN");
            LocalDateTime changedAt = null;

            when(repo.save(any(RoleAuditEntity.class))).thenReturn(new RoleAuditEntity());

            // When
            adapter.recordChange(adminEmail, userId, rolesBefore, rolesAfter, changedAt);

            // Then
            verify(repo).save(any(RoleAuditEntity.class));
        }
    }

    private RoleAuditEntity createRoleAuditEntity(
            String adminEmail, long userId, String rolesBefore, String rolesAfter, LocalDateTime changedAt) {
        RoleAuditEntity entity = new RoleAuditEntity();
        entity.setAdminEmail(adminEmail);
        entity.setUserId(userId);
        entity.setRolesBefore(rolesBefore);
        entity.setRolesAfter(rolesAfter);
        entity.setChangedAt(changedAt);
        return entity;
    }
}
