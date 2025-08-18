package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Style;

/**
 * Utility class for centralized animation management and effects.
 * Eliminates duplication of animation patterns across the application.
 */
public final class AnimationHelper {

    private AnimationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Set fade in animation
     */
    public static void setFadeIn(Component component) {
        Style style = component.getStyle();
        style.set("opacity", "0");
        style.set("animation", "fadeIn 0.5s ease-in forwards");
    }

    /**
     * Set fade out animation
     */
    public static void setFadeOut(Component component) {
        Style style = component.getStyle();
        style.set("animation", "fadeOut 0.5s ease-out forwards");
    }

    /**
     * Set slide in from left animation
     */
    public static void setSlideInLeft(Component component) {
        Style style = component.getStyle();
        style.set("transform", "translateX(-100%)");
        style.set("animation", "slideInLeft 0.5s ease-out forwards");
    }

    /**
     * Set slide in from right animation
     */
    public static void setSlideInRight(Component component) {
        Style style = component.getStyle();
        style.set("transform", "translateX(100%)");
        style.set("animation", "slideInRight 0.5s ease-out forwards");
    }

    /**
     * Set slide in from top animation
     */
    public static void setSlideInTop(Component component) {
        Style style = component.getStyle();
        style.set("transform", "translateY(-100%)");
        style.set("animation", "slideInTop 0.5s ease-out forwards");
    }

    /**
     * Set slide in from bottom animation
     */
    public static void setSlideInBottom(Component component) {
        Style style = component.getStyle();
        style.set("transform", "translateY(100%)");
        style.set("animation", "slideInBottom 0.5s ease-out forwards");
    }

    /**
     * Set scale in animation
     */
    public static void setScaleIn(Component component) {
        Style style = component.getStyle();
        style.set("transform", "scale(0)");
        style.set("animation", "scaleIn 0.5s ease-out forwards");
    }

    /**
     * Set scale out animation
     */
    public static void setScaleOut(Component component) {
        Style style = component.getStyle();
        style.set("animation", "scaleOut 0.5s ease-out forwards");
    }

    /**
     * Set bounce animation
     */
    public static void setBounce(Component component) {
        Style style = component.getStyle();
        style.set("animation", "bounce 0.6s ease-out");
    }

    /**
     * Set pulse animation
     */
    public static void setPulse(Component component) {
        Style style = component.getStyle();
        style.set("animation", "pulse 1s ease-in-out infinite");
    }

    /**
     * Set shake animation
     */
    public static void setShake(Component component) {
        Style style = component.getStyle();
        style.set("animation", "shake 0.5s ease-in-out");
    }

    /**
     * Set rotate animation
     */
    public static void setRotate(Component component) {
        Style style = component.getStyle();
        style.set("animation", "rotate 1s linear infinite");
    }

    /**
     * Set flip animation
     */
    public static void setFlip(Component component) {
        Style style = component.getStyle();
        style.set("animation", "flip 0.6s ease-in-out");
    }

    /**
     * Set hinge animation
     */
    public static void setHinge(Component component) {
        Style style = component.getStyle();
        style.set("animation", "hinge 1s ease-in-out");
    }

    /**
     * Set roll in animation
     */
    public static void setRollIn(Component component) {
        Style style = component.getStyle();
        style.set("transform", "translateX(-100%) rotate(-120deg)");
        style.set("animation", "rollIn 0.6s ease-out forwards");
    }

    /**
     * Set roll out animation
     */
    public static void setRollOut(Component component) {
        Style style = component.getStyle();
        style.set("animation", "rollOut 0.6s ease-in forwards");
    }

    /**
     * Set zoom in animation
     */
    public static void setZoomIn(Component component) {
        Style style = component.getStyle();
        style.set("transform", "scale(0.3)");
        style.set("animation", "zoomIn 0.6s ease-out forwards");
    }

    /**
     * Set zoom out animation
     */
    public static void setZoomOut(Component component) {
        Style style = component.getStyle();
        style.set("animation", "zoomOut 0.6s ease-in forwards");
    }

    /**
     * Set light speed in animation
     */
    public static void setLightSpeedIn(Component component) {
        Style style = component.getStyle();
        style.set("transform", "translateX(100%) skewX(-30deg)");
        style.set("animation", "lightSpeedIn 0.5s ease-out forwards");
    }

    /**
     * Set light speed out animation
     */
    public static void setLightSpeedOut(Component component) {
        Style style = component.getStyle();
        style.set("animation", "lightSpeedOut 0.5s ease-in forwards");
    }

    /**
     * Set swing animation
     */
    public static void setSwing(Component component) {
        Style style = component.getStyle();
        style.set("transform-origin", "top center");
        style.set("animation", "swing 1s ease-in-out");
    }

    /**
     * Set wobble animation
     */
    public static void setWobble(Component component) {
        Style style = component.getStyle();
        style.set("animation", "wobble 1s ease-in-out");
    }

    /**
     * Set tada animation
     */
    public static void setTada(Component component) {
        Style style = component.getStyle();
        style.set("animation", "tada 1s ease-in-out");
    }

    /**
     * Set jello animation
     */
    public static void setJello(Component component) {
        Style style = component.getStyle();
        style.set("animation", "jello 1s ease-in-out");
    }

    /**
     * Set heart beat animation
     */
    public static void setHeartBeat(Component component) {
        Style style = component.getStyle();
        style.set("animation", "heartBeat 1.3s ease-in-out infinite");
    }

