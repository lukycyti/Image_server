<script setup lang="ts">




</script>

<template>
  <div id="app">
    <div class="container-fluid d-flex">
      <router-view />
    </div>
    
  </div>
</template>

<style>
#app {

  font-family: Arial, Helvetica, sans-serif;
  color: #2c3e50;
}

.unblocked:hover, .blocked{
    opacity:50%;
}

.unblocked, .blocked{
  margin-right:2%;
}

ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background-color: #333;
}
li {
  float: left;
}
li a {
  display: block;
  color: white;
  text-align: center;
  padding: 14px 16px;
  text-decoration: none;
}
.error {
  color: red;
  font-weight: bold;
  list-style-type: none;
  margin-top: 1em;
}
</style>
