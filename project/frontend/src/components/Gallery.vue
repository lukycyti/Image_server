<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { api } from '@/http-api';
import { ImageType } from '@/image'
import Image from './Image.vue';
import { modulo } from '@/util'
import router from '@/router';
import NavBar from './NavBar.vue';


const imageList = ref<ImageType[]>([]);
const divList = ref();
const index = ref(0);






//Récupération de la liste d'image et initialisation du carousel.
api.getImageList()
  .then(async (data) => {
    imageList.value = data;
    await nextTick();
    if (data.length > 0)
      initCarousel();

  })
  .catch(e => {
    console.log(e.message);
  });





/**
 * Retourne l'id de l'image
 * @param i la position de l'élément du carousel correspondant à l'image
 */
function getId(i: number): number {
  var diff = i - imageList.value.length
  if (diff >= 0) {
    switch (diff) {
      case 1:
        if (imageList.value.length < 3) i = 0;
        else i = 2;
        break;
      case 0:
      case 3:
        i = imageList.value.length < 2 ? 0 : 1;
        break;
      case 2:
        i = 0;
        break;
      default:
        return i;
    }
  }
  return imageList.value[i].id;
}


/**
 * Supprime les classes de chacuns des éléments visibles du carousel
 */
function clearCarousel() {
  const list = divList.value;
  const length = list.length;
  var i = index.value;
  var active = list[i];
  active.classList.remove("act");
  list[modulo(i + 1, length)].classList.remove("firstnxt");
  list[modulo(i - 1, length)].classList.remove("firstprv");
  list[modulo(i + 2, length)].classList.remove("lastnxt");
  list[modulo(i - 2, length)].classList.remove("lastprv");
}


/**
 * Définit un nouvel élément actif du carousel 
 * @param i la position de cet élément.
 */
function setCarousel(i: number) {
  const list = divList.value;
  const length = list.length;
  index.value = i;
  var active = list[i];
  active.classList.add("act");

  console.log(divList.value);
  list[modulo(i + 1, length)].classList.add("firstnxt");
  list[modulo(i - 1, length)].classList.add("firstprv");
  list[modulo(i + 2, length)].classList.add("lastnxt");
  list[modulo(i - 2, length)].classList.add("lastprv");
}

/**
 * Change l'élément actif du carousel. Si i est déjà la position de l'élément actif du carousel, lance l'interface de modification de l'image correspondant à i.
 * @param i la position du nouvel élément
 */
async function clearSetCarousel(i: number) {

  clearCarousel();
  const list = divList.value;

  if (i == index.value) {

    list[i].animate([
      { width: '250px', height: '250px' },
      { width: '70%', height: '70%' }
    ],
      {
        duration: 500

      });
    router.push({ name: "image", params: { id: getId(i) } });

  }
  else {
    setCarousel(modulo(i, divList.value.length));
  }
}


/**
 * Initialise le carousel, et ajoute à la liste des éléments du carousel les images ayant été copiées.
 */
function initCarousel() {
  var cpy = document.getElementsByClassName("crsl-copy");
  for (var i = 0; i < cpy.length; i++) {
    var div = cpy.item(i);
    var name = div?.getAttribute("name");
    if (name !== undefined && name !== null) divList.value[imageList.value.length + parseInt(name)] = div;
  }
  setCarousel(0);
}



</script>

<template>
  <NavBar>
    <a class="unblocked" href="/upload">
      <font-awesome-icon icon="fa-solid fa-upload" size="2xl" style="color: #888a85;" />
    </a>
  </NavBar>
  <div id="crsl" v-if="imageList.length > 0">

    <div v-if="imageList.length < 2" class="crsl-item crsl-copy " name="3"
      v-on:click="clearSetCarousel(imageList.length + 2)">
      <Image :image="imageList[0]" :newId="'imageCopied-' + 3" :sized="false"></Image>
    </div>
    <div v-if="imageList.length < 3" class="crsl-item crsl-copy " name="2"
      v-on:click="clearSetCarousel(imageList.length + 3)">
      <Image :image="imageList[imageList.length < 2 ? 0 : 1]" :newId="'imageCopied-' + 2" :sized="false"></Image>
    </div>
    <div v-for="i in imageList.length" ref="divList" class="crsl-item" v-on:click="clearSetCarousel(i - 1)">
      <Image :image="imageList[i - 1]" :sized="false"></Image>
    </div>
    <div v-if="imageList.length < 5" class="crsl-item crsl-copy " name="0">
      <Image :image="imageList[imageList.length < 2 ? 0 : 1]" :newId="'imageCopied-' + 0" :sized="false"
        v-on:click="clearSetCarousel(imageList.length)"></Image>
    </div>
    <div v-if="imageList.length < 4" class="crsl-item crsl-copy " name="1">
      <Image :image="imageList[imageList.length < 3 ? 0 : 2]" :newId="'imageCopied-' + 1" :sized="false"
        v-on:click="clearSetCarousel(imageList.length + 1)"></Image>
    </div>
    <div id="arrowleft" class="arrow unblocked" v-on:click="clearSetCarousel(index - 1)">
      <font-awesome-icon icon="fa-solid fa-arrow-left" size="2xl" style="color: #312f2f;" />
    </div>
    <div id="arrowright" class="arrow unblocked" v-on:click="clearSetCarousel(index + 1)">
      <font-awesome-icon icon="fa-solid fa-arrow-right" size="2xl" style="color: #312f2f;" />
    </div>

  </div>
  <div class="mx-auto my-auto" v-else>
    No images to show
  </div>
</template>

<style scoped>
/** inspiré par : https://codepen.io/YousifW/pen/LKBxZX 
Je ne me suis inspiré que du css*/
#arrowleft,
#arrowright {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 4;
}

#arrowleft {
  transform: translate(-350%, 600%);
}

#arrowright {
  transform: translate(250%, 600%);
}

.crsl {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translateX(-50%);
  user-select: none;
}

.crsl-item {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(50%, -50%);
  width: 250px;
  height: 250px;
  border-radius: 50%;
  transition: all 300ms ease-in-out;
  overflow: hidden;
  z-index: -1;
  opacity: 0;
}

.act {
  opacity: 1;
  transform: translate(-50%, -50%);
  z-index: 3;
  box-shadow: 0px 0px 105px -35px rgba(0, 0, 0, 0.75);
  order: 3
}


.act:hover {
  width: 300px;
  height: 300px;
}

.firstprv {
  order: 2;
  z-index: 2;
  opacity: 0.6;
  transform: translate(-125%, -50%);
}

.firstnxt {
  order: 4;
  z-index: 2;
  opacity: 0.6;
  transform: translate(25%, -50%);
}

.lastprv {
  order: 1;
  z-index: 1;
  opacity: 0.25;
  transform: translate(-200%, -50%);
}

.lastnxt {
  order: 5;
  z-index: 1;
  opacity: 0.25;
  transform: translate(100%, -50%);

}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}</style>
