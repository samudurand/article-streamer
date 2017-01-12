const Sequelize = require('sequelize');

module.exports = new Sequelize('database', 'username', 'password', {
  dialect: 'sqlite',
  define: {
    timestamps: false
  },
  storage: __dirname + '/database.sqlite',
  logging: false
});