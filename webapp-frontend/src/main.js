import Vue from 'vue';
import dateFns from 'date-fns'
import store from './store'
import router from './routing'

import VueResource from 'vue-resource'

Vue.use(VueResource);
Vue.http.options.root = '/root';

Vue.filter('parseDate', function (value) {
  const date = dateFns.parse(value);
  return dateFns.format(date, 'HH:MM:ss - DD MMM');
});

const app = new Vue({
  router,
  store,
  data: {
    store: store
  }
}).$mount('#app');
