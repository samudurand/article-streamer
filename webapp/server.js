'use strict';

const Hapi = require('hapi');
const Config = require('config');
const Sequelize = require('sequelize');

const appConfig = Config.get('app');
const mysqlConfig = Config.get('mysql');

let sequelize = new Sequelize(mysqlConfig.database, mysqlConfig.username, mysqlConfig.password, {
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
});

sequelize.define('article', {
  id: {
    type: Sequelize.TEXT,
    primaryKey: true,
    allowNull: true
  },
  originalId: {
    type: Sequelize.TEXT,
    allowNull: true
  },
  publicationDate: {
    type: Sequelize.DATE,
    allowNull: true
  },
  content: {
    type: Sequelize.TEXT,
    allowNull: true
  },
  author: {
    type: Sequelize.BIGINT,
    allowNull: true
  },
  score: {
    type: Sequelize.INTEGER(11),
    allowNull: true
  }
}, {
  tableName: 'article'
});

const Article = sequelize.model('article');

// Create a server with a host and port
const server = new Hapi.Server();
server.connection({
  host: appConfig.host,
  port: appConfig.port
});

// Add the route
server.route({
  method: 'GET',
  path: '/hello',
  handler: function (request, reply) {
    return Article.findOne().then(function (article) {
      return reply(article);
    });
  }
});

// Start the server
server.start((err) => {

  if (err) {
    throw err;
  }
  console.log('Server running at:', server.info.uri);
});