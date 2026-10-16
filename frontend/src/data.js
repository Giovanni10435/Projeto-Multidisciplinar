export const INSTITUICOES_INIT = [
  {
    id: 1,
    nome: 'Abrigo Luz da Esperança',
    status: 'Aberto 24h',
    vagas_totais: 50,
    vagas_ocupadas: 32,
    localizacao: {
      endereco: 'R. Mauá, 51 – Luz',
      latitude: -23.5358,
      longitude: -46.6342
    }
  },
  {
    id: 2,
    nome: 'Casa Acolhida Sé',
    status: 'Aberto 24h',
    vagas_totais: 60,
    vagas_ocupadas: 35,
    localizacao: {
      endereco: 'Praça da Sé, 108 – Centro',
      latitude: -23.5505,
      longitude: -46.6333
    }
  },
  {
    id: 3,
    nome: 'Abrigo São Bento',
    status: 'Horário limitado',
    vagas_totais: 40,
    vagas_ocupadas: 28,
    localizacao: {
      endereco: 'Largo de São Bento, 2 – Centro',
      latitude: -23.5438,
      longitude: -46.6345
    }
  },
  {
    id: 4,
    nome: 'Centro de Acolhida Glicério',
    status: 'Aberto 24h',
    vagas_totais: 80,
    vagas_ocupadas: 54,
    localizacao: {
      endereco: 'R. do Glicério, 225 – Liberdade',
      latitude: -23.5552,
      longitude: -46.6281
    }
  },
  {
    id: 5,
    nome: 'Abrigo República',
    status: 'Aberto 24h',
    vagas_totais: 45,
    vagas_ocupadas: 32,
    localizacao: {
      endereco: 'Praça da República, 30 – Centro',
      latitude: -23.5432,
      longitude: -46.6425
    }
  }
]

export function calcularCorOcupacao(taxa) {
  if (taxa >= 0.8) return '#ef4444' // Vermelho
  if (taxa >= 0.5) return '#d97706' // Laranja/Marrom
  return '#10b981' // Verde
}
