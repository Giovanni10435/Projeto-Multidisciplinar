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
      <div style="display: flex; flex-wrap: wrap; gap: 8px; padding: 12px 24px 0; border-bottom: 1px solid #e2e8f0;">
        <button type="button" @click="aba = 'perfil'" :style="estiloAba(aba === 'perfil')">🏠 Perfil</button>
        <button type="button" @click="aba = 'doacoes'" :style="estiloAba(aba === 'doacoes')">📦 Necessidades</button>
        <button type="button" @click="aba = 'voluntarios'" :style="estiloAba(aba === 'voluntarios')">🤝 Voluntários</button>
        <button v-if="!somenteLeitura" type="button" @click="aba = 'solicitacoes'; carregarSolicitacoes()" :style="estiloAba(aba === 'solicitacoes')">Solicitações</button>
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

        <!-- ================= ABA NECESSIDADES ================= -->
        <div v-else-if="aba === 'doacoes'">
          <p style="margin: 0 0 18px; font-size: 14px; color: #475569; line-height: 1.5;">
            {{ somenteLeitura
              ? 'Estes são os itens que o abrigo está precisando no momento.'
              : 'Liste os itens que seu abrigo precisa. Os usuários e voluntários verão esses pedidos.' }}
          </p>

          <form
            v-if="!somenteLeitura"
            @submit.prevent="adicionarNecessidade"
            style="padding: 18px; margin-bottom: 18px; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 16px;"
          >
            <div style="margin-bottom: 14px; font-size: 14px; font-weight: 800; color: #166534;">{{ necessidadeEmEdicao ? 'Editar necessidade' : '➕ Adicionar necessidade' }}</div>

            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 14px; margin-bottom: 14px;">
              <div>
                <label :style="estiloLabel">Item necessário</label>
                <input v-model="novaNec.descricao" required type="text" placeholder="Ex: Cobertores" :style="estiloInput" />
              </div>
              <div>
                <label :style="estiloLabel">Quantidade</label>
                <input v-model.number="novaNec.quantidade" required type="number" min="1" :style="estiloInput" />
              </div>
            </div>

            <div style="display: flex; justify-content: flex-end;">
              <button
                type="submit"
                :disabled="salvandoNec"
                style="padding: 11px 24px; background: #15803d; color: white; font-size: 14px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
              >
                {{ salvandoNec ? 'Salvando...' : (necessidadeEmEdicao ? 'Salvar alterações' : 'Adicionar') }}
              </button>
              <button v-if="necessidadeEmEdicao" type="button" @click="cancelarEdicaoNecessidade" :style="estiloAcao">Cancelar</button>
            </div>
          </form>

          <p v-if="erroNec" :style="{ ...estiloErroObj, marginBottom: '12px' }">{{ erroNec }}</p>
          <p v-if="carregandoNec" style="margin: 0; font-size: 14px; color: #94a3b8;">Carregando...</p>

          <div style="display: flex; flex-direction: column; gap: 10px;">
            <div
              v-for="n in necessidadesVisiveis"
              :key="n.id"
              style="padding: 14px 16px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;"
            >
              <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px;">
                <div style="display: flex; align-items: center; flex-wrap: wrap; gap: 8px; min-width: 0; font-size: 14px;">
                  <span style="font-weight: 800; color: #0f172a;">{{ n.descricao }}</span>
                  <span style="color: #64748b;">· {{ n.quantidadeRecebida }} de {{ n.quantidadeSolicitada }}</span>
                  <span
                    v-if="n.status !== 'ABERTA'"
                    style="padding: 3px 9px; font-size: 11px; font-weight: 800; color: #166534; background: #dcfce7; border-radius: 999px;"
                  >
                    {{ n.status }}
                  </span>
                </div>
                <button v-if="!somenteLeitura" type="button" @click="editarNecessidade(n)" :style="estiloAcao">Editar</button>
                <button v-if="!somenteLeitura && n.status === 'ABERTA'" type="button" @click="encerrarNecessidade(n)" :style="estiloAcao">Encerrar</button>
                <button v-if="!somenteLeitura" type="button" @click="excluirNecessidade(n.id)" aria-label="Remover necessidade" :style="estiloBotaoRemover">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="3" stroke-linecap="round">
                    <path d="M6 6l12 12M18 6L6 18" />
                  </svg>
                </button>
              </div>

              <div style="height: 8px; margin-top: 10px; background: #e2e8f0; border-radius: 999px; overflow: hidden;">
                <div :style="{ height: '100%', width: porcentagemNec(n) + '%', background: '#10b981', borderRadius: '999px' }"></div>
              </div>

              <form v-if="somenteLeitura && n.status === 'ABERTA'" @submit.prevent="doar(n)" style="display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-top: 12px;">
                <label :for="'doacao-' + n.id">Quantidade a doar:</label>
                <input :id="'doacao-' + n.id" type="number" min="1" required :disabled="!voluntarioId"
                  :value="quantidadesDoacao[n.id] ?? 1" @input="quantidadesDoacao[n.id] = $event.target.value"
                  style="width: 70px; padding: 8px;" />
                <button type="submit" :disabled="enviandoDoacao !== null" :style="estiloAcao">
                  {{ voluntarioId ? (enviandoDoacao === n.id ? 'Enviando...' : 'Quero doar') : 'Entrar para doar' }}
                </button>
              </form>
              <p v-if="mensagensDoacao[n.id]" role="status" style="font-size: 13px;">{{ mensagensDoacao[n.id] }}</p>

              <div v-if="!somenteLeitura" style="display: flex; align-items: center; gap: 8px; margin-top: 10px; font-size: 13px; color: #475569;">
                Recebido:
                <input
                  type="number"
                  min="0"
                  :value="n.quantidadeRecebida"
                  @change="salvarRecebida(n, $event.target.value)"
                  style="width: 80px; padding: 6px 8px; font-size: 13px; border: 1px solid #cbd5e1; border-radius: 8px;"
                />
              </div>
            </div>

            <p v-if="!carregandoNec && !necessidadesVisiveis.length" style="margin: 0; font-size: 14px; color: #94a3b8;">
              {{ somenteLeitura ? 'Este abrigo ainda não cadastrou necessidades.' : 'Nenhuma necessidade cadastrada ainda.' }}
            </p>
          </div>
        </div>

        <!-- ================= ABA VOLUNTÁRIOS ================= -->
        <div v-else-if="aba === 'voluntarios'">
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
            <div style="margin-bottom: 14px; font-size: 14px; font-weight: 800; color: #1e40af;">{{ horarioEmEdicao ? 'Editar horário' : '➕ Adicionar horário' }}</div>

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
                :disabled="salvandoHorario"
                style="padding: 11px 24px; background: #1d4ed8; color: white; font-size: 14px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;"
              >
                {{ salvandoHorario ? 'Salvando...' : (horarioEmEdicao ? 'Salvar horário' : 'Adicionar') }}
              </button>
              <button v-if="horarioEmEdicao" type="button" @click="cancelarEdicaoHorario" :style="estiloAcao">Cancelar</button>
            </div>
          </form>

          <div v-if="somenteLeitura && voluntarioId" style="display: grid; gap: 10px; margin-bottom: 16px;">
            <label>Telefone (opcional)<input v-model="contatoVoluntario.telefone" maxlength="20" type="tel" :style="estiloInput" /></label>
            <label>Disponibilidade (opcional)<input v-model="contatoVoluntario.disponibilidade" maxlength="255" :style="estiloInput" /></label>
          </div>
          <p v-if="erroHorario && somenteLeitura" role="alert" :style="estiloErroObj">{{ erroHorario }}</p>
          <p v-if="carregandoHorarios" role="status">Carregando horários...</p>
          <p v-if="mensagemVoluntariado" role="status">{{ mensagemVoluntariado }}</p>

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
                @click="candidatarHorario(h)"
                :disabled="enviandoCandidatura !== null"
                style="padding: 8px 14px; background: #1d4ed8; color: white; font-size: 13px; font-weight: 700; border: none; border-radius: 10px; cursor: pointer;"
              >
                {{ enviandoCandidatura === h.id ? 'Enviando...' : (voluntarioId ? 'Quero ser voluntário' : 'Entrar para se candidatar') }}
              </button>

              <button v-if="!somenteLeitura" type="button" @click="editarHorario(h)" :style="estiloAcao">Editar</button>
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

        <div v-else-if="aba === 'solicitacoes' && !somenteLeitura">
          <p>Gerencie as doações e candidaturas recebidas pelo seu abrigo.</p>
          <button type="button" @click="carregarSolicitacoes" :style="estiloAcao">Atualizar lista</button>
          <p v-if="erroSolicitacoes" role="alert" :style="estiloErroObj">{{ erroSolicitacoes }}</p>
          <p v-if="carregandoSolicitacoes" role="status">Carregando solicitações...</p>
          <p v-if="!carregandoSolicitacoes && !solicitacoes.length">Nenhuma solicitação recebida.</p>
          <article v-for="item in solicitacoes" :key="item.tipo + '-' + item.id" :style="estiloCard" style="margin-top: 12px;">
            <strong>{{ item.tipo === 'DOACAO' ? 'Doação' : 'Voluntariado' }} — {{ item.voluntarioNome }}</strong>
            <p>Status: {{ item.status }} · {{ item.data }}</p>
            <p v-if="item.tipo === 'DOACAO'">{{ item.quantidade }} × {{ item.necessidadeDescricao }}</p>
            <p v-if="item.horarioId">Horário escolhido: {{ descricaoHorario(item.horarioId) }}</p>
            <p v-if="item.voluntarioTelefone">Telefone: {{ item.voluntarioTelefone }}</p>
            <p v-if="item.voluntarioDisponibilidade">Disponibilidade: {{ item.voluntarioDisponibilidade }}</p>
            <div style="display: flex; flex-wrap: wrap; gap: 8px;">
              <button v-for="acao in acoesSolicitacao(item)" :key="acao.status" type="button"
                :disabled="atualizandoSolicitacao !== null" @click="alterarSolicitacao(item, acao.status)" :style="estiloAcao">{{ acao.texto }}</button>
            </div>
          </article>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { calcularCorOcupacao } from '../data'
