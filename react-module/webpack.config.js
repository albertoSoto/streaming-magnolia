const webpack = require('webpack');

module.exports = {
    node: {
        console: true,
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: ['babel-loader']
            },{
                test: /\.css$/,
                include: /node_modules/,
                use: [ 'style-loader', 'css-loader' ]
            }
        ]
    },
    resolve: {
        extensions: ['*', '.js', '.jsx']
    },
    entry: {
        MagnoliaStreamingApp: __dirname + "/src/react-mgnl-streaming.js"
        //sandbox: __dirname + "/src/hello-props.js"
    },
    output: {
        path: __dirname + "/dist",
        publicPath: '/',
        filename: "[name]-bundle.js"
    },
    plugins: [
        new webpack.HotModuleReplacementPlugin()
    ],
    devServer: {
        contentBase: './dist',
        hot: true
    }
};