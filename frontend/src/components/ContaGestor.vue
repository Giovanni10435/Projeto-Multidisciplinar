<template>
  <!-- Fundo escurecido (clicar fora também fecha) -->
  <div
    style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 16px; box-sizing: border-box; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); overflow-y: auto;"
    @click.self="emit('close')"
  >
    <!-- Card -->
    <div
      style="box-sizing: border-box; width: 580px; max-width: 100%; margin: auto; background: white; border: 1px solid #e2e8f0; border-radius: 20px; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25); font-family: sans-serif; overflow: hidden;"
    >
      <!-- Cabeçalho: ícone, nome, endereço e X -->
      <div style="display: flex; align-items: center; gap: 16px; padding: 20px 24px; border-bottom: 1px solid #e2e8f0;">
        <div style="width: 52px; height: 52px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 28px; background: #ecfeff; border: 2px solid #0891b2; border-radius: 12px;">
          🏠
        </div>
        <div style="flex: 1; min-width: 0;">
          <div style="font-size: 20px; font-weight: 800; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
            {{ instituicao.nome }}
          </div>
          <div style="margin-top: 2px; font-size: 14px; color: #64748b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
            {{ instituicao.localizacao?.endereco || 'Endereço não informado' }}
          </div>
        </div>
        <button
          type="button"
          @click="emit('close')"
          aria-label="Fechar"
          style="width: 40px; height: 40px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #f1f5f9; border: none; border-radius: 999px; cursor: pointer;"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2.5" stroke-linecap="round">
            <path d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      </div>

      <!-- Abas -->
      <div style="display: flex; gap: 8px; padding: 12px 24px 0; border-bottom: 1px solid #e2e8f0;">
        <button type="button" @click="aba = 'perfil'" :style="estiloAba(aba === 'perfil')">🏠 Perfil</button>
        <button type="button" @click="aba = 'doacoes'" :style="estiloAba(aba === 'doacoes')">📦 Doações</button>
        <button type="button" @click="aba = 'voluntarios'" :style="estiloAba(aba === 'voluntarios')">🤝 Voluntários</button>
      </div>

      <div style="padding: 24px;">

        <!-- ================= ABA PERFIL ================= -->
        <div v-if="aba === 'perfil'">

          <!-- Modo visualização -->
          <div v-if="somenteLeitura || modoPerfil === 'ver'">
            <!-- Capacidade / Ocupação / Vagas -->
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px; margin-bottom: 20px;">
              <div :style="estiloStat">
                <div :style="estiloStatLabel">Capacidade</div>
                <div style="font-size: 30px; font-weight: 800; color: #1e293b;">{{ total }}</div>
              </div>
              <div :style="estiloStat">
                <div :style="estiloStatLabel">Ocupação</div>
                <div :style="{ fontSize: '30px', fontWeight: 800, color: corOcupacao }">{{ ocupadas }}</div>
              </div>
              <div :style="estiloStat">
                <div :style="estiloStatLabel">Vagas</div>
                <div style="font-size: 30px; font-weight: 800; color: #15803d;">{{ vagas }}</div>
              </div>
            </div>

            <!-- Barra de ocupação -->
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
              <span style="font-size: 15px; font-weight: 700; color: #1e293b;">Ocupação atual</span>
              <span :style="{ fontSize: '15px', fontWeight: 800, color: corOcupacao }">{{ percentual }}%</span>
            </div>
            <div style="height: 12px; background: #e2e8f0; border-radius: 999px; overflow: hidden; margin-bottom: 24px;">
              <div :style="{ height: '100%', width: Math.min(percentual, 100) + '%', background: corOcupacao, borderRadius: '999px' }"></div>
            </div>

            <!-- Informações -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 24px;">
              <div :style="estiloCard">
                <div :style="estiloCardLabel">CNPJ</div>
                <div :style="estiloCardValor">{{ instituicao.cnpj || '—' }}</div>
              </div>
              <div :style="estiloCard">
                <div :style="estiloCardLabel">RESPONSÁVEL</div>
                <div :style="estiloCardValor">{{ instituicao.responsavel || '—' }}</div>
              </div>
              <div :style="estiloCard">
                <div :style="estiloCardLabel">TELEFONE</div>
                <div :style="estiloCardValor">{{ instituicao.telefone || '—' }}</div>
              </div>
              <div :style="estiloCard">
                <div :style="estiloCardLabel">E-MAIL</div>
                <div :style="estiloCardValor">{{ instituicao.email_contato || '—' }}</div>
              </div>
              <div v-if="instituicao.descricao" :style="[estiloCard, 'grid-column: 1 / -1;']">
                <div :style="estiloCardLabel">DESCRIÇÃO</div>
                <div :style="estiloCardValor">{{ instituicao.descricao }}</div>
              </div>
            </div>

            <button
              type="button"
              v-if="!somenteLeitura"
              @click="iniciarEdicao"
              style="display: block; padding: 10px 18px; margin-bottom: 16px; background: #eff6ff; color: #1d4ed8; font-size: 14px; font-weight: 700; border: 1px solid #2563eb; border-radius: 10px; cursor: pointer;"
            >
              ✎ Editar informações
            </button>

            <button
              type="button"
              v-if="!somenteLeitura"
              @click="emit('sair')"
              style="padding: 9px 16px; background: #fef2f2; color: #b91c1c; font-size: 14px; font-weight: 700; border: 1px solid #fecaca; border-radius: 10px; cursor: pointer;"
            >
              ↩ Sair da conta
            </button>
          </div>

          <!-- Modo edição -->
          <form v-else @submit.prevent="salvarEdicao" style="display: flex; flex-direction: column; gap: 14px;">
            <div>
              <label :style="estiloLabel">Nome da Instituição</label>
              <input v-model="form.nome" required type="text" :style="estiloInput" />
            </div>
            <div>
              <label :style="estiloLabel">Endereço</label>
              <input v-model="form.endereco" required type="text" :style="estiloInput" />
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
              <div>
                <label :style="estiloLabel">CNPJ</label>
                <input v-model="form.cnpj" type="text" placeholder="00.000.000/0001-00" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">Responsável</label>
                <input v-model="form.responsavel" type="text" placeholder="Nome do responsável" :style="estiloInput" />
              </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
              <div>
                <label :style="estiloLabel">Telefone</label>
                <input v-model="form.telefone" type="text" placeholder="(11) 90000-0000" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">E-mail</label>
                <input v-model="form.email_contato" type="email" placeholder="contato@abrigo.com" :style="estiloInput" />
              </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
              <div>
                <label :style="estiloLabel">Capacidade</label>
                <input v-model.number="form.capacidade" required type="number" min="1" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">Ocupação</label>
                <input v-model.number="form.ocupacao" required type="number" min="0" :style="estiloInput" />
              </div>
            </div>

            <p v-if="erro" :style="estiloErro">{{ erro }}</p>

            <div style="display: flex; gap: 12px; margin-top: 4px;">
              <button
                type="submit"
                style="flex: 1; height: 48px; background: #0891b2; color: white; font-size: 15px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
              >
                Salvar alterações
              </button>
              <button
                type="button"
                @click="modoPerfil = 'ver'"
                style="height: 48px; padding: 0 24px; background: #f1f5f9; color: #475569; font-size: 15px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>

        <!-- ================= ABA DOAÇÕES ================= -->
        <div v-else-if="aba === 'doacoes'">
          <p style="margin: 0 0 18px; font-size: 14px; color: #475569; line-height: 1.5;">
            {{ somenteLeitura
              ? 'Estes são os itens que o abrigo está precisando no momento.'
              : 'Liste os itens que seu abrigo precisa. Os usuários e voluntários verão esses pedidos.' }}
          </p>

          <form
            v-if="!somenteLeitura"
            @submit.prevent="adicionarDoacao"
            style="padding: 18px; margin-bottom: 18px; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 16px;"
          >
            <div style="margin-bottom: 14px; font-size: 14px; font-weight: 800; color: #166534;">➕ Adicionar pedido de doação</div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
              <div>
                <label :style="estiloLabel">Item necessário</label>
                <input v-model="novaDoacao.item" required type="text" placeholder="Ex: Cobertores" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">Quantidade</label>
                <input v-model="novaDoacao.quantidade" type="text" placeholder="Ex: 20 unidades" :style="estiloInput" />
              </div>
            </div>

            <div style="display: flex; align-items: center; justify-content: space-between;">
              <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 700; color: #1e293b; cursor: pointer;">
                <input v-model="novaDoacao.urgente" type="checkbox" style="width: 18px; height: 18px; cursor: pointer;" />
                Urgente
              </label>
              <button
                type="submit"
                style="padding: 11px 24px; background: #15803d; color: white; font-size: 14px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
              >
                Adicionar
              </button>
            </div>
          </form>

          <div style="display: flex; flex-direction: column; gap: 10px;">
            <div
              v-for="d in doacoesOrdenadas"
              :key="d.id"
              :style="{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px',
                padding: '14px 16px', borderRadius: '12px',
                background: d.urgente ? '#fff7ed' : '#f8fafc',
                border: d.urgente ? '1px solid #fed7aa' : '1px solid #e2e8f0'
              }"
            >
              <div style="display: flex; align-items: center; flex-wrap: wrap; gap: 8px; min-width: 0; font-size: 14px;">
                <span style="font-weight: 800; color: #0f172a;">{{ d.item }}</span>
                <span style="color: #64748b;">· {{ d.quantidade || 'Qualquer quantidade' }}</span>
                <span
                  v-if="d.urgente"
                  style="padding: 3px 9px; font-size: 11px; font-weight: 800; letter-spacing: 0.03em; color: #b91c1c; background: #fee2e2; border-radius: 999px;"
                >
                  URGENTE
                </span>
              </div>
              <button v-if="!somenteLeitura" type="button" @click="removerDoacao(d.id)" aria-label="Remover pedido" :style="estiloBotaoRemover">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="3" stroke-linecap="round">
                  <path d="M6 6l12 12M18 6L6 18" />
                </svg>
              </button>
            </div>

            <p v-if="!doacoesOrdenadas.length" style="margin: 0; font-size: 14px; color: #94a3b8;">
              {{ somenteLeitura ? 'Este abrigo ainda não cadastrou pedidos de doação.' : 'Nenhum pedido de doação ainda.' }}
            </p>
          </div>
        </div>

        <!-- ================= ABA VOLUNTÁRIOS ================= -->
        <div v-else>
          <p style="margin: 0 0 18px; font-size: 14px; color: #475569;">
            {{ somenteLeitura
              ? 'Veja os horários em que o abrigo precisa de voluntários.'
              : 'Cadastre os horários disponíveis para voluntários.' }}
          </p>

          <form
            v-if="!somenteLeitura"
            @submit.prevent="adicionarHorario"
            style="padding: 18px; margin-bottom: 18px; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 16px;"
          >
            <div style="margin-bottom: 14px; font-size: 14px; font-weight: 800; color: #1e40af;">➕ Adicionar horário</div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
              <div>
                <label :style="estiloLabel">Dia da semana</label>
                <select v-model="novoHorario.dia" :style="estiloInput">
                  <option v-for="dia in DIAS" :key="dia" :value="dia">{{ dia }}</option>
                </select>
              </div>
              <div>
                <label :style="estiloLabel">Função</label>
                <input v-model="novoHorario.funcao" required type="text" placeholder="Ex: Cozinha, Recepção" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">Início</label>
                <input v-model="novoHorario.inicio" required type="time" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">Fim</label>
                <input v-model="novoHorario.fim" required type="time" :style="estiloInput" />
              </div>
            </div>

            <p v-if="erroHorario" :style="{ ...estiloErroObj, marginBottom: '12px' }">{{ erroHorario }}</p>

            <div style="display: flex; justify-content: flex-end;">
              <button
                type="submit"
                style="padding: 11px 24px; background: #1d4ed8; color: white; font-size: 14px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
              >
                Adicionar
              </button>
            </div>
          </form>

          <div style="display: flex; flex-direction: column; gap: 10px;">
            <div
              v-for="h in horariosOrdenados"
              :key="h.id"
              style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; padding: 14px 16px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;"
            >
              <div style="display: flex; align-items: center; flex-wrap: wrap; gap: 10px; min-width: 0; font-size: 14px;">
                <span style="font-weight: 800; color: #0f172a;">{{ h.dia }}</span>
                <span style="padding: 4px 10px; font-weight: 600; color: #1d4ed8; background: #dbeafe; border-radius: 8px;">
                  {{ h.inicio }} – {{ h.fim }}
                </span>
                <span style="color: #475569;">🤝 {{ h.funcao }}</span>
              </div>
              <!-- Visitante: botão de voluntariar (ainda sem ação, só avisa o App) -->
              <button
                v-if="somenteLeitura"
                type="button"
                @click="emit('voluntariar', { instituicaoId: instituicao.id, horario: h })"
                style="padding: 8px 14px; background: #1d4ed8; color: white; font-size: 13px; font-weight: 700; border: none; border-radius: 10px; cursor: pointer;"
              >
                Quero ser voluntário
              </button>

              <button v-if="!somenteLeitura" type="button" @click="removerHorario(h.id)" aria-label="Remover horário" :style="estiloBotaoRemover">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="3" stroke-linecap="round">
                  <path d="M6 6l12 12M18 6L6 18" />
                </svg>
              </button>
            </div>

            <p v-if="!horariosOrdenados.length" style="margin: 0; font-size: 14px; color: #94a3b8;">
              {{ somenteLeitura ? 'Este abrigo ainda não cadastrou horários para voluntários.' : 'Nenhum horário cadastrado ainda.' }}
            </p>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { calcularCorOcupacao } from '../data'

