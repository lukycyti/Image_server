<script setup lang="ts">

import { useRoute } from 'vue-router';
const location = useRoute();

</script>
<template>
    <div class=" mt-2 mr-5 d-inline-flex flex-row">      
            <a v-if="location.path != '/'" class="unblocked" href="/">
                <font-awesome-icon icon="fa-solid fa-house" size="2xl" style="color: #888a85;" />
            </a>     
        <slot></slot>
    </div>
</template>