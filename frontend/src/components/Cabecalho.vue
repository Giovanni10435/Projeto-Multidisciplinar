<template>
  <!-- Cabeçalho no topo da página -->
  <header class="cabecalho">
    <!-- Logo AcolheSP: A em formato de casa + SP -->
    <a
      href="/"
      class="cabecalho-logo"
      aria-label="AcolheSP — página inicial"
      title="AcolheSP"
    >
      <svg
        width="112"
        height="44"
        viewBox="0 0 112 44"
        fill="none"
        aria-hidden="true"
        focusable="false"
      >
        <!-- A minimalista com formato de telhado -->
        <path
          d="M5 36L21 8L37 36"
          stroke="currentColor"
          stroke-width="4.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />

        <!-- Travessa do A com abertura central remetendo a uma porta -->
        <path
          d="M11 26H17V36M25 36V26H31"
          stroke="currentColor"
          stroke-width="3.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />

        <!-- SP -->
        <text
          x="46"
          y="35"
          fill="currentColor"
          font-family="Arial, Helvetica, sans-serif"
          font-size="34"
          font-weight="800"
          letter-spacing="-2"
        >
          SP
        </text>
      </svg>
    </a>

    <!-- Seções posicionadas à direita da logo -->
    <nav class="cabecalho-secoes" aria-label="Informações sobre o AcolheSP">
      <button
        v-for="secao in secoes"
        :key="secao.id"
        type="button"
        class="cabecalho-botao"
        @click="secaoAbertaId = secao.id"
      >
        {{ secao.titulo }}
      </button>
    </nav>
  </header>

  <!-- Janela pequena da seção escolhida (clicar fora também fecha) -->
  <div
    v-if="secaoAberta"
    style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 16px; box-sizing: border-box; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); overflow-y: auto;"
    @click.self="secaoAbertaId = null"
  >
    <div
      style="box-sizing: border-box; width: 440px; max-width: 100%; margin: auto; background: white; border: 1px solid #e2e8f0; border-radius: 20px; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25); font-family: sans-serif; overflow: hidden;"
    >
      <!-- Título + X -->
      <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 20px 24px; border-bottom: 1px solid #e2e8f0;">
        <div style="display: flex; align-items: center; gap: 12px; min-width: 0;">
          <span style="font-size: 26px; line-height: 1;">{{ secaoAberta.icone }}</span>
          <h2 style="margin: 0; font-size: 20px; font-weight: 800; color: #0f172a;">{{ secaoAberta.titulo }}</h2>
        </div>
        <button
          type="button"
          @click="secaoAbertaId = null"
          aria-label="Fechar"
          style="width: 40px; height: 40px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #f1f5f9; border: none; border-radius: 999px; cursor: pointer;"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2.5" stroke-linecap="round">
            <path d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      </div>

      <!-- Conteúdo -->
      <div style="padding: 24px; font-size: 14px; line-height: 1.6; color: #475569;">
        <p
          v-for="(paragrafo, i) in secaoAberta.conteudo"
          :key="i"
          style="margin: 0 0 12px;"
        >
          <template v-if="paragrafo.startsWith('http')">
            <a
              :href="paragrafo"
              target="_blank"
              rel="noopener noreferrer"
              style="color: #2563eb; font-weight: 700; text-decoration: underline; word-break: break-word;"
            >
              {{ paragrafo }}
            </a>
          </template>

          <template v-else>
            {{ paragrafo }}
          </template>
        </p>

        <p v-if="!secaoAberta.conteudo.length" style="margin: 0; color: #94a3b8;">
          Nenhuma informação disponível.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Para preencher depois: cada texto da lista "conteudo" vira um parágrafo na janela.
