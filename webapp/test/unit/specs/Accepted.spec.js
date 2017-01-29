import ArticleService from 'src/service/articles.service';
import Accepted from 'src/components/Accepted.vue';

describe('Article.vue', () => {

  it('has all expected attributes', () => {
    expect(typeof Accepted.asyncComputed.articles).toBe('function');

  });

  it('should retrieve accepted articles', () => {
    spyOn(ArticleService, 'get');

    Accepted.asyncComputed.articles({});

    expect(ArticleService.get.calls.count()).toEqual(1);
    expect(ArticleService.get).toHaveBeenCalledWith({}, 'accepted');
  });

  // TODO Fix tests, got issues with mocking service , a problem with scopes I believe
  // it('should reject an article', () => {
  //   spyOn(ArticleService, 'get');
  //   spyOn(ArticleService, 'setState').and.returnValue(Promise.resolve());
  //
  //   Accepted.methods.reject('1');
  //
  //   expect(ArticleService.setState.calls.count()).toEqual(1);
  //   expect(ArticleService.setState).toHaveBeenCalledWith(jasmine.any(Object), '1', -1);
  //   expect(ArticleService.get).toHaveBeenCalled();
  // });
});
