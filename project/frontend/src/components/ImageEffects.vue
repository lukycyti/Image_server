<script setup lang="ts">
import NavBar from './NavBar.vue';
import { api } from '@/http-api';
import { ImageType, switchImage, reset,  getSrc } from '@/image';
import { defineProps, ref} from 'vue';
import Image from './Image.vue';
import FormEffects from './FormEffects.vue';
import { allFormEffects, getText } from '@/form-effects';
import { toast } from 'vue3-toastify';
const props = defineProps<{ id: number }>();
const image = ref<ImageType>();

api.getImageList()
    .then(function (data: (ImageType)[]) {
        image.value = data.find(d => d.id == props.id);
        const div = document.getElementsByClassName("imagecontainer").item(0);
        div?.animate([
            { opacity: 0, transform: 'translateX(-50%)', width: '70%', height: '70%' },
            { opacity: 1, transform: 'translateX(0%)' }
        ],
            {
                duration: 300
            });
    })
    .catch(e => {
        console.log(e.message);
    });



const selected = ref();



function deleteImage(){
    if(image.value !== undefined) api.deleteImage(image.value.id);

}


//change l'image actuelle par une image de la mémoire.
function swtch(delta: number) {
    if (image.value !== undefined) {
        switchImage(image.value, delta);
    }
}

/**
 * Sauvegarde l'image courante en tant qu'une nouvelle image
 */
function save() {
    if (image.value !== undefined) {
        fetch(getSrc(image.value))
            .then(res => res.blob())
            .then(blob => {
                if (image.value !== undefined) {
                    const file = new File([blob], image.value.name, { type: "image/" + image.value.type });
                    let formData = new FormData();
                    formData.append("file", file);
                     api.createImage(formData).then((newImage) => {
                        toast.success("L'image a correctement été sauvegardée");
                        toast.done("");
                    })

                        .catch(e => {
                            console.log(e.message);
                            toast.error("Problème lors de la sauvegarde de l'image : \n" + e.message);
                        });
                }
            });
    }
}


</script>

<template>
    <div class="container-fluid main_container d-flex">
        <div class="row flex-fill" style="min-height:90vh">
            <nav-bar>
                <a class="unblocked" v-bind:href="image?.memory[image.index]" v-bind:download="image?.name">
                    <font-awesome-icon icon="fa-solid fa-download" size="2xl" style="color: #888a85;" />
                </a>
                <a class="unblocked" v-on:click="save()">
                    <font-awesome-icon icon="fa-regular fa-floppy-disk" size="2xl" style="color: #888a85;" />
                </a>
                <a class="unblocked" href="/" v-on:click="deleteImage()">
                    <font-awesome-icon icon="fa-solid fa-trash" size="2xl" style="color: #888a85;" />
                </a>
            </nav-bar>
            <div class="col-sm-6 h-sm-100 d-flex align-items-center ">
                <div class="mx-auto imagecontainer ">
                    <div class="text-center arrowcontainer mb-5">
                        <span class="blocked" v-on:click="swtch(-1);">
                            <font-awesome-icon icon="fa-regular fa-circle-left" size="2xl" style="color: #babdb6;" />
                        </span>
                        <span class="mx-5 unblocked" v-on:click="if (image !== undefined) reset(image);">
                            <font-awesome-icon icon="fa-solid fa-rotate-left" size="2xl" style="color: #babdb6;" />
                        </span>
                        <span class="blocked" v-on:click="swtch(1);">
                            <font-awesome-icon icon="fa-regular fa-circle-right" size="2xl" style="color: #babdb6;" />
                        </span>
                    </div>
                    <Image :image="image" :classes="'image rounded shadow'" v-if="image !== undefined"></Image>
                    <div class="text-center mt-5">

                    </div>
                </div>


            </div>
            <div class="col-sm-6 h-sm-100 d-flex align-items-center">
                <div class="card w-75 mx-auto border border-dark rounded shadow-lg " id="form">
                    <div class="mb-2" style="text-align: left;">
                        <div v-if="image !== undefined">
                            <h2 class="text-center mb-1">{{ image.name }}</h2>
                            Size: {{ image.size }} <br>

                        </div>
                    </div>
                    <h5 class="text-center">
                        Apply an effect :

                    </h5>
                    <select v-model="selected" class="mb-5 w-25 mx-auto form-select">
                        <option v-for="effect in allFormEffects" v-bind:value="effect">
                            {{ getText(effect.htmlAtt) }}
                        </option>
                    </select>
                    <div class="mx-auto text-center mb-3">
                        <FormEffects :image="image" v-if="selected != undefined && image !== undefined" :effect="selected"
                            :id="id"></FormEffects>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>


<style scoped>
#form {
    background-color: #cfcfcf;
    border: thick double #32a1ce;
}

.imagecontainer {
    width: 90%;
}

.arrowcontainer .unblocked:hover,
.blocked {
    opacity: 50%;
}

.image {
    width: 100%;
}

#image:deep(img) {
    transform: transition(-50%, -50%);
}
</style>

