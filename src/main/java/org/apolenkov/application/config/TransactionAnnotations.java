package org.apolenkov.application.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom transaction annotations to eliminate @Transactional duplication.
 *
 * <p>Provides semantic transaction annotations with specific configurations
 * for different operation types.</p>
 */
public class TransactionAnnotations {

    /**
     * Read-only transaction for query operations.
     *
     * <p>Optimizes database performance for data retrieval.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    public @interface ReadOnlyTransaction {}

    /**
     * Write transaction for create/update operations.
     *
     * <p>Provides standard transaction behavior for data modification.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface WriteTransaction {}

    /**
     * Delete transaction for remove operations.
     *
     * <p>Ensures data consistency during deletion.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface DeleteTransaction {}

    /**
     * Batch transaction for multiple operations.
     *
     * <p>Ensures atomic execution of multiple related database operations.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    @SuppressWarnings("unused") // IDE Community problem
    public @interface BatchTransaction {}
}
