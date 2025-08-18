package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Utility class for centralized responsive design management.
 * Eliminates duplication of responsive patterns across the application.
 */
public final class ResponsiveHelper {

    // Breakpoint constants
    public static final String MOBILE = "mobile";
    public static final String TABLET = "tablet";
    public static final String DESKTOP = "desktop";
    public static final String LARGE_DESKTOP = "large-desktop";

    // CSS classes for responsive design
    public static final String RESPONSIVE_HIDDEN = "responsive-hidden";
    public static final String RESPONSIVE_VISIBLE = "responsive-visible";
    public static final String RESPONSIVE_STACK = "responsive-stack";
    public static final String RESPONSIVE_GRID = "responsive-grid";

    private ResponsiveHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a responsive container that adapts to screen size
     */
    public static Div createResponsiveContainer() {
        Div container = new Div();
        container.addClassName("responsive-container");
        container
                .getElement()
                .executeJs(
                        """
            this.addEventListener('resize', function() {
                this.classList.remove('mobile', 'tablet', 'desktop', 'large-desktop');
                if (window.innerWidth < 768) {
                    this.classList.add('mobile');
                } else if (window.innerWidth < 1024) {
                    this.classList.add('tablet');
                } else if (window.innerWidth < 1440) {
                    this.classList.add('desktop');
                } else {
                    this.classList.add('large-desktop');
                }
            });
            // Trigger initial check
            this.dispatchEvent(new Event('resize'));
        """);
        return container;
    }

    /**
     * Create a responsive grid layout
     */
    public static HorizontalLayout createResponsiveGrid() {
        HorizontalLayout grid = new HorizontalLayout();
        grid.addClassName("responsive-grid");
        grid.getElement()
                .executeJs(
                        """
            this.addEventListener('resize', function() {
                if (window.innerWidth < 768) {
                    this.style.flexDirection = 'column';
                    this.style.alignItems = 'stretch';
                } else {
                    this.style.flexDirection = 'row';
                    this.style.alignItems = 'center';
                }
            });
            // Trigger initial check
            this.dispatchEvent(new Event('resize'));
        """);
        return grid;
    }

    /**
     * Create a responsive form layout
     */
    public static VerticalLayout createResponsiveForm() {
        VerticalLayout form = new VerticalLayout();
        form.addClassName("responsive-form");
        form.getElement()
                .executeJs(
                        """
            this.addEventListener('resize', function() {
                if (window.innerWidth < 768) {
                    this.style.width = '100%';
                    this.style.maxWidth = 'none';
                } else if (window.innerWidth < 1024) {
                    this.style.width = '80%';
                    this.style.maxWidth = '600px';
                } else {
                    this.style.width = '60%';
                    this.style.maxWidth = '800px';
                }
            });
            // Trigger initial check
            this.dispatchEvent(new Event('resize'));
        """);
        return form;
    }

    /**
     * Hide component on mobile devices
     */
    public static void hideOnMobile(Component component) {
        component.addClassName("hide-mobile");
        component
                .getElement()
                .executeJs(
                        """
            if (window.innerWidth < 768) {
                this.style.display = 'none';
            }
            window.addEventListener('resize', function() {
                if (window.innerWidth < 768) {
                    this.style.display = 'none';
                } else {
                    this.style.display = '';
                }
            }.bind(this));
        """);
    }

    /**
     * Hide component on tablet devices
     */
    public static void hideOnTablet(Component component) {
        component.addClassName("hide-tablet");
        component
                .getElement()
                .executeJs(
                        """
            if (window.innerWidth >= 768 && window.innerWidth < 1024) {
                this.style.display = 'none';
            }
            window.addEventListener('resize', function() {
                if (window.innerWidth >= 768 && window.innerWidth < 1024) {
                    this.style.display = 'none';
                } else {
                    this.style.display = '';
                }
            }.bind(this));
        """);
    }

    /**
     * Hide component on desktop devices
     */
    public static void hideOnDesktop(Component component) {
        component.addClassName("hide-desktop");
        component
                .getElement()
                .executeJs(
                        """
            if (window.innerWidth >= 1024) {
                this.style.display = 'none';
            }
            window.addEventListener('resize', function() {
                if (window.innerWidth >= 1024) {
                    this.style.display = 'none';
                } else {
                    this.style.display = '';
                }
            }.bind(this));
        """);
    }

