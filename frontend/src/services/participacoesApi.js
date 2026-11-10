import { requisicao } from './http.js'

const horarios = id => `/api/instituicoes/${id}/horarios-voluntariado`

export function listarHorarios(id) {
  return requisicao(horarios(id))
}

export function criarHorario(id, dados) {
  return requisicao(horarios(id), { method: 'POST', body: JSON.stringify(dados) })
}

export function atualizarHorario(id, horarioId, dados) {
  return requisicao(`${horarios(id)}/${horarioId}`, { method: 'PUT', body: JSON.stringify(dados) })
}

export function removerHorario(id, horarioId) {
  return requisicao(`${horarios(id)}/${horarioId}`, { method: 'DELETE' })
}

export function candidatarVoluntario(id, dados) {
  return requisicao(`/api/instituicoes/${id}/voluntariados`, { method: 'POST', body: JSON.stringify(dados) })
}

export function realizarDoacao(dados) {
  return requisicao('/api/doacoes', { method: 'POST', body: JSON.stringify(dados) })
}

export function listarSolicitacoes(id) {
  return requisicao(`/api/instituicoes/${id}/solicitacoes`)
}

export function atualizarSolicitacao(id, solicitacaoId, dados) {
  return requisicao(`/api/instituicoes/${id}/solicitacoes/${solicitacaoId}`, {
    method: 'PUT', body: JSON.stringify(dados)
  })
}
