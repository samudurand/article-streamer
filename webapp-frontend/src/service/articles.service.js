
function getArticles(context, state) {
    return context.$http.get('/api/article/' + state).then(function (res) {
        return res.body;
    }, function (err) {
        console.log('Could not load articles: ' + err);
        return [];
    });
}

function setState(context, id, state) {
    return context.$http.put('/api/article/' + id + '/status/' + state).then(function (res) {
        return res.body;
    }, function (err) {
        console.log('Could not load articles: ' + err);
        return [];
    });
}

export default {
    get: getArticles,
    setState: setState
};
