import Vue from 'vue';
import VueResource from 'vue-resource'
import dateFns from 'date-fns'
import store from './store'
import router from './routing'
import AsyncComputed from 'vue-async-computed'

Vue.use(VueResource);
Vue.use(AsyncComputed);
Vue.http.options.root = '/root';

Vue.filter('parseDate', function (value) {
  const date = dateFns.parse(value);
  return dateFns.format(date, 'HH:mm:ss - DD MMM');
});

const app = new Vue({
  router,
  store,
  data: {
    store: store
  }
}).$mount('#app');
