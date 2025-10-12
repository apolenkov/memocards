package org.apolenkov.application.config.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.apolenkov.application.BaseIntegrationTest;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Integration tests for Caffeine cache functionality.
 * Verifies cache eviction behavior for user repository operations.
 */
@DisplayName("Caffeine Cache Integration Tests")
class CaffeineCacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cache usersByEmailCache;

    @BeforeEach
    void setUp() {
        usersByEmailCache = cacheManager.getCache(CacheConfiguration.USER_BY_EMAIL_CACHE);
        assertThat(usersByEmailCache).isNotNull();

        // Clear cache before each test
        usersByEmailCache.clear();
    }

    @Test
    @DisplayName("Should evict entire cache on user save")
    void shouldEvictCacheOnSave() {
        // Given: user in database with unique email
        String email = "evict." + System.nanoTime() + "@test.com";
        User user = createAndSaveTestUser(email, "Evict User");

        // Prime cache
        userRepository.findByEmail(email);

        // Verify cache contains user
        assertThat(usersByEmailCache.get(email)).isNotNull();

        // When: update user (triggers @CacheEvict(allEntries=true))
        user.setName("New Name");
        userRepository.save(user);

        // Then: entire cache is cleared
        assertThat(usersByEmailCache.get(email)).isNull();
    }

    @Test
    @DisplayName("Should evict all entries when any user is saved")
    void shouldEvictAllEntriesWhenAnyUserIsSaved() {
        // Given: 2 users with unique emails
        long timestamp = System.nanoTime();
        String email1 = "user1." + timestamp + "@test.com";
        String email2 = "user2." + (timestamp + 1) + "@test.com";

        User user1 = createAndSaveTestUser(email1, "User 1");
        createAndSaveTestUser(email2, "User 2");

        // Prime cache
        userRepository.findByEmail(email1);
        userRepository.findByEmail(email2);

        // Verify both in cache
        assertThat(usersByEmailCache.get(email1)).isNotNull();
        assertThat(usersByEmailCache.get(email2)).isNotNull();

        // When: save ANY user (triggers allEntries eviction)
        user1.setName("Updated Name");
        userRepository.save(user1);

        // Then: ALL users evicted from cache
        assertThat(usersByEmailCache.get(email1)).isNull();
        assertThat(usersByEmailCache.get(email2)).isNull();
    }

    @Test
    @DisplayName("Should evict all entries when user is deleted")
    void shouldEvictAllEntriesWhenUserDeleted() {
        // Given: 2 users with unique emails
        long timestamp = System.nanoTime();
        String email1 = "delete1." + timestamp + "@test.com";
        String email2 = "delete2." + (timestamp + 1) + "@test.com";

        User user1 = createAndSaveTestUser(email1, "Delete User 1");
        createAndSaveTestUser(email2, "Delete User 2");

        // Prime cache
        userRepository.findByEmail(email1);
        userRepository.findByEmail(email2);

        // Verify both in cache
        assertThat(usersByEmailCache.get(email1)).isNotNull();
        assertThat(usersByEmailCache.get(email2)).isNotNull();

        // When: delete one user (triggers allEntries eviction)
        userRepository.deleteById(user1.getId());

        // Then: ALL users evicted from cache
        assertThat(usersByEmailCache.get(email1)).isNull();
        assertThat(usersByEmailCache.get(email2)).isNull();
    }

    @Test
    @DisplayName("Should cache user lookup results")
    void shouldCacheUserLookupResults() {
        // Given: user in database with unique email
        String email = "lookup." + System.nanoTime() + "@test.com";
        createAndSaveTestUser(email, "Lookup User");

        // When: first lookup
        Optional<User> first = userRepository.findByEmail(email);

        // Then: user found
        assertThat(first).isPresent();

        // When: second lookup (should hit cache)
        Optional<User> second = userRepository.findByEmail(email);

        // Then: same result
        assertThat(second).isPresent();
        assertThat(second.get().getEmail()).isEqualTo(email);
    }

    /**
     * Helper method to create and save a test user with unique email.
     *
     * @param email user email
     * @param name user name
     * @return saved user
     */
    private User createAndSaveTestUser(final String email, final String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash("hashedPassword123");
        user.addRole(SecurityConstants.ROLE_USER);
        return userRepository.save(user);
    }
}
