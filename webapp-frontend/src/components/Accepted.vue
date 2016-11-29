<template xmlns:v-bind="http://www.w3.org/1999/xhtml">

  <div class="mdl-grid">

    <div class="mdl-cell mdl-cell--2-col"></div>

    <div class="mdl-cell mdl-cell--8-col">
      <table width="100%" class="mdl-data-table mdl-js-data-table mdl-shadow--2dp">
        <thead>
          <tr>
            <th class="mdl-data-table__cell--non-numeric">Date</th>
            <th class="mdl-data-table__cell--non-numeric">Content</th>
            <th>Score</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="article in articles">
            <td class="mdl-data-table__cell--non-numeric">{{article.publicationDate | parseDate }}</td>
            <td class="mdl-data-table__cell--non-numeric">
              <div style="word-break: break-all; white-space: normal;">{{article.content}}</div></td>
            <td>{{article.score}}</td>
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
  export default {
    name: 'accepted',
    data() {
      return {
          articles: [
            {
              "id": "b84802a4-ba1b-49eb-b995-bf8a7c318a10",
              "originalId": "801568316779274240",
              "publicationDate": "2016-11-23T00:00:00.000Z",
              "content": "#spark teststest https://t.co/WLUWq7roAm",
              "author": 1935423961,
              "score": 0,
              "status": 0
            },
            {
              "id": "5c0a28cf-d154-43a5-8a9b-7acf5e840c9c",
              "originalId": "801807662388219904",
              "publicationDate": "2016-11-24T00:00:00.000Z",
              "content": "#apache #Spark #MachineLearning Tutorial  #Analytics vía @BigDataReport_ https://t.co/G1YvU9dmq8 https://t.co/lj0PdRvA9B",
              "author": 191537652,
              "score": 100,
              "status": 0
            }
          ]
      };
    },
  };
</script>

<style scoped>
  .mdl-data-table {
    /*table-layout:fixed;*/
    /*width:100%;*/
  }

  .tab-logo {
    min-width: 72px;
    max-width: 100px;
  }

  .tab-logo img {
    width: 100%;
  }
</style>
