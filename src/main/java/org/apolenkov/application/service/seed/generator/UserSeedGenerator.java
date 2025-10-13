package org.apolenkov.application.service.seed.generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Generator for test user data.
 * Creates batches of users with realistic names and emails.
 */
@Component
@Profile({"dev", "test"})
public class UserSeedGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSeedGenerator.class);

    private static final String[] USER_NAMES = {"Alex", "Maria", "John", "Anna", "David", "Elena", "Michael", "Sophia"};
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"
    };

    private final DataSeedRepository seedRepository;
    private final TransactionTemplate transactionTemplate;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates UserSeedGenerator with required dependencies.
     *
     * @param seedRepositoryValue repository for batch operations
     * @param transactionTemplateValue transaction template for TX control
     */
    public UserSeedGenerator(
            final DataSeedRepository seedRepositoryValue, final TransactionTemplate transactionTemplateValue) {
        this.seedRepository = seedRepositoryValue;
        this.transactionTemplate = transactionTemplateValue;
    }

    /**
     * Generates test users in batches.
     *
     * @param totalUsers total number of users to generate
     * @param batchSize batch size for processing
     * @param cachedPasswordHash pre-encoded password hash
     * @return list of generated users
     */
    public List<User> generateUsers(final int totalUsers, final int batchSize, final String cachedPasswordHash) {
        LOGGER.info("Generating {} test users in batches of {}...", totalUsers, batchSize);
        List<User> allUsers = new ArrayList<>();

        for (int i = 0; i < totalUsers; i += batchSize) {
            int end = Math.min(i + batchSize, totalUsers);
            int currentBatch = i;

            // Create batch in separate transaction to reduce lock time
            List<User> batch = transactionTemplate.execute(status -> {
                List<User> users = new ArrayList<>(batchSize);
                for (int j = currentBatch; j < end; j++) {
                    users.add(createTestUser(j, cachedPasswordHash));
                }
                return seedRepository.batchInsertUsers(users);
            });

            assert batch != null;
            allUsers.addAll(batch);

            if ((i / batchSize) % 5 == 0) {
                LOGGER.info("Generated {}/{} users", allUsers.size(), totalUsers);
            }
        }

        LOGGER.info("Successfully generated {} users", allUsers.size());
        return allUsers;
    }

    /**
     * Creates a single test user with random data.
     *
     * @param index user index for unique email generation
     * @param cachedPasswordHash pre-encoded password hash
     * @return configured test user
     */
    private User createTestUser(final int index, final String cachedPasswordHash) {
        String firstName = USER_NAMES[random.nextInt(USER_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + index + "@example.com";
        String name = firstName + " " + lastName;

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(cachedPasswordHash);
        user.setRoles(Set.of("USER"));

        return user;
    }
}
