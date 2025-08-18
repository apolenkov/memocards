package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/**
 * Utility class for centralized text element creation and styling.
 * Eliminates duplication of text element creation patterns across the application.
 */
public final class TextHelper {

    private TextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a main title (H1)
     */
    public static H1 createMainTitle(String text) {
        H1 title = new H1(text);
        title.addClassName("main-title");
        return title;
    }

    /**
     * Create a page title (H2)
     */
    public static H2 createPageTitle(String text) {
        H2 title = new H2(text);
        title.addClassName("page-title");
        return title;
    }

    /**
     * Create a section title (H3)
     */
    public static H3 createSectionTitle(String text) {
        H3 title = new H3(text);
        title.addClassName("section-title");
        return title;
    }

    /**
     * Create a subsection title (H4)
     */
    public static H4 createSubsectionTitle(String text) {
        H4 title = new H4(text);
        title.addClassName("subsection-title");
        return title;
    }

    /**
     * Create a card title (H5)
     */
    public static H5 createCardTitle(String text) {
        H5 title = new H5(text);
        title.addClassName("card-title");
        return title;
    }

    /**
     * Create a small title (H6)
     */
    public static H6 createSmallTitle(String text) {
        H6 title = new H6(text);
        title.addClassName("small-title");
        return title;
    }