const props = defineProps({
  instituicao: { type: Object, required: true },
  // true = visitante: só visualiza (sem editar, sem remover, sem sair)
  somenteLeitura: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'atualizar', 'sair', 'voluntariar'])

const aba = ref('perfil')
const modoPerfil = ref('ver')
const erro = ref('')
const erroHorario = ref('')

/* ---------- Estilos ---------- */
const estiloLabel = 'display: block; margin-bottom: 6px; font-size: 14px; font-weight: 700; color: #1e293b;'
const estiloInput = 'box-sizing: border-box; width: 100%; height: 46px; padding: 0 14px; font-size: 15px; font-family: inherit; color: #1e293b; background: #f8fafc; border: 2px solid #cbd5e1; border-radius: 12px; outline: none;'
const estiloStat = 'padding: 16px 12px; text-align: center; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 16px;'
const estiloStatLabel = 'margin-bottom: 4px; font-size: 14px; color: #94a3b8;'
const estiloCard = 'padding: 14px 16px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 14px; min-width: 0;'
const estiloCardLabel = 'font-size: 11px; font-weight: 700; letter-spacing: 0.05em; color: #94a3b8;'
const estiloCardValor = 'margin-top: 4px; font-size: 15px; font-weight: 700; color: #0f172a; word-break: break-word;'
const estiloErroObj = { margin: 0, padding: '10px 12px', fontSize: '13px', color: '#b91c1c', background: '#fef2f2', border: '1px solid #fecaca', borderRadius: '10px' }
const estiloErro = estiloErroObj
const estiloBotaoRemover = 'width: 38px; height: 38px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #fef2f2; border: none; border-radius: 999px; cursor: pointer;'

