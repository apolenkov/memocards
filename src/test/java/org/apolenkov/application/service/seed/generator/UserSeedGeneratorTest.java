package org.apolenkov.application.service.seed.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Unit tests for UserSeedGenerator.
 * Tests user generation logic and batch processing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserSeedGenerator Tests")
class UserSeedGeneratorTest {

    @Mock
    private DataSeedRepository seedRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private UserSeedGenerator userGenerator;

    private static final String TEST_PASSWORD_HASH = "$2a$10$test.hash.value";

    @BeforeEach
    void setUp() {
        userGenerator = new UserSeedGenerator(seedRepository, transactionTemplate);

        // Mock TransactionTemplate to execute callback immediately
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
            TransactionStatus mockStatus = mock(TransactionStatus.class);
            return callback.doInTransaction(mockStatus);
        });
    }

    @Test
    @DisplayName("Should generate specified number of users")
    void shouldGenerateSpecifiedNumberOfUsers() {
        // Given
        int totalUsers = 3;
        int batchSize = 10;

        // Mock repository to return the users passed to it
        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<User> result = userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        assertThat(result)
                .hasSize(totalUsers)
                .allMatch(user -> user.getEmail() != null)
                .allMatch(user -> user.getName() != null)
                .allMatch(user -> user.getPasswordHash().equals(TEST_PASSWORD_HASH));
    }

    @Test
    @DisplayName("Should generate users in batches")
    void shouldGenerateUsersInBatches() {
        // Given
        int totalUsers = 25;
        int batchSize = 10;

        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<User> result = userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        assertThat(result).hasSize(totalUsers);
        // Verify called 3 times (10 + 10 + 5)
        verify(seedRepository, org.mockito.Mockito.times(3)).batchInsertUsers(any());
    }

    @Test
    @DisplayName("Should generate unique emails for each user")
    void shouldGenerateUniqueEmailsForEachUser() {
        // Given
        int totalUsers = 10;
        int batchSize = 10;

        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<User> result = userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        List<String> emails = result.stream().map(User::getEmail).toList();
        assertThat(emails).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Should use cached password hash for all users")
    void shouldUseCachedPasswordHashForAllUsers() {
        // Given
        int totalUsers = 5;
        int batchSize = 10;

        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<User> result = userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        assertThat(result).isNotEmpty().allMatch(user -> user.getPasswordHash().equals(TEST_PASSWORD_HASH));
    }

    @Test
    @DisplayName("Should generate users with USER role")
    void shouldGenerateUsersWithUserRole() {
        // Given
        int totalUsers = 3;
        int batchSize = 10;

        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<User> result = userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        assertThat(result)
                .isNotEmpty()
                .allMatch(user -> user.getRoles().contains("USER"))
                .allMatch(user -> user.getRoles().size() == 1);
    }

    @Test
    @DisplayName("Should generate users with valid name format")
    void shouldGenerateUsersWithValidNameFormat() {
        // Given
        int totalUsers = 5;
        int batchSize = 10;

        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<User> result = userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        assertThat(result)
                .isNotEmpty()
                .allMatch(user -> user.getName().contains(" ")) // First Last format
                .allMatch(user -> user.getName().length() > 3);
    }

    @Test
    @DisplayName("Should batch insert users via repository")
    void shouldBatchInsertUsersViaRepository() {
        // Given
        int totalUsers = 15;
        int batchSize = 10;

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);

        when(seedRepository.batchInsertUsers(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userGenerator.generateUsers(totalUsers, batchSize, TEST_PASSWORD_HASH);

        // Then
        // Verify called 2 times (10 + 5)
        verify(seedRepository, org.mockito.Mockito.times(2)).batchInsertUsers(usersCaptor.capture());

        // First batch should have 10 users, second batch should have 5
        List<List<User>> allBatches = usersCaptor.getAllValues();
        assertThat(allBatches).hasSize(2);
        assertThat(allBatches.get(0)).hasSize(10);
        assertThat(allBatches.get(1)).hasSize(5);
    }
}
