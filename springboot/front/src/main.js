import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

import VeLine from 'v-charts/lib/line'
import VeBar from 'v-charts/lib/bar'
import VePie from 'v-charts/lib/pie'
import VeRing from 'v-charts/lib/ring'
import VeHistogram from 'v-charts/lib/histogram'
import VeFunnel from 'v-charts/lib/funnel'
import VeRadar from 'v-charts/lib/radar'
import VeScatter from 'v-charts/lib/scatter'
import VeMap from 'v-charts/lib/map'
import VeHeatmap from 'v-charts/lib/heatmap'
import 'v-charts/lib/style.css'
import './styles/index.scss'
import './utils/request'

// v-charts 依赖 Vue 2.6 内部的 _watchers 属性，Vue 2.7 中已移除，需在此补全兼容
Vue.mixin({
  beforeCreate() {
    if (!this._watchers) {
      this._watchers = []
    }
  }
})

Vue.use(ElementUI)
Vue.component('ve-line', VeLine)
Vue.component('ve-bar', VeBar)
Vue.component('ve-pie', VePie)
Vue.component('ve-ring', VeRing)
Vue.component('ve-histogram', VeHistogram)
Vue.component('ve-funnel', VeFunnel)
Vue.component('ve-radar', VeRadar)
Vue.component('ve-scatter', VeScatter)
Vue.component('ve-map', VeMap)
Vue.component('ve-heatmap', VeHeatmap)

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')

