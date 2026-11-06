# Modelo de ameaças e decisões de acesso

Versão de implementação: 06/11/2026. Base: D01-D08 da inspeção e P01-P18 do plano seguro.

## Ativos e fronteiras

- Navegador não confiável → aplicação: formulários, identificadores e texto não definem identidade ou privilégios.
- Aplicação → MySQL: conta restrita em produção; transações, consultas parametrizadas, FKs, UNIQUE e locks de pedido.
- Repositório/PR → CI: contribuições sem credenciais de publicação; dependências e ações verificáveis.
- CI → registro → servidor: candidato testado, digest, manifesto, origem da execução e aprovação.
- Servidor → backup: cópia criptografada; chave privada separada; restauração em base nova.

## Cenários e controles

| Ameaça | Controle e teste |
|---|---|
| Cliente altera ID para acessar pedido alheio | Consulta por titular e verificação no serviço; `SegurancaIntegrationTests` |
| Cliente aceita serviço em nome do profissional | Cliente só pode cancelar; profissional atribuído aceita/inicia/conclui |
| Conta profissional 1 confundida com cliente 1 | Principal inclui tipo e ID; tipo escolhido no login e conferido no provider |
| Formulário forja `clienteId`, `id` ou papel | Titular vem da sessão; ID de cadastro profissional zerado; manutenção só externa |
| Outro site usa sessão ativa | Token CSRF nos POSTs, cookies SameSite, nenhum GET de gravação |
| Comentário executa script | `th:text`, CSP e teste de saída; sem HTML livre |
| Duas avaliações simultâneas | Lock pessimista do pedido + UNIQUE; conflito tratado sem detalhes técnicos |
| Hash de senha exposto | Argon2id para novas senhas, BCrypt legado validado/atualizado no login; sem senha/token em log |
| Brute force e consumo de memória | Janela de 15 min: 10 falhas por conta, 50 por origem; cache limitado e Retry-After |
| Roubo de segredo na CI | Actions por SHA, token mínimo, ausência de segredos de deploy em PR e procedência do candidato |
| Migração incompatível ou falha operacional | Linha de origem explícita, V3 comum, backup, intervalo de esquema e retorno condicionado |

## Matriz de permissões implementada

Visitante consulta catálogo e apresentação; pode cadastrar sua conta, sem atribuir papel privilegiado.
Cliente consulta somente sua conta e seus pedidos, cria pedido em seu nome, cancela quando permitido e avalia conclusão própria.
Profissional edita seu perfil e atua somente em pedidos atribuídos a ele.
Manutenção tem catálogo e suporte restritos; sua identidade é configurada por e-mail e hash externos, sem senha padrão nem autocadastro.
Avaliação é exclusiva do cliente titular, mesmo quando a manutenção pode inspecionar o pedido.

## Dados e limites

Contatos de clientes não aparecem em páginas públicas. Telefone do atendimento é mostrado apenas na listagem privada já filtrada por participante/manutenção. Descrições e observações não devem conter dados sensíveis.
Alteração de e-mail exige futura confirmação; o serviço mantém o endereço original na edição de perfil. Alterar senha requer a senha atual.
Recuperação de senha, pagamentos e identidade verificada por documentos continuam fora do escopo aprovado destes planos.

A proteção de login é local à instância, não um limitador distribuído. Validar limiares com usuários e adicionar controle no perímetro ao ampliar escala.
Hospedagem, regras remotas do repositório, retenção institucional e resposta humana a incidentes exigem responsáveis reais; não são inferidos pelo código.
