package org.apolenkov.application.views.deck.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.components.MenuButton;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified header component for deck view containing navigation, title, description, and actions.
 * Combines functionality of DeckHeader, DeckInfo, and DeckActions for simpler code structure.
 *
 * <p>Features:
 * <ul>
 *   <li>Back navigation to decks list</li>
 *   <li>Deck title and statistics display</li>
 *   <li>Deck description section</li>
 *   <li>Action buttons: Practice, Edit, Delete</li>
 * </ul>
 */
public final class DeckDetailHeader extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckDetailHeader.class);

    // Header components
    private final Button backButton;
    private final H2 deckTitle;
    private final Span deckStats;

    // Action buttons (desktop)
    private final Button practiceButtonDesktop;
    private final Button resetProgressButton;
    private final Button addCardButtonDesktop;
    private final Button editDeckButton;
    private final Button deleteDeckButton;

    // Action buttons (mobile)
    private final Button addCardButtonMobile;

    // Action buttons for mobile
    private MenuButton deckActionsMenu;

    // Action callbacks (stored for menu creation)
    private ComponentEventListener<ClickEvent<Button>> practiceListener;
    private ComponentEventListener<ClickEvent<Button>> editDeckListener;
    private ComponentEventListener<ClickEvent<Button>> deleteDeckListener;
    private ComponentEventListener<ClickEvent<Button>> resetProgressListener;

    /**
     * Creates a new DeckDetailHeader component.
     */
    public DeckDetailHeader() {
        this.backButton = new Button();
        this.deckTitle = new H2();
        this.deckStats = new Span();
        // Action buttons (desktop)
        this.practiceButtonDesktop = new Button();
        this.resetProgressButton = new Button();
        this.addCardButtonDesktop = new Button();
        this.editDeckButton = new Button();
        this.deleteDeckButton = new Button();
        // Action buttons (mobile)
        this.addCardButtonMobile = new Button();
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setPadding(false);
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        // Header section: [← Back + Menu] + [Title + Stats]
        VerticalLayout headerRow = createHeaderRow();

        // Action buttons row
        HorizontalLayout actionsRow = createActionsRow();

        container.add(headerRow, actionsRow);
        return container;
    }

    /**
     * Creates header row with back button, action buttons, and menu.
     * Desktop: [← Back] [...space...] [Edit] [Delete]
     * Mobile: [← Back] [...space...] [⋮]
     * Title row below
     *
     * @return configured header layout
     */
    private VerticalLayout createHeaderRow() {
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.setWidthFull();
        headerContainer.setPadding(false);
        headerContainer.setSpacing(true);
        headerContainer.addClassName(DeckConstants.DECK_VIEW_HEADER_CLASS);

        // Top row: Back button + Desktop buttons + Mobile menu
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        configureBackButton();
        configureTitle();
        configureStats();
        configureEditDeckButton();
        configureDeleteDeckButton();

        // Desktop: right buttons [Edit, Delete] - visible on desktop
        HorizontalLayout desktopHeaderButtons = new HorizontalLayout();
        desktopHeaderButtons.setSpacing(true);
        desktopHeaderButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        desktopHeaderButtons.addClassName("desktop-only");
        desktopHeaderButtons.add(editDeckButton, deleteDeckButton);

        // Create menu (visible only on mobile via CSS)
        createDeckActionsMenu();
        deckActionsMenu.addClassName("mobile-only");

        topRow.add(backButton, desktopHeaderButtons, deckActionsMenu);

        // Title row: Only title (first line)
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.START);
        titleRow.addClassName(DeckConstants.DECK_HEADER_CENTER_CLASS);
        titleRow.add(deckTitle);

        // Stats row: Description + count (second line)
        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.setAlignItems(FlexComponent.Alignment.START);
        statsRow.addClassName("deck-stats-row");
        statsRow.add(deckStats);

        headerContainer.add(topRow, titleRow, statsRow);
        return headerContainer;
    }

    /**
     * Creates actions row with buttons.
     * Desktop: [Filter, Practice, Reset] - centered (Edit/Delete moved to header)
     * Mobile: [Filter, + Practice] - centered
     *
     * @return configured actions row
     */
    private HorizontalLayout createActionsRow() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.addClassName(DeckConstants.DECK_VIEW_ACTIONS_CLASS);
        actions.addClassName(DeckConstants.DECK_CENTERED_SECTION_CLASS);

        // Configure all buttons
        configurePracticeButtons();
        configureResetProgressButton();
        configureAddCardButton();

        // Desktop: left buttons [Practice, Reset, Add] - visible on desktop
        HorizontalLayout desktopLeftButtons = new HorizontalLayout();
        desktopLeftButtons.setSpacing(true);
        desktopLeftButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        desktopLeftButtons.addClassName("desktop-only");
        desktopLeftButtons.add(practiceButtonDesktop, resetProgressButton, addCardButtonDesktop);

        // Mobile: only Add button visible, Practice and Reset in menu
        HorizontalLayout mobileButtons = new HorizontalLayout();
        mobileButtons.setSpacing(true);
        mobileButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        mobileButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        mobileButtons.setWidthFull();
        mobileButtons.addClassName("mobile-only");
        mobileButtons.addClassName("mobile-actions");
        mobileButtons.add(addCardButtonMobile);

        actions.add(desktopLeftButtons, mobileButtons);
        return actions;
    }

    /**
     * Configures the back navigation button.
     */
    private void configureBackButton() {
        backButton.setText(getTranslation(DeckConstants.DECKS_TITLE_KEY));
        backButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClassName(DeckConstants.DECK_VIEW_BACK_BUTTON_CLASS);
        backButton.addClickListener(e -> NavigationHelper.navigateToDecks());
    }

    /**
     * Configures the deck title display.
     */
    private void configureTitle() {
        deckTitle.setText(getTranslation(DeckConstants.DECK_LOADING));
        deckTitle.addClassName(DeckConstants.PAGE_TITLE_CLASS);
    }

    /**
     * Configures the deck statistics display.
     */
    private void configureStats() {
        deckStats.addClassName(DeckConstants.DECK_VIEW_STATS_CLASS);
    }

    /**
     * Configures the practice button (desktop only).
     */
    private void configurePracticeButtons() {
        // Desktop practice button
        practiceButtonDesktop.setText(getTranslation(DeckConstants.DECK_START_SESSION));
        practiceButtonDesktop.setIcon(VaadinIcon.PLAY.create());
        practiceButtonDesktop.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    /**
     * Configures the reset progress button (desktop only).
     */
    private void configureResetProgressButton() {
        resetProgressButton.setText(getTranslation(DeckConstants.DECK_RESET_PROGRESS));
        resetProgressButton.setIcon(VaadinIcon.ROTATE_LEFT.create());
        resetProgressButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    }

    /**
     * Configures the add card buttons (desktop and mobile).
     */
    private void configureAddCardButton() {
        // Desktop add card button
        addCardButtonDesktop.setText(getTranslation(DeckConstants.DECK_ADD_CARD));
        addCardButtonDesktop.setIcon(VaadinIcon.PLUS.create());
        addCardButtonDesktop.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCardButtonDesktop.addClassName("deck-add-card-button");

        // Mobile add card button
        addCardButtonMobile.setText(getTranslation(DeckConstants.DECK_ADD_CARD));
        addCardButtonMobile.setIcon(VaadinIcon.PLUS.create());
        addCardButtonMobile.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCardButtonMobile.addClassName("deck-add-card-button");
    }

    /**
     * Configures the edit deck button (desktop only).
     */
    private void configureEditDeckButton() {
        editDeckButton.setText(getTranslation(DeckConstants.COMMON_EDIT));
        editDeckButton.setIcon(VaadinIcon.EDIT.create());
        editDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editDeckButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, getTranslation(DeckConstants.DECK_EDIT_TOOLTIP));
    }

    /**
     * Configures the delete deck button (desktop only).
     */
    private void configureDeleteDeckButton() {
        deleteDeckButton.setText(getTranslation(DeckConstants.COMMON_DELETE));
        deleteDeckButton.setIcon(VaadinIcon.TRASH.create());
        deleteDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    }

    /**
     * Creates the deck actions menu with practice, edit, delete, and reset progress options.
     */
    private void createDeckActionsMenu() {
        List<MenuButton.MenuItem> menuItems = List.of(
                MenuButton.MenuItem.of(getTranslation(DeckConstants.DECK_MENU_PRACTICE), VaadinIcon.PLAY, v -> {
                    if (practiceListener != null) {
                        practiceListener.onComponentEvent(new ClickEvent<>(practiceButtonDesktop));
                    }
                }),
                MenuButton.MenuItem.of(getTranslation(DeckConstants.DECK_MENU_RESET), VaadinIcon.ROTATE_LEFT, v -> {
                    if (resetProgressListener != null) {
                        resetProgressListener.onComponentEvent(new ClickEvent<>(resetProgressButton));
                    }
                }),
                MenuButton.MenuItem.of(getTranslation(DeckConstants.DECK_MENU_EDIT), VaadinIcon.EDIT, v -> {
                    if (editDeckListener != null) {
                        editDeckListener.onComponentEvent(new ClickEvent<>(editDeckButton));
                    }
                }),
                MenuButton.MenuItem.withTheme(
                        getTranslation(DeckConstants.DECK_MENU_DELETE),
                        VaadinIcon.TRASH,
                        v -> {
                            if (deleteDeckListener != null) {
                                deleteDeckListener.onComponentEvent(new ClickEvent<>(deleteDeckButton));
                            }
                        },
                        "error"));

        deckActionsMenu = new MenuButton(menuItems);
        deckActionsMenu.addClassName(DeckConstants.DECK_ACTIONS_MENU_CLASS);
    }

    // ==================== Public API ====================

    /**
     * Updates the deck title with the provided text.
     *
     * @param title the new title text to display
     */
    public void setDeckTitle(final String title) {
        deckTitle.setText(title);
    }

    /**
     * Updates the deck statistics with the provided text.
     *
     * @param stats the new statistics text to display
     */
    public void setDeckStats(final String stats) {
        deckStats.setText(stats);
    }

    /**
     * Adds a listener for practice button clicks (desktop button + mobile menu).
     *
     * @param listener the event listener for practice button clicks
     * @return registration for removing the listener
     */
    public Registration addPracticeClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        LOGGER.debug("Practice listener registered");
        this.practiceListener = listener;

        // Desktop button
        Registration reg1 = practiceButtonDesktop.addClickListener(e -> {
            LOGGER.debug("Practice button (desktop) clicked");
            listener.onComponentEvent(e);
        });

        return () -> {
            reg1.remove();
            this.practiceListener = null;
        };
    }

    /**
     * Adds a listener for reset progress button clicks.
     *
     * @param listener the event listener for reset progress button clicks
     * @return registration for removing the listener
     */
    public Registration addResetProgressClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        LOGGER.debug("Reset progress listener registered");
        this.resetProgressListener = listener;
        // Register on button and menu
        Registration reg1 = resetProgressButton.addClickListener(e -> {
            LOGGER.debug("Reset progress button clicked");
            listener.onComponentEvent(e);
        });

        return () -> {
            reg1.remove();
            this.resetProgressListener = null;
        };
    }

    /**
     * Adds a listener for add card button clicks (both desktop and mobile).
     *
     * @param listener the event listener for add card button clicks
     * @return registration for removing the listener
     */
    public Registration addAddCardClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        LOGGER.debug("Add card listener registered");

        // Desktop button
        Registration reg1 = addCardButtonDesktop.addClickListener(e -> {
            LOGGER.debug("Add card button (desktop) clicked");
            listener.onComponentEvent(e);
        });

        // Mobile button
        Registration reg2 = addCardButtonMobile.addClickListener(e -> {
            LOGGER.debug("Add card button (mobile) clicked");
            listener.onComponentEvent(e);
        });

        // Return combined registration
        return () -> {
            reg1.remove();
            reg2.remove();
        };
    }

    /**
     * Adds a listener for edit deck clicks (button + menu).
     *
     * @param listener the event listener for edit deck action
     * @return registration for removing the listener
     */
    public Registration addEditDeckClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        LOGGER.debug("Edit deck listener registered");
        this.editDeckListener = listener;
        // Register on button and menu
        Registration reg1 = editDeckButton.addClickListener(e -> {
            LOGGER.debug("Edit deck button clicked");
            listener.onComponentEvent(e);
        });

        return () -> {
            reg1.remove();
            this.editDeckListener = null;
        };
    }

    /**
     * Adds a listener for delete deck clicks (button + menu).
     *
     * @param listener the event listener for delete deck action
     * @return registration for removing the listener
     */
    public Registration addDeleteDeckClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        LOGGER.debug("Delete deck listener registered");
        this.deleteDeckListener = listener;
        // Register on button and menu
        Registration reg1 = deleteDeckButton.addClickListener(e -> {
            LOGGER.debug("Delete deck button clicked");
            listener.onComponentEvent(e);
        });

        return () -> {
            reg1.remove();
            this.deleteDeckListener = null;
        };
    }
}
