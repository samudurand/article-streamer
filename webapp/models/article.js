/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('article', {
    id: {
      type: DataTypes.TEXT,
      allowNull: true
    },
    originalId: {
      type: DataTypes.TEXT,
      allowNull: true
    },
    publicationDate: {
      type: DataTypes.DATE,
      allowNull: true
    },
    content: {
      type: DataTypes.TEXT,
      allowNull: true
    },
    author: {
      type: DataTypes.BIGINT,
      allowNull: true
    },
    score: {
      type: DataTypes.INTEGER(11),
      allowNull: true
    }
  }, {
    tableName: 'article'
  });
};
