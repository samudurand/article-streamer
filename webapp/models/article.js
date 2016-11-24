/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('article', {
    id: {
      type: DataTypes.STRING,
      allowNull: false,
      primaryKey: true
    },
    originalId: {
      type: DataTypes.TEXT,
      allowNull: false
    },
    publicationDate: {
      type: DataTypes.DATE,
      allowNull: false
    },
    content: {
      type: DataTypes.TEXT,
      allowNull: false
    },
    author: {
      type: DataTypes.BIGINT,
      allowNull: true
    },
    score: {
      type: DataTypes.INTEGER(11),
      allowNull: false
    },
    status: {
      type: DataTypes.INTEGER(11),
      allowNull: false
    }
  }, {
    tableName: 'article'
  });
};