    /**
     * Show component only on mobile devices
     */
    public static void showOnlyOnMobile(Component component) {
        component.addClassName("show-mobile-only");
        component
                .getElement()
                .executeJs(
                        """
            if (window.innerWidth >= 768) {
                this.style.display = 'none';
            }
            window.addEventListener('resize', function() {
                if (window.innerWidth >= 768) {
                    this.style.display = 'none';
                } else {
                    this.style.display = '';
                }
            }.bind(this));
        """);
    }

    /**
     * Show component only on tablet devices
     */
    public static void showOnlyOnTablet(Component component) {
        component.addClassName("show-tablet-only");
        component
                .getElement()
                .executeJs(
                        """
            if (window.innerWidth < 768 || window.innerWidth >= 1024) {
                this.style.display = 'none';
            }
            window.addEventListener('resize', function() {
                if (window.innerWidth < 768 || window.innerWidth >= 1024) {
                    this.style.display = 'none';
                } else {
                    this.style.display = '';
                }
            }.bind(this));
        """);
    }

    /**
     * Show component only on desktop devices
     */
    public static void showOnlyOnDesktop(Component component) {
        component.addClassName("show-desktop-only");
        component
                .getElement()
                .executeJs(
                        """
            if (window.innerWidth < 1024) {
                this.style.display = 'none';
            }
            window.addEventListener('resize', function() {
                if (window.innerWidth < 1024) {
                    this.style.display = 'none';
                } else {
                    this.style.display = '';
                }
            }.bind(this));
        """);
    }

    /**
     * Set responsive width based on screen size
     */
    public static void setResponsiveWidth(
            Component component, String mobileWidth, String tabletWidth, String desktopWidth) {
        component
                .getElement()
                .executeJs(
                        """
            function updateWidth() {
                if (window.innerWidth < 768) {
                    this.style.width = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.width = arguments[1];
                } else {
                    this.style.width = arguments[2];
                }
            }
            updateWidth.call(this);
            window.addEventListener('resize', updateWidth.bind(this));
        """,
                        mobileWidth,
                        tabletWidth,
                        desktopWidth);
    }

    /**
     * Set responsive height based on screen size
     */
    public static void setResponsiveHeight(
            Component component, String mobileHeight, String tabletHeight, String desktopHeight) {
        component
                .getElement()
                .executeJs(
                        """
            function updateHeight() {
                if (window.innerWidth < 768) {
                    this.style.height = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.height = arguments[1];
                } else {
                    this.style.height = arguments[2];
                }
            }
            updateHeight.call(this);
            window.addEventListener('resize', updateHeight.bind(this));
        """,
                        mobileHeight,
                        tabletHeight,
                        desktopHeight);
    }

    /**
     * Set responsive font size based on screen size
     */
    public static void setResponsiveFontSize(
            Component component, String mobileSize, String tabletSize, String desktopSize) {
        component
                .getElement()
                .executeJs(
                        """
            function updateFontSize() {
                if (window.innerWidth < 768) {
                    this.style.fontSize = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.fontSize = arguments[1];
                } else {
                    this.style.fontSize = arguments[2];
                }
            }
            updateFontSize.call(this);
            window.addEventListener('resize', updateFontSize.bind(this));
        """,
                        mobileSize,
                        tabletSize,
                        desktopSize);
    }

    /**
     * Set responsive padding based on screen size
     */
    public static void setResponsivePadding(
            Component component, String mobilePadding, String tabletPadding, String desktopPadding) {
        component
                .getElement()
                .executeJs(
                        """
            function updatePadding() {
                if (window.innerWidth < 768) {
                    this.style.padding = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.padding = arguments[1];
                } else {
                    this.style.padding = arguments[2];
                }
            }
            updatePadding.call(this);
            window.addEventListener('resize', updatePadding.bind(this));
        """,
                        mobilePadding,
                        tabletPadding,
                        desktopPadding);
    }

