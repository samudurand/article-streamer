<template>

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
          <tr>
            <td class="mdl-data-table__cell--non-numeric">2016-11-24</td>
            <td class="mdl-data-table__cell--non-numeric">
              <div style="word-break: break-all; white-space: normal;">#apache #Spark #MachineLearning Tutorial  #Analytics vía @BigDataReport_ https://t.co/G1YvU9dmq8 https://t.co/lj0PdRvA9B</div></td>
            <td>100</td>
            <td class="tab-logo">
              <a href="https://twitter.com/any/status/801807662388219904"><img src="../assets/twitter.png"></a>
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
    name: 'pending',
    data() {
      return {};
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