    /**
     * Set rubber band animation
     */
    public static void setRubberBand(Component component) {
        Style style = component.getStyle();
        style.set("animation", "rubberBand 0.8s ease-in-out");
    }

    /**
     * Set flip in X animation
     */
    public static void setFlipInX(Component component) {
        Style style = component.getStyle();
        style.set("transform", "perspective(400px) rotateX(90deg)");
        style.set("animation", "flipInX 0.8s ease-out forwards");
    }

    /**
     * Set flip in Y animation
     */
    public static void setFlipInY(Component component) {
        Style style = component.getStyle();
        style.set("transform", "perspective(400px) rotateY(90deg)");
        style.set("animation", "flipInY 0.8s ease-out forwards");
    }

    /**
     * Set flip out X animation
     */
    public static void setFlipOutX(Component component) {
        Style style = component.getStyle();
        style.set("animation", "flipOutX 0.8s ease-in forwards");
    }

    /**
     * Set flip out Y animation
     */
    public static void setFlipOutY(Component component) {
        Style style = component.getStyle();
        style.set("animation", "flipOutY 0.8s ease-in forwards");
    }

    /**
     * Set custom animation
     */
    public static void setCustomAnimation(
            Component component, String animationName, double duration, String timingFunction) {
        Style style = component.getStyle();
        style.set("animation", String.format("%s %.2fs %s", animationName, duration, timingFunction));
    }

    /**
     * Set animation delay
     */
    public static void setAnimationDelay(Component component, double delay) {
        Style style = component.getStyle();
        style.set("animation-delay", delay + "s");
    }

    /**
     * Set animation iteration count
     */
    public static void setAnimationIterationCount(Component component, int count) {
        Style style = component.getStyle();
        style.set("animation-iteration-count", String.valueOf(count));
    }

    /**
     * Set animation direction
     */
    public static void setAnimationDirection(Component component, String direction) {
        Style style = component.getStyle();
        style.set("animation-direction", direction);
    }

    /**
     * Set animation fill mode
     */
    public static void setAnimationFillMode(Component component, String fillMode) {
        Style style = component.getStyle();
        style.set("animation-fill-mode", fillMode);
    }

    /**
     * Set transition for smooth animations
     */
    public static void setTransition(Component component, String property, double duration, String timingFunction) {
        Style style = component.getStyle();
        style.set("transition", String.format("%s %.2fs %s", property, duration, timingFunction));
    }

    /**
     * Set all transitions
     */
    public static void setAllTransitions(Component component, double duration, String timingFunction) {
        setTransition(component, "all", duration, timingFunction);
    }

    /**
     * Set transform transition
     */
    public static void setTransformTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "transform", duration, timingFunction);
    }

    /**
     * Set opacity transition
     */
    public static void setOpacityTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "opacity", duration, timingFunction);
    }

    /**
     * Set color transition
     */
    public static void setColorTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "color", duration, timingFunction);
    }

    /**
     * Set background color transition
     */
    public static void setBackgroundColorTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "background-color", duration, timingFunction);
    }

    /**
     * Set border transition
     */
    public static void setBorderTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "border", duration, timingFunction);
    }

    /**
     * Set box shadow transition
     */
    public static void setBoxShadowTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "box-shadow", duration, timingFunction);
    }

    /**
     * Set width transition
     */
    public static void setWidthTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "width", duration, timingFunction);
    }

    /**
     * Set height transition
     */
    public static void setHeightTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "height", duration, timingFunction);
    }

    /**
     * Set margin transition
     */
    public static void setMarginTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "margin", duration, timingFunction);
    }

    /**
     * Set padding transition
     */
    public static void setPaddingTransition(Component component, double duration, String timingFunction) {
        setTransition(component, "padding", duration, timingFunction);
    }

    /**
     * Set hover effect with transition
     */
    public static void setHoverEffect(Component component, double duration, String timingFunction) {
        setAllTransitions(component, duration, timingFunction);
        component
                .getElement()
                .executeJs(
                        """
            this.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.05)';
                this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.2)';
            });
            this.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
                this.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
            });
        """);
    }

    /**
     * Set focus effect with transition
     */
    public static void setFocusEffect(Component component, double duration, String timingFunction) {
        setAllTransitions(component, duration, timingFunction);
        component
                .getElement()
                .executeJs(
                        """
            this.addEventListener('focus', function() {
                this.style.transform = 'scale(1.02)';
                this.style.boxShadow = '0 0 0 2px #1976d2';
            });
            this.addEventListener('blur', function() {
                this.style.transform = 'scale(1)';
                this.style.boxShadow = 'none';
            });
        """);
    }

    /**
     * Set loading spinner animation
     */
    public static void setLoadingSpinner(Component component) {
        Style style = component.getStyle();
        style.set("animation", "spin 1s linear infinite");
        style.set("border", "2px solid #f3f3f3");
        style.set("border-top", "2px solid #1976d2");
        style.set("border-radius", "50%");
        style.set("width", "20px");
        style.set("height", "20px");
    }

    /**
     * Set typing animation effect
     */
    public static void setTypingEffect(Component component, String text, double speed) {
        component
                .getElement()
                .executeJs(
                        """
            let text = arguments[0];
            let speed = arguments[1];
            let i = 0;
            let element = this;

            element.textContent = '';

            function typeWriter() {
                if (i < text.length) {
                    element.textContent += text.charAt(i);
                    i++;
                    setTimeout(typeWriter, speed);
                }
            }
            typeWriter();
        """,
                        text,
                        speed);
    }
}