    /**
     * Create a paragraph with text
     */
    public static Paragraph createParagraph(String text) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.addClassName("text-paragraph");
        return paragraph;
    }

    /**
     * Create a paragraph with custom class
     */
    public static Paragraph createParagraph(String text, String className) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.addClassName(className);
        return paragraph;
    }

    /**
     * Create a span with text
     */
    public static Span createSpan(String text) {
        Span span = new Span(text);
        span.addClassName("text-span");
        return span;
    }

    /**
     * Create a span with custom class
     */
    public static Span createSpan(String text, String className) {
        Span span = new Span(text);
        span.addClassName(className);
        return span;
    }

    /**
     * Create a label with text (using Span under the hood)
     */
    public static Span createLabel(String text) {
        Span label = new Span(text);
        label.addClassName("text-label");
        return label;
    }

    /**
     * Create a label with custom class (using Span under the hood)
     */
    public static Span createLabel(String text, String className) {
        Span label = new Span(text);
        label.addClassName(className);
        return label;
    }

    /**
     * Create a div with text
     */
    public static Div createTextDiv(String text) {
        Div div = new Div();
        div.setText(text);
        div.addClassName("text-div");
        return div;
    }

    /**
     * Create a div with custom class
     */
    public static Div createTextDiv(String text, String className) {
        Div div = new Div();
        div.setText(text);
        div.addClassName(className);
        return div;
    }

    /**
     * Create a link
     */
    public static Anchor createLink(String text, String href) {
        Anchor link = new Anchor(href, text);
        link.addClassName("text-link");
        return link;
    }

    /**
     * Create a link with custom class
     */
    public static Anchor createLink(String text, String href, String className) {
        Anchor link = new Anchor(href, text);
        link.addClassName(className);
        return link;
    }

    /**
     * Create a success message
     */
    public static Span createSuccessMessage(String text) {
        Span message = createSpan(text, "success-message");
        ColorHelper.setSuccessColor(message);
        return message;
    }

    /**
     * Create an error message
     */
    public static Span createErrorMessage(String text) {
        Span message = createSpan(text, "error-message");
        ColorHelper.setErrorColor(message);
        return message;
    }

    /**
     * Create a warning message
     */
    public static Span createWarningMessage(String text) {
        Span message = createSpan(text, "warning-message");
        ColorHelper.setWarningColor(message);
        return message;
    }

    /**
     * Create an info message
     */
    public static Span createInfoMessage(String text) {
        Span message = createSpan(text, "info-message");
        ColorHelper.setInfoColor(message);
        return message;
    }

    /**
     * Create a status text
     */
    public static Span createStatusText(String text, boolean isActive) {
        String className = isActive ? "status-active" : "status-inactive";
        Span status = createSpan(text, className);
        status.getStyle().set("color", isActive ? ColorHelper.SUCCESS_COLOR : ColorHelper.GRAY);
        return status;
    }

    /**
     * Create a badge text
     */
    public static Span createBadge(String text, String variant) {
        Span badge = createSpan(text, "badge");
        badge.addClassName("badge-" + variant);
        return badge;
    }

    /**
     * Create a primary badge
     */
    public static Span createPrimaryBadge(String text) {
        return createBadge(text, "primary");
    }

    /**
     * Create a success badge
     */
    public static Span createSuccessBadge(String text) {
        return createBadge(text, "success");
    }

    /**
     * Create a warning badge
     */
    public static Span createWarningBadge(String text) {
        return createBadge(text, "warning");
    }

    /**
     * Create an error badge
     */
    public static Span createErrorBadge(String text) {
        return createBadge(text, "error");
    }

    /**
     * Create a info badge
     */
    public static Span createInfoBadge(String text) {
        return createBadge(text, "info");
    }

    /**
     * Create a secondary badge
     */
    public static Span createSecondaryBadge(String text) {
        return createBadge(text, "secondary");
    }

    /**
     * Create a light badge
     */
    public static Span createLightBadge(String text) {
        return createBadge(text, "light");
    }

    /**
     * Create a dark badge
     */
    public static Span createDarkBadge(String text) {
        return createBadge(text, "dark");
    }

    /**
     * Create a pill badge
     */
    public static Span createPillBadge(String text, String variant) {
        Span badge = createBadge(text, variant);
        badge.addClassName("badge-pill");
        return badge;
    }

    /**
     * Create a large badge
     */
    public static Span createLargeBadge(String text, String variant) {
        Span badge = createBadge(text, variant);
        badge.addClassName("badge-lg");
        return badge;
    }

    /**
     * Create a small badge
     */
    public static Span createSmallBadge(String text, String variant) {
        Span badge = createBadge(text, variant);
        badge.addClassName("badge-sm");
        return badge;
    }

    /**
     * Create a truncated text with ellipsis
     */
    public static Span createTruncatedText(String text, int maxLength) {
        String displayText = text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
        Span truncated = createSpan(displayText, "truncated-text");
        if (text.length() > maxLength) {
            truncated.getElement().setProperty("title", text);
        }
        return truncated;
    }

    /**
     * Create a highlighted text
     */
    public static Span createHighlightedText(String text, String highlight) {
        if (highlight == null || highlight.isEmpty()) {
            return createSpan(text);
        }

        String lowerText = text.toLowerCase();
        String lowerHighlight = highlight.toLowerCase();
        int index = lowerText.indexOf(lowerHighlight);

        if (index == -1) {
            return createSpan(text);
        }

        Div container = new Div();
        container.addClassName("highlighted-text");

        if (index > 0) {
            container.add(createSpan(text.substring(0, index)));
        }

        Span highlighted = createSpan(text.substring(index, index + highlight.length()), "highlight");
        container.add(highlighted);

        if (index + highlight.length() < text.length()) {
            container.add(createSpan(text.substring(index + highlight.length())));
        }

        return new Span(container);
    }

    /**
     * Create a formatted number
     */
    public static Span createFormattedNumber(Number number, String format) {
        String formatted = String.format(format, number);
        return createSpan(formatted, "formatted-number");
    }

    /**
     * Create a formatted currency
     */
    public static Span createFormattedCurrency(Number amount, String currency) {
        String formatted = String.format("%s %.2f", currency, amount.doubleValue());
        return createSpan(formatted, "formatted-currency");
    }

    /**
     * Create a formatted percentage
     */
    public static Span createFormattedPercentage(Number value) {
        String formatted = String.format("%.1f%%", value.doubleValue());
        return createSpan(formatted, "formatted-percentage");
    }

    /**
     * Create a formatted date
     */
    public static Span createFormattedDate(java.time.LocalDate date, String format) {
        String formatted = date.format(java.time.format.DateTimeFormatter.ofPattern(format));
        return createSpan(formatted, "formatted-date");
    }

    /**
     * Create a formatted time
     */
    public static Span createFormattedTime(java.time.LocalTime time, String format) {
        String formatted = time.format(java.time.format.DateTimeFormatter.ofPattern(format));
        return createSpan(formatted, "formatted-time");
    }

    /**
     * Create a formatted datetime
     */
    public static Span createFormattedDateTime(java.time.LocalDateTime dateTime, String format) {
        String formatted = dateTime.format(java.time.format.DateTimeFormatter.ofPattern(format));
        return createSpan(formatted, "formatted-datetime");
    }
}
