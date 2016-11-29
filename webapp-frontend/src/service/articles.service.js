
function getArticles(context, state) {
    return context.$http.get('http://localhost:8000/article/' + state).then(function (res) {
        return res.body;
    }, function (err) {
        console.log('Could not load articles: ' + err);
        return [];
    });
}

export default {
    get: getArticles
};
