package org.apolenkov.application.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom transaction annotations to eliminate duplication of @Transactional annotations.
 *
 * <p>This class provides semantic transaction annotations that wrap Spring's
 * @Transactional annotation with specific configurations for different types of
 * operations. These annotations provide consistent transaction behavior across
 * the application and make the intent of each method clear.</p>
 *
 * <p>Each annotation is designed for a specific use case and includes appropriate
 * transaction settings such as read-only mode for queries and proper isolation
 * levels for different operation types.</p>
 *
 */
public class TransactionAnnotations {

    /**
     * Read-only transaction for query operations.
     *
     * <p>This annotation marks methods that perform read-only operations such as
     * queries and data retrieval. It optimizes database performance by setting
     * the transaction to read-only mode, which can improve performance and
     * reduce resource usage.</p>
     *
     * <p>Use this annotation for methods that only retrieve data and do not
     * modify the database state.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    public @interface ReadOnlyTransaction {}

    /**
     * Write transaction for create/update operations.
     *
     * <p>This annotation marks methods that perform data modification operations
     * such as creating new records or updating existing ones. It provides
     * standard transaction behavior with appropriate isolation and rollback
     * capabilities.</p>
     *
     * <p>Use this annotation for methods that create, update, or otherwise
     * modify data in the database.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface WriteTransaction {}

    /**
     * Delete transaction for remove operations.
     *
     * <p>This annotation marks methods that perform data deletion operations.
     * It provides transaction support with appropriate isolation levels to
     * ensure data consistency during removal operations.</p>
     *
     * <p>Use this annotation for methods that remove data from the database,
     * ensuring that deletion operations are atomic and can be rolled back
     * if necessary.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    public @interface DeleteTransaction {}

    /**
     * Batch transaction for multiple operations.
     *
     * <p>This annotation marks methods that perform multiple related operations
     * that should be executed within a single transaction. It ensures that
     * all operations either succeed together or fail together, maintaining
     * data consistency.</p>
     *
     * <p>Use this annotation for methods that perform multiple database
     * operations that are logically related and should be atomic.</p>
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional
    @SuppressWarnings("unused")
    public @interface BatchTransaction {}
}
