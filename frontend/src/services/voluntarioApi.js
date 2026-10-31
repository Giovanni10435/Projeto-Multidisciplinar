export async function cadastrarVoluntario(dados) {
    const resposta = await fetch('/api/voluntarios', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dados)
    })

    if (!resposta.ok) {
        if (resposta.status == 409) {
            throw new Error('Já existe uma conta com esse e-mail.')
        }

        if (resposta.status == 400) {
            throw new Error('Não foi possível cadastrar. Confita os dados informados.')
        }

        if (resposta.status == 401 || resposta.status == 403) {
            throw new Error('O acesso ao cadastro foi bloqueado pela configuração de segurança.')
        }

        throw new Error(
            `Não foi possível realizar o cadastro (HTTP ${resposta.status})`
        )  
    }
    return resposta.json()
}

export async function loginVoluntario(dados) {
  const resposta = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(dados)
  })

  if (!resposta.ok) {
    if (resposta.status === 401) {
      throw new Error('E-mail ou senha inválidos.')
    }

    if (resposta.status === 403) {
      throw new Error('O acesso foi negado. Verifique a situação da conta.')
    }

    if (resposta.status === 400) {
      throw new Error('Confira o e-mail e a senha informados.')
    }

    throw new Error(
      `Não foi possível realizar o login (HTTP ${resposta.status}).`
    )
  }

  return resposta.json()
}