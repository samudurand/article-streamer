require('chai').should();
const server = require('./server');
const sequelize = require('./test-sequelize');

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

  describe('Status', function () {

    const articlePending1 = {
      'id': '27193e76-b9cb-406d-963f-a82823c599fc',
      'originalId': '816643621646663680',
      'publicationDate': '2017-01-04T13:53:06.000Z',
      'content': 'Apache #Hadoop vs #Apache #Spark: Two Popular #BigData Frameworks Compared. ?? https://t.co/vKJEnRQkyx\n#IoT… https://t.co/mXwJIhSadB',
      'author': 8742802,
      'score': 0,
      'status': 0
    };

    const articlePending2 = {
      'id': '613b68d3-d50c-44fd-b3d6-db5c723ee039',
      'originalId': '816646637711921152',
      'publicationDate': '2017-01-04T14:05:05.000Z',
      'content': '#BigData trends 2k17\n#MachineLearning #DataLakes #Hadoop vs #Spark\nhttps://t.co/gxMF2yrN92\n#BigDataAnalytics\nvisit: https://t.co/erDmOmaaNS',
      'author': 3040662624,
      'score': 2,
      'status': 0
    };

    const articleAccepted1 = {
      'id': '208e80db-73eb-4be3-8f7b-0219a17314d6',
      'originalId': '816678724007436288',
      'publicationDate': '2017-01-04T16:12:35.000Z',
      'content': 'Databricks and Apache #Spark Year in Review https://t.co/5rC2tlyBwD',
      'author': 16672776,
      'score': 0,
      'status': 1
    };

    const articleAccepted2 = {
      'id': 'cc67656d-de9c-4bd9-816b-55d43023a9e4',
      'originalId': '816695699999956992',
      'publicationDate': '2017-01-04T17:20:03.000Z',
      'content': '#Bigdata and business intelligence #trends for 2017 https://t.co/dlYVET3PMl #machinelearning #hadoop #Spark https://t.co/jXdCZOUgZ5',
      'author': 2153053644,
      'score': 117,
      'status': 1
    };

    const articleRejected1 = {
      'id': 'd2447696-56df-4474-86fa-16ca039b4a38',
      'originalId': '816683212042866689',
      'publicationDate': '2017-01-04T16:30:25.000Z',
      'content': 'Centizen is hiring in Beaverton, OR #job #nosql #spark https://t.co/NkoLIf65ck',
      'author': 201971646,
      'score': 0,
      'status': -1
    };

    const articleRejected2 = {
      'id': 'c0c35fd1-6e25-4695-b9dc-a16ef6e7b6fb',
      'originalId': '816694701533249536',
      'publicationDate': '2017-01-04T17:16:04.000Z',
      'content': 'It was mint to be! Come check out the 17 Chevy Spark??#chevy #chevrolet #spark #mint #minttobe #haha #testdrive… https://t.co/SUSKCpJ8d4 https://t.co/s0vEax2XBJ',
      'author': 243695491,
      'score': 100,
      'status': -1
    };

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
      cleanupDb(done);
    });

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

    function cleanupDb(callback) {
      const Article = sequelize.model('article');
      Article.truncate().then(() => callback());
    }

  });

});

