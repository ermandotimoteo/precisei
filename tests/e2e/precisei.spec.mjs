import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('@smoke catálogo público, privacidade e formulário acessíveis', async ({ page }) => {
  for (const url of ['/', '/servicos', '/profissionais', '/login', '/cadastro/cliente', '/privacidade']) {
    await page.goto(url);
    await expect(page.locator('main')).toBeVisible();
    const result = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
    expect(result.violations, JSON.stringify(result.violations, null, 2)).toEqual([]);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy();
  }
  await page.goto('/solicitacoes');
  await expect(page).toHaveURL(/\/login/);
  await page.goto('/cadastro/cliente');
  await page.keyboard.press('Tab');
  await expect(page.getByRole('link', { name: 'Pular para o conteúdo' })).toBeFocused();
});

test('cliente solicita, profissional conclui e titular avalia', async ({ page }) => {
  const unique = Date.now() + '-' + test.info().project.name;
  const email = `cliente-${unique}@example.invalid`;
  const professionalEmail = `prof-${unique}@example.invalid`;
  const professionalName = `Profissional ${unique}`;
  const password = 'TesteLocalSeguro123';

  await page.goto('/profissionais');
  await page.locator('#prof-nome').fill(professionalName);
  await page.locator('#prof-email').fill(professionalEmail);
  await page.locator('#prof-telefone').fill('00000000000');
  await page.locator('#prof-senha').fill(password);
  await page.locator('#prof-cidade').fill('Cidade de teste');
  await page.locator('#prof-bairro').fill('Bairro de teste');
  await page.getByLabel('Disponível para novos atendimentos').check();
  const service = page.locator('input[name="servicoIds"]').first();
  const serviceId = await service.getAttribute('value');
  await service.check();
  await page.getByRole('button', { name: 'Cadastrar profissional' }).click();
  await expect(page.getByRole('status')).toContainText('cadastrado');

  await page.goto('/cadastro/cliente');
  await page.locator('#cliente-nome').fill(`Cliente ${unique}`);
  await page.locator('#cliente-telefone').fill('00000000000');
  await page.locator('#cliente-email').fill(email);
  await page.locator('#cliente-senha').fill(password);
  await page.getByRole('button', { name: 'Cadastrar cliente' }).click();
  await expect(page).toHaveURL(/\/login/);

  async function login(tipo, usuario) {
    await page.goto('/login');
    const before = (await page.context().cookies()).find(c => c.name === 'JSESSIONID')?.value;
    await page.locator('#tipo').selectOption(tipo);
    await page.locator('#username').fill(usuario);
    await page.locator('#password').fill(password);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await expect(page).toHaveURL(/\/solicitacoes/);
    const after = (await page.context().cookies()).find(c => c.name === 'JSESSIONID')?.value;
    expect(before).toBeTruthy();
    expect(after).toBeTruthy();
    expect(after).not.toBe(before);
  }
  async function sair() { await page.getByRole('button', { name: 'Sair' }).click(); }
  await login('CLIENTE', email);
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze()).violations).toEqual([]);
  const professionalId = await page.locator('#sol-profissional option').filter({ hasText: professionalName }).getAttribute('value');
  await page.locator('#sol-profissional').selectOption(professionalId);
  await page.locator('#sol-servico').selectOption(serviceId);
  const description = `Atendimento E2E ${unique}`;
  await page.locator('#sol-descricao').fill(description);
  await page.getByRole('button', { name: 'Enviar solicitação' }).click();
  await expect(page.locator('.cartao-solicitacao').filter({ hasText: description })).toContainText('Pendente');
  await sair();
  await login('PROFISSIONAL', professionalEmail);
  const card = page.locator('.cartao-solicitacao').filter({ hasText: description });
  for (const estado of ['Aceita', 'Em andamento', 'Concluída']) {
    await card.getByRole('button', { name: `Marcar: ${estado}` }).click();
    await expect(card.locator('.status-solicitacao')).toHaveText(estado);
  }
  await expect(card.getByRole('button', { name: 'Enviar avaliação' })).toHaveCount(0);
  await sair();
  await login('CLIENTE', email);
  const own = page.locator('.cartao-solicitacao').filter({ hasText: description });
  await own.getByLabel('Avaliar atendimento').selectOption('5');
  await own.getByLabel('Comentário (opcional)').fill('Atendimento de teste concluído.');
  await own.getByRole('button', { name: 'Enviar avaliação' }).click();
  await expect(own).toContainText('5 de 5 estrelas');
});
