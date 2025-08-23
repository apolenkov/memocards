package org.apolenkov.application.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom transaction annotations to eliminate @Transactional duplication.
 * Provides semantic transaction annotations with specific configurations
 * for different operation types.
 */
public class TransactionAnnotations {

    /**
     * Read-only transaction for query operations.
     * Optimizes database performance for data retrieval.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    public @interface ReadOnlyTransaction {}

    /**
     * Write transaction for create/update operations.
     * Provides standard transaction behavior for data modification.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface WriteTransaction {}

    /**
     * Delete transaction for remove operations.
     * Ensures data consistency during deletion.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface DeleteTransaction {}

    /**
     * Batch transaction for multiple operations.
     * Ensures atomic execution of multiple related database operations.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    @SuppressWarnings("unused") // IDE Community problem
    public @interface BatchTransaction {}
}
