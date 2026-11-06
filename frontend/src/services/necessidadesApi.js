import { requisicao as chamar } from './http.js'

export function listarNecessidades(instituicaoId) {
  return chamar(`/api/instituicoes/${instituicaoId}/necessidades`)
}

export function criarNecessidade(instituicaoId, { descricao, quantidadeSolicitada }) {
  return chamar(`/api/instituicoes/${instituicaoId}/necessidades`, {
    method: 'POST',
    body: JSON.stringify({ descricao, quantidadeSolicitada })
  })
}

export function atualizarNecessidade(instituicaoId, necessidadeId, campos) {
  return chamar(`/api/instituicoes/${instituicaoId}/necessidades/${necessidadeId}`, {
    method: 'PUT',
    body: JSON.stringify(campos)
  })
}

export function removerNecessidade(instituicaoId, necessidadeId) {
  return chamar(`/api/instituicoes/${instituicaoId}/necessidades/${necessidadeId}`, {
    method: 'DELETE'
  })
}
