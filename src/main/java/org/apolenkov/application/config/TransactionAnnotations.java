package org.apolenkov.application.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom transaction annotations to eliminate duplication of @Transactional annotations.
 * Provides semantic meaning and consistent transaction behavior across the application.
 */
public class TransactionAnnotations {

    /**
     * Read-only transaction for query operations
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    public @interface ReadOnlyTransaction {}

    /**
     * Write transaction for create/update operations
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface WriteTransaction {}

    /**
     * Delete transaction for remove operations
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface DeleteTransaction {}

    /**
     * Batch transaction for multiple operations
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface BatchTransaction {}
}
