<template>
  <div class="fixed inset-0 z-[2000] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-md overflow-y-auto">
    <div class="bg-white rounded-3xl shadow-2xl p-6 md:p-8 max-w-lg w-full flex flex-col items-center border border-slate-100 my-8">
      
      <div class="text-4xl mb-2">🏠</div>
      <h2 class="text-2xl font-bold text-slate-900 text-center">Como você quer entrar?</h2>
      <p class="text-slate-500 text-sm mt-1 mb-6 text-center">Escolha seu perfil ou cadastre sua instituição</p>

      <!-- Seleção de Perfil Inicial -->
      <div v-if="!abaSelecionada" class="w-full flex flex-col gap-4">
        <button 
          @click="selecionar('usuario')"
          class="w-full p-4 rounded-2xl text-left border-2 border-blue-600 bg-blue-50/50 hover:bg-blue-50 transition-all flex items-center gap-4 cursor-pointer"
        >
          <div class="text-3xl shrink-0">🙋‍♀️</div>
          <div>
            <div class="font-bold text-slate-900">Usuário / Voluntário</div>
            <div class="text-xs text-slate-500 mt-0.5">Buscar abrigos, registrar visitas e oferecer ajuda</div>
          </div>
        </button>

        <button 
          @click="abaSelecionada = 'abrigo'"
          class="w-full p-4 rounded-2xl text-left border-2 border-cyan-500 bg-cyan-50/30 hover:bg-cyan-50/70 transition-all flex items-center gap-4 cursor-pointer"
        >
          <div class="text-3xl shrink-0">🏡</div>
          <div>
            <div class="font-bold text-slate-900">Abrigo / Gestor</div>
            <div class="text-xs text-slate-500 mt-0.5">Cadastrar ou gerenciar seu abrigo e doações</div>
          </div>
        </button>
      </div>

      <!-- Formulário com os campos da Tabela 'instituicao' e 'localizacao' -->
      <form v-else-if="abaSelecionada === 'abrigo'" @submit.prevent="salvarInstituicao" class="w-full flex flex-col gap-3">
        <div class="flex items-center justify-between mb-2">
          <h3 class="font-bold text-slate-900">Dados da Instituição</h3>
          <button type="button" @click="abaSelecionada = null" class="text-xs text-blue-600 font-semibold">← Voltar</button>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">Nome da Instituição (nome)</label>
          <input v-model="formInstituicao.nome" required type="text" placeholder="Ex: Abrigo Esperança" class="w-full px-3 py-2 border rounded-xl text-sm" />
        </div>

        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1">CNPJ (cnpj)</label>
            <input v-model="formInstituicao.cnpj" required type="text" placeholder="00.000.000/0001-00" class="w-full px-3 py-2 border rounded-xl text-sm" />
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1">Telefone (telefone)</label>
            <input v-model="formInstituicao.telefone" required type="text" placeholder="(11) 90000-0000" class="w-full px-3 py-2 border rounded-xl text-sm" />
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">E-mail de Contato (email_contato)</label>
          <input v-model="formInstituicao.email_contato" required type="email" placeholder="contato@abrigo.com" class="w-full px-3 py-2 border rounded-xl text-sm" />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">Endereço Completo (endereco)</label>
          <input v-model="formInstituicao.localizacao.endereco" required type="text" placeholder="Rua, número, bairro - Cidade/UF" class="w-full px-3 py-2 border rounded-xl text-sm" />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">Descrição (descricao)</label>
          <textarea v-model="formInstituicao.descricao" rows="2" placeholder="Descreva os serviços do abrigo..." class="w-full px-3 py-2 border rounded-xl text-sm"></textarea>
        </div>

        <button type="submit" class="w-full bg-cyan-600 hover:bg-cyan-700 text-white font-bold py-2.5 rounded-xl text-sm mt-2 transition-colors">
          Cadastrar / Entrar como Gestor
        </button>
      </form>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const emit = defineEmits(['select', 'submitInstituicao'])
const abaSelecionada = ref(null)

const formInstituicao = reactive({
  nome: '',
  cnpj: '',
  descricao: '',
  telefone: '',
  email_contato: '',
  localizacao: {
    endereco: '',
    latitude: -23.550520, // Default Sé
    longitude: -46.633308
  }
})

function selecionar(role) {
  emit('select', role)
}

function salvarInstituicao() {
  emit('submitInstituicao', { ...formInstituicao })
}
</script>