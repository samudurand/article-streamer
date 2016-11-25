const Joi = require('joi');
const ARTICLE_MODEL = 'article';
const Status = {
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: -1
};

function getByStatus(request, reply, status) {
  const Article = request.getDb().getModel(ARTICLE_MODEL);
  return Article.findAll({where: {status: status}}).then(function (articles) {
    return reply(articles);
  });
}

module.exports = [
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
      Article.update(
        { status: request.params.status },
        { where: {id: request.params.id }}
      ).then(
        (count) => {return reply('success');},
        (err) => {return ('error');}
      );

      // return Article.find({ where: { title: 'aProject' } })
      //   .on('success', function (project) {
      //     // Check if record exists in db
      //     if (project) {
      //       project.updateAttributes({
      //         title: 'a very different title now'
      //       })
      //         .success(function () {})
      //     }
      //   });

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