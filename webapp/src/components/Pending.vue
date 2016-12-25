<template xmlns:v-bind="http://www.w3.org/1999/xhtml" xmlns:v-on="http://www.w3.org/1999/xhtml">

  <div>

    <div class="mdl-grid">

      <div class="mdl-cell mdl-cell--2-col"></div>

      <div class="mdl-cell mdl-cell--8-col">
        <table width="100%" class="mdl-data-table mdl-js-data-table mdl-shadow--2dp">
          <thead>
            <tr>
              <th class="mdl-data-table__cell--non-numeric">Date</th>
              <th class="mdl-data-table__cell--non-numeric">Content</th>
              <th></th>
              <th></th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(article, index) in articles">
              <td class="mdl-data-table__cell--non-numeric">{{article.publicationDate | parseDate }}</td>
              <td class="mdl-data-table__cell--non-numeric">
                <div style="word-break: break-all; white-space: normal;" v-html="formatContent(article.content)"></div></td>
              <td class="nopadding">
                <button v-if="index === 0" class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored" v-on:click="accept(article.id)">
                  <i class="material-icons green">add</i>
                </button>
              </td>
              <td class="nopadding">
                <button v-if="index === 0" class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored" v-on:click="reject(article.id)">
                  <i class="material-icons red">remove</i>
                </button>
              </td>
              <td class="nopadding">
                <button v-if="index === 0" class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored" v-on:click="displayConfirmDeletion(article.id)">
                  <i class="material-icons grey">delete forever</i>
                </button>
              </td>
              <td class="tab-logo">
                <a v-bind:href="'https://twitter.com/any/status/' + article.originalId" target="_blank"><img src="../assets/twitter.png"></a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mdl-layout-spacer"></div>

    </div>

    <confirm-dialog></confirm-dialog>

  </div>

</template>

<script>
  import ArticleService from '../service/articles.service'
  import ContentFormattingService from '../service/contentFormatting.service'
  import ConfirmDialog from './ConfirmDialog.vue'
  import bus from '../Bus'

  export default {
    name: 'pending',
    created: function () {
      const vm = this;
      bus.$on('delete-article-confirmed', function (id) {
        vm.remove(id);
      });
    },
    asyncComputed: {
      articles: (context) => ArticleService.get(context, 'pending')
    },
    methods: {
      displayConfirmDeletion: function (id) {
        bus.$emit('delete-article', id);
      },
      accept: function (id) {
        const vm = this;
        ArticleService.setState(this, id, 1).then(
          ()    => vm.$data.articles.shift(),
          (err) => console.log(err)
        );
      },
      reject: function (id) {
        const vm = this;
        ArticleService.setState(this, id, -1).then(
          () => vm.$data.articles.shift()
        );
      },
      remove: function (id) {
        const vm = this;
        ArticleService.delete(vm, id).then(
          () => vm.$data.articles.shift(), () => {}
        );
      },
      formatContent: function (content) {
        return ContentFormattingService.formatLinks(content);
      }
    },
    components: {
      ConfirmDialog
    }
  };
</script>

<style scoped>
</style>
