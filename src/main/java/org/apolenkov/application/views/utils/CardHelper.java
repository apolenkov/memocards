package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Utility class for centralized card and tile creation.
 * Eliminates duplication of card creation patterns across the application.
 */
public final class CardHelper {

    private CardHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a basic info card
     */
    public static Div createInfoCard(String title, String content) {
        Div card = new Div();
        card.addClassName("info-card");

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("info-card__title");

        Span cardContent = new Span(content);
        cardContent.addClassName("info-card__content");

        card.add(cardTitle, cardContent);
        return card;
    }

    /**
     * Create an action card with buttons
     */
    public static Div createActionCard(String title, String content, Component... actions) {
        Div card = new Div();
        card.addClassName("action-card");

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("action-card__title");

        Span cardContent = new Span(content);
        cardContent.addClassName("action-card__content");

        card.add(cardTitle, cardContent);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("action-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a stats card
     */
    public static Div createStatsCard(String label, String value, String modifier) {
        Div card = new Div();
        card.addClassName("stats-card");
        if (modifier != null && !modifier.isEmpty()) {
            card.addClassName("stats-card--" + modifier);
        }

        Div valueDiv = new Div();
        valueDiv.addClassName("stats-card__value");
        valueDiv.setText(value);

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-card__label");
        labelDiv.setText(label);

        card.add(valueDiv, labelDiv);
        return card;
    }

    /**
     * Create a deck card
     */
    public static Div createDeckCard(String title, String description, int cardCount, Component... actions) {
        Div card = new Div();
        card.addClassName("deck-card");

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("deck-card__title");

        Span cardDescription = new Span(description);
        cardDescription.addClassName("deck-card__description");

        Span cardCountSpan = new Span("Cards: " + cardCount);
        cardCountSpan.addClassName("deck-card__count");

        card.add(cardTitle, cardDescription, cardCountSpan);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("deck-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a user card
     */
    public static Div createUserCard(String name, String email, String role, Component... actions) {
        Div card = new Div();
        card.addClassName("user-card");

        H4 cardTitle = new H4(name);
        cardTitle.addClassName("user-card__title");

        Span cardEmail = new Span(email);
        cardEmail.addClassName("user-card__email");

        Span cardRole = new Span(role);
        cardRole.addClassName("user-card__role");

        card.add(cardTitle, cardEmail, cardRole);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("user-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a news card
     */
    public static Div createNewsCard(String title, String content, String author, String date, Component... actions) {
        Div card = new Div();
        card.addClassName("news-card");

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("news-card__title");

        Span cardContent = new Span(content);
        cardContent.addClassName("news-card__content");

        HorizontalLayout metaInfo = new HorizontalLayout();
        metaInfo.setSpacing(true);
        metaInfo.addClassName("news-card__meta");

        Span cardAuthor = new Span("By: " + author);
        cardAuthor.addClassName("news-card__author");

        Span cardDate = new Span(date);
        cardDate.addClassName("news-card__date");

        metaInfo.add(cardAuthor, cardDate);

        card.add(cardTitle, cardContent, metaInfo);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("news-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a practice card
     */
    public static Div createPracticeCard(String question, String answer, Component... actions) {
        Div card = new Div();
        card.addClassName("practice-card");

        H4 cardQuestion = new H4("Question");
        cardQuestion.addClassName("practice-card__question-label");

        Span questionText = new Span(question);
        questionText.addClassName("practice-card__question");

        H4 cardAnswer = new H4("Answer");
        cardAnswer.addClassName("practice-card__answer-label");

        Span answerText = new Span(answer);
        answerText.addClassName("practice-card__answer");

        card.add(cardQuestion, questionText, cardAnswer, answerText);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("practice-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a settings card
     */
    public static Div createSettingsCard(String title, String description, Component... controls) {
        Div card = new Div();
        card.addClassName("settings-card");

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("settings-card__title");

        Span cardDescription = new Span(description);
        cardDescription.addClassName("settings-card__description");

        card.add(cardTitle, cardDescription);

        if (controls.length > 0) {
            VerticalLayout controlsLayout = new VerticalLayout();
            controlsLayout.setSpacing(true);
            controlsLayout.addClassName("settings-card__controls");
            controlsLayout.add(controls);
            card.add(controlsLayout);
        }

        return card;
    }

    /**
     * Create a progress card
     */
    public static Div createProgressCard(String title, int current, int total, String unit) {
        Div card = new Div();
        card.addClassName("progress-card");

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("progress-card__title");

        Div progressBar = new Div();
        progressBar.addClassName("progress-card__bar");

        double percentage = total > 0 ? (double) current / total * 100 : 0;
        progressBar.getStyle().set("width", percentage + "%");

        Span progressText = new Span(current + " / " + total + " " + unit);
        progressText.addClassName("progress-card__text");

        card.add(cardTitle, progressBar, progressText);
        return card;
    }

    /**
     * Create a feature card
     */
    public static Div createFeatureCard(String title, String description, String icon, Component... actions) {
        Div card = new Div();
        card.addClassName("feature-card");

        Div iconDiv = new Div();
        iconDiv.addClassName("feature-card__icon");
        iconDiv.setText(icon);

        H4 cardTitle = new H4(title);
        cardTitle.addClassName("feature-card__title");

        Span cardDescription = new Span(description);
        cardDescription.addClassName("feature-card__description");

        card.add(iconDiv, cardTitle, cardDescription);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("feature-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a notification card
     */
    public static Div createNotificationCard(String title, String message, String type, Component... actions) {
        Div card = new Div();
        card.addClassName("notification-card");
        card.addClassName("notification-card--" + type);

        H5 cardTitle = new H5(title);
        cardTitle.addClassName("notification-card__title");

        Span cardMessage = new Span(message);
        cardMessage.addClassName("notification-card__message");

        card.add(cardTitle, cardMessage);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = LayoutHelper.createButtonRow(actions);
            actionsLayout.addClassName("notification-card__actions");
            card.add(actionsLayout);
        }

        return card;
    }

    /**
     * Create a simple card container
     */
    public static Div createCardContainer(String className) {
        Div card = new Div();
        card.addClassName("card");
        if (className != null && !className.isEmpty()) {
            card.addClassName(className);
        }
        return card;
    }

    /**
     * Apply hover effect to card
     */
    public static void applyHoverEffect(Div card) {
        card.addClassName("card--hoverable");
        AnimationHelper.setHoverEffect(card, 0.3, "ease");
    }

    /**
     * Apply shadow to card
     */
    public static void applyShadow(Div card) {
        card.addClassName("card--shadow");
        ColorHelper.setMediumShadow(card);
    }

    /**
     * Apply border to card
     */
    public static void applyBorder(Div card) {
        card.addClassName("card--bordered");
        ColorHelper.setPrimaryBorder(card);
    }
}