    /**
     * Set responsive margin based on screen size
     */
    public static void setResponsiveMargin(
            Component component, String mobileMargin, String tabletMargin, String desktopMargin) {
        component
                .getElement()
                .executeJs(
                        """
            function updateMargin() {
                if (window.innerWidth < 768) {
                    this.style.margin = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.margin = arguments[1];
                } else {
                    this.style.margin = arguments[2];
                }
            }
            updateMargin.call(this);
            window.addEventListener('resize', updateMargin.bind(this));
        """,
                        mobileMargin,
                        tabletMargin,
                        desktopMargin);
    }

    /**
     * Set responsive flex direction
     */
    public static void setResponsiveFlexDirection(
            Component component, String mobileDirection, String tabletDirection, String desktopDirection) {
        component
                .getElement()
                .executeJs(
                        """
            function updateFlexDirection() {
                if (window.innerWidth < 768) {
                    this.style.flexDirection = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.flexDirection = arguments[1];
                } else {
                    this.style.flexDirection = arguments[2];
                }
            }
            updateFlexDirection.call(this);
            window.addEventListener('resize', updateFlexDirection.bind(this));
        """,
                        mobileDirection,
                        tabletDirection,
                        desktopDirection);
    }

    /**
     * Set responsive alignment
     */
    public static void setResponsiveAlignment(
            Component component, String mobileAlign, String tabletAlign, String desktopAlign) {
        component
                .getElement()
                .executeJs(
                        """
            function updateAlignment() {
                if (window.innerWidth < 768) {
                    this.style.alignItems = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.alignItems = arguments[1];
                } else {
                    this.style.alignItems = arguments[2];
                }
            }
            updateAlignment.call(this);
            window.addEventListener('resize', updateAlignment.bind(this));
        """,
                        mobileAlign,
                        tabletAlign,
                        desktopAlign);
    }

    /**
     * Set responsive justification
     */
    public static void setResponsiveJustification(
            Component component, String mobileJustify, String tabletJustify, String desktopJustify) {
        component
                .getElement()
                .executeJs(
                        """
            function updateJustification() {
                if (window.innerWidth < 768) {
                    this.style.justifyContent = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.justifyContent = arguments[1];
                } else {
                    this.style.justifyContent = arguments[2];
                }
            }
            updateJustification.call(this);
            window.addEventListener('resize', updateJustification.bind(this));
        """,
                        mobileJustify,
                        tabletJustify,
                        desktopJustify);
    }

    /**
     * Set responsive grid columns
     */
    public static void setResponsiveGridColumns(Component component, int mobileCols, int tabletCols, int desktopCols) {
        component
                .getElement()
                .executeJs(
                        """
            function updateGridColumns() {
                if (window.innerWidth < 768) {
                    this.style.gridTemplateColumns = 'repeat(' + arguments[0] + ', 1fr)';
                } else if (window.innerWidth < 1024) {
                    this.style.gridTemplateColumns = 'repeat(' + arguments[1] + ', 1fr)';
                } else {
                    this.style.gridTemplateColumns = 'repeat(' + arguments[2] + ', 1fr)';
                }
            }
            updateGridColumns.call(this);
            window.addEventListener('resize', updateGridColumns.bind(this));
        """,
                        mobileCols,
                        tabletCols,
                        desktopCols);
    }

    /**
     * Set responsive spacing
     */
    public static void setResponsiveSpacing(
            Component component, String mobileSpacing, String tabletSpacing, String desktopSpacing) {
        component
                .getElement()
                .executeJs(
                        """
            function updateSpacing() {
                if (window.innerWidth < 768) {
                    this.style.gap = arguments[0];
                } else if (window.innerWidth < 1024) {
                    this.style.gap = arguments[1];
                } else {
                    this.style.gap = arguments[2];
                }
            }
            updateSpacing.call(this);
            window.addEventListener('resize', updateSpacing.bind(this));
        """,
                        mobileSpacing,
                        tabletSpacing,
                        desktopSpacing);
    }

    /**
     * Get current screen size category
     */
    public static String getCurrentScreenSize() {
        // This would need to be called from JavaScript context
        return "desktop"; // Default fallback
    }

    /**
     * Check if current screen is mobile
     */
    public static boolean isMobile() {
        return false; // Would need JavaScript integration
    }

    /**
     * Check if current screen is tablet
     */
    public static boolean isTablet() {
        return false; // Would need JavaScript integration
    }

    /**
     * Check if current screen is desktop
     */
    public static boolean isDesktop() {
        return true; // Would need JavaScript integration
    }
}
