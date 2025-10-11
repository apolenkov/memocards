package org.apolenkov.application.config.cache;

import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped cache for current authenticated user.
 */
@Component
@RequestScope
public class RequestScopedUserCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestScopedUserCache.class);

    private User cachedUser;

    /**
     * Gets cached user or returns null if not cached yet.
     *
     * @return cached user or null
     */
    public User get() {
        if (cachedUser != null) {
            LOGGER.debug("Cache HIT: Returning cached user [userId={}]", cachedUser.getId());
        } else {
            LOGGER.debug("Cache MISS: No user in cache");
        }
        return cachedUser;
    }

    /**
     * Caches user for current request scope.
     *
     * @param user user to cache
     */
    public void set(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.cachedUser = user;
        LOGGER.debug("User cached for current request [userId={}, email={}]", user.getId(), user.getEmail());
    }

    /**
     * Clears cached user.
     */
    public void clear() {
        if (cachedUser != null) {
            LOGGER.debug("Cache cleared [userId={}]", cachedUser.getId());
        }
        this.cachedUser = null;
    }
}
