module.exports = {
    plugins: [
        require('postcss-custom-media')(),
        require('postcss-custom-properties')(),
        require('postcss-nesting')(),
        require('postcss-functions')(),
        require('autoprefixer')({
            overrideBrowserslist: [
                'last 2 versions',
                '> 1%',
                'not dead'
            ]
        }),
        require('cssnano')({
            preset: ['default', {
                discardComments: {
                    removeAll: true,
                },
                normalizeWhitespace: true,
                colormin: true,
                minifyFontValues: true,
                minifySelectors: true
            }]
        })
    ]
};


