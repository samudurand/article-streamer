require('chai').should();
const fs = require('fs');
const server = require('./server');
const sequelize = require('./test-sequelize');

const originalDBName = './test/database.sqlite';
const tempDBName = './test/database-temp.sqlite';

const articlePending1 = require('./data/article-pending-1.json');
const articlePending2 = require('./data/article-pending-2.json');
const articleAccepted1 = require('./data/article-accepted-1.json');
const articleAccepted2 = require('./data/article-accepted-2.json');
const articleRejected1 = require('./data/article-rejected-1.json');
const articleRejected2 = require('./data/article-rejected-2.json');

describe('Articles API', function () {

  describe('Access Control', function () {
    it('provides an option endpoint with Access Control headers', function (done) {
      server.inject({method: 'OPTIONS', url: '/any'}, function (response) {
        try {
          response.statusCode.should.equal(200);
          response.headers.should.have.ownProperty('access-control-allow-origin');
          response.headers.should.have.ownProperty('access-control-allow-methods');
          response.headers.should.have.ownProperty('access-control-allow-headers');
          response.headers.should.have.ownProperty('access-control-allow-credentials');
          done();
        } catch (err) {
          done(err);
        }
      });
    });
  });

  describe('Managed DB', function () {

    before(function (done) {
      cleanupDb(done);
    });

    beforeEach(function (done) {
      const Article = sequelize.model('article');
      Article.bulkCreate([articlePending1, articlePending2, articleAccepted1, articleAccepted2, articleRejected1, articleRejected2])
        .then(function () {
          done();
        });
    });

    afterEach(function (done) {
      // If database name was changed to provoke database errors resets it
      fs.access(tempDBName, fs.constants.F_OK, (err) => {
        if (!err) {
          fs.rename(tempDBName, originalDBName, () => {
            cleanupDb(done);
          });
        } else {
          cleanupDb(done);
        }
      });

    });

    describe('Status', function () {

      it('gets all pending articles', (done) => {
        server.inject({method: 'GET', url: '/article/pending'}, function (response) {
          try {
            response.statusCode.should.equal(200);
            const results = response.result;
            results.should.have.length(2);
            results[0].id.should.equal(articlePending1.id);
            results[1].id.should.equal(articlePending2.id);
            done();
          } catch (err) {
            done(err);
          }
        });
      });

      it('gets all accepted articles', (done) => {
        server.inject({method: 'GET', url: '/article/accepted'}, function (response) {
          try {
            response.statusCode.should.equal(200);
            const results = response.result;
            results.should.have.length(2);
            results[0].id.should.equal(articleAccepted2.id);
            results[1].id.should.equal(articleAccepted1.id);
            done();
          } catch (err) {
            done(err);
          }
        });
      });

      it('gets all rejected articles', (done) => {
        server.inject({method: 'GET', url: '/article/rejected'}, function (response) {
          try {
            response.statusCode.should.equal(200);
            const results = response.result;
            results.should.have.length(2);
            results[0].id.should.equal(articleRejected2.id);
            results[1].id.should.equal(articleRejected1.id);
            done();
          } catch (err) {
            done(err);
          }
        });
      });

      it('change status from pending to accepted', (done) => {
        server.inject({method: 'PUT', url: '/article/' + articlePending1.id + '/status/1'}, function (response) {
          try {
            response.statusCode.should.equal(204);
            done();
          } catch (err) {
            done(err);
          }
        });
      });

      it('fail to change status for non existing article', (done) => {
        server.inject({
          method: 'PUT',
          url: '/article/00000000-0000-0000-0000-000000000000/status/1'
        }, function (response) {
          try {
            response.statusCode.should.equal(500);
            done();
          } catch (err) {
            done(err);
          }
        });
      });

      it('fail to change status if database unreachable', (done) => {
        fs.rename(originalDBName, tempDBName, () => {
          server.inject({method: 'PUT', url: '/article/' + articlePending1.id + '/status/1'}, function (response) {
            try {
              response.statusCode.should.equal(500);
              done();
            } catch (err) {
              done(err);
            }
          });
        });
      });

    });

    describe('Article CRUD', function () {

      it('delete article', (done) => {
        server.inject({method: 'DELETE', url: '/article/' + articlePending1.id}, function (response) {
          try {
            response.statusCode.should.equal(204);

            const Article = sequelize.model('article');
            Article.findAll({where: {status: 0}}).then((articles) => {
              articles.should.have.length(1);
              done();
            });
          } catch (err) {
            done(err);
          }
        });
      });

      it('fail to delete not existing article', (done) => {
        server.inject({method: 'DELETE', url: '/article/00000000-0000-0000-0000-000000000000'}, function (response) {
          try {
            response.statusCode.should.equal(500);

            const Article = sequelize.model('article');
            Article.findAll({where: {status: 0}}).then((articles) => {
              articles.should.have.length(2);
              done();
            });
          } catch (err) {
            done(err);
          }
        });
      });

      it('fail to delete not existing article', (done) => {
        fs.rename(originalDBName, tempDBName, () => {
          server.inject({method: 'DELETE', url: '/article/' + articlePending1.id}, function (response) {
            try {
              response.statusCode.should.equal(500);
              done();
            } catch (err) {
              done(err);
            }
          });
        });
      });
    });

    function cleanupDb(callback) {
      const Article = sequelize.model('article');
      Article.truncate().then(() => callback(), () => callback());
    }
  });
});

