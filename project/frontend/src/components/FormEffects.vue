<script setup lang="ts">
import { FormEffects } from '@/form-effects'
import { getText } from '@/form-effects'
import { api } from '@/http-api';
import { ImageType, getSrc, setImage } from '@/image';
import { ref } from 'vue';
import { toast } from 'vue3-toastify';

const props = defineProps<{ effect: FormEffects, image: ImageType }>();
const id = props.image.id;
const count = ref(0);
count.value = 0;

/**
 * Récupère tout les élements input
 */
function getAllInputs() {
    return document.getElementsByClassName("input");
}

/*
* Soumet le formulaire. Genère un message d'erreur si des paramètres sont manquants.
*/
function submitForm() {
    var inputs = getAllInputs();
    let formData: FormData = new FormData();
    if (props.effect.htmlAtt.name !== undefined) formData.append("algorithm", props.effect.htmlAtt.name);
    for (var i = 0; i < inputs.length; i++) {
        var elementInput = <HTMLInputElement>inputs[i];
        var name: string = "p-" + i;
        var value = elementInput.value;
        elementInput.value = elementInput.defaultValue;

        if (value !== undefined && value != "") formData.append(name, value);
        else{
            toast.error("Veuillez spécifier tout les paramètres demandés" );
            return;
        }
    }

    getImageAfterAlgo(formData);
}

/**
 * Envoie au serveur l'image actuelle et change l'image actuelle par une nouvelle image appliquée par un algo. 
 * @param formData un FormData contenant l'image et les paramètres de l'algorithme voulu
 */
function getImageAfterAlgo(formData: FormData) {
    if (getSrc(props.image) !== undefined) {
        fetch(getSrc(props.image))
            .then(res => res.blob())
            .then(blob => {
                const file = new File([blob], props.image.name, { type: "image/" + props.image.type });
                formData.append("file", file);
                api.getImageFromData(formData).then((data: Blob) => {
                    updateHref(data);
                }).catch(e => {
                    console.log(e.message);
                });
            })
    }
}

/**
 * Actualise l'image par une nouvelle image en châine de caractères.
 * @param data une image en Blob qui sera convertie en chaine de caractères.
 */
function updateHref(data: Blob) {
    const reader = new window.FileReader();
    console.log(data);
    reader.readAsDataURL(<Blob>data);
    reader.onload = () => {
        if (reader.result as string) {
            setImage(props.image,reader.result as string);
        }
    }
}


</script>

<template>
    <form id="formeffect" v-on:submit.prevent="submitForm">

        <div v-for="line in effect.lines" class="mb-2">
            <label v-bind:for="line.htmlAtt.name">
                {{ getText(line.htmlAtt) }}
            </label> :
            <div v-if="line.type !== 'select'">
                <input class="input" v-bind="{
                    type: line.type, id: line.htmlAtt.name, value: line.htmlAtt.value,
                    min: line.htmlAtt.min, max: line.htmlAtt.max
                }" v-bind:name="'p-' + count++">
            </div>
            <div v-else>
                <select class="input form-select" v-bind:id="line.htmlAtt.name" v-bind:name="'p-' + count++"
                    v-bind:value="line.htmlAtt.value">
                    <option v-for="option in line.options"
                        v-bind:value="option.value != undefined ? option.value : option.name">
                        <span>{{ option.text != undefined ? option.text : option.name }}</span>
                    </option>
                </select>
            </div>
        </div>
        <input type="submit" value="send" class="btn btn-light">

    </form>
</template>