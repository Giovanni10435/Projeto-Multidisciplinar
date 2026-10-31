<template>
  <!-- Contêiner pai com posição relativa e 100vh fixo -->
  <div style="position: relative; width: 100vw; height: 100vh; overflow: hidden; font-family: sans-serif;">

    <!-- Modal de Entrada -->
    <RoleModal 
      v-if="exibirModalRole" 
      @select="definirPapel"
      @close="exibirModalRole = false"
      @usuarioAutenticado="usuarioAutenticado"
      @gestorAutenticado="gestorAutenticado"
      @submitInstituicao="cadastrarInstituicao" 
    />

    <!-- Modal do Gestor (Perfil / Doações / Voluntários) -->
    <ContaGestor
      v-if="exibirContaGestor && instituicaoGestor"
      :instituicao="instituicaoGestor"
      @close="exibirContaGestor = false"
      @atualizar="atualizarInstituicao(instituicaoGestor.id, $event)"
      @sair="sairGestor"
    />

    <!-- Informações do abrigo (somente leitura, para qualquer visitante) -->
    <ContaGestor
      v-if="abrigoInfo"
      :instituicao="abrigoInfo"
      somente-leitura
      @close="abrigoInfoId = null"
    />

    <!-- Modal Minha Conta (Usuário / Voluntário) -->
    <ContaUsuario
      v-if="exibirMinhaConta"
      :perfil="perfilUsuario"
      :pesquisas="pesquisasRecentes"
      :abrigos="abrigosVisitados"
      @close="exibirMinhaConta = false"
      @salvar="salvarPerfil"
      @sair="sairDaConta"
    />

    <!-- 1. MAPA LEAFLET (Fundo em Tela Cheia) -->
    <l-map
      :center="centroMapa"
      :zoom="14"
      :max-zoom="19"
      :min-zoom="10"
      :max-bounds="limitesSP"
      :options="{ zoomControl: false }"
      style="width: 100%; height: 100%; position: absolute; top: 0; left: 0; z-index: 1;"
      @ready="mapaPronto"
    >
      <l-tile-layer
        url="https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}"
        attribution="&copy; Esri"
        :max-zoom="19"
        :max-native-zoom="16"
      />
      <l-tile-layer
        url="https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer/tile/{z}/{y}/{x}"
        :max-zoom="19"
        :max-native-zoom="16"
      />

      <!-- Botões de zoom no canto inferior direito -->
      <l-control-zoom position="bottomright" />

      <!-- Marcador da Localização do Usuário -->
      <l-marker
        v-if="localizacaoUsuario"
        :lat-lng="localizacaoUsuario"
        :icon="getIconeUsuario()"
      >
        <l-popup>
          <div style="padding: 4px; font-weight: bold; color: #2563eb; font-size: 13px;">
            📍 Você está aqui
          </div>
        </l-popup>
      </l-marker>

      <!-- Marcadores das Instituições no Mapa -->
      <l-marker
        v-for="item in instituicoesFiltradas"
        :key="item.id"
        :lat-lng="[item.localizacao.latitude, item.localizacao.longitude]"
        :icon="getIconeAbrigo(idSelecionado === item.id)"
        :ref="el => registrarMarcador(item.id, el)"
        @click="selecionarInstituicao(item)"
      >
        <l-popup>
          <div style="padding: 4px; min-width: 190px;">
            <div style="font-weight: bold; color: #0f172a; font-size: 14px;">{{ item.nome }}</div>
            <div style="font-size: 12px; color: #64748b; margin-bottom: 4px;">{{ item.localizacao.endereco }}</div>
            <!-- // TODO: Alterar depois, para testes com backend, deixando assim: -->
            <div
              v-if="item.vagas_totais != null && item.vagas_totais > 0 && item.vagas_ocupadas != null"
              style="font-size: 12px; font-weight: 600;"
              :style="{
                color: calcularCorOcupacao(item.vagas_ocupadas / item.vagas_totais)
              }"
            >
              {{ item.vagas_totais - item.vagas_ocupadas }} vagas disponíveis
            </div>

            <div v-else style="font-size: 12px; color: #64748b;">
              Informações de vagas indisponíveis
            </div>

            <button
              @click.stop="abrirInfoAbrigo(item)"
              style="display: block; width: 100%; margin-top: 10px; padding: 8px 0; background: #2563eb; color: white; font-size: 13px; font-weight: 700; border: none; border-radius: 10px; cursor: pointer;"
            >
              Mais informações
            </button>
          </div>
        </l-popup>
      </l-marker>
    </l-map>

    <!-- CABEÇALHO (Sobre / O que é / Contato / Guia) -->
    <Cabecalho/>

    <!-- 2. PAINEL LATERAL ESQUERDO -->
    <div style="position: absolute; top: 80px; left: 16px; z-index: 1000; width: 360px; display: flex; flex-direction: column; gap: 10px; max-height: calc(100vh - 96px);">

      <!-- BARRA VERMELHA (Busca) -->
      <div style="display: flex; align-items: center; gap: 12px; box-sizing: border-box; width: 100%; height: 52px; padding: 0 20px; background: white; border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.12); flex-shrink: 0;">
        <svg width="20" height="20" style="flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="#94a3b8" stroke-width="2.5">
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.35-4.35" />
        </svg>
        <input
          v-model="termoBusca"
          @keyup.enter="registrarPesquisa"
          @blur="registrarPesquisa"
          placeholder="Buscar abrigo, bairro..."
          style="width: 100%; border: none; outline: none; background: transparent; font-size: 14px; font-weight: 500; color: #1e293b;"
        />
      </div>

      <!-- BARRA AZUL (Definir minha localização) -->
      <button
        @click="obterLocalizacaoUsuario"
        style="display: flex; align-items: center; justify-content: flex-start; gap: 12px; box-sizing: border-box; width: 100%; height: 52px; padding: 0 20px; background: white; color: #2563eb; font-weight: 700; font-size: 14px; border: 1px solid #3b82f6; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.12); cursor: pointer; flex-shrink: 0;"
      >
        <svg width="20" height="20" style="flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="#2563eb" stroke-width="2.5">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 2v3m0 14v3M2 12h3m14 0h3" />
        </svg>
        Definir minha localização
      </button>

      <!-- ÁREA VERDE (Lista de abrigos) -->
      <div style="display: flex; flex-direction: column; flex: 1; min-height: 0; background: white; border: 1px solid #e2e8f0; border-radius: 24px; box-shadow: 0 10px 25px rgba(0,0,0,0.15); overflow: hidden;">

        <!-- Cabeçalho da Lista -->
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #f1f5f9; flex-shrink: 0;">
          <span style="font-weight: 700; font-size: 13px; color: #1e293b;">
            {{ localizacaoUsuario ? 'Abrigos mais próximos' : 'Todos os abrigos' }}
          </span>
          <span style="font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 999px; background: #eff6ff; color: #2563eb;">
            {{ instituicoesFiltradas.length }} resultados
          </span>
        </div>

        <!-- Conteúdo com Scroll -->
        <div style="flex: 1; overflow-y: auto;">
          <button
            v-for="item in instituicoesFiltradas"
            :key="item.id"
            @click="selecionarInstituicao(item)"
            :style="{
              display: 'block',
              width: '100%',
              boxSizing: 'border-box',
              padding: '14px',
              textAlign: 'left',
              background: idSelecionado === item.id ? 'rgba(239,246,255,0.7)' : 'white',
              border: 'none',
              borderBottom: '1px solid #f1f5f9',
              borderLeft: idSelecionado === item.id ? '4px solid #2563eb' : '4px solid transparent',
              cursor: 'pointer'
            }"
          >
            <div style="font-size: 13px; font-weight: 800; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ item.nome }}
            </div>

            <div
              style="font-size: 11px; font-weight: 700; margin-top: 2px;"
              :style="{ color: item.status === 'Aberto 24h' || !item.status ? '#059669' : '#d97706' }"
            >
              {{ item.status || 'Aberto 24h' }}
            </div>

            <div style="font-size: 12px; color: #94a3b8; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ item.localizacao.endereco }}
            </div>

            <!-- Distância calculada pelo backend -->
            <div
              v-if="item.distanciaKm != null"
              style="font-size: 11px; font-weight: 800; margin-top: 4px; color: #2563eb;"
            >
              {{ item.distanciaKm }} km de você
            </div>

            <!-- Barra de Vagas: aparece somente quando há dados disponíveis -->
            <div
              v-if="item.vagas_totais != null && item.vagas_totais > 0 && item.vagas_ocupadas != null"
              style="display: flex; align-items: center; gap: 8px; margin-top: 10px;"
            >
              <div style="flex: 1; height: 6px; background: #f1f5f9; border-radius: 999px; overflow: hidden;">
                <div
                  :style="{
                    height: '100%',
                    borderRadius: '999px',
                    width: `${(item.vagas_ocupadas / item.vagas_totais) * 100}%`,
                    backgroundColor: calcularCorOcupacao(item.vagas_ocupadas / item.vagas_totais)
                  }"
                />
              </div>

              <span
                style="font-size: 11px; font-weight: 700; flex-shrink: 0;"
                :style="{
                  color: calcularCorOcupacao(item.vagas_ocupadas / item.vagas_totais)
                }"
              >
                {{ item.vagas_totais - item.vagas_ocupadas }} vagas
              </span>
            </div>

            <div
              v-else
              style="margin-top: 10px; font-size: 11px; color: #64748b;"
            >
              Vagas não informadas
            </div>
          </button>

          <div
            v-if="instituicoesFiltradas.length === 0"
            style="padding: 16px; text-align: center; font-size: 12px; color: #94a3b8;"
          >
            Nenhum abrigo encontrado.
          </div>
        </div>

      </div>
    </div>

    <!-- 3. BOTÃO ENTRAR (Flutuando no canto superior direito, dentro da faixa do cabeçalho) -->
    <div style="position: absolute; top: 0; right: 16px; height: 64px; display: flex; align-items: center; z-index: 1001;">
      <!-- Conta salva: abre as informações do perfil -->
      <button
        v-if="perfilUsuario"
        @click="exibirMinhaConta = true"
        style="display: flex; align-items: center; gap: 10px; height: 48px; padding: 0 16px 0 8px; background: white; border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.12); cursor: pointer;"
      >
        <span style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: #dbeafe; color: #2563eb; font-size: 14px; font-weight: 800; border-radius: 999px;">
          {{ (perfilUsuario.nome || '?').charAt(0).toUpperCase() }}
        </span>
        <span style="max-width: 140px; font-size: 14px; font-weight: 700; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
          {{ perfilUsuario.nome }}
        </span>
      </button>

      <!-- Gestor logado: abre o painel do abrigo -->
      <button
        v-else-if="instituicaoGestor"
        @click="exibirContaGestor = true"
        style="display: flex; align-items: center; gap: 10px; height: 48px; padding: 0 16px 0 8px; background: white; border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.12); cursor: pointer;"
      >
        <span style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: #cffafe; color: #0891b2; font-size: 14px; font-weight: 800; border-radius: 999px;">
          {{ (instituicaoGestor.nome || '?').charAt(0).toUpperCase() }}
        </span>
        <span style="max-width: 140px; font-size: 14px; font-weight: 700; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
          {{ instituicaoGestor.nome }}
        </span>
      </button>

      <!-- Sem conta: botão Entrar -->
      <button
        v-else
        @click="exibirModalRole = true"
        style="display: flex; align-items: center; gap: 6px; padding: 0 20px; height: 44px; background: #2563eb; color: white; font-weight: 700; font-size: 14px; border: none; border-radius: 16px; box-shadow: 0 4px 12px rgba(37,99,235,0.35); cursor: pointer;"
      >
        Entrar →
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { LMap, LTileLayer, LMarker, LPopup, LControlZoom } from '@vue-leaflet/vue-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

