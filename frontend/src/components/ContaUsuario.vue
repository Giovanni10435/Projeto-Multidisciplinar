<template>
  <!-- Fundo escurecido (clicar fora também fecha) -->
  <div
    style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 16px; box-sizing: border-box; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); overflow-y: auto;"
    @click.self="emit('close')"
  >
    <!-- Card -->
    <div
      style="box-sizing: border-box; width: 500px; max-width: 100%; margin: auto; background: white; border: 1px solid #e2e8f0; border-radius: 20px; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25); font-family: sans-serif; overflow: hidden;"
    >
      <!-- Cabeçalho: título, X e abas -->
      <div style="padding: 28px 32px 0; border-bottom: 1px solid #e2e8f0;">
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <h2 style="margin: 0; font-size: 22px; font-weight: 800; color: #0f172a;">Minha Conta</h2>
          <button
            type="button"
            @click="emit('close')"
            aria-label="Fechar"
            style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; background: #f1f5f9; border: none; border-radius: 999px; cursor: pointer;"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2.5" stroke-linecap="round">
              <path d="M6 6l12 12M18 6L6 18" />
            </svg>
          </button>
        </div>

        <div style="display: flex; gap: 8px; margin: 20px 0;">
          <button type="button" @click="aba = 'perfil'" :style="estiloAba(aba === 'perfil')">
            👤 Perfil
          </button>
          <button type="button" @click="aba = 'historico'" :style="estiloAba(aba === 'historico')">
            📋 Histórico
          </button>
        </div>
      </div>

      <!-- ABA PERFIL: MODO VISUALIZAÇÃO (conta já salva) -->
      <div v-if="aba === 'perfil' && modo === 'ver'" style="padding: 24px 32px 32px;">
        <div style="display: flex; align-items: center; gap: 24px; margin-bottom: 24px;">
          <div style="width: 88px; height: 88px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 44px; background: #dbeafe; border: 3px solid #2563eb; border-radius: 999px;">
            🙋‍♀️
          </div>
          <div style="min-width: 0;">
            <div style="font-size: 20px; font-weight: 800; color: #0f172a; word-break: break-word;">{{ perfil?.nome || 'Sem nome' }}</div>
            <div style="margin: 2px 0 10px; font-size: 14px; color: #64748b; word-break: break-all;">{{ perfil?.email || 'Email não informado' }}</div>
            <button
              type="button"
              @click="modo = 'editar'"
              style="padding: 6px 14px; background: #eff6ff; color: #1d4ed8; font-size: 13px; font-weight: 700; border: 1px solid #2563eb; border-radius: 10px; cursor: pointer;"
            >
              ✎ Editar perfil
            </button>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 28px;">
          <div :style="estiloCard">
            <div :style="estiloCardLabel">IDADE</div>
            <div :style="estiloCardValor">{{ perfil?.idade ? perfil.idade + ' anos' : '—' }}</div>
          </div>
          <div :style="estiloCard">
            <div :style="estiloCardLabel">TELEFONE</div>
            <div :style="estiloCardValor">{{ perfil?.telefone || '—' }}</div>
          </div>
          <div :style="estiloCard">
            <div :style="estiloCardLabel">E-MAIL</div>
            <div :style="estiloCardValor">{{ perfil?.email || '—' }}</div>
          </div>
          <div :style="estiloCard">
            <div :style="estiloCardLabel">CPF</div>
            <div :style="estiloCardValor">{{ perfil?.cpf || '—' }}</div>
          </div>
        </div>

        <button
          type="button"
          @click="emit('sair')"
          style="padding: 9px 16px; background: #fef2f2; color: #b91c1c; font-size: 14px; font-weight: 700; border: 1px solid #fecaca; border-radius: 10px; cursor: pointer;"
        >
          ↩ Sair da conta
        </button>
      </div>

      <!-- ABA PERFIL: MODO EDIÇÃO (cadastro ou edição) -->
      <form
        v-else-if="aba === 'perfil'"
        @submit.prevent="salvar"
        style="padding: 24px 32px 32px;"
      >
        <!-- Avatar + resumo -->
        <div style="display: flex; align-items: center; gap: 24px; margin-bottom: 24px;">
          <div style="position: relative; flex-shrink: 0;">
            <div style="width: 76px; height: 76px; display: flex; align-items: center; justify-content: center; font-size: 38px; background: #dbeafe; border: 3px solid #2563eb; border-radius: 999px;">
              🙋‍♀️
            </div>
            <div style="position: absolute; right: -2px; bottom: -2px; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: #2563eb; border: 2px solid white; border-radius: 999px;">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 20h9M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4z" />
              </svg>
            </div>
          </div>
          <div style="min-width: 0;">
            <div style="font-size: 18px; font-weight: 800; color: #0f172a;">{{ form.nome || 'Sem nome' }}</div>
            <div style="margin-top: 2px; font-size: 14px; color: #64748b; word-break: break-all;">{{ form.email || 'Email não informado' }}</div>
          </div>
        </div>

        <!-- Campos -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
          <div>
            <label :style="estiloLabel">Nome completo</label>
            <input v-model="form.nome" required type="text" placeholder="Seu nome" :style="estiloInput" />
          </div>
          <div>
            <label :style="estiloLabel">Idade</label>
            <input v-model="form.idade" type="number" min="0" placeholder="Ex: 32" :style="estiloInput" />
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
          <div>
            <label :style="estiloLabel">Telefone</label>
            <input v-model="form.telefone" type="text" placeholder="(11) 9 9999-9999" :style="estiloInput" />
          </div>
          <div>
            <label :style="estiloLabel">E-mail</label>
            <input v-model="form.email" type="email" placeholder="email@exemplo.com" :style="estiloInput" />
          </div>
        </div>

        <div style="margin-bottom: 24px;">
          <label :style="estiloLabel">CPF</label>
          <input v-model="form.cpf" type="text" placeholder="000.000.000-00" :style="estiloInput" />
        </div>

        <!-- Botões -->
        <div style="display: flex; gap: 12px; margin-bottom: 24px;">
          <button
            type="submit"
            style="flex: 1; height: 52px; background: #1d4ed8; color: white; font-size: 15px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
          >
            Salvar alterações
          </button>
          <button
            type="button"
            @click="cancelar"
            style="height: 52px; padding: 0 24px; background: #f1f5f9; color: #475569; font-size: 15px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
          >
            Cancelar
          </button>
        </div>

        <button
          type="button"
          @click="emit('sair')"
          style="padding: 9px 16px; background: #fef2f2; color: #b91c1c; font-size: 14px; font-weight: 700; border: 1px solid #fecaca; border-radius: 10px; cursor: pointer;"
        >
          ↩ Sair da conta
        </button>
      </form>

      <!-- ABA HISTÓRICO -->
      <div v-else style="padding: 24px 32px 32px;">
        <h3 style="margin: 0 0 12px; font-size: 17px; font-weight: 800; color: #0f172a;">🔍 Pesquisas recentes</h3>

        <ul
          v-if="pesquisas.length"
          style="list-style: none; margin: 0 0 28px; padding: 0; display: flex; flex-direction: column; gap: 6px;"
        >
          <li
            v-for="p in pesquisas"
            :key="p.termo"
            style="display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 9px 14px; font-size: 14px; color: #334155; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px;"
          >
            <span style="min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">"{{ p.termo }}"</span>
            <span style="flex-shrink: 0; font-size: 12px; color: #94a3b8;">{{ p.hora }}</span>
          </li>
        </ul>
        <p v-else style="margin: 0 0 28px; font-size: 14px; color: #94a3b8;">Nenhuma pesquisa ainda.</p>

        <h3 style="margin: 0 0 12px; font-size: 17px; font-weight: 800; color: #0f172a;">🏠 Abrigos visitados recentemente</h3>

        <ul
          v-if="abrigos.length"
          style="list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 6px;"
        >
          <li
            v-for="a in abrigos"
            :key="a.id"
            style="display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 9px 14px; font-size: 14px; font-weight: 600; color: #15803d; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 10px;"
          >
            <span style="min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">📍 {{ a.nome }}</span>
            <span style="flex-shrink: 0; font-size: 12px; font-weight: 500; color: #22c55e;">{{ a.hora }}</span>
          </li>
        </ul>
        <p v-else style="margin: 0; font-size: 14px; color: #94a3b8;">Nenhum abrigo visualizado ainda.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  perfil: { type: Object, default: null },
  pesquisas: { type: Array, default: () => [] },
  abrigos: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'salvar', 'sair'])

