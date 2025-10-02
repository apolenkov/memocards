package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Builder for creating collapsible statistics sections.
 * Handles creation of section headers with toggle functionality.
 * Extends Composite to access getTranslation() method.
 */
public final class CollapsibleSectionBuilder extends Composite<Component> {

    /**
     * Creates a new CollapsibleSectionBuilder.
     * No parameters needed as getTranslation() is inherited from Component.
     */
    public CollapsibleSectionBuilder() {
        // Intentionally empty - getTranslation() is available from Composite
    }

    /**
     * Creates a collapsible statistics section.
     *
     * @param titleKey translation key for section title
     * @param content the content to be collapsed/expanded
     * @param openByDefault whether section should be open by default
     * @return configured collapsible section
     */
    public VerticalLayout createCollapsibleSection(
            final String titleKey, final VerticalLayout content, final boolean openByDefault) {

        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.addClassName(StatsConstants.STATS_SECTION_CLASS);
        section.addClassName(StatsConstants.SURFACE_PANEL_CLASS);

        // Create header
        HorizontalLayout headerLayout = createSectionHeader(titleKey, openByDefault);
        section.add(headerLayout);

        // Setup toggle functionality
        setupToggleFunctionality(headerLayout, content);

        // Add content
        content.setVisible(openByDefault);
        section.add(content);

        return section;
    }

    /**
     * Creates section header with title and toggle button.
     *
     * @param titleKey translation key for section title
     * @param openByDefault whether section should be open by default
     * @return configured header layout
     */
    private HorizontalLayout createSectionHeader(final String titleKey, final boolean openByDefault) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.addClassName(StatsConstants.STATS_SECTION_HEADER_CLASS);

        H3 sectionTitle = new H3(getTranslation(titleKey));
        sectionTitle.addClassName(StatsConstants.STATS_SECTION_TITLE_CLASS);
        sectionTitle.addClassName(StatsConstants.CLICKABLE_TITLE_CLASS);

        Button toggleButton = ButtonHelper.createIconButton(
                openByDefault ? VaadinIcon.CHEVRON_DOWN : VaadinIcon.CHEVRON_RIGHT, ButtonVariant.LUMO_TERTIARY);
        toggleButton
                .getElement()
                .setAttribute(
                        StatsConstants.TITLE_ATTRIBUTE,
                        getTranslation(
                                openByDefault ? StatsConstants.STATS_COLLAPSE_KEY : StatsConstants.STATS_EXPAND_KEY));

        headerLayout.add(sectionTitle, toggleButton);
        return headerLayout;
    }

    /**
     * Sets up toggle functionality for collapsible section.
     *
     * @param headerLayout layout containing title and toggle button
     * @param content the content to be toggled
     */
    private void setupToggleFunctionality(final HorizontalLayout headerLayout, final VerticalLayout content) {

        // Find components
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

        if (toggleButton == null) {
            return;
        }

        // Toggle action
        Runnable toggleAction = () -> {
            boolean isVisible = content.isVisible();
            content.setVisible(!isVisible);
            toggleButton.setIcon((isVisible ? VaadinIcon.CHEVRON_RIGHT : VaadinIcon.CHEVRON_DOWN).create());
            toggleButton
                    .getElement()
                    .setAttribute(
                            StatsConstants.TITLE_ATTRIBUTE,
                            getTranslation(
                                    isVisible ? StatsConstants.STATS_EXPAND_KEY : StatsConstants.STATS_COLLAPSE_KEY));
        };

        // Add click listeners
        toggleButton.addClickListener(e -> toggleAction.run());
        if (sectionTitle != null) {
            sectionTitle.addClickListener(e -> toggleAction.run());
        }
    }
}