import RoleModal from './components/RoleModal.vue'
import ContaUsuario from './components/ContaUsuario.vue'
import ContaGestor from './components/ContaGestor.vue'
import Cabecalho from './components/Cabecalho.vue'
import { atualizarPerfil } from './auth'

// Teste de conexão entre front -> bff -> backend para solicitar lista de abrigos e construir mapa:
import { 
  INSTITUICOES_INIT, calcularCorOcupacao 
} from './data'
import { 
  buscarInstituicoes,
  buscarInstituicoesProximas
} from './services/instituicoesApi.js'

const exibirModalRole = ref(false)
const exibirMinhaConta = ref(false)
const perfilUsuario = ref(null)
const contaUsuarioId = ref(null)
const exibirContaGestor = ref(false)
const contaGestorId = ref(null)
const instituicaoGestorId = ref(null)
const abrigoInfoId = ref(null)
const marcadores = {} // guarda os marcadores do mapa por id (para abrir o popup)
const pesquisasRecentes = ref([])
const abrigosVisitados = ref([])
const instituicoes = ref(INSTITUICOES_INIT)
const termoBusca = ref('')
const idSelecionado = ref(null)
const centroMapa = ref([-23.5475, -46.6361])
const localizacaoUsuario = ref(null)
const mapaRef = ref(null)

