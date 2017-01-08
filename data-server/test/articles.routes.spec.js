require('chai').should();
const server = require('./server');

describe('Articles API', function() {

  describe('Access Control', function() {
    it('provides an option endpoint with Access Control headers', function(done) {
      server.inject({method: 'OPTIONS', url: '/any'}, function(response) {
        try {
          response.statusCode.should.equal(200);
          response.headers.should.have.ownProperty('access-control-allow-origin');
          response.headers.should.have.ownProperty('access-control-allow-methods');
          response.headers.should.have.ownProperty('access-control-allow-headers');
          response.headers.should.have.ownProperty('access-control-allow-credentials');
          done();
        } catch (err) { done(err); }
      });
    });
  });

  // describe('Status', function() {
  //   it('get pending articles', function(done) {
  //     server.inject({method: 'GET', url: '/article/pending'}, function(response) {
  //       try {
  //         response.statusCode.should.equal(200);
  //         done();
  //       } catch (err) { done(err); }
  //     });
  //   });
  // });

});

