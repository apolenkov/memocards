# VAADIN MOBILE BUTTON TEXT HIDING SOLUTION

## Проблема
Скрыть текст кнопок Vaadin на мобильных устройствах, оставив только иконки.

## Решение
Использовать `::part(label)` селектор для доступа к Shadow DOM частям Vaadin:

```css
@media (max-width: 767px) {
    .deck-toolbar__add-button {
        position: relative;
        width: 44px;
        height: 44px;
        padding: 0;
    }
    
    /* Скрываем текст через ::part(label) */
    .deck-toolbar__add-button::part(label) {
        display: none;
    }
    
    /* Показываем иконку поверх текста */
    .deck-toolbar__add-button vaadin-icon {
        position: absolute;
        top: 50%;
        left: 50%;
        font-size: var(--lumo-icon-size-m);
        transform: translate(-50%, -50%);
    }
}
```

## Ключевые моменты
- `::part(label)` - правильный способ доступа к Shadow DOM частям Vaadin
- `display: none` - полностью скрывает элемент с текстом
- `position: absolute` + `transform: translate(-50%, -50%)` - центрирует иконку
- Работает для всех кнопок: `.deck-toolbar__add-button`, `.deck-view__header button`, `.deck-card__practice-button vaadin-button`

## Альтернативы НЕ работают
- `text-indent: -9999px` - не проникает в Shadow DOM
- `font-size: 0` - не проникает в Shadow DOM  
- CSS Custom Properties - не всегда работают с Vaadin

## Результат
Мобильные устройства показывают только иконки, планшеты/десктопы - полные кнопки с текстом.

## Применение
Использовать для всех Vaadin кнопок, где нужно скрыть текст на мобильных устройствах.
