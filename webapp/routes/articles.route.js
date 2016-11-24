module.exports = [
  {
    method: 'GET',
    path: '/article/pending',
    handler: function (request, reply) {
      const Article = request.getDb().getModel('article');
      return Article.findAll().then(function (articles) {
        return reply(articles);
      });
    }
  }
];