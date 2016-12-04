
function getArticles(context, state) {
    return context.$http.get('http://localhost:8000/article/' + state).then(function (res) {
        return res.body;
    }, function (err) {
        console.log('Could not load articles: ' + err);
        return [];
    });
}

function setState(context, id, state) {
    return context.$http.put('http://localhost:8000/article/' + id + '/status/' + state).then(function (res) {
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
