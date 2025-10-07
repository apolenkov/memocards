package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * UI component for creating collapsible statistics sections.
 * Extends Composite to access translation methods directly.
 */
public final class CollapsibleSectionBuilder extends Composite<VerticalLayout> {

    private final String titleKey;
    private final transient VerticalLayout content;
    private final boolean openByDefault;

    // Event Registrations
    private Registration toggleButtonListenerRegistration;
    private Registration titleClickListenerRegistration;

    /**
     * Creates a new collapsible section builder.
     *
     * @param titleKeyParam translation key for section title
     * @param contentParam the content to be collapsed/expanded
     * @param openByDefaultParam whether section should be open by default
     */
    public CollapsibleSectionBuilder(
            final String titleKeyParam, final VerticalLayout contentParam, final boolean openByDefaultParam) {
        this.titleKey = titleKeyParam;
        this.content = contentParam;
        this.openByDefault = openByDefaultParam;
    }

    @Override
    protected VerticalLayout initContent() {
        return createCollapsibleSection();
    }

    /**
     * Creates a collapsible statistics section.
     *
     * @return configured collapsible section
     */
    private VerticalLayout createCollapsibleSection() {

        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.addClassName(StatsConstants.STATS_SECTION_CLASS);
        section.addClassName(StatsConstants.SURFACE_PANEL_CLASS);

        // Create header
        HorizontalLayout headerLayout = createSectionHeader();
        section.add(headerLayout);

        // Setup toggle functionality
        setupToggleFunctionality(headerLayout);

        // Add content
        content.setVisible(openByDefault);
        section.add(content);

        return section;
    }

    /**
     * Creates section header with title and toggle button.
     *
     * @return configured header layout
     */
    private HorizontalLayout createSectionHeader() {
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
     */
    private void setupToggleFunctionality(final HorizontalLayout headerLayout) {

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
        toggleButtonListenerRegistration = toggleButton.addClickListener(e -> toggleAction.run());
        if (sectionTitle != null) {
            titleClickListenerRegistration = sectionTitle.addClickListener(e -> toggleAction.run());
        }
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        if (toggleButtonListenerRegistration != null) {
            toggleButtonListenerRegistration.remove();
            toggleButtonListenerRegistration = null;
        }
        if (titleClickListenerRegistration != null) {
            titleClickListenerRegistration.remove();
            titleClickListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
