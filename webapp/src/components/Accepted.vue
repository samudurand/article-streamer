<template xmlns:v-bind="http://www.w3.org/1999/xhtml" xmlns:v-on="http://www.w3.org/1999/xhtml">

  <div class="mdl-grid">

    <div class="mdl-cell mdl-cell--2-col"></div>

    <div class="mdl-cell mdl-cell--8-col">
      <table width="100%" class="mdl-data-table mdl-js-data-table mdl-shadow--2dp">
        <thead>
          <tr>
            <th class="mdl-data-table__cell--non-numeric">Date</th>
            <th class="mdl-data-table__cell--non-numeric">Content</th>
            <!--<th>Score</th>-->
            <th></th>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="article in articles">
            <td class="mdl-data-table__cell--non-numeric">{{article.publicationDate | parseDate }}</td>
            <td class="mdl-data-table__cell--non-numeric">
              <div style="word-break: break-all; white-space: normal;">{{article.content}}</div>
            </td>
            <td class="nopadding">
              <button class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored" v-on:click="reject(article.id)">
                <i class="material-icons red">remove</i>
              </button>
            </td>
            <td class="nopadding">
              <button class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored" v-on:click="backToPending(article.id)">
                <i class="material-icons grey">hourglass_empty</i>
              </button>
            </td>
            <!--<td>{{article.score}}</td>-->
            <td class="tab-logo">
              <a v-bind:href="'https://twitter.com/any/status/' + article.originalId" target="_blank"><img src="../assets/twitter.png"></a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="mdl-layout-spacer"></div>


  </div>

</template>

<script>
  import ArticleService from '../service/articles.service'

  function getAccepted(context) {
    return ArticleService.get(context, 'accepted');
  }

  export default {
    name: 'accepted',
    asyncComputed: {
      articles: (context) => getAccepted(context),
    },
    methods: {
      reject: function (id) {
        ArticleService.setState(this, id, -1).then(
          function () {
            this.$data.articles.shift();
          },
          function (err) {
            console.log(err)
          }
        );
      },
      backToPending: function (id) {
        ArticleService.setState(this, id, 0).then(
          function () {
            const vm = this;
            getAccepted(this).then(function (articles) {
              vm.articles = articles;
            });
          },
          function (err) {
            console.log(err)
          }
        );
      }
    }
  };
</script>

<style scoped>
</style>
