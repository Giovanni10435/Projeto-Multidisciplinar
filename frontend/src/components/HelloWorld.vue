```vue
<script setup>
import { ref, onMounted } from 'vue'

import heroImg from '../assets/hero.png'
import viteLogo from '../assets/vite.svg'
import vueLogo from '../assets/vue.svg'

const count = ref(0)
const mensagem = ref('Carregando...')

onMounted(async () => {
  try {
    const resposta = await fetch('/mensagens')
    const dados = await resposta.json()

    mensagem.value = dados.mensagem
  } catch (erro) {
    mensagem.value = 'Erro ao conectar com o BFF'
    console.error(erro)
  }
})
</script>

<template>
  <section id="center">

    <div class="hero">
      <img :src="heroImg" class="base" width="170" height="179" alt="" />
      <img :src="vueLogo" class="framework" alt="Vue logo" />
      <img :src="viteLogo" class="vite" alt="Vite logo" />
    </div>

    <div>
      <h1>AcolheSP</h1>

      <p>{{ mensagem }}</p>
    </div>

    <button type="button" class="counter" @click="count++">
      Count is {{ count }}
    </button>

  </section>

  <div class="ticks"></div>

  <section id="next-steps">

    <div id="docs">
      <svg class="icon" role="presentation" aria-hidden="true">
        <use href="/icons.svg#documentation-icon"></use>
      </svg>

      <h2>Backend</h2>

      <p>
        Esta mensagem veio do Backend através do BFF.
      </p>
    </div>

    <div id="social">
      <svg class="icon" role="presentation" aria-hidden="true">
        <use href="/icons.svg#social-icon"></use>
      </svg>

      <h2>Teste de conexão</h2>

      <p>
        Vue → BFF → Backend → PostgreSQL
      </p>
    </div>

  </section>

  <div class="ticks"></div>

  <section id="spacer"></section>
</template>
```
