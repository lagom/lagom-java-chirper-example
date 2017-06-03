const webpack = require('webpack');
const path = require('path');

const APP_DIR = path.resolve(__dirname, 'src/main/resources/assets/');
const BUILD_DIR = process.env.BUILD_SYSTEM && process.env.BUILD_SYSTEM.toLowerCase() === 'maven'
    ? path.resolve(__dirname, 'target/classes/public/')
    : path.resolve(__dirname, 'target/web/public/main/');

module.exports = {
  context: APP_DIR,
  entry: {
    main: APP_DIR + '/main.jsx',
    circuitbreaker: APP_DIR + '/circuitbreaker.jsx',
  },
  output: {
    path: BUILD_DIR,
    filename: '[name].js'
  },
  module: {
    loaders: [
      { test: /\.jsx?/, loader: 'babel-loader', exclude: /(node_modules|bower_components)/ }
    ]
  },
  plugins: [
    new webpack.ProvidePlugin({
        $: "jquery",
        jQuery: "jquery"
    })
  ]
};
