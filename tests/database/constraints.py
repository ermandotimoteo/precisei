"""Reteste de restrições reais no MySQL. Toda fixture é transacional e revertida."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import uuid

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--database', required=True)
parser.add_argument('--mysql', default='mysql')
args = parser.parse_args()
if not re.fullmatch(r'[a-zA-Z0-9_]+_test(?:_[a-zA-Z0-9_]+)?', args.database):
    parser.error('Recusado: use uma base exclusiva _test.')
connection = [args.mysql, '-h', os.environ.get('MYSQL_HOST', 'localhost'),
              '-u', os.environ.get('MYSQL_USER', 'root'), '--batch', '--skip-column-names', args.database]
cases = [
    ('email_unico', "INSERT INTO clientes(nome,telefone,email,senha_hash) SELECT nome,telefone,email,senha_hash FROM clientes WHERE id_cliente=@c;", 1062),
    ('fk_obrigatoria', 'UPDATE solicitacoes_servico SET id_cliente=NULL WHERE id_solicitacao=@r;', 1048),
    ('fk_inexistente', 'UPDATE solicitacoes_servico SET id_cliente=9223372036854775807 WHERE id_solicitacao=@r;', 1452),
    ('preco_negativo', 'UPDATE servicos SET preco_referencia=-0.01 WHERE id_servico=@s;', 3819),
    ('status_invalido', "UPDATE solicitacoes_servico SET status='INVALIDO' WHERE id_solicitacao=@r;", 3819),
    ('data_passada', 'UPDATE solicitacoes_servico SET data_agendada=DATE_SUB(data_solicitacao,INTERVAL 1 DAY) WHERE id_solicitacao=@r;', 3819),
    ('nota_zero', 'UPDATE avaliacoes SET nota=0 WHERE id_solicitacao=@r;', 3819),
    ('nota_seis', 'UPDATE avaliacoes SET nota=6 WHERE id_solicitacao=@r;', 3819),
    ('avaliacao_unica', 'INSERT INTO avaliacoes(nota,id_solicitacao) VALUES(5,@r);', 1062),
    ('remocao_referenciada', 'DELETE FROM categorias WHERE id_categoria=@cat;', 1451)
]
results = []
for name, invalid, expected in cases:
    token = uuid.uuid4().hex
    fixture = f"""
START TRANSACTION;
SET @cat=(SELECT MIN(id_categoria) FROM categorias);
INSERT INTO clientes(nome,telefone,email,senha_hash) VALUES('Integridade','000000000','{token}@example.invalid','!fixture');
SET @c=LAST_INSERT_ID();
INSERT INTO servicos(nome,descricao,id_categoria) VALUES('{token}','Teste de integridade',@cat);
SET @s=LAST_INSERT_ID();
INSERT INTO profissionais(nome,telefone,email,senha_hash,cidade,bairro,disponivel) VALUES('Integridade','000000000','p{token}@example.invalid','!fixture','Cidade','Bairro',1);
SET @p=LAST_INSERT_ID();
INSERT INTO profissionais_servicos VALUES(@p,@s);
INSERT INTO solicitacoes_servico(descricao,id_cliente,id_profissional,id_servico) VALUES('Teste de integridade',@c,@p,@s);
SET @r=LAST_INSERT_ID();
INSERT INTO avaliacoes(nota,id_solicitacao) VALUES(5,@r);
{invalid}
ROLLBACK;
"""
    process = subprocess.run(connection, input=fixture.encode(), capture_output=True)
    error = re.search(rb'ERROR (\d+)', process.stderr)
    passed = process.returncode != 0 and error and int(error[1]) == expected
    # Não publicar o stderr: pode conter valores dos registros.
    results.append({'case': name, 'expectedMySqlError': expected, 'passed': bool(passed)})
Path('artifacts').mkdir(exist_ok=True)
Path('artifacts/database-constraints.json').write_text(json.dumps(results, indent=2)+'\n', encoding='utf-8')
print(json.dumps(results, indent=2))
raise SystemExit(int(not all(r['passed'] for r in results)))
