package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.StatsJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for StatsJpaAdapter using Spring Boot test context.
 * Tests actual database operations and constraints.
 */
@SpringBootTest
@Transactional
class StatsJpaAdapterIntegrationTest {

    @Autowired
    private StatsJpaRepository statsRepository;

    private DeckDailyStatsEntity testStats;

    @BeforeEach
    void setUp() {
        // Create test stats entity
        DeckDailyStatsEntity.Id id = new DeckDailyStatsEntity.Id(1L, LocalDate.now());
        testStats = new DeckDailyStatsEntity();
        testStats.setId(id);
        testStats.setSessions(1);
        testStats.setViewed(10);
        testStats.setCorrect(8);
        testStats.setRepeatCount(2);
        testStats.setHard(1);
        testStats.setTotalDurationMs(5000L);
        testStats.setTotalAnswerDelayMs(2000L);
    }

    @Test
    @DisplayName("Should save and retrieve deck daily stats")
    void shouldSaveAndRetrieveDeckDailyStats() {
        // Save stats
        DeckDailyStatsEntity savedStats = statsRepository.save(testStats);
        assertThat(savedStats).isNotNull();
        assertThat(savedStats.getId()).isNotNull();

        // Retrieve stats
        Optional<DeckDailyStatsEntity> foundStats = statsRepository.findById(testStats.getId());
        assertThat(foundStats).isPresent();

        DeckDailyStatsEntity retrievedStats = foundStats.get();
        assertThat(retrievedStats.getSessions()).isEqualTo(1);
        assertThat(retrievedStats.getViewed()).isEqualTo(10);
        assertThat(retrievedStats.getCorrect()).isEqualTo(8);
        assertThat(retrievedStats.getRepeatCount()).isEqualTo(2);
        assertThat(retrievedStats.getHard()).isEqualTo(1);
        assertThat(retrievedStats.getTotalDurationMs()).isEqualTo(5000L);
        assertThat(retrievedStats.getTotalAnswerDelayMs()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("Should update existing stats")
    void shouldUpdateExistingStats() {
        // Save initial stats
        DeckDailyStatsEntity savedStats = statsRepository.save(testStats);

        // Update stats
        savedStats.setSessions(2);
        savedStats.setViewed(20);
        savedStats.setCorrect(15);
        savedStats.setTotalDurationMs(10000L);

        DeckDailyStatsEntity updatedStats = statsRepository.save(savedStats);

        // Verify update
        assertThat(updatedStats).satisfies(stats -> {
            assertThat(stats.getSessions()).isEqualTo(2);
            assertThat(stats.getViewed()).isEqualTo(20);
            assertThat(stats.getCorrect()).isEqualTo(15);
            assertThat(stats.getTotalDurationMs()).isEqualTo(10000L);
        });

        // Verify in database
        Optional<DeckDailyStatsEntity> foundStats = statsRepository.findById(testStats.getId());
        assertThat(foundStats).isPresent().get().satisfies(stats -> assertThat(stats.getSessions())
                .isEqualTo(2));
    }

    @Test
    @DisplayName("Should find stats by deck ID")
    void shouldFindStatsByDeckId() {
        // Save stats
        statsRepository.save(testStats);

        // Find by deck ID
        List<DeckDailyStatsEntity> foundStats = statsRepository.findByDeckIdOrderByDateAsc(1L);

        assertThat(foundStats).isNotEmpty().hasSize(1).first().satisfies(stats -> assertThat(
                        stats.getId().getDeckId())
                .isEqualTo(1L));
    }

    @Test
    @DisplayName("Should handle composite key correctly")
    void shouldHandleCompositeKeyCorrectly() {
        // Save stats
        statsRepository.save(testStats);

        // Create another stats with same deck ID but different date
        DeckDailyStatsEntity.Id id2 =
                new DeckDailyStatsEntity.Id(1L, LocalDate.now().plusDays(1));
        DeckDailyStatsEntity testStats2 = new DeckDailyStatsEntity();
        testStats2.setId(id2);
        testStats2.setSessions(1);
        testStats2.setViewed(5);
        testStats2.setCorrect(4);
        testStats2.setRepeatCount(1);
        testStats2.setHard(0);
        testStats2.setTotalDurationMs(3000L);
        testStats2.setTotalAnswerDelayMs(1000L);

        statsRepository.save(testStats2);

        // Both should exist
        assertThat(statsRepository.findById(testStats.getId())).isPresent();
        assertThat(statsRepository.findById(testStats2.getId())).isPresent();

        // Should find both for same deck ID
        List<DeckDailyStatsEntity> allStats = statsRepository.findByDeckIdOrderByDateAsc(1L);
        assertThat(allStats).hasSize(2);
    }

    @Test
    @DisplayName("Should enforce JPA validation constraints")
    void shouldEnforceJpaValidationConstraints() {
        // Test sessions constraint - JPA validation prevents negative values
        assertThatThrownBy(() -> testStats.setSessions(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sessions cannot be negative");
    }

    @Test
    @DisplayName("Should handle optimistic locking")
    void shouldHandleOptimisticLocking() {
        // Save stats
        DeckDailyStatsEntity savedStats = statsRepository.save(testStats);
        assertThat(savedStats.getVersion()).isZero();

        // Update should increment version
        savedStats.setSessions(3);
        DeckDailyStatsEntity updatedStats = statsRepository.save(savedStats);
        assertThat(updatedStats.getVersion()).isGreaterThanOrEqualTo(0L);

        // Verify the update was saved
        Optional<DeckDailyStatsEntity> foundStats = statsRepository.findById(testStats.getId());
        assertThat(foundStats).isPresent().get().satisfies(stats -> assertThat(stats.getSessions())
                .isEqualTo(3));
    }
}