function estiloAba(ativa) {
  return `padding: 10px 16px; font-size: 14px; font-weight: 700; border: none; border-radius: 10px 10px 0 0; cursor: pointer; background: ${ativa ? '#0891b2' : '#f1f5f9'}; color: ${ativa ? 'white' : '#475569'};`
}

// Único ponto que avisa o App para alterar dados do abrigo.
// No modo somente leitura ele não faz nada, mesmo que algum botão fosse acionado.
function atualizar(campos) {
  if (props.somenteLeitura) return
  emit('atualizar', campos)
}

function novoId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
}

/* ---------- Perfil ---------- */
const total = computed(() => Number(props.instituicao.vagas_totais) || 0)
const ocupadas = computed(() => Number(props.instituicao.vagas_ocupadas) || 0)
const vagas = computed(() => Math.max(total.value - ocupadas.value, 0))
const razao = computed(() => (total.value > 0 ? ocupadas.value / total.value : 0))
const percentual = computed(() => Math.round(razao.value * 100))
const corOcupacao = computed(() => calcularCorOcupacao(razao.value))

const form = reactive({
  nome: '',
  endereco: '',
  cnpj: '',
  responsavel: '',
  telefone: '',
  email_contato: '',
  capacidade: 0,
  ocupacao: 0
})

