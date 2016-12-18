import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const state = {
  navState: ''
};

const mutations = {
  pageChange (state, page){
    state.navState = page;
  }
};

export default new Vuex.Store({
  state,
  mutations
})
