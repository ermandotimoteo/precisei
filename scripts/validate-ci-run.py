import json
import os
import re
import urllib.request

run = os.environ['RUN_ID']
if not re.fullmatch(r'[0-9]+', run):
    raise SystemExit('ID de execução inválido')
repo = os.environ['GITHUB_REPOSITORY']
def api(path):
    req = urllib.request.Request('https://api.github.com/' + path, headers={
        'Authorization': 'Bearer ' + os.environ['GH_TOKEN'],
        'Accept': 'application/vnd.github+json', 'X-GitHub-Api-Version': '2022-11-28'})
    with urllib.request.urlopen(req, timeout=30) as res:
        return json.load(res)
data = api(f'repos/{repo}/actions/runs/{run}')
if not (data['event'] == 'push' and data['conclusion'] == 'success'
        and data['head_branch'] in ['main', 'modulo-3']
        and data['head_repository']['full_name'] == repo
        and data['path'] == '.github/workflows/ci.yml'):
    raise SystemExit('Origem do candidato não autorizada')
if os.environ['DEPLOY_ENVIRONMENT'] == 'production':
    staged = os.environ.get('STAGING_RUN_ID', '')
    if not re.fullmatch(r'[0-9]+', staged):
        raise SystemExit('Produção exige o ID da homologação bem-sucedida')
    stage = api(f'repos/{repo}/actions/runs/{staged}')
    if not (stage['conclusion'] == 'success' and stage['path'] == '.github/workflows/cd.yml'
            and stage['head_repository']['full_name'] == repo
            and stage['head_branch'] in ['main', 'modulo-3']):
        raise SystemExit('Homologação sem origem autorizada ou sem sucesso')
    env = api(f'repos/{repo}/environments/production')
    if not any(rule['type'] == 'required_reviewers' and rule.get('reviewers') for rule in env['protection_rules']):
        raise SystemExit('Produção exige aprovador obrigatório no ambiente GitHub')
with open(os.environ['GITHUB_ENV'], 'a', encoding='utf-8') as f:
    f.write('CANDIDATE_RUN=' + run + '\nCANDIDATE_SHA=' + data['head_sha'] + '\n')
