<script setup lang="ts">
import { defineProps, ref } from 'vue';
import { api } from '@/http-api';
import { watch } from 'vue';
import { ImageType, initialize } from '@/image';

const props = defineProps<{ image: ImageType, newId?: string, height?: number, classes?: string, sized?: boolean }>()
const id = props.image.id;


//Observe l'image pour vérifier si elle a été modifiée.
watch(props.image, () => {
  showImage();
 
  });
    


function getId(): string{
  return props.newId !== undefined ? props.newId : 'image-' + id;
}

/**
 * Affiche l'image correspondant à l'image "actuelle" parmis toute la mémoire d'image (composée des modifications avant et/ou après)
 */
function showImage(){
  document.getElementById(getId())?.setAttribute("src",props.image.memory[props.image.index]);
}


api.getImage(id)
  .then((data: Blob) => {
    const reader = new window.FileReader();
    reader.readAsDataURL(data);
    reader.onload = () => {
      if (reader.result as string) {
          initialize(props.image, reader.result as string);
          console.log(props.image.memory);
          showImage();
        }
    };
  })
  .catch(e => {
    console.log(e.message);
  });


</script>

<template>
  <img :id=" newId !== undefined ? newId : 'image-' + id"  v-bind:class="classes !== undefined ? classes : ''">
</template>

