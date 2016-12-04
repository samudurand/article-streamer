'use strict';

const logger = require('winston');
const Hapi = require('hapi');
const Config = require('config');
const Sequelize = require('sequelize');

const appConfig = Config.get('app');
const mysqlConfig = Config.get('mysql');

const server = new Hapi.Server();
server.connection({
  host: appConfig.host,
  port: appConfig.port
});

server.register([
  {
    // Handle DB with the ORM Sequelize
    register: require('hapi-sequelize'),
    options: [
      {
        name: 'mysql', // identifier
        models: ['./models/**/*.js'],
        sequelize: new Sequelize(mysqlConfig.database, mysqlConfig.username, mysqlConfig.password, {
          host: mysqlConfig.host,
          dialect: 'mysql',
          pool: {
            max: 5,
            min: 0,
            idle: 10000
          },
          define: {
            timestamps: false
          }
        }),
        sync: true,
        forceSync: false
      }
    ]
  },
  {
    // Load the routes
    register: require('hapi-router'),
    options: {
      routes: 'routes/**/*.route.js' // uses glob to include files
    }
  }
], {}, (err) => {

  if (err) {
    logger.error('Server failed to start', err);
  } else {

    // Start the server
    server.start((err) => {

      if (err) {
        throw err;
      }
      logger.log('Server running at:', server.info.uri);
    });
  }

});
