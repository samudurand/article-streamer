var logger = require('winston');
const Joi = require('joi');
const ARTICLE_MODEL = 'article';
const Status = {
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: -1
};

function getByStatus(request, reply, status) {
  const Article = request.getDb().getModel(ARTICLE_MODEL);
  return Article.findAll({ where: { status: status }})
    .then(
      (articles) => {
        return reply(articles)
            .header('Access-Control-Allow-Origin', '*')
            .header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
            .header('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
            .header('Access-Control-Allow-Credentials', true)
            .code(200);
      },
      (err) => {
        logger.error('Cannot retrieve articles.', err);
        return reply()
            .header('Access-Control-Allow-Origin', '*')
            .header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
            .header('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
            .header('Access-Control-Allow-Credentials', true)
            .code(500);
      });
}

module.exports = [
  {
    method: 'OPTIONS',
    path: '/{path*}',
    handler: function (request, reply) {
        return reply()
            .header('Access-Control-Allow-Origin', '*')
            .header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
            .header('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
            .header('Access-Control-Allow-Credentials', true)
            .code(200);
    }
  },
  {
    method: 'GET',
    path: '/article/pending',
    handler: function (request, reply) {
      return getByStatus(request, reply, Status.PENDING);
    }
  },
  {
    method: 'GET',
    path: '/article/accepted',
    handler: function (request, reply) {
      return getByStatus(request, reply, Status.ACCEPTED);
    }
  },
  {
    method: 'GET',
    path: '/article/rejected',
    handler: function (request, reply) {
      return getByStatus(request, reply, Status.REJECTED);
    }
  },
  {
    method: 'PUT',
    path: '/article/{id}/status/{status}',
    handler: function (request, reply) {
      const Article = request.getDb().getModel(ARTICLE_MODEL);
      const status = request.params.status;
      const id = request.params.id;

      Article.update({status: status}, {where: {id: id}})
        .then(
          (count) => {
            if (count == 1) {
              return reply({})
                  .header('Access-Control-Allow-Origin', '*')
                  .header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
                  .header('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
                  .header('Access-Control-Allow-Credentials', true)
                  .code(202);
            } else {
              logger.error('Failed to update status of article' + id);
              return reply({error: 'no records affected'})
                  .header('Access-Control-Allow-Origin', '*')
                  .header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
                  .header('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
                  .header('Access-Control-Allow-Credentials', true)
                  .code(500);
            }
          },
          (err) => {
            logger.error('Failed to update status of article', err);
            return reply()
                .header('Access-Control-Allow-Origin', '*')
                .header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
                .header('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
                .header('Access-Control-Allow-Credentials', true)
                .code(500);
          }
        );

    },
    config: {
      validate: {
        params: {
          id: Joi.string().guid(),
          status: Joi.number().integer().min(-1).max(1)
        }
      }
    }
  }
];