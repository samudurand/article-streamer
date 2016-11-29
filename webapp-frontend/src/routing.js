import Vue from 'vue'
import store from './store'
import VueRouter from 'vue-router'
import Pending from './components/Pending.vue'
import Accepted from './components/Accepted.vue'
import Rejected from './components/Rejected.vue'

Vue.use(VueRouter);

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

export default router;