const aba = ref('perfil')
// Se já existe conta salva, abre mostrando os dados; senão, abre o cadastro
const modo = ref(props.perfil ? 'ver' : 'editar')

const form = reactive({
  nome: props.perfil?.nome ?? '',
  idade: props.perfil?.idade ?? '',
  telefone: props.perfil?.telefone ?? '',
  email: props.perfil?.email ?? '',
  cpf: props.perfil?.cpf ?? ''
})

const estiloLabel = 'display: block; margin-bottom: 6px; font-size: 14px; font-weight: 700; color: #1e293b;'
const estiloInput = 'box-sizing: border-box; width: 100%; height: 48px; padding: 0 14px; font-size: 15px; font-family: inherit; color: #1e293b; background: #f8fafc; border: 2px solid #cbd5e1; border-radius: 12px; outline: none;'
const estiloCard = 'padding: 14px 16px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 14px; min-width: 0;'
const estiloCardLabel = 'font-size: 11px; font-weight: 700; letter-spacing: 0.05em; color: #94a3b8;'
const estiloCardValor = 'margin-top: 4px; font-size: 15px; font-weight: 700; color: #0f172a; word-break: break-word;'

function estiloAba(ativa) {
  return `padding: 9px 16px; font-size: 14px; font-weight: 700; border: none; border-radius: 10px; cursor: pointer; background: ${ativa ? '#1d4ed8' : '#f1f5f9'}; color: ${ativa ? 'white' : '#475569'};`
}

function salvar() {
  emit('salvar', { ...form })
  modo.value = 'ver'
}

function cancelar() {
  if (props.perfil) {
    // Descarta o que foi digitado e volta a mostrar os dados salvos
    Object.assign(form, {
      nome: props.perfil.nome ?? '',
      idade: props.perfil.idade ?? '',
      telefone: props.perfil.telefone ?? '',
      email: props.perfil.email ?? '',
      cpf: props.perfil.cpf ?? ''
    })
    modo.value = 'ver'
  } else {
    emit('close')
  }
}
</script>