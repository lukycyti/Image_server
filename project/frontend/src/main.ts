import { createApp} from 'vue'
import App from './App.vue'
import router from './router';
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"

import { library } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faRotateLeft, faRotateRight, faUserSecret, faHouse, faUpload,faDownload } from '@fortawesome/free-solid-svg-icons'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons';
import { faArrowLeft, faTrash } from '@fortawesome/free-solid-svg-icons';
import { faCircleLeft, faCircleRight, faFloppyDisk} from '@fortawesome/free-regular-svg-icons'
import Vue3Toastify, { type ToastContainerOptions } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

library.add(faUserSecret, faArrowRight, faArrowLeft, faRotateRight, faRotateLeft, faCircleLeft, faCircleRight, faHouse, faUpload,faDownload, faFloppyDisk, faTrash);


createApp(App).use(Vue3Toastify, {
    autoClose: 3000,
  } as ToastContainerOptions).use(router).component('font-awesome-icon', FontAwesomeIcon).mount('#app')
  