function iniciarEdicao() {
  if (props.somenteLeitura) return
  const i = props.instituicao
  Object.assign(form, {
    nome: i.nome ?? '',
    endereco: i.localizacao?.endereco ?? '',
    cnpj: i.cnpj ?? '',
    responsavel: i.responsavel ?? '',
    telefone: i.telefone ?? '',
    email_contato: i.email_contato ?? '',
    capacidade: Number(i.vagas_totais) || 0,
    ocupacao: Number(i.vagas_ocupadas) || 0
  })
  erro.value = ''
  modoPerfil.value = 'editar'
}

function salvarEdicao() {
  const capacidade = Number(form.capacidade)
  const ocupacao = Number(form.ocupacao)

  if (!Number.isInteger(capacidade) || capacidade < 1) {
    erro.value = 'A capacidade precisa ser um número inteiro maior que zero.'
    return
  }
  if (!Number.isInteger(ocupacao) || ocupacao < 0) {
    erro.value = 'A ocupação precisa ser um número inteiro, de zero para cima.'
    return
  }
  if (ocupacao > capacidade) {
    erro.value = 'A ocupação não pode ser maior que a capacidade.'
    return
  }

  atualizar({
    nome: form.nome.trim(),
    cnpj: form.cnpj.trim(),
    responsavel: form.responsavel.trim(),
    telefone: form.telefone.trim(),
    email_contato: form.email_contato.trim(),
    vagas_totais: capacidade,
    vagas_ocupadas: ocupacao,
    localizacao: { endereco: form.endereco.trim() }
  })
  modoPerfil.value = 'ver'
}