const secoes = [
  {
    id: 'sobre',
    icone: '📖',
    titulo: 'Sobre o Projeto',
    conteudo: [
      'O AcolheSP é um projeto acadêmico desenvolvido por estudantes da Universidade Presbiteriana Mackenzie.',
      'Seu objetivo é facilitar o acesso a informações sobre abrigos e serviços de assistência social na cidade de São Paulo.',
      'A plataforma permite visualizar instituições em um mapa interativo, encontrar locais próximos e apoiar abrigos por meio de doações e voluntariado.',
      'O projeto utiliza tecnologia para contribuir com pessoas em situação de vulnerabilidade social.'
    ]
  },
  {
    id: 'o-que-e',
    icone: '🏡',
    titulo: 'O que é o AcolheSP?',
    conteudo: [
      'O AcolheSP é uma plataforma que permite localizar instituições de assistência social.',
      'Por meio de um mapa interativo, o usuário pode visualizar abrigos e outros pontos de apoio.',
      'A plataforma também permite identificar instituições próximas de uma determinada localização e listar estas instituições de mais próximo até mais distante.'
    ]
  },
  {
    id: 'contato',
    icone: '✉️',
    titulo: 'Informações de Contato',
    conteudo: [
      'Para obter mais informações sobre o projeto, entre em contato com nossa equipe.',
      'GitHub: https://github.com/JoaoVitorMintz',
      'LinkedIn: https://www.linkedin.com/in/joaomintz/',
      'Projeto acadêmico desenvolvido na Universidade Presbiteriana Mackenzie.'
    ]
  },
  {
    id: 'guia',
    icone: '🧭',
    titulo: 'Guia para Navegação',
    conteudo: [
      '1. Acesse o mapa principal da plataforma.',
      '2. Visualize as instituições disponíveis.',
      '3. Clique em um marcador para consultar informações sobre a instituição.',
      '4. Utilize o mapa para encontrar pontos de assistência próximos.',
      '5. Caso queira se candidatar a voluntariado, realize login com uma conta voluntário, selecione o abrigo e se atribua como voluntário nas datas disponíveis pelo abrigo',
      '6. Caso queira se candidatar a abrigo, realize login com uma conta abrigo e altere disponibilidade de voluntariado ou necessidades do abrigo.'
    ]
  },
  {
  id: 'aviso',
  icone: '⚠️',
  titulo: 'Aviso importante',
  conteudo: [
    'Por segurança, abrigos e serviços de acolhimento destinados a mulheres vítimas de violência não são exibidos neste mapa.',
    'Essa medida evita a exposição da localização desses espaços de proteção.',
    'Para encontrar esses serviços, acesse a página oficial da Prefeitura de São Paulo sobre a rede de apoio a mulheres vítimas de violência:',
    'https://prefeitura.sp.gov.br/web/assistencia_social/w/rede_socioassistencial/mulheres_vitimas_de_violencia/332513'
  ]
},
]

const secaoAbertaId = ref(null)
const secaoAberta = computed(() => secoes.find(s => s.id === secaoAbertaId.value) ?? null)
</script>

<style scoped>
.cabecalho {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 64px;
  box-sizing: border-box;
  z-index: 1000;

  display: flex;
  align-items: center;
  gap: 16px;

  /* Reserva o canto direito para o botão Entrar ou a conta */
  padding: 0 220px 0 20px;

  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  font-family: sans-serif;
}

.cabecalho-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  color: #2563eb;
  text-decoration: none;
  border-radius: 10px;
}

.cabecalho-logo svg {
  display: block;
}

.cabecalho-logo:hover {
  color: #1d4ed8;
}

.cabecalho-secoes {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;

  flex: 1;
  min-width: 0;
}

.cabecalho-botao {
  padding: 8px 14px;
  flex-shrink: 0;

  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;

  color: #334155;
  background: transparent;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.cabecalho-botao:hover {
  background: #f1f5f9;
  color: #1d4ed8;
}

.cabecalho-logo:focus-visible,
.cabecalho-botao:focus-visible {
  outline: 3px solid #2563eb;
  outline-offset: 3px;
}

/* Em telas menores, mantém as seções em uma faixa com rolagem */
@media (max-width: 1200px) {
  .cabecalho {
    gap: 16px;
    padding-left: 16px;
  }

  .cabecalho-secoes {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .cabecalho-botao {
    padding: 8px 10px;
  }
}
</style>