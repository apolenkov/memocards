module.exports = {
  extends: [
    "stylelint-config-standard",
  ],
  plugins: [
    "stylelint-order"
  ],
  rules: {
    // 1. STRICT COLOR MANAGEMENT - ONLY LUMO TOKENS
    "color-named": "never",
    "color-no-hex": true,
    "color-no-invalid-hex": true,
    "color-function-notation": "modern",
    
    // 2. BEM NAMING ENFORCEMENT
    "selector-class-pattern": [
      "^[a-z][a-z0-9]*(?:-[a-z0-9]+)*(?:__(?:[a-z0-9]+(?:-[a-z0-9]+)*))?(?:--[a-z0-9]+(?:-[a-z0-9]+)*)?$",
      { 
        message: "Use strict BEM naming: block__element--modifier (English only)" 
      }
    ],
    
    // 3. NO HARDCODED VALUES - ONLY CSS VARIABLES
    "declaration-property-value-allowed-list": {
      "background-color": ["/^var\\(--lumo-.*\\)$/", "/^color-mix\\(.*\\)$/"],
      "color": ["/^var\\(--lumo-.*\\)$/", "/^color-mix\\(.*\\)$/"],
      "border-color": ["/^var\\(--lumo-.*\\)$/", "/^color-mix\\(.*\\)$/"],
      "box-shadow": ["/^var\\(--lumo-.*\\)$/", "none", "/^0 0 0 2px var\\(--lumo-.*\\)$/"],
      "text-shadow": ["/^var\\(--lumo-.*\\)$/", "none"]
    },
    
    // 4. STRICT SPACING AND FORMATTING
    "rule-empty-line-before": ["always", { 
      except: ["first-nested", "after-single-line-comment"] 
    }],
    "comment-empty-line-before": ["always", { 
      except: ["first-nested"] 
    }],
    "declaration-empty-line-before": "never",
    "at-rule-empty-line-before": ["always", { 
      except: ["first-nested", "blockless-after-same-name-blockless"] 
    }],
    
    // 5. NO IMPORTANT (except for shadow DOM overrides)
    "declaration-no-important": true,
    
    // 6. STRICT SELECTOR COMPLEXITY
    "selector-max-compound-selectors": 3,
    "selector-max-specificity": "0,3,0",
    "selector-no-qualifying-type": true,
    
    // 7. NO ID SELECTORS - COMPONENT-BASED ARCHITECTURE
    "selector-max-id": 0,
    
    // 8. STRICT UNITS
    "length-zero-no-unit": true,
    "unit-allowed-list": ["px", "em", "rem", "%", "vw", "vh", "deg", "ms", "s", "fr"],
    
    // 9. NO BROWSER PREFIXES - USE AUTOPREFIXER
    "property-no-vendor-prefix": true,
    "value-no-vendor-prefix": true,
    
    // 10. STRICT ORDERING
    "order/properties-order": [
      "position",
      "top",
      "right",
      "bottom", 
      "left",
      "z-index",
      "display",
      "flex-direction",
      "flex-wrap",
      "align-items",
      "justify-content",
      "order",
      "flex-grow",
      "flex-shrink",
      "flex-basis",
      "width",
      "height",
      "min-width",
      "min-height",
      "max-width",
      "max-height",
      "margin",
      "margin-top",
      "margin-right",
      "margin-bottom",
      "margin-left",
      "padding",
      "padding-top",
      "padding-right",
      "padding-bottom",
      "padding-left",
      "border",
      "border-width",
      "border-style",
      "border-color",
      "border-radius",
      "background",
      "background-color",
      "background-image",
      "background-size",
      "background-position",
      "background-repeat",
      "color",
      "font-family",
      "font-size",
      "font-weight",
      "line-height",
      "text-align",
      "text-decoration",
      "text-transform",
      "opacity",
      "transform",
      "transition",
      "animation",
      "cursor",
      "overflow",
      "box-sizing",
      "box-shadow",
      "filter"
    ],
    
    // 11. VAADIN INTEGRATION
    "selector-type-no-unknown": [true, { 
      ignore: ["custom-elements"] 
    }],
    "selector-pseudo-element-no-unknown": [true, { 
      ignorePseudoElements: ["part"] 
    }],
    
    // 12. ACCESSIBILITY
    "selector-pseudo-class-no-unknown": [true, {
      ignorePseudoClasses: ["focus-visible"]
    }],
    
    // 13. PERFORMANCE
    "no-unknown-animations": true,
    "no-duplicate-selectors": true,
    
    // 14. MAINTAINABILITY
    "max-nesting-depth": 3,
    "no-empty-source": true,
    "no-invalid-double-slash-comments": true,
    
    // 15. ALLOW ALL CSS VARIABLES
    // "custom-property-pattern": "^--(lumo|app)-[a-zA-Z0-9-]+$",
    "keyframes-name-pattern": "^[a-z][a-z0-9-]*$",
    
    // 16. IMPORT NOTATION
    "import-notation": "string",
    
    // 17. MEDIA FEATURE RANGE + CUSTOM MEDIA
    "media-feature-range-notation": "prefix",
    "at-rule-no-unknown": [true, {
      ignoreAtRules: ["layer", "custom-media"]
    }],
    
    // 18. DECLARATION BLOCK
    "declaration-block-single-line-max-declarations": 1,
    "block-no-empty": true
  },
  
  // OVERRIDE FOR VAADIN COMPONENTS
  overrides: [
    {
      files: ["**/vaadin-*.css"],
      rules: {
        "selector-class-pattern": null,
        "custom-property-pattern": null
      }
    },
    {
      files: ["src/main/frontend/themes/memocards/mobile-responsive.css"],
      rules: {
        "unit-allowed-list": ["px", "em", "rem", "%", "vw", "vh", "deg", "ms", "s", "fr"]
      }
    },
  ]
};

