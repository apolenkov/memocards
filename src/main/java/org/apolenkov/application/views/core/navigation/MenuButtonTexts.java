package org.apolenkov.application.views.core.navigation;

/**
 * Record holding translated texts for menu buttons.
 * Simplifies parameter passing between menu components.
 *
 * @param decks the text for the decks menu button
 * @param stats the text for the stats menu button
 * @param settings the text for the settings menu button
 * @param adminContent the text for the admin content menu button
 * @param logout the text for the logout menu button
 */
public record MenuButtonTexts(String decks, String stats, String settings, String adminContent, String logout) {}