/* ---------- Doações ---------- */
const novaDoacao = reactive({ item: '', quantidade: '', urgente: false })
const doacoes = computed(() => props.instituicao.doacoes ?? [])
// Urgentes primeiro
const doacoesOrdenadas = computed(() =>
  [...doacoes.value].sort((a, b) => Number(b.urgente) - Number(a.urgente))
)

function adicionarDoacao() {
  const item = novaDoacao.item.trim()
  if (!item) return

  atualizar({
    doacoes: [
      ...doacoes.value,
      { id: novoId(), item, quantidade: novaDoacao.quantidade.trim(), urgente: novaDoacao.urgente }
    ]
  })
  novaDoacao.item = ''
  novaDoacao.quantidade = ''
  novaDoacao.urgente = false
}

function removerDoacao(id) {
  atualizar({ doacoes: doacoes.value.filter(d => d.id !== id) })
}

/* ---------- Voluntários ---------- */
const DIAS = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo']
const novoHorario = reactive({ dia: 'Segunda-feira', funcao: '', inicio: '08:00', fim: '12:00' })
const horarios = computed(() => props.instituicao.horarios_voluntarios ?? [])
// Ordena por dia da semana e depois pelo horário de início
const horariosOrdenados = computed(() =>
  [...horarios.value].sort(
    (a, b) => DIAS.indexOf(a.dia) - DIAS.indexOf(b.dia) || a.inicio.localeCompare(b.inicio)
  )
)

function adicionarHorario() {
  erroHorario.value = ''
  const funcao = novoHorario.funcao.trim()
  if (!funcao) return

  if (novoHorario.fim <= novoHorario.inicio) {
    erroHorario.value = 'O horário de fim precisa ser depois do início.'
    return
  }

  atualizar({
    horarios_voluntarios: [
      ...horarios.value,
      { id: novoId(), dia: novoHorario.dia, funcao, inicio: novoHorario.inicio, fim: novoHorario.fim }
    ]
  })
  novoHorario.funcao = ''
}

function removerHorario(id) {
  atualizar({ horarios_voluntarios: horarios.value.filter(h => h.id !== id) })
}
</script>