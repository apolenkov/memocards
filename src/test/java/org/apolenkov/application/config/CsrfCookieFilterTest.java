package org.apolenkov.application.config;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;

@ExtendWith(MockitoExtension.class)
class CsrfCookieFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private CsrfToken csrfToken;

    private CsrfCookieFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CsrfCookieFilter();
    }

    @Test
    void shouldProcessGetRequestWithCsrfToken() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);
        when(csrfToken.getToken()).thenReturn("test-token");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(csrfToken).getToken();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipNonGetRequests() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("POST");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response, never()).setHeader(anyString(), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleMissingCsrfToken() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response, never()).setHeader(anyString(), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipVaadinInternalPaths() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/VAADIN/test");

        // When
        boolean shouldSkip = filter.shouldNotFilter(request);

        // Then
        assert shouldSkip : "Should skip Vaadin internal paths";
    }

    @Test
    void shouldSkipStaticResources() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/css/style.css");

        // When
        boolean shouldSkip = filter.shouldNotFilter(request);

        // Then
        assert shouldSkip : "Should skip static resources";
    }

    @Test
    void shouldProcessRegularPaths() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/home");

        // When
        boolean shouldSkip = filter.shouldNotFilter(request);

        // Then
        assert !shouldSkip : "Should process regular paths";
    }
}
