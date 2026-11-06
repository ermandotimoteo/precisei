import fs from 'node:fs';
import path from 'node:path';
import { parseDocument } from 'yaml';

let failed = false;
function fail(file, message) { console.error(`${file}: ${message}`); failed = true; }
function files(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(d => d.isDirectory() ? files(path.join(dir, d.name)) : [path.join(dir, d.name)]);
}
for (const file of files('.github').filter(f => /\.ya?ml$/.test(f))) {
  const source = fs.readFileSync(file, 'utf8');
  const doc = parseDocument(source, { uniqueKeys: true });
  for (const err of doc.errors) fail(file, err.message);
  for (const match of source.matchAll(/\buses:\s*([^\s#]+)/g)) {
    const ref = match[1];
    if (!ref.startsWith('./') && !/@[a-f0-9]{40}$/.test(ref)) fail(file, `Action não fixada por SHA: ${ref}`);
  }
}
for (const file of files('deploy').filter(f => /\.ya?ml$/.test(f))) {
  for (const err of parseDocument(fs.readFileSync(file, 'utf8')).errors) fail(file, err.message);
}
for (const file of files('src/main/resources/templates')) {
  const source = fs.readFileSync(file, 'utf8');
  if (!source.includes('lang="pt-BR"')) fail(file, 'Idioma ausente.');
  if (source.includes('th:utext')) fail(file, 'HTML não escapado exige revisão explícita.');
  for (const match of source.matchAll(/th:href="@\{\/css\/([^}]+)\}"/g)) {
    if (!fs.existsSync('src/main/resources/static/css/' + match[1])) fail(file, 'CSS inexistente.');
  }
}
for (const file of [...files('src/main'), ...files('.github'), ...files('deploy')].filter(f => /\.(java|properties|sql|ya?ml|html)$/.test(f))) {
  const source = fs.readFileSync(file, 'utf8');
  if (/-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----|\bAKIA[0-9A-Z]{16}\b|gh[pousr]_[A-Za-z0-9]{30,}/.test(source)) fail(file, 'Possível segredo: revisar e rotacionar; valor omitido.');
  if (/spring\.datasource\.password\s*=\s*[^$\s]/.test(source)) fail(file, 'Senha de banco literal.');
}
for (const file of ['README.md', ...files('docs').filter(f => f.endsWith('.md'))]) {
  const source = fs.readFileSync(file, 'utf8');
  for (const match of source.matchAll(/\]\(([^)]+)\)/g)) {
    const link = match[1].replace(/^<|>$/g, '').split('#')[0];
    if (!link || /^(https?:|mailto:|data:)/.test(link)) continue;
    if (!fs.existsSync(path.resolve(path.dirname(file), decodeURIComponent(link)))) fail(file, `Link local inexistente: ${link}`);
  }
}
// V1/V2 históricas são imutáveis; hashes impedem trocar uma migração já aplicada.
const { createHash } = await import('node:crypto');
const historical = {
  'V1__criar_esquema_inicial.sql': 'a1ff010425df2a9319ada324a1629f234678b0b3d9970939f7bbfa92aed022bb',
  'V2__inserir_dados_demonstracao.sql': 'a414b3886e99e9e161acfd7132bda4447863ce846e51e9a4e77ffd974ea9e365'
};
for (const [name, expected] of Object.entries(historical)) {
  const relative = 'src/main/resources/db/migration/' + name;
  // Baseline do commit 469e4d3, com LF normalizado para checkout Windows/Linux.
  if (!fs.existsSync(relative)) { fail(relative, 'Migração histórica removida.'); continue; }
  const actual = createHash('sha256').update(fs.readFileSync(relative, 'utf8').replaceAll('\r\n', '\n')).digest('hex');
  if (actual !== expected) fail(relative, 'Migração histórica alterada; criar V3+ em db/common.');
}
if (failed) process.exit(1);
console.log('YAML, referências locais, práticas de interface e padrões de segredos verificados.');