const limitesSP = ref([
  [-24.1000, -47.1000],
  [-23.1000, -45.7000]
])

onMounted(async() => {
  try {
    // Solicita ao serviço os abrigos disponibilizados pelo BFF:
    const dados = await buscarInstituicoes()

    // Atualiza a lista reattiva usada pelo mapa e pela busca
    instituicoes.value = dados

    console.log('Abrigos carregados do BFF:', dados)
  } catch(erro) {
    // Mantém os dados de teste caso a requisição falhe:
    console.error("Erro ao carregar abrigos do BFF: ", erro)
    instituicoes.value = [...INSTITUICOES_INIT]
  }
})

const instituicoesFiltradas = computed(() => {
  const q = termoBusca.value.toLowerCase().trim()
  if (!q) return instituicoes.value
  return instituicoes.value.filter(i => 
    i.nome.toLowerCase().includes(q) || 
    i.localizacao.endereco.toLowerCase().includes(q)
  )
})

// Abrigo do gestor que está logado (null se não houver)
const instituicaoGestor = computed(
  () => instituicoes.value.find(i => i.id === instituicaoGestorId.value) ?? null
)

// Abrigo cuja tela de informações (somente leitura) está aberta
const abrigoInfo = computed(
  () => instituicoes.value.find(i => i.id === abrigoInfoId.value) ?? null
)

