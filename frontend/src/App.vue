
<template>
    <!-- Parte do HTML, Criacao do estilo -->
  <div class="hello-container">
    <h1>TG2 - Teste de Arquitetura</h1>

    <div class="card">
      <p class="status">Status da Conexão: <span :class="statusClasse">{{ statusTexto }}</span></p>
      
      <h2>{{ mensagemBackend || 'Aguardando resposta do servidor...' }}</h2>
      
      <button @click="buscarDados" :disabled="carregando">
        {{ carregando ? 'Carregando...' : 'Testar Conexão Novamente' }}
      </button>
    </div>
  </div>
</template>



<script setup>
// Parte de JS para a execucao e chamada do Backend
import {ref, onMounted, computed} from 'vue';
import axios from 'axios';

const mensagemBackend = ref('');
const carregando = ref(false);
const erroConexao = ref(false);

// Altere para a URL/porta onde o seu backend vai rodar
const API_URL = '/api/hello-world';

const buscarDados = async () => {
  carregando.value = true;
  erroConexao.value = false;
  
  try {
    const response = await axios.get(API_URL);
    mensagemBackend.value = response.data.mensagem;
  } catch(error) {
    console.error('Erro de integração no TG2:', error);
    mensagemBackend.value = 'Falha ao comunicar com o Backend.';
    erroConexao.value = true;
  } finally {
    carregando.value = false;
  }
};

const statusTexto = computed(() => {
  if (carregando.value) return 'Conectando...';
  if (erroConexao.value) return 'Erro de Comunicação';
  return mensagemBackend.value ? 'Conectado com Sucesso' : 'Pendente';
});

const statusClasse = computed(() => ({
  'sucesso': mensagemBackend.value && !erroConexao.value,
  'erro': erroConexao.value,
  'aguardando': carregando.value
}));

onMounted(() => {
  buscarDados();
});
</script>



<style scoped>
/* Parte de estilo em CSS */

.hello-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-family: Arial, sans-serif;
  background-color: #f4f6f8;
  color: #333;
}

.card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.status {
  font-size: 0.9rem;
  color: #666;
}

.sucesso { color: #42b883; font-weight: bold; }
.erro { color: #e74c3c; font-weight: bold; }
.aguardando { color: #f39c12; font-weight: bold; }

button {
  margin-top: 1rem;
  padding: 0.6rem 1.2rem;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

button:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}
</style>