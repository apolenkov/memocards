package org.apolenkov.application.views.core.navigation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;

/**
 * Utility class for setting up Vaadin context in unit tests.
 * Provides mocked UI, Session, and Request/Response objects needed for @UIScope component testing.
 */
public class VaadinTestContext implements AutoCloseable {

    private final MockedStatic<UI> uiMock;
    private final MockedStatic<VaadinSession> sessionMock;
    private final MockedStatic<VaadinServletRequest> requestMock;
    private final MockedStatic<VaadinServletResponse> responseMock;

    private final Map<String, Object> sessionAttributes = new ConcurrentHashMap<>();

    /**
     * Creates a new VaadinTestContext with mocked UI, Session, and Request/Response objects.
     */
    public VaadinTestContext() {
        // Mock Session first
        sessionMock = mockStatic(VaadinSession.class);
        VaadinSession mockSession = mock(VaadinSession.class);
        when(mockSession.getAttribute(String.class)).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return sessionAttributes.get(key);
        });
        doAnswer(this::setSessionAttribute).when(mockSession).setAttribute(anyString(), any());
        sessionMock.when(VaadinSession::getCurrent).thenReturn(mockSession);

        // Mock UI
        uiMock = mockStatic(UI.class);
        UI mockUI = mock(UI.class);
        when(mockUI.getUIId()).thenReturn(1);
        when(mockUI.getLocale()).thenReturn(Locale.ENGLISH);
        when(mockUI.getSession()).thenReturn(mockSession);
        uiMock.when(UI::getCurrent).thenReturn(mockUI);

        // Mock Request/Response
        requestMock = mockStatic(VaadinServletRequest.class);
        responseMock = mockStatic(VaadinServletResponse.class);

        VaadinServletRequest mockVaadinRequest = mock(VaadinServletRequest.class);
        VaadinServletResponse mockVaadinResponse = mock(VaadinServletResponse.class);
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockHttpResponse = mock(HttpServletResponse.class);

        when(mockVaadinRequest.getHttpServletRequest()).thenReturn(mockHttpRequest);
        when(mockVaadinResponse.getHttpServletResponse()).thenReturn(mockHttpResponse);

        requestMock.when(VaadinServletRequest::getCurrent).thenReturn(mockVaadinRequest);
        responseMock.when(VaadinServletResponse::getCurrent).thenReturn(mockVaadinResponse);
    }

    private Object setSessionAttribute(final InvocationOnMock invocation) {
        String key = invocation.getArgument(0);
        Object value = invocation.getArgument(1);
        sessionAttributes.put(key, value);
        return null;
    }

    /**
     * Cleans up all mocked static contexts.
     * This method should be called in test teardown to prevent memory leaks.
     */
    @Override
    public void close() {
        uiMock.close();
        sessionMock.close();
        requestMock.close();
        responseMock.close();
    }
}
