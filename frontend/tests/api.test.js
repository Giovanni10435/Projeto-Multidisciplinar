import { test, beforeEach, afterEach } from 'node:test'
import assert from 'node:assert/strict'
import { fetchComSessao, limparCsrf, requisicao } from '../src/services/http.js'
import { loginVoluntario, cadastrarVoluntario, consultarSessao, sairDaSessao } from '../src/services/voluntarioApi.js'
import * as participacoes from '../src/services/participacoesApi.js'
import { criarNecessidade, atualizarNecessidade, removerNecessidade } from '../src/services/necessidadesApi.js'

const fetchOriginal = globalThis.fetch
let chamadas
let respostas
const json = (body, status = 200) => new Response(JSON.stringify(body), {
  status, headers: { 'Content-Type': 'application/json' }
})
const csrf = token => json({ headerName: 'X-CSRF-TOKEN', token })

beforeEach(() => {
  limparCsrf()
  chamadas = []
  respostas = []
  globalThis.fetch = async (url, opcoes = {}) => {
    chamadas.push({ url, opcoes })
    assert.ok(respostas.length, `Requisição inesperada: ${url}`)
    return respostas.shift()
  }
})
afterEach(() => { globalThis.fetch = fetchOriginal })

const header = (indice, nome = 'X-CSRF-TOKEN') => new Headers(chamadas[indice].opcoes.headers).get(nome)

test('consultas públicas não precisam iniciar CSRF e mantêm cookies same-origin', async () => {
  respostas.push(json([]))
  await participacoes.listarHorarios(7)
  assert.equal(chamadas.length, 1)
  assert.equal(chamadas[0].url, '/api/instituicoes/7/horarios-voluntariado')
  assert.equal(chamadas[0].opcoes.credentials, 'same-origin')
})

test('escrita obtém CSRF antes do POST e preserva os dados', async () => {
  respostas.push(csrf('token-1'), json({ id: 5 }, 201))
  await criarNecessidade(7, { descricao: 'Arroz', quantidadeSolicitada: 10 })
  assert.equal(chamadas[0].url, '/api/auth/csrf')
  assert.equal(header(1), 'token-1')
  assert.deepEqual(JSON.parse(chamadas[1].opcoes.body), { descricao: 'Arroz', quantidadeSolicitada: 10 })
})

test('requisições simultâneas compartilham a obtenção do CSRF', async () => {
  respostas.push(csrf('token-1'), json({ id: 1 }), json({ id: 2 }))
  await Promise.all([
    participacoes.realizarDoacao({ voluntarioId: 1, necessidadeId: 7, quantidade: 1 }),
    participacoes.candidatarVoluntario(7, { voluntarioId: 1, horarioId: 2 })
  ])
  assert.equal(chamadas.filter(c => c.url === '/api/auth/csrf').length, 1)
  assert.equal(header(1), 'token-1')
  assert.equal(header(2), 'token-1')
})

test('login renova o token usado na operação seguinte', async () => {
  respostas.push(csrf('antes'), json({ id: 1, role: 'abrigo', instituicaoId: 7 }),
    csrf('depois'), json({ id: 9 }, 201))
  await loginVoluntario({ email: 'gestor@exemplo.com', senha: 'Ab;c123' })
  await participacoes.criarHorario(7, { dia: 'Sábado', funcao: 'Cozinha', inicio: '08:00', fim: '12:00' })
  assert.equal(header(1), 'antes')
  assert.equal(chamadas[2].url, '/api/auth/csrf')
  assert.equal(header(3), 'depois')
  assert.equal(JSON.parse(chamadas[1].opcoes.body).senha, 'Ab;c123')
})

test('logout sem corpo renova CSRF antes do próximo cadastro', async () => {
  respostas.push(csrf('antes'), new Response(null, { status: 204 }), csrf('depois'), json({ id: 3 }, 201))
  assert.equal(await sairDaSessao(), null)
  await cadastrarVoluntario({ nome: 'Ana', email: 'ana@exemplo.com', senha: 'senha123' })
  assert.equal(header(3), 'depois')
})

test('401 ao consultar a sessão significa visitante, sem erro de login obrigatório', async () => {
  respostas.push(json({ status: 401 }, 401))
  assert.equal(await consultarSessao(), null)
})

test('409 preserva o motivo do backend e não repete a doação', async () => {
  respostas.push(csrf('token'), json({ detail: 'Saldo indisponível' }, 409))
  await assert.rejects(participacoes.realizarDoacao({ voluntarioId: 1, necessidadeId: 7, quantidade: 8 }),
    erro => erro.status === 409 && erro.message === 'Saldo indisponível')
  assert.equal(chamadas.length, 2)
})

test('403 invalida token, sem repetir automaticamente uma escrita', async () => {
  respostas.push(csrf('velho'), json({ status: 403 }, 403), csrf('novo'), json({ id: 2 }))
  await assert.rejects(requisicao('/api/doacoes', { method: 'POST', body: '{}' }), erro => erro.status === 403)
  assert.equal(chamadas.length, 2)
  await requisicao('/api/doacoes', { method: 'POST', body: '{}' })
  assert.equal(header(3), 'novo')
})

test('falha ao obter CSRF não envia a escrita e permite nova tentativa', async () => {
  respostas.push(json({ status: 503 }, 503))
  await assert.rejects(fetchComSessao('/api/doacoes', { method: 'POST' }))
  assert.equal(chamadas.length, 1)
  respostas.push(csrf('recuperado'), json({ id: 1 }))
  await participacoes.realizarDoacao({ voluntarioId: 1, necessidadeId: 7, quantidade: 1 })
  assert.equal(header(2), 'recuperado')
})

test('edição e remoção de necessidades usam as rotas existentes e tratam 204', async () => {
  respostas.push(csrf('token'), json({ id: 4 }), new Response(null, { status: 204 }))
  await atualizarNecessidade(7, 4, { quantidadeSolicitada: 20 })
  assert.equal(await removerNecessidade(7, 4), null)
  assert.equal(chamadas[1].url, '/api/instituicoes/7/necessidades/4')
  assert.equal(chamadas[1].opcoes.method, 'PUT')
  assert.equal(chamadas[2].opcoes.method, 'DELETE')
})

test('edição e remoção de horários usam o ID persistido', async () => {
  respostas.push(csrf('token'), json({ id: 8 }), new Response(null, { status: 204 }))
  await participacoes.atualizarHorario(7, 8, { dia: 'Sábado', funcao: 'Recepção', inicio: '09:00', fim: '12:00' })
  assert.equal(await participacoes.removerHorario(7, 8), null)
  assert.equal(chamadas[1].url, '/api/instituicoes/7/horarios-voluntariado/8')
  assert.equal(chamadas[2].opcoes.method, 'DELETE')
})

test('gestão de solicitações envia tipo e status sem confundir IDs', async () => {
  respostas.push(json([{ id: 2, tipo: 'DOACAO' }]), csrf('token'), json({ id: 2, status: 'ENTREGUE' }))
  await participacoes.listarSolicitacoes(7)
  await participacoes.atualizarSolicitacao(7, 2, { tipo: 'DOACAO', status: 'ENTREGUE' })
  assert.equal(chamadas[2].url, '/api/instituicoes/7/solicitacoes/2')
  assert.deepEqual(JSON.parse(chamadas[2].opcoes.body), { tipo: 'DOACAO', status: 'ENTREGUE' })
})
