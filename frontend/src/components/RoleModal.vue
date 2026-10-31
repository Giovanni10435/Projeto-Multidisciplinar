<template>
  <!-- Fundo escurecido (clicar fora também fecha) -->
  <div
    style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 16px; box-sizing: border-box; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); overflow-y: auto;"
    @click.self="emit('close')"
  >
    <!-- Card -->
    <div
      style="position: relative; box-sizing: border-box; width: 460px; max-width: 100%; margin: auto; padding: 40px 36px 36px; background: white; border: 1px solid #e2e8f0; border-radius: 20px; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25); font-family: sans-serif;"
    >
      <!-- Botão X -->
      <button
        type="button"
        @click="emit('close')"
        aria-label="Fechar"
        style="position: absolute; top: 14px; right: 14px; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: transparent; border: none; border-radius: 999px; cursor: pointer;"
        @mouseover="$event.currentTarget.style.background = '#f1f5f9'"
        @mouseleave="$event.currentTarget.style.background = 'transparent'"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2.5" stroke-linecap="round">
          <path d="M6 6l12 12M18 6L6 18" />
        </svg>
      </button>

      <!-- ============ PASSO 1: ESCOLHER PERFIL ============ -->
      <div v-if="!perfilEscolhido">
        <div style="text-align: center;">
          <div style="font-size: 44px; line-height: 1;">🏡</div>
          <h2 style="margin: 16px 0 6px; font-size: 24px; font-weight: 800; color: #0f172a;">
            Como você quer entrar?
          </h2>
          <p style="margin: 0 0 28px; font-size: 14px; color: #64748b;">
            Escolha seu perfil para continuar
          </p>
        </div>

        <!-- Usuário / Voluntário -->
        <button
          type="button"
          @click="escolherPerfil('usuario')"
          style="display: flex; align-items: center; gap: 16px; box-sizing: border-box; width: 100%; padding: 18px 20px; margin-bottom: 14px; text-align: left; background: #eff6ff; border: 2px solid #2563eb; border-radius: 14px; cursor: pointer;"
        >
          <span style="font-size: 34px; line-height: 1; flex-shrink: 0;">🙋‍♀️</span>
          <span style="display: block;">
            <span style="display: block; font-size: 16px; font-weight: 800; color: #0f172a;">Usuário / Voluntário</span>
            <span style="display: block; margin-top: 4px; font-size: 13px; color: #475569; line-height: 1.4;">
              Buscar abrigos, registrar visitas e oferecer ajuda
            </span>
          </span>
        </button>

        <!-- Abrigo / Gestor -->
        <button
          type="button"
          @click="escolherPerfil('gestor')"
          style="display: flex; align-items: center; gap: 16px; box-sizing: border-box; width: 100%; padding: 18px 20px; text-align: left; background: #ecfeff; border: 2px solid #0891b2; border-radius: 14px; cursor: pointer;"
        >
          <span style="font-size: 34px; line-height: 1; flex-shrink: 0;">🏠</span>
          <span style="display: block;">
            <span style="display: block; font-size: 16px; font-weight: 800; color: #0f172a;">Abrigo / Gestor</span>
            <span style="display: block; margin-top: 4px; font-size: 13px; color: #475569; line-height: 1.4;">
              Gerenciar seu abrigo, doações e horários de voluntários
            </span>
          </span>
        </button>
      </div>

      <!-- ============ PASSO 2: ENTRAR OU CADASTRAR ============ -->
      <div v-else>
        <button
          type="button"
          @click="voltar"
          style="background: none; border: none; padding: 0; font-size: 13px; font-weight: 600; color: #2563eb; cursor: pointer;"
        >
          ← Voltar
        </button>

        <div style="text-align: center; margin: 4px 0 22px;">
          <div style="font-size: 38px; line-height: 1;">{{ perfilEscolhido === 'usuario' ? '🙋‍♀️' : '🏠' }}</div>
          <h2 style="margin: 10px 0 0; font-size: 20px; font-weight: 800; color: #0f172a;">
            {{ perfilEscolhido === 'usuario' ? 'Usuário / Voluntário' : 'Abrigo / Gestor' }}
          </h2>
        </div>

        <!-- Abas Entrar / Cadastrar -->
        <div style="display: flex; gap: 4px; padding: 4px; margin-bottom: 22px; background: #f1f5f9; border-radius: 14px;">
          <button type="button" @click="trocarModo('entrar')" :style="estiloTab(modo === 'entrar')">
            Entrar
          </button>
          <button type="button" @click="trocarModo('cadastrar')" :style="estiloTab(modo === 'cadastrar')">
            Cadastrar
          </button>
        </div>
        <p
          v-if="sucesso"
          role="status"
          style="margin: 0 0 16px; padding: 10px 12px; font-size: 13px; color: #166534; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 10px;"
        >
          {{ sucesso }}
        </p>

        <!-- ----- ENTRAR (já tenho conta) ----- -->
        <form v-if="modo === 'entrar'" @submit.prevent="entrar" style="display: flex; flex-direction: column; gap: 12px;">
          <p style="margin: 0 0 4px; font-size: 13px; color: #64748b; text-align: center;">
            Já tem conta? Entre com seu e-mail e senha.
          </p>

          <div>
            <label :style="estiloLabel">E-mail</label>
            <input v-model="loginForm.email" required type="email" autocomplete="email" placeholder="email@exemplo.com" :style="estiloInput" />
          </div>
          <div>
            <label :style="estiloLabel">Senha</label>
            <input v-model="loginForm.senha" required type="password" autocomplete="current-password" placeholder="Sua senha" :style="estiloInput" />
          </div>

          <p v-if="erro" :style="estiloErro">{{ erro }}</p>

          <button
            type="submit"
            :disabled="entrando"
            :style="estiloBotaoPrincipal"
          >
            {{ entrando ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <!-- ----- CADASTRAR: USUÁRIO ----- -->
        <form
          v-else-if="perfilEscolhido === 'usuario'"
          @submit.prevent="cadastrarUsuario"
          style="display: flex; flex-direction: column; gap: 12px;"
        >
          <p style="margin: 0 0 4px; font-size: 13px; color: #64748b; text-align: center;">
            Ainda não tem conta? Crie a sua em poucos segundos.
          </p>

          <div>
            <label :style="estiloLabel">Nome completo</label>
            <input v-model="cadastroUsuario.nome" required type="text" autocomplete="name" placeholder="Seu nome" :style="estiloInput" />
          </div>
          <div>
            <label :style="estiloLabel">E-mail</label>
            <input v-model="cadastroUsuario.email" required type="email" autocomplete="email" placeholder="email@exemplo.com" :style="estiloInput" />
          </div>
          <div>
            <label :style="estiloLabel">Senha</label>
            <input v-model="cadastroUsuario.senha" required type="password" autocomplete="new-password" placeholder="Mínimo de 6 caracteres" :style="estiloInput" />
          </div>

          <p v-if="erro" :style="estiloErro">{{ erro }}</p>

          <button
            type="submit"
            :disabled="cadastrando"
            :style="estiloBotaoPrincipal"
          >
            {{ cadastrando ? 'Criando conta...' : 'Criar conta' }}
          </button>
        </form>

        <!-- ----- CADASTRAR: ABRIGO / GESTOR ----- -->
        <form
          v-else
          @submit.prevent="cadastrarGestor"
          style="display: flex; flex-direction: column; gap: 12px;"
        >
          <div>
            <label :style="estiloLabel">Nome da Instituição</label>
            <input v-model="formInstituicao.nome" required type="text" placeholder="Ex: Abrigo Esperança" :style="estiloInput" />
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
            <div>
              <label :style="estiloLabel">CNPJ</label>
              <input v-model="formInstituicao.cnpj" required type="text" placeholder="00.000.000/0001-00" :style="estiloInput" />
            </div>
            <div>
              <label :style="estiloLabel">Telefone</label>
              <input v-model="formInstituicao.telefone" required type="text" placeholder="(11) 90000-0000" :style="estiloInput" />
            </div>
          </div>

          <div>
            <label :style="estiloLabel">E-mail de Contato (será seu login)</label>
            <input v-model="formInstituicao.email_contato" required type="email" autocomplete="email" placeholder="contato@abrigo.com" :style="estiloInput" />
          </div>

          <div>
            <label :style="estiloLabel">Senha</label>
            <input v-model="senhaGestor" required type="password" autocomplete="new-password" placeholder="Mínimo de 6 caracteres" :style="estiloInput" />
          </div>

          <div>
            <label :style="estiloLabel">Endereço Completo</label>
            <input v-model="formInstituicao.localizacao.endereco" required type="text" placeholder="Rua, número, bairro - Cidade/UF" :style="estiloInput" />
          </div>

          <div>
            <label :style="estiloLabel">Descrição</label>
            <textarea v-model="formInstituicao.descricao" rows="2" placeholder="Descreva os serviços do abrigo..." :style="estiloInput"></textarea>
          </div>

          <p v-if="erro" :style="estiloErro">{{ erro }}</p>

          <button type="submit" :style="estiloBotaoPrincipal">Cadastrar abrigo</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { cadastrarConta, entrarNaConta } from '../auth'
import { cadastrarVoluntario, loginVoluntario } from '../services/voluntarioApi'

const emit = defineEmits(['select', 'submitInstituicao', 'usuarioAutenticado', 'gestorAutenticado', 'close'])

const perfilEscolhido = ref(null) // null | 'usuario' | 'gestor'
const modo = ref('entrar')        // 'entrar' | 'cadastrar'
const erro = ref('')
const sucesso = ref('')
const cadastrando = ref(false)
const entrando = ref(false)

const loginForm = reactive({ email: '', senha: '' })
const cadastroUsuario = reactive({ nome: '', email: '', senha: '' })
const senhaGestor = ref('')

const formInstituicao = reactive({
  nome: '',
  cnpj: '',
  descricao: '',
  telefone: '',
  email_contato: '',
  localizacao: {
    endereco: '',
    latitude: -23.550520, // Default Sé
    longitude: -46.633308
  }
})

// Cor principal muda conforme o perfil (azul = usuário, ciano = gestor)
const corPrincipal = computed(() => (perfilEscolhido.value === 'gestor' ? '#0891b2' : '#1d4ed8'))

const estiloLabel = 'display: block; margin-bottom: 4px; font-size: 12px; font-weight: 600; color: #334155;'
const estiloInput = 'box-sizing: border-box; width: 100%; padding: 10px 12px; font-size: 14px; font-family: inherit; color: #1e293b; background: white; border: 1px solid #cbd5e1; border-radius: 12px; outline: none; resize: vertical;'
const estiloErro = 'margin: 0; padding: 10px 12px; font-size: 13px; color: #b91c1c; background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px;'

const estiloBotaoPrincipal = computed(
  () => `width: 100%; margin-top: 6px; padding: 12px 0; background: ${corPrincipal.value}; color: white; font-size: 14px; font-weight: 700; border: none; border-radius: 12px; cursor: pointer;`
)

function estiloTab(ativa) {
  return `flex: 1; padding: 10px 0; font-size: 14px; font-weight: 700; border: none; border-radius: 10px; cursor: pointer; background: ${ativa ? corPrincipal.value : 'transparent'}; color: ${ativa ? 'white' : '#475569'};`
}

function escolherPerfil(perfil) {
  if (entrando.value || cadastrando.value) return

  perfilEscolhido.value = perfil
  modo.value = 'entrar'
  erro.value = ''
  sucesso.value = ''
}

function voltar() {
  if (entrando.value || cadastrando.value) return

  perfilEscolhido.value = null
  erro.value = ''
  sucesso.value = ''
}

function trocarModo(novoModo) {
  if (entrando.value || cadastrando.value) return

  modo.value = novoModo
  erro.value = ''
  sucesso.value = ''
}

// ----- Entrar (já tenho conta) no frontend -----
// function entrar() {
//   erro.value = ''
//   const resultado = entrarNaConta({
//     tipo: perfilEscolhido.value,
//     email: loginForm.email,
//     senha: loginForm.senha
//   })

//   if (!resultado.ok) {
//     erro.value = resultado.erro
//     return
//   }

//   if (perfilEscolhido.value === 'usuario') {
//     emit('usuarioAutenticado', { id: resultado.conta.id, perfil: resultado.conta.perfil, novo: false })
//   } else {
//     const instituicaoId = resultado.conta.perfil?.instituicaoId
//     if (!instituicaoId) {
//       erro.value = 'Não encontramos o abrigo ligado a esta conta.'
//       return
//     }
//     emit('gestorAutenticado', { id: resultado.conta.id, instituicaoId })
//   }
// }

// ----- Entrar (já tenho conta) no BFF -----
async function entrar() {
  if (entrando.value) return

  erro.value = ''
  sucesso.value = ''
  entrando.value = true

  try {
    // Usuário / Voluntário: autenticação pelo BFF.
    if (perfilEscolhido.value === 'usuario') {
      const resultado = await loginVoluntario({
        email: loginForm.email.trim(),
        senha: loginForm.senha
      })

      if (resultado.role !== 'usuario') {
        erro.value = 'Esta conta não é de usuário/voluntário.'
        return
      }

      loginForm.senha = ''

      emit('usuarioAutenticado', {
        id: resultado.id,
        perfil: {
          nome: resultado.nome,
          email: resultado.email
        },
        novo: false
      })

      return
    }

    // Gestor: mantém o fluxo anterior por enquanto.
    const resultado = entrarNaConta({
      tipo: perfilEscolhido.value,
      email: loginForm.email,
      senha: loginForm.senha
    })

    if (!resultado.ok) {
      erro.value = resultado.erro
      return
    }

    const instituicaoId = resultado.conta.perfil?.instituicaoId

    if (!instituicaoId) {
      erro.value = 'Não encontramos o abrigo ligado a esta conta.'
      return
    }

    loginForm.senha = ''

    emit('gestorAutenticado', {
      id: resultado.conta.id,
      instituicaoId
    })
  } catch (e) {
    erro.value = e instanceof TypeError
      ? 'Não foi possível conectar ao serviço de login. Confira o console do navegador.'
      : e.message || 'Não foi possível realizar o login.'
  } finally {
    entrando.value = false
  }
}

// ----- Cadastrar usuário no frontend -----
// function cadastrarUsuario() {
//   erro.value = ''
//   if (cadastroUsuario.senha.length < 6) {
//     erro.value = 'A senha precisa ter pelo menos 6 caracteres.'
//     return
//   }

//   const email = cadastroUsuario.email.trim()
//   const resultado = cadastrarConta({
//     tipo: 'usuario',
//     email,
//     senha: cadastroUsuario.senha,
//     perfil: { nome: cadastroUsuario.nome.trim(), idade: '', telefone: '', email, cpf: '' }
//   })

//   if (!resultado.ok) {
//     erro.value = resultado.erro
//     return
//   }

//   emit('usuarioAutenticado', { id: resultado.conta.id, perfil: resultado.conta.perfil, novo: true })
// }

// ----- Cadastrar usuário pelo BFF -----
async function cadastrarUsuario() {
  if (cadastrando.value) return

  erro.value = ''
  sucesso.value = ''

  if (cadastroUsuario.senha.length < 6) {
    erro.value = 'A senha precisa ter pelo menos 6 caracteres.'
    return
  }

  cadastrando.value = true

  try {
    await cadastrarVoluntario({
      nome: cadastroUsuario.nome.trim(),
      email: cadastroUsuario.email.trim(),
      senha: cadastroUsuario.senha,
      telefone: null,
      disponibilidade: null
    })

    // Preenche o email para o usuário entrar depois.
    loginForm.email = cadastroUsuario.email.trim()
    loginForm.senha = ''

    cadastroUsuario.nome = ''
    cadastroUsuario.email = ''
    cadastroUsuario.senha = ''

    modo.value = 'entrar'
    sucesso.value = 'Conta criada com sucesso! Entre com seu e-mail e senha.'
  } catch (e) {
    erro.value = e instanceof TypeError
      ? 'Não foi possível conectar ao BFF. Verifique a conexão e o console do navegador.'
      : e.message || 'Não foi possível realizar o cadastro.'
  } finally {
    cadastrando.value = false
  }
}

// ----- Cadastrar abrigo / gestor -----
function cadastrarGestor() {
  erro.value = ''
  if (senhaGestor.value.length < 6) {
    erro.value = 'A senha precisa ter pelo menos 6 caracteres.'
    return
  }

  const resultado = cadastrarConta({
    tipo: 'gestor',
    email: formInstituicao.email_contato,
    senha: senhaGestor.value,
    perfil: { nome: formInstituicao.nome }
  })

  if (!resultado.ok) {
    erro.value = resultado.erro
    return
  }

  // A senha não vai junto com os dados do abrigo
  emit(
    'submitInstituicao',
    { ...formInstituicao, localizacao: { ...formInstituicao.localizacao } },
    resultado.conta.id
  )
}
</script>