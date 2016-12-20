function getArticles(context, state) {
  return new Promise((resolve, reject) => {
    context.$http.get('/api/article/' + state).then(
      (res) => resolve(res.body),
      (err) => {
        console.log('Could not load articles: ' + err);
        reject([]);
      });
  });
}

function setState(context, id, state) {
  return new Promise((resolve, reject) => {
    context.$http.put('/api/article/' + id + '/status/' + state).then(
      (res) => resolve(res.body),
      (err) => {
        console.log('Could not load articles: ' + err);
        reject(err);
      })
  });
}

function deleteArticle(context, id) {
  return new Promise((resolve, reject) => {
    context.$http.delete('/api/article/' + id).then(
      (res) => resolve(res.body),
      (err) => {
        console.log('Could not delete article: ' + err);
        reject(err);
      });
  });
}

export default {
  get: getArticles,
  setState: setState,
  delete: deleteArticle
};
