import Vue from 'vue';
import App from './App';
import Hello from './components/Hello.vue'

import VueRouter from 'vue-router'
import VueResource from 'vue-resource'

Vue.use(VueResource);
Vue.use(VueRouter);

const routes = [
  { path: '/hello', component: Hello },
  { path: '*', component: App }
];

const router = new VueRouter({
  routes // short for routes: routes
});

const app = new Vue({
  router
}).$mount('#app');