function getIconeAbrigo(selecionado) {
  const size = selecionado ? 44 : 36
  return L.divIcon({
    html: `<svg width="${size}" height="${size}" viewBox="0 0 46 46">
      <circle cx="23" cy="23" r="20" fill="white" stroke="#334155" stroke-width="3"/>
      <circle cx="23" cy="23" r="${selecionado ? 13 : 10}" fill="#334155"/>
      <circle cx="23" cy="23" r="4" fill="white"/>
    </svg>`,
    className: '',
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2]
  })
}

function getIconeUsuario() {
  return L.divIcon({
    html: `<svg width="32" height="32" viewBox="0 0 32 32">
      <circle cx="16" cy="16" r="14" fill="#3b82f6" fill-opacity="0.25"/>
      <circle cx="16" cy="16" r="8" fill="white" stroke="#2563eb" stroke-width="3"/>
      <circle cx="16" cy="16" r="4" fill="#2563eb"/>
    </svg>`,
    className: '',
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  })
}

function mapaPronto(map) {
  mapaRef.value = map
  nextTick(() => {
    setTimeout(() => {
      map.invalidateSize()
    }, 200)
  })
}

function selecionarInstituicao(item) {
  idSelecionado.value = item.id
  registrarAbrigoVisitado(item)

  const mapa = mapaRef.value
  if (!mapa) return

  // Abre o popup do marcador quando o mapa terminar de se mover
  let aberto = false
  const abrirPopup = () => {
    if (aberto) return
    aberto = true
    marcadores[item.id]?.leafletObject?.openPopup()
  }

  mapa.once('moveend', abrirPopup)
  mapa.flyTo([item.localizacao.latitude, item.localizacao.longitude], 16, { duration: 0.8 })
  setTimeout(abrirPopup, 1200) // garantia, caso o mapa já estivesse no ponto
}

function registrarMarcador(id, el) {
  if (el) marcadores[id] = el
  else delete marcadores[id]
}

// Botão "Mais informações" do popup
function abrirInfoAbrigo(item) {
  mapaRef.value?.closePopup()
  abrigoInfoId.value = item.id
}

