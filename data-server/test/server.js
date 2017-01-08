const Hapi = require('hapi');

const server = new Hapi.Server();
server.connection();
server.register([
  {
    // Load the routes
    register: require('hapi-router'),
    options: {
      routes: 'routes/**/*.route.js'
    }
  }
]);

module.exports = server;