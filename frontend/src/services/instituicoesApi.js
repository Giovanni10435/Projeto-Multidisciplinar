import { fetchComSessao } from './http.js'

// Pega as insituições do banco para serem exibidas no mapa
export async function buscarInstituicoes() {
    const resposta = await fetch('/api/abrigos')

    if (!resposta.ok) {
        throw new Error(`Erro ao buscar abrigos: HTTP ${resposta.status}`)
    }

    const abrigos = await resposta.json()

    // Adapta o JSON da API ao formato usado pelo frontend
    return abrigos.map((abrigo) => ({
        id: abrigo.id,
        nome: abrigo.nome,

        // Verificação
        status: abrigo.statusVerificacao,

        cnpj: abrigo.cnpj,
        telefone: abrigo.telefone,
        email_contato: abrigo.emailContato,
        descricao: abrigo.descricao,

        vagas_totais: null,
        vagas_ocupadas: null,

        localizacao: {
            endereco: abrigo.endereco,
            latitude: Number(abrigo.latitude),
            longitude: Number(abrigo.longitude)
        }
    }))
}

// Pega as instituições próximas do usuário e ordena na barra lateral de lista
export async function buscarInstituicoesProximas(latitude, longitude) {
    const resposta = await fetch(
        `/api/local/abrigos?lat=${encodeURIComponent(latitude)}&lng=${encodeURIComponent(longitude)}`
    )

    if (!resposta.ok) {
        throw new Error(`Erro ao buscar abrigos próximos: HTTP ${resposta.status}`)
    }

    const abrigos = await resposta.json()

    return abrigos.map((abrigo) => ({
        id: abrigo.id,
        nome: abrigo.nome,

        status: abrigo.statusVerificacao,

        vagas_totais: null,
        vagas_ocupadas: null,

        distanciaKm: abrigo.distanciaKm ?? null,

        localizacao: {
            endereco: abrigo.endereco,
            latitude: Number(abrigo.latitude),
            longitude: Number(abrigo.longitude)
        }
    }))
}

// Cadastra abrigo (cria a conta role=abrigo no backend)
export async function cadastrarAbrigo(dados) {
    const resposta = await fetchComSessao('/api/abrigos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    })

    if (!resposta.ok) {
        if (resposta.status === 409) {
            throw new Error('Já existe um abrigo ou conta com esse CNPJ/e-mail.')
        }
        if (resposta.status === 400) {
            throw new Error('Não foi possível cadastrar. Confira os dados informados.')
        }
        throw new Error(`Não foi possível realizar o cadastro (HTTP ${resposta.status})`)
    }

    return resposta.json()
}
