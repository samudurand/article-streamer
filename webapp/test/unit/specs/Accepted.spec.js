import Accepted from 'src/components/Accepted.vue';
import ArticleService from 'src/service/articles.service';

describe('Article.vue', () => {

  it('has all expected attributes', () => {
    expect(typeof Accepted.asyncComputed.articles).toBe('function');

  });

  it('should retrieve accepted articles', () => {
    spyOn(ArticleService, 'get');

    Accepted.asyncComputed.articles({});

    expect(ArticleService.get).toHaveBeenCalled();
  });
});
