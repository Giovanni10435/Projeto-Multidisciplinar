// Estrutura alinhada exatamente com os nomes das tabelas instituicao + localizacao
export const INSTITUICOES_INIT = [
  {
    id: 1,
    nome: 'Abrigo Luz da Esperança',
    cnpj: '12.345.678/0001-90',
    descricao: 'Acolhimento noturno e refeições quentes para pessoas em situação de rua.',
    telefone: '(11) 3000-0001',
    email_contato: 'contato@luzdaesperanca.org',
    status_verificacao: 'VERIFICADA',
    localizacao: {
      endereco: 'R. Mauá, 51 - Luz, São Paulo - SP',
      latitude: -23.5365,
      longitude: -46.6338
    },
    // Vagas / Necessidades
    vagas_totais: 30,
    vagas_ocupadas: 12
  },
  {
    id: 2,
    nome: 'Casa Acolhida Sé',
    cnpj: '98.765.432/0001-10',
    descricao: 'Atendimento 24h com apoio psicológico e triagem social.',
    telefone: '(11) 3000-0002',
    email_contato: 'atendimento@acolhidase.org',
    status_verificacao: 'VERIFICADA',
    localizacao: {
      endereco: 'Praça da Sé, 108 - Centro, São Paulo - SP',
      latitude: -23.5505,
      longitude: -46.6333
    },
    vagas_totais: 50,
    vagas_ocupadas: 25
  }
]

export function calcularCorOcupacao(taxa) {
  if (taxa >= 0.9) return '#ef4444' // Vermelho
  if (taxa >= 0.7) return '#f59e0b' // Amarelo/Laranja
  return '#10b981' // Verde
}