import {
  listarNecessidades,
  criarNecessidade,
  atualizarNecessidade,
  removerNecessidade
} from '../services/necessidadesApi'
import * as participacoes from '../services/participacoesApi.js'

const props = defineProps({
  instituicao: { type: Object, required: true },
  // true = visitante: só visualiza (sem editar, sem remover, sem sair)
  somenteLeitura: { type: Boolean, default: false },
  voluntarioId: { type: Number, default: null }
})

const emit = defineEmits(['close', 'atualizar', 'sair', 'voluntariar', 'solicitarLogin'])

const aba = ref('perfil')
const modoPerfil = ref('ver')
const erro = ref('')
const erroHorario = ref('')
const estiloAcao = 'padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; background: white; color: #1d4ed8; cursor: pointer;'

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

/* ---------- Necessidades (backend) ---------- */
const necessidades = ref([])
const carregandoNec = ref(false)
const erroNec = ref('')
const novaNec = reactive({ descricao: '', quantidade: 1 })
const necessidadeEmEdicao = ref(null)
const salvandoNec = ref(false)

// Visitante só vê as abertas; o gestor vê todas
const necessidadesVisiveis = computed(() =>
  props.somenteLeitura
    ? necessidades.value.filter(n => n.status === 'ABERTA')
    : necessidades.value
)

