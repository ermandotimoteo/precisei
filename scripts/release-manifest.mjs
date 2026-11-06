import fs from 'node:fs';
import crypto from 'node:crypto';
import { execFileSync } from 'node:child_process';

const jar = 'target/precisei-0.0.1-SNAPSHOT.jar';
const sha = execFileSync('git', ['rev-parse', 'HEAD'], { encoding: 'utf8' }).trim();
const dirty = execFileSync('git', ['status', '--porcelain', '--untracked-files=no'], { encoding: 'utf8' }).trim().length > 0;
if (!fs.existsSync(jar)) throw new Error('Empacote o JAR antes de gerar o manifesto.');
const digest = process.env.IMAGE_DIGEST || '';
if (digest && !/^sha256:[a-f0-9]{64}$/.test(digest)) throw new Error('Digest inválido.');
const image = process.env.IMAGE_REPOSITORY || '';
if (image && !/^ghcr\.io\/[a-z0-9_.-]+\/[a-z0-9_.-]+$/.test(image)) throw new Error('Registro não permitido.');
fs.mkdirSync('artifacts', { recursive: true });
const manifest = {
  version: 1, commit: sha, dirty, builtAt: new Date().toISOString(),
  image, digest, baseImage: process.env.RESOLVED_JAVA_RUNTIME || '',
  testedImageId: process.env.TESTED_IMAGE_ID || '',
  jarSha256: crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex'),
  schema: { lineage: 'production', minimum: 3, maximum: 3 },
  evidence: ['target/surefire-reports', 'target/site/jacoco', 'target/classes/META-INF/sbom', 'test-results/e2e.xml']
};
fs.writeFileSync('artifacts/release.json', JSON.stringify(manifest, null, 2) + '\n');
fs.writeFileSync('artifacts/CHANGELOG.md', `# Candidato ${sha}\n\nImagem: ${image}@${digest}\n\nEsquema compatível: 3. Aprovação de produção ainda necessária.\n`);
console.log(`Manifesto criado para ${sha}; dirty=${dirty}`);
