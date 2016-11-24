const Status = {
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: -1
};

module.exports = [
  {
    method: 'GET',
    path: '/article/pending',
    handler: function (request, reply) {
      const Article = request.getDb().getModel('article');
      return Article.findAll({ where: { status: Status.PENDING } }).then(function (articles) {
        return reply(articles);
      });
    }
  }
];