function porcentagemNec(n) {
  if (!n.quantidadeSolicitada) return 0
  return Math.min(100, Math.round((n.quantidadeRecebida / n.quantidadeSolicitada) * 100))
}

async function carregarNecessidades() {
  carregandoNec.value = true
  erroNec.value = ''
  try {
    necessidades.value = await listarNecessidades(props.instituicao.id)
  } catch (e) {
    erroNec.value = e.message || 'Não foi possível carregar as necessidades.'
    console.error(e)
  } finally {
    carregandoNec.value = false
  }
}

async function adicionarNecessidade() {
  if (props.somenteLeitura || salvandoNec.value) return
  erroNec.value = ''
  salvandoNec.value = true
  try {
    const dados = { descricao: novaNec.descricao.trim(), quantidadeSolicitada: Number(novaNec.quantidade) }
    if (necessidadeEmEdicao.value) {
      await atualizarNecessidade(props.instituicao.id, necessidadeEmEdicao.value, dados)
    } else {
      await criarNecessidade(props.instituicao.id, dados)
    }
    cancelarEdicaoNecessidade()
    await carregarNecessidades()
  } catch (e) {
    erroNec.value = e.message || 'Não foi possível adicionar a necessidade.'
    console.error(e)
  } finally {
    salvandoNec.value = false
  }
}

async function salvarRecebida(n, valor) {
  if (props.somenteLeitura) return
  erroNec.value = ''
  const recebida = Number(valor)
  if (!Number.isInteger(recebida) || recebida < 0) {
    erroNec.value = 'A quantidade recebida precisa ser um número inteiro, de zero para cima.'
    await carregarNecessidades()
    return
  }
  try {
    await atualizarNecessidade(props.instituicao.id, n.id, {
      quantidadeRecebida: recebida,
      status: recebida >= n.quantidadeSolicitada ? 'ATENDIDA' : 'ABERTA'
    })
    await carregarNecessidades()
  } catch (e) {
    erroNec.value = e.message || 'Não foi possível atualizar a necessidade.'
    console.error(e)
  }
}

async function excluirNecessidade(id) {
  if (props.somenteLeitura) return
  erroNec.value = ''
  try {
    await removerNecessidade(props.instituicao.id, id)
    await carregarNecessidades()
  } catch (e) {
    erroNec.value = e.message || 'Não foi possível remover a necessidade.'
    console.error(e)
  }
}

function editarNecessidade(n) {
  necessidadeEmEdicao.value = n.id
  Object.assign(novaNec, { descricao: n.descricao, quantidade: n.quantidadeSolicitada })
}

function cancelarEdicaoNecessidade() {
  necessidadeEmEdicao.value = null
  Object.assign(novaNec, { descricao: '', quantidade: 1 })
}

async function encerrarNecessidade(n) {
  if (props.somenteLeitura) return
  try {
    await atualizarNecessidade(props.instituicao.id, n.id, { status: 'ENCERRADA' })
    await carregarNecessidades()
  } catch (e) {
    erroNec.value = e.message
  }
}

const quantidadesDoacao = reactive({})
const mensagensDoacao = reactive({})
const enviandoDoacao = ref(null)
async function doar(n) {
  if (!props.voluntarioId) { emit('solicitarLogin'); return }
  if (enviandoDoacao.value !== null) return
  const quantidade = Number(quantidadesDoacao[n.id] ?? 1)
  if (!Number.isInteger(quantidade) || quantidade < 1) {
    mensagensDoacao[n.id] = 'Informe uma quantidade inteira maior que zero.'
    return
  }
  enviandoDoacao.value = n.id
  mensagensDoacao[n.id] = ''
  try {
    await participacoes.realizarDoacao({ voluntarioId: props.voluntarioId, necessidadeId: n.id, quantidade })
    mensagensDoacao[n.id] = 'Doação registrada! Aguarde a confirmação do abrigo.'
  } catch (e) {
    mensagensDoacao[n.id] = e.message
  } finally {
    enviandoDoacao.value = null
  }
}

/* ---------- Voluntários ---------- */
const DIAS = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo']
const novoHorario = reactive({ dia: 'Segunda-feira', funcao: '', inicio: '08:00', fim: '12:00' })
const horarios = ref([])
const carregandoHorarios = ref(false)
const horarioEmEdicao = ref(null)
const salvandoHorario = ref(false)
const contatoVoluntario = reactive({ telefone: '', disponibilidade: '' })
const enviandoCandidatura = ref(null)
const mensagemVoluntariado = ref('')
// Ordena por dia da semana e depois pelo horário de início
const horariosOrdenados = computed(() =>
  [...horarios.value].sort(
    (a, b) => DIAS.indexOf(a.dia) - DIAS.indexOf(b.dia) || a.inicio.localeCompare(b.inicio)
  )
)

async function carregarHorarios() {
  carregandoHorarios.value = true
  erroHorario.value = ''
  try {
    horarios.value = await participacoes.listarHorarios(props.instituicao.id)
  } catch (e) {
    erroHorario.value = e.message
  } finally {
    carregandoHorarios.value = false
  }
}

function editarHorario(h) {
  if (props.somenteLeitura) return
  horarioEmEdicao.value = h.id
  Object.assign(novoHorario, { dia: h.dia, funcao: h.funcao, inicio: h.inicio.slice(0, 5), fim: h.fim.slice(0, 5) })
}

function cancelarEdicaoHorario() {
  horarioEmEdicao.value = null
  Object.assign(novoHorario, { dia: 'Segunda-feira', funcao: '', inicio: '08:00', fim: '12:00' })
}

async function adicionarHorario() {
  if (props.somenteLeitura || salvandoHorario.value) return
  erroHorario.value = ''
  const funcao = novoHorario.funcao.trim()
  if (!funcao) return
  if (novoHorario.fim <= novoHorario.inicio) {
    erroHorario.value = 'O horário de fim precisa ser depois do início.'
    return
  }
  salvandoHorario.value = true
  try {
    const dados = { ...novoHorario, funcao }
    if (horarioEmEdicao.value) {
      await participacoes.atualizarHorario(props.instituicao.id, horarioEmEdicao.value, dados)
    } else {
      await participacoes.criarHorario(props.instituicao.id, dados)
    }
    cancelarEdicaoHorario()
    await carregarHorarios()
  } catch (e) {
    erroHorario.value = e.message
  } finally {
    salvandoHorario.value = false
  }
}

async function removerHorario(id) {
  if (props.somenteLeitura) return
  try {
    await participacoes.removerHorario(props.instituicao.id, id)
    if (horarioEmEdicao.value === id) cancelarEdicaoHorario()
    await carregarHorarios()
  } catch (e) {
    erroHorario.value = e.message
  }
}

async function candidatarHorario(h) {
  if (!props.voluntarioId) { emit('solicitarLogin'); return }
  if (enviandoCandidatura.value !== null) return
  enviandoCandidatura.value = h.id
  mensagemVoluntariado.value = ''
  try {
    await participacoes.candidatarVoluntario(props.instituicao.id, {
      voluntarioId: props.voluntarioId, horarioId: h.id, ...contatoVoluntario
    })
    mensagemVoluntariado.value = 'Candidatura enviada! Aguarde a resposta do abrigo.'
    emit('voluntariar', { instituicaoId: props.instituicao.id, horario: h })
  } catch (e) {
    mensagemVoluntariado.value = e.message
  } finally {
    enviandoCandidatura.value = null
  }
}

const solicitacoes = ref([])
const carregandoSolicitacoes = ref(false)
const erroSolicitacoes = ref('')
const atualizandoSolicitacao = ref(null)

async function carregarSolicitacoes() {
  if (props.somenteLeitura) return
  carregandoSolicitacoes.value = true
  erroSolicitacoes.value = ''
  try {
    solicitacoes.value = await participacoes.listarSolicitacoes(props.instituicao.id)
  } catch (e) {
    erroSolicitacoes.value = e.message
  } finally {
    carregandoSolicitacoes.value = false
  }
}

function descricaoHorario(id) {
  const horario = horarios.value.find(h => h.id === id)
  return horario
    ? `${horario.dia}, ${horario.inicio.slice(0, 5)} às ${horario.fim.slice(0, 5)} — ${horario.funcao}`
    : 'Este horário foi removido do cadastro.'
}

function acoesSolicitacao(item) {
  const mapas = item.tipo === 'DOACAO' ? {
    PENDENTE: [['CONFIRMADA', 'Confirmar'], ['CANCELADA', 'Recusar']],
    CONFIRMADA: [['ENTREGUE', 'Registrar entrega'], ['CANCELADA', 'Cancelar']]
  } : {
    PENDENTE: [['APROVADO', 'Aprovar'], ['RECUSADO', 'Recusar']],
    APROVADO: [['EM_ANDAMENTO', 'Iniciar'], ['CANCELADO', 'Cancelar']],
    EM_ANDAMENTO: [['FINALIZADO', 'Concluir'], ['CANCELADO', 'Cancelar']]
  }
  return (mapas[item.status] || []).map(([status, texto]) => ({ status, texto }))
}

async function alterarSolicitacao(item, status) {
  if (props.somenteLeitura || atualizandoSolicitacao.value !== null) return
  atualizandoSolicitacao.value = item.tipo + '-' + item.id
  erroSolicitacoes.value = ''
  try {
    await participacoes.atualizarSolicitacao(props.instituicao.id, item.id, { tipo: item.tipo, status })
    await Promise.all([carregarSolicitacoes(), carregarNecessidades()])
  } catch (e) {
    erroSolicitacoes.value = e.message
  } finally {
    atualizandoSolicitacao.value = null
  }
}

watch(() => props.instituicao.id, () => {
  cancelarEdicaoNecessidade()
  cancelarEdicaoHorario()
  mensagemVoluntariado.value = ''
  carregarNecessidades()
  carregarHorarios()
  if (!props.somenteLeitura) carregarSolicitacoes()
}, { immediate: true })
</script>