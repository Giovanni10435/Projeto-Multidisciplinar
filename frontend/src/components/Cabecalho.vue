<template>
  <!-- Cabeçalho no topo da página -->
  <header
    style="position: absolute; top: 0; left: 0; width: 100%; height: 64px; box-sizing: border-box; z-index: 1000; display: flex; align-items: center; gap: 4px; padding: 0 16px; background: white; border-bottom: 1px solid #e2e8f0; box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08); font-family: sans-serif;"
  >
    <button
      v-for="secao in secoes"
      :key="secao.id"
      type="button"
      class="cabecalho-botao"
      @click="secaoAbertaId = secao.id"
    >
      {{ secao.titulo }}
    </button>
  </header>

  <!-- Janela pequena da seção escolhida (clicar fora também fecha) -->
  <div
    v-if="secaoAberta"
    style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 16px; box-sizing: border-box; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); overflow-y: auto;"
    @click.self="secaoAbertaId = null"
  >
    <div
      style="box-sizing: border-box; width: 440px; max-width: 100%; margin: auto; background: white; border: 1px solid #e2e8f0; border-radius: 20px; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25); font-family: sans-serif; overflow: hidden;"
    >
      <!-- Título + X -->
      <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 20px 24px; border-bottom: 1px solid #e2e8f0;">
        <div style="display: flex; align-items: center; gap: 12px; min-width: 0;">
          <span style="font-size: 26px; line-height: 1;">{{ secaoAberta.icone }}</span>
          <h2 style="margin: 0; font-size: 20px; font-weight: 800; color: #0f172a;">{{ secaoAberta.titulo }}</h2>
        </div>
        <button
          type="button"
          @click="secaoAbertaId = null"
          aria-label="Fechar"
          style="width: 40px; height: 40px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #f1f5f9; border: none; border-radius: 999px; cursor: pointer;"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2.5" stroke-linecap="round">
            <path d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      </div>

      <!-- Conteúdo -->
      <div style="padding: 24px; font-size: 14px; line-height: 1.6; color: #475569;">
        <p
          v-for="(paragrafo, i) in secaoAberta.conteudo"
          :key="i"
          style="margin: 0 0 12px;"
        >
          {{ paragrafo }}
        </p>
        <p v-if="!secaoAberta.conteudo.length" style="margin: 0; color: #94a3b8;">
          Conteúdo em breve.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Para preencher depois: cada texto da lista "conteudo" vira um parágrafo na janela.
const secoes = [
  { id: 'sobre',   icone: '📖', titulo: 'Sobre o Projeto?',       conteudo: [] },
  { id: 'o-que-e', icone: '🏡', titulo: 'O que é o AcolheSP?',    conteudo: [] },
  { id: 'contato', icone: '✉️', titulo: 'Informações de Contato', conteudo: [] },
  { id: 'guia',    icone: '🧭', titulo: 'Guia para Navegação',    conteudo: [] }
]

const secaoAbertaId = ref(null)
const secaoAberta = computed(() => secoes.find(s => s.id === secaoAbertaId.value) ?? null)
</script>

<style scoped>
.cabecalho-botao {
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  background: transparent;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.cabecalho-botao:hover {
  background: #f1f5f9;
  color: #1d4ed8;
}
</style>