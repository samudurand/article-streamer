const Hapi = require('hapi');
const Sequelize = require('sequelize');

// const sequelize = new SequelizeMock();
// sequelize.authenticate = function () { return Promise.resolve(); };

const server = new Hapi.Server();
server.connection();
server.register([
  {
    register: require('hapi-sequelize'),
    options: [
      {
        name: 'sqlite',
        models: ['./models/**/*.js'],
        sequelize: new Sequelize('database', 'username', 'password', {
          dialect: 'sqlite',
          define: {
            timestamps: false
          },
          storage: __dirname + '/database.sqlite'
        }),
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