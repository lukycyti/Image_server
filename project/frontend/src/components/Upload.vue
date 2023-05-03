<script setup lang="ts">
import { ref } from 'vue';
import { api } from '@/http-api';
import { toast } from 'vue3-toastify';
import  NavBar  from './NavBar.vue'


const target = ref<HTMLInputElement>();

function submitFile() {
  if (target.value !== null && target.value !== undefined && target.value.files !== null) {
    const file = target.value.files[0];
    if (file === undefined) {
      return;
    }
    let formData = new FormData();
    formData.append("file", file);
    api.createImage(formData).then(() => {
      if (target.value !== undefined) {
        target.value.value = '';
      }
      toast.success("L'image a correctement été sauvegardée");
      toast.done("");
    }
    
    )
    
    .catch(e => {
      console.log(e.message);
      toast.error("Le format d'image ne correspond pas");
    });
  }
  else {
    toast.info("Veuillez selectionner une image");
  }
}

function handleFileUpload(event: Event) {
  target.value = (event.target as HTMLInputElement);
}
</script>

<template>
  <NavBar></NavBar>
  <div id ="uploadcontainer" class="card text-center shadow">
    <div class="card-title ">
    <h3>Upload an image</h3>
  </div>
    <div class="card-body">
      <input type="file" id="file" ref="file" @change="handleFileUpload" />
    </div>
    <div>
      <button @click="submitFile" class="btn btn-dark mb-2">Submit</button>
    </div>
  </div>
</template>

<style scoped>

.card{
    background-color: #cfcfcf;
    position: absolute;
  top: 40%;
  left: 40%;

}
</style>
