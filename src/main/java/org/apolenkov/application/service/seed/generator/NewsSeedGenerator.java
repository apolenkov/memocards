package org.apolenkov.application.service.seed.generator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.model.News;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Generator for test news articles.
 * Creates news items for landing page testing.
 */
@Component
@Profile({"dev", "test"})
public class NewsSeedGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsSeedGenerator.class);

    private final DataSeedRepository seedRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Creates NewsSeedGenerator with required dependencies.
     *
     * @param seedRepositoryValue repository for batch operations
     * @param transactionTemplateValue transaction template for TX control
     */
    public NewsSeedGenerator(
            final DataSeedRepository seedRepositoryValue, final TransactionTemplate transactionTemplateValue) {
        this.seedRepository = seedRepositoryValue;
        this.transactionTemplate = transactionTemplateValue;
    }

    /**
     * Generates news articles in batch for testing.
     *
     * @param totalNews number of news articles to generate
     * @return number of news articles generated
     */
    public int generateNews(final int totalNews) {
        LOGGER.info("Generating {} news articles in batch...", totalNews);

        List<News> newsList = new ArrayList<>(totalNews);
        for (int i = 0; i < totalNews; i++) {
            newsList.add(createTestNews(i));
        }

        transactionTemplate.execute(status -> {
            seedRepository.batchInsertNews(newsList);
            return null;
        });

        LOGGER.info("Successfully generated {} news articles", totalNews);
        return totalNews;
    }

    /**
     * Creates a test news article.
     *
     * @param index news index for unique content
     * @return configured test news
     */
    private News createTestNews(final int index) {
        return new News(
                null,
                "Test News " + (index + 1),
                "Test content for news item " + (index + 1),
                "admin",
                LocalDateTime.now());
    }
}
