<template>
  <div class="w-full h-screen relative overflow-hidden font-sans">

    <!-- Modal de Entrada -->
    <RoleModal 
      v-if="exibirModalRole" 
      @select="definirPapel"
      @submitInstituicao="cadastrarInstituicao" 
    />

    <!-- Mapa Leaflet com Zoom Expandido -->
    <l-map
      :center="centroMapa"
      :zoom="14"
      :max-zoom="20"
      :zoom-control="false"
      class="w-full h-full z-0"
      @ready="mapaPronto"
    >
      <!-- Camada Base do Esri Light Gray -->
      <l-tile-layer
        url="https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}"
        attribution="&copy; Esri"
        :max-zoom="20"
        :max-native-zoom="16"
      />
      <!-- Camada de Rótulos/Textos do Esri -->
      <l-tile-layer
        url="https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer/tile/{z}/{y}/{x}"
        :max-zoom="20"
        :max-native-zoom="16"
      />

      <!-- Marcadores no Mapa -->
      <l-marker
        v-for="item in instituicoesFiltradas"
        :key="item.id"
        :lat-lng="[item.localizacao.latitude, item.localizacao.longitude]"
        :icon="getIconeAbrigo(idSelecionado === item.id)"
        @click="idSelecionado = item.id"
      >
        <l-popup>
          <div class="p-1 font-sans">
            <div class="font-bold text-slate-900">{{ item.nome }}</div>
            <div class="text-xs text-slate-500 mb-1">{{ item.localizacao.endereco }}</div>
            <div class="text-xs font-semibold" :style="{ color: calcularCorOcupacao(item.vagas_ocupadas / item.vagas_totais) }">
              {{ item.vagas_totais - item.vagas_ocupadas }} vagas disponíveis
            </div>
          </div>
        </l-popup>
      </l-marker>
    </l-map>

    <!-- Sidebar de Busca -->
    <div class="absolute top-4 left-4 z-[500] flex flex-col gap-3 w-80 max-h-[calc(100vh-32px)]">
      
      <div class="flex items-center gap-2 px-4 py-3 rounded-2xl bg-white shadow-md border border-slate-200">
        <svg class="w-5 h-5 stroke-slate-400" fill="none" viewBox="0 0 24 24" stroke-width="2">
          <circle cx="11" cy="11" r="8" /><path d="m21 21-4.35-4.35" />
        </svg>
        <input
          v-model="termoBusca"
          placeholder="Buscar abrigo, bairro..."
          class="w-full bg-transparent outline-none text-slate-800 text-sm placeholder:text-slate-400"
        />
      </div>

      <div class="bg-white rounded-2xl shadow-md border border-slate-200 overflow-hidden flex flex-col max-h-[calc(100vh-120px)]">
        <div class="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
          <span class="text-xs font-bold text-slate-800 uppercase tracking-wider">🏠 Todos os abrigos</span>
          <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-600">
            {{ instituicoesFiltradas.length }} resultados
          </span>
        </div>

        <div class="overflow-y-auto">
          <button
            v-for="item in instituicoesFiltradas"
            :key="item.id"
            class="w-full px-4 py-3 text-left border-b border-slate-100 flex gap-3 items-start hover:bg-slate-50 transition-colors cursor-pointer"
            :class="{ 'bg-blue-50/60 border-l-4 border-l-blue-600': idSelecionado === item.id }"
            @click="selecionarInstituicao(item)"
          >
            <div class="w-9 h-9 rounded-xl bg-cyan-50 border border-cyan-500 flex items-center justify-center text-base shrink-0">
              🏠
            </div>
            <div class="flex-1 min-w-0">
              <div class="text-sm font-bold text-slate-900 truncate">{{ item.nome }}</div>
              <div class="text-xs text-slate-500 truncate mt-0.5">{{ item.localizacao.endereco }}</div>
              
              <!-- Barra de Vagas -->
              <div class="mt-2 flex items-center gap-2">
                <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
                  <div 
                    class="h-full rounded-full"
                    :style="{ 
                      width: `${(item.vagas_ocupadas / item.vagas_totais) * 100}%`, 
                      backgroundColor: calcularCorOcupacao(item.vagas_ocupadas / item.vagas_totais) 
                    }"
                  />
                </div>
                <span class="text-xs font-semibold shrink-0" :style="{ color: calcularCorOcupacao(item.vagas_ocupadas / item.vagas_totais) }">
                  {{ item.vagas_totais - item.vagas_ocupadas }} vagas
                </span>
              </div>
            </div>
          </button>
        </div>
      </div>

    </div>

    <!-- Botão Entrar -->
    <div class="absolute top-4 right-4 z-[500]">
      <button 
        @click="exibirModalRole = true"
        class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-bold text-sm shadow-lg transition-colors cursor-pointer"
      >
        Entrar →
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { LMap, LTileLayer, LMarker, LPopup } from '@vue-leaflet/vue-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

import RoleModal from './components/RoleModal.vue'
import { INSTITUICOES_INIT, calcularCorOcupacao } from './data'

const exibirModalRole = ref(false)
const instituicoes = ref(INSTITUICOES_INIT)
const termoBusca = ref('')
const idSelecionado = ref(null)
const centroMapa = ref([-23.5475, -46.6361])
const mapaRef = ref(null)

onMounted(() => {
  console.warn('BFF ainda não conectado. Usando dados locais do data.js')
})

const instituicoesFiltradas = computed(() => {
  const q = termoBusca.value.toLowerCase().trim()
  if (!q) return instituicoes.value
  return instituicoes.value.filter(i => 
    i.nome.toLowerCase().includes(q) || 
    i.localizacao.endereco.toLowerCase().includes(q)
  )
})

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
  mapaRef.value?.flyTo([item.localizacao.latitude, item.localizacao.longitude], 18, { duration: 0.8 })
}

function definirPapel(role) {
  exibirModalRole.value = false
  alert(`Papel selecionado: ${role}`)
}

async function cadastrarInstituicao(dados) {
  instituicoes.value.push({
    id: Date.now(),
    ...dados,
    vagas_totais: 20,
    vagas_ocupadas: 0
  })
  exibirModalRole.value = false
}
</script>