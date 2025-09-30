package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.shared.interfaces.TranslationProvider;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Factory for creating statistics section headers with collapsible functionality.
 * Handles creation of section headers with toggle buttons and collapsible behavior.
 */
public final class StatsSectionHeaderFactory {

    // Dependencies
    private TranslationProvider translationProvider;

    /**
     * Sets the translation provider for localized strings.
     *
     * @param translationProviderParam provider for translations
     */
    public void setTranslationProvider(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
    }

    /**
     * Creates a statistics section header with title and toggle button.
     *
     * @param titleKey translation key for the section title
     * @return configured vertical layout with header
     */
    public VerticalLayout createStatsSectionHeader(final String titleKey) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.addClassName(StatsConstants.STATS_SECTION_CLASS);
        section.addClassName(StatsConstants.SURFACE_PANEL_CLASS);

        // Create collapsible header
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.addClassName("stats-section__header");

        H3 sectionTitle = new H3(translationProvider.getTranslation(titleKey));
        sectionTitle.addClassName(StatsConstants.STATS_SECTION_TITLE_CLASS);
        sectionTitle.addClassName(StatsConstants.CLICKABLE_TITLE_CLASS);

        Button toggleButton = ButtonHelper.createIconButton(VaadinIcon.CHEVRON_DOWN, ButtonVariant.LUMO_TERTIARY);
        toggleButton
                .getElement()
                .setAttribute(
                        StatsConstants.TITLE_ATTRIBUTE,
                        translationProvider.getTranslation(StatsConstants.STATS_COLLAPSE_KEY));

        headerLayout.add(sectionTitle, toggleButton);
        section.add(headerLayout);

        return section;
    }

    /**
     * Sets up collapsible functionality for a statistics section.
     *
     * @param section the main section container
     * @param contentContainer the content to be collapsed/expanded
     * @param openByDefault whether the section should be open by default
     */
    public void setupCollapsibleSection(
            final VerticalLayout section, final VerticalLayout contentContainer, final boolean openByDefault) {
        // Set initial visibility
        contentContainer.setVisible(openByDefault);

        // Find header layout and components
        HorizontalLayout headerLayout = (HorizontalLayout) section.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElse(null);

        if (headerLayout == null) {
            section.add(contentContainer);
            return;
        }

        // Find title and toggle button
        H3 sectionTitle = (H3) headerLayout
                .getChildren()
                .filter(H3.class::isInstance)
                .findFirst()
                .orElse(null);

        Button toggleButton = (Button) headerLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);

        if (toggleButton != null) {
            // Set initial icon and tooltip
            if (openByDefault) {
                toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                toggleButton
                        .getElement()
                        .setAttribute(
                                StatsConstants.TITLE_ATTRIBUTE,
                                translationProvider.getTranslation(StatsConstants.STATS_COLLAPSE_KEY));
            } else {
                toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                toggleButton
                        .getElement()
                        .setAttribute(
                                StatsConstants.TITLE_ATTRIBUTE,
                                translationProvider.getTranslation(StatsConstants.STATS_EXPAND_KEY));
            }

            // Toggle functionality
            Runnable toggleAction = () -> {
                if (contentContainer.isVisible()) {
                    contentContainer.setVisible(false);
                    toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                    toggleButton
                            .getElement()
                            .setAttribute(
                                    StatsConstants.TITLE_ATTRIBUTE,
                                    translationProvider.getTranslation(StatsConstants.STATS_EXPAND_KEY));
                } else {
                    contentContainer.setVisible(true);
                    toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                    toggleButton
                            .getElement()
                            .setAttribute(
                                    StatsConstants.TITLE_ATTRIBUTE,
                                    translationProvider.getTranslation(StatsConstants.STATS_COLLAPSE_KEY));
                }
            };

            // Add click listener to toggle button
            toggleButton.addClickListener(e -> toggleAction.run());

            // Add click listener to section title
            if (sectionTitle != null) {
                sectionTitle.addClickListener(e -> toggleAction.run());
            }
        }

        // Add content container to section
        section.add(contentContainer);
    }

    /**
     * Sets up collapsible functionality for a statistics section (closed by default).
     *
     * @param section the main section container
     * @param contentContainer the content to be collapsed/expanded
     */
    public void setupCollapsibleSection(final VerticalLayout section, final VerticalLayout contentContainer) {
        setupCollapsibleSection(section, contentContainer, false);
    }
}
