import Vue from 'vue';
import Pending from './components/Pending.vue'
import Accepted from './components/Accepted.vue'
import Rejected from './components/Rejected.vue'
import dateFns from 'date-fns'
import store from './store'

import VueRouter from 'vue-router'
import VueResource from 'vue-resource'

Vue.use(VueResource);
Vue.use(VueRouter);

Vue.filter('parseDate', function (value) {
  const date = dateFns.parse(value);
  return dateFns.format(date, 'HH:MM:ss - DD MMM');
});

const routes = [
  { path: '/pending', component: Pending },
  { path: '/accepted', component: Accepted },
  { path: '/rejected', component: Rejected },
  { path: '*', component: Pending }
];

const router = new VueRouter({
  routes
});

router.beforeEach((to, from, next) => {
  store.commit('pageChange', to.path);
  next();
});

const app = new Vue({
  router,
  store,
  data: {
    store: store
  }
}).$mount('#app');
