"""Verifica links externos atuais em tarefa separada do bloqueio de entrega."""
import concurrent.futures
import json
from pathlib import Path
import re
import urllib.error
import urllib.request

files = [Path('README.md'), *Path('docs/qualidade').glob('*.md'),
         *Path('docs/seguranca').glob('*.md'), *Path('docs/operacao').glob('*.md')]
links = sorted({url.rstrip('.,;') for file in files
                for url in re.findall(r'https://[^\s)<>`]+', file.read_text(encoding='utf-8'))
                if '.invalid' not in url and '$' not in url})
def check(url):
    request = urllib.request.Request(url, headers={'User-Agent': 'Precisei-documentation-check/1'}, method='HEAD')
    try:
        with urllib.request.urlopen(request, timeout=15) as result:
            return {'url': url, 'status': result.status, 'ok': result.status < 400}
    except urllib.error.HTTPError as error:
        return {'url': url, 'status': error.code, 'ok': False}
    except (OSError, ValueError):
        return {'url': url, 'status': 'unavailable', 'ok': False}
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
    results = list(pool.map(check, links))
Path('artifacts').mkdir(exist_ok=True)
Path('artifacts/links.json').write_text(json.dumps(results, indent=2) + '\n', encoding='utf-8')
print(f'Links: {len(results)}; indisponíveis/revisar: {sum(not r["ok"] for r in results)}. Evidência: artifacts/links.json')
raise SystemExit(int(any(not r['ok'] for r in results)))
