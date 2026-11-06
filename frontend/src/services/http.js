let csrfPromise = null

export function limparCsrf() {
  csrfPromise = null
}

function obterCsrf() {
  if (!csrfPromise) {
    const promise = fetch('/api/auth/csrf', { credentials: 'same-origin' })
      .then(async resposta => {
        if (!resposta.ok) throw new Error('Não foi possível iniciar uma sessão. Tente novamente.')
        const dados = await resposta.json()
        if (!dados.token || !dados.headerName) throw new Error('Não foi possível validar a sessão.')
        return dados
      })
      .catch(erro => {
        if (csrfPromise === promise) csrfPromise = null
        throw erro
      })
    csrfPromise = promise
  }
  return csrfPromise
}

// Cookie HttpOnly e enviado pelo navegador; o token fica somente na memoria da pagina.
export async function fetchComSessao(url, opcoes = {}) {
  const metodo = (opcoes.method || 'GET').toUpperCase()
  const headers = new Headers(opcoes.headers)
  if (opcoes.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (!['GET', 'HEAD', 'OPTIONS'].includes(metodo)) {
    const csrf = await obterCsrf()
    headers.set(csrf.headerName, csrf.token)
  }
  const resposta = await fetch(url, { ...opcoes, credentials: 'same-origin', headers })
  if ((resposta.ok && ['/api/auth/login', '/api/auth/logout'].includes(url)) || resposta.status === 403) {
    limparCsrf()
  }
  if (resposta.status === 401 && !['/api/auth/login', '/api/auth/me'].includes(url)
      && typeof window !== 'undefined') {
    window.dispatchEvent(new Event('sessao-expirada'))
  }
  return resposta
}

export async function requisicao(url, opcoes = {}) {
  const resposta = await fetchComSessao(url, opcoes)
  const texto = await resposta.text()
  let dados = null
  if (texto) {
    try { dados = JSON.parse(texto) } catch { dados = texto }
  }
  if (!resposta.ok) {
    const mensagens = {
      401: 'Sua sessão expirou. Entre novamente.',
      403: 'Não foi possível autorizar a operação. Verifique sua conta e entre novamente.',
      404: 'O registro não foi encontrado. Atualize as informações.',
      409: 'A operação conflita com o estado atual. Verifique a disponibilidade e se já existe uma solicitação.',
      400: 'Confira os campos informados.'
    }
    const erro = new Error(dados?.detail || dados?.message || mensagens[resposta.status]
      || `Não foi possível concluir a operação (HTTP ${resposta.status}).`)
    erro.status = resposta.status
    throw erro
  }
  return dados
}
