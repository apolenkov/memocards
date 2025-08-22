module.exports = {
    plugins: [
        require('postcss-custom-media')(),
        require('autoprefixer')(),
        require('cssnano')({ preset: 'default' })
    ]
};


