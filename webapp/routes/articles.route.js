const Status = {
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: -1
};

function getByStatus(request, reply, status) {
  const Article = request.getDb().getModel('article');
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
  }
];