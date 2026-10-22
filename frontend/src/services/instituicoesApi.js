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

        vagas_totais: null,
        vagas_ocupadas: null,

        localizacao: {
            endereco: abrigo.endereco,
            latitude: Number(abrigo.latitude),
            longitude: Number(abrigo.longitude)
        }
    }))
}