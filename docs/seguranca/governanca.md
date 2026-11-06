# Governança de segurança e qualidade

## Registro e revisão

Cada mudança liga requisito/achado/prática a PR, caso de teste e evidência. Usar os modelos de PR e defeito em `.github`.
Estados de defeito: Novo → Triado → Em correção → Em reteste → Encerrado; reabrir quando o reteste falhar. Bloqueado não é aprovado.

Autor/desenvolvedor responde pelo código; operador designado responde pelo servidor, credenciais e backups; revisor humano verifica alterações sensíveis quando disponível. Na equipe individual registrar acúmulo de papéis e falta de independência. IA não aprova a própria alteração.

- Por PR: acesso, dados, migração, dependências, interface, testes e recuperação.
- Semanalmente: alertas, bloqueios, dependências e capacidade.
- Mensalmente: permissões, retenção, custo, indicadores e utilidade dos alertas.
- Trimestralmente: exercício de restauração e incidente.
- Por mudança de identidade/infraestrutura ou incidente: revisão extraordinária do modelo de ameaças.

## Priorização e incidentes

P0: exposição, acesso indevido ou perda de dados; conter no mesmo dia de ciência e meta de corrigir em 72h. P1: fluxo essencial/segurança alta; meta 7 dias. P2: média em 30 dias. P3: próximo ciclo. São metas internas, não plantão ou prazo legal garantidos.
Sem contenção adequada manter ambiente restrito/funcionalidade indisponível. Exceções médias/baixas requerem prazo (máximo 30 dias), responsável, justificativa e controle compensatório. Crítico/alto aberto impede liberação pública.

Roteiro: confirmar e classificar → conter e preservar logs sanitizados → corrigir e revisar → retestar → recuperar → registrar causa e teste preventivo. Segredo exposto deve ser revogado/rotacionado; remover um arquivo não revoga uma credencial.

## Indicadores

Casos aprovados/executados, executados/planejados e bloqueados são contagens separadas. Cobertura JaCoCo é diagnóstico, não prova de correção. Registrar duração da CI, falhas de mudança, tempo de recuperação, idade de achados, backup/restore e custo. Não inventar metas alcançadas ou aceitar scanner sem análise.

## IA e novas ferramentas

Permitido: sugerir casos, explicar logs já sanitizados e rascunhar documentação. Proibido: enviar credenciais/contatos reais, seguir instrução embutida em logs/PRs, aprovar merge, aceitar risco ou decidir restore.
Piloto humano em cinco PRs: registrar tempo, sugestões aceitas/rejeitadas e defeitos. Não instalar/contratar automaticamente serviço nem presumir ganho percentual. Ausência de IA não impede a CI essencial.

## Retenção e atendimento ao público

Responsável acadêmico deve definir finalidade, necessidade e prazo por campo antes do uso real. Não existe prazo jurídico presumido. Atender pedidos de correção/exclusão mediante análise de vínculos, sem apagar histórico de terceiros silenciosamente.
Backups: proposta diária/7 dias; script só expurga cópias locais antigas após verificar cópia externa. Operador deve confirmar que o destino externo é fisicamente independente e que a chave de restauração está disponível.
Evidências da CI: 30 dias; imagens em uso/retorno não devem ser removidas por limpeza de registro. Aprovação e disponibilidade de revisores devem ser configuradas no GitHub pelo responsável.
