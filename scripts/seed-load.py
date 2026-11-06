"""Cria 100 profissionais e 1.000 pedidos sintéticos em banco _test já migrado.

MYSQL_PWD deve vir do ambiente. Não apaga dados e recusa uma segunda carga.
As contas têm hash inutilizável e não são credenciais de acesso.
"""
import argparse
import os
import re
import subprocess

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--database', required=True)
parser.add_argument('--mysql', default='mysql')
args = parser.parse_args()
if not re.fullmatch(r'[a-zA-Z0-9_]+_test(?:_[a-zA-Z0-9_]+)?', args.database):
    parser.error('Utilize somente banco exclusivo com sufixo _test ou _test_IDENTIFICADOR.')
command = [args.mysql, '-h', os.environ.get('MYSQL_HOST', 'localhost'), '-u', os.environ.get('MYSQL_USER', 'root'),
           '--batch', '--skip-column-names', args.database]
def query(sql):
    return subprocess.check_output(command, input=sql.encode(), stderr=subprocess.PIPE).decode().strip()
if query("SELECT COUNT(*) FROM clientes WHERE email='load-fixture@example.invalid';") != '0':
    raise SystemExit('Carga já presente; use outra base de teste para repetir a preparação.')
sql = ["START TRANSACTION;",
       "INSERT INTO servicos(nome,descricao,preco_referencia,id_categoria) SELECT 'Carga sintética','Somente teste',100,MIN(id_categoria) FROM categorias;",
       "SET @service=LAST_INSERT_ID();",
       "INSERT INTO clientes(nome,telefone,email,senha_hash) VALUES('Cliente de carga','000000000','load-fixture@example.invalid','!conta-inutilizavel');",
       "SET @client=LAST_INSERT_ID();"]
for index in range(100):
    sql.extend([
        f"INSERT INTO profissionais(nome,telefone,email,senha_hash,descricao,cidade,bairro,disponivel) VALUES('Carga {index:03}','000000000','load-{index:03}@example.invalid','!conta-inutilizavel','Profissional sintético','Cidade teste','Bairro teste',TRUE);",
        "SET @professional=LAST_INSERT_ID();",
        "INSERT INTO profissionais_servicos VALUES(@professional,@service);"])
    for order in range(10):
        sql.append(f"INSERT INTO solicitacoes_servico(data_solicitacao,status,descricao,id_cliente,id_profissional,id_servico) VALUES(NOW(),'PENDENTE','Carga sintética {index:03}-{order}',@client,@professional,@service);")
sql.append('COMMIT;')
query('\n'.join(sql))
print('Carga sintética adicionada: 100 profissionais e 1.000 solicitações; nenhum dado anterior removido.')