async function obterLocalizacaoUsuario() {
  if (!navigator.geolocation) {
    alert('Geolocalização não é suportada pelo seu navegador.')
    return
  }

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      const coords = [pos.coords.latitude, pos.coords.longitude]

      localizacaoUsuario.value = coords
      mapaRef.value?.flyTo(coords, 16, { duration: 0.8 })

      try {
        // Pede ao BFF os a brigos ordenados por proximidade
        const abrigosProximos = await buscarInstituicoesProximas(
          coords[0],
          coords[1]
        )

        // Reaproveita a mesma lista usada pelo mapa e pelo painel lateral
        instituicoes.value = abrigosProximos

        console.log('Abrigos ordenados por proximidade: ', abrigosProximos)
      } catch (erro) {
        console.error('Erro ao buscar abrigos próximos: ', erro)
        alert('Não foi possível ordenar os abrigos pela sua localização.')
      }
    },
    async () => {
      const coordsPadrao = [-23.5458, -46.6521] // Localização da faculdade

      localizacaoUsuario.value = coordsPadrao
      mapaRef.value?.flyTo(coordsPadrao, 16, { duration: 0.8 })

      try {
        const abrigosProximos = await buscarInstituicoesProximas(
          coordsPadrao[0],
          coordsPadrao[1]
        )

        instituicoes.value = abrigosProximos
      } catch (erro) {
        console.error('Erro ao buscar abrigos próximos: ', erro)
      }
    }
  )
}

function definirPapel(role) {
  exibirModalRole.value = false
  if (role === 'usuario') {
    exibirMinhaConta.value = true
    return
  }
  alert(`Papel selecionado: ${role}`)
}

function salvarPerfil(dados) {
  perfilUsuario.value = dados
  if (contaUsuarioId.value !== null) atualizarPerfil(contaUsuarioId.value, dados)
}

function sairDaConta() {
  perfilUsuario.value = null
  contaUsuarioId.value = null
  exibirMinhaConta.value = false
}

// Chamado quando o usuário faz login (novo = false) ou cria a conta (novo = true)
function usuarioAutenticado({ id, perfil, novo }) {
  contaUsuarioId.value = id
  perfilUsuario.value = perfil
  exibirModalRole.value = false
  if (novo) exibirMinhaConta.value = true
}

// Chamado quando o gestor faz login
function gestorAutenticado({ id, instituicaoId }) {
  contaGestorId.value = id
  instituicaoGestorId.value = instituicaoId
  exibirModalRole.value = false
}

// Atualiza qualquer campo do abrigo (perfil, doações, horários de voluntários)
function atualizarInstituicao(id, campos) {
  const i = instituicoes.value.findIndex(x => x.id === id)
  if (i === -1) return
  const atual = instituicoes.value[i]
  instituicoes.value[i] = {
    ...atual,
    ...campos,
    localizacao: { ...atual.localizacao, ...(campos.localizacao || {}) }
  }
}

function sairGestor() {
  contaGestorId.value = null
  instituicaoGestorId.value = null
  exibirContaGestor.value = false
}

function registrarPesquisa() {
  const termo = termoBusca.value.trim()
  if (termo.length < 2) return
  // Remove repetidas, coloca a mais recente no topo e guarda no máximo 5
  pesquisasRecentes.value = [
    { termo, hora: horaAtual() },
    ...pesquisasRecentes.value.filter(p => p.termo.toLowerCase() !== termo.toLowerCase())
  ].slice(0, 5)
}

function registrarAbrigoVisitado(item) {
  // Remove repetidos, coloca o mais recente no topo e guarda no máximo 5
  abrigosVisitados.value = [
    { id: item.id, nome: item.nome, hora: horaAtual() },
    ...abrigosVisitados.value.filter(a => a.id !== item.id)
  ].slice(0, 5)
}

function horaAtual() {
  return new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

function cadastrarInstituicao(dados, contaId) {
  const novoId = Date.now()
  instituicoes.value.push({
    id: novoId,
    ...dados,
    vagas_totais: 20,
    vagas_ocupadas: 0,
    doacoes: [],
    horarios_voluntarios: []
  })

  // Liga a conta do gestor ao abrigo recém-criado e abre o painel dele
  atualizarPerfil(contaId, { instituicaoId: novoId })
  contaGestorId.value = contaId
  instituicaoGestorId.value = novoId
  exibirModalRole.value = false
  exibirContaGestor.value = true
}
</script>