const Hapi = require('hapi');
const sequelize = require('./test-sequelize');

const server = new Hapi.Server();
server.connection();
server.register([
  {
    register: require('hapi-sequelize'),
    options: [
      {
        name: 'sqlite',
        models: ['./models/**/*.js'],
        sequelize: sequelize,
        sync: true,
        forceSync: false
      }
    ]
  }, {
    register: require('hapi-router'),
    options: {
      routes: 'routes/**/*.route.js'
    }
  }
]);

module.exports = server;