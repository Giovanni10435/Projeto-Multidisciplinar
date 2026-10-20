import { reactive } from 'vue'

// Autentificador provisório, depois vai ser trocado pelo Banco de Dados
// Quando fizer a conexões com o back, as funções abaixo viram chamadas à API.
const contas = reactive([])
let proximoId = 1

function normalizar(email) {
  return email.trim().toLowerCase()
}

export function cadastrarConta({ tipo, email, senha, perfil = {} }) {
  const emailNorm = normalizar(email)

  if (contas.some(c => c.email === emailNorm)) {
    return { ok: false, erro: 'Já existe uma conta com esse e-mail. Use a aba Entrar.' }
  }

  const conta = { id: proximoId++, tipo, email: emailNorm, senha, perfil }
  contas.push(conta)
  return { ok: true, conta }
}

export function entrarNaConta({ tipo, email, senha }) {
  const emailNorm = normalizar(email)
  const conta = contas.find(c => c.email === emailNorm && c.tipo === tipo)

  if (!conta || conta.senha !== senha) {
    return { ok: false, erro: 'E-mail ou senha incorretos. Ainda não tem conta? Use a aba Cadastrar.' }
  }
  return { ok: true, conta }
}

export function atualizarPerfil(id, perfil) {
  const conta = contas.find(c => c.id === id)
  if (conta) conta.perfil = perfil
}