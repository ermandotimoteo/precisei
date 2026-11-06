"""Exercício local de backup/restore, exclusivamente entre bancos de teste.

Requer MYSQL_PWD no ambiente; nunca recebe a senha na linha de comando.
Não substitui o exercício dos scripts Docker no servidor de homologação.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import time


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", required=True)
    parser.add_argument("--destination", required=True)
    parser.add_argument("--mysql", default="mysql")
    parser.add_argument("--mysqldump", default="mysqldump")
    parser.add_argument("--age", default="age")
    parser.add_argument("--age-keygen", default="age-keygen")
    args = parser.parse_args()
    safe = r"[a-zA-Z0-9_]+"
    if not re.fullmatch(safe, args.source) or "_test" not in args.source:
        parser.error("A origem deve ser um banco de teste explícito.")
    if not re.fullmatch(safe + r"_restore_test", args.destination) or args.source == args.destination:
        parser.error("O destino deve ser novo, distinto e terminado em _restore_test.")
    connection = ["-h", os.environ.get("MYSQL_HOST", "localhost"),
                  "-u", os.environ.get("MYSQL_USER", "root")]

    def query(sql, database=None):
        command = [args.mysql, *connection, "--batch", "--skip-column-names"]
        if database:
            command.append(database)
        return subprocess.check_output(command, input=sql.encode(), stderr=subprocess.PIPE).decode().strip()

    if query("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='" + args.destination + "';") != "0":
        raise RuntimeError("O destino já existe; nenhum dado foi sobrescrito.")
    tables = query("SHOW TABLES;", args.source).splitlines()
    if not tables:
        raise RuntimeError("Origem vazia: exercício não seria representativo.")
    # Nomes retornados pelo banco também são validados antes de compor SQL.
    if any(not re.fullmatch(safe, name) for name in tables):
        raise RuntimeError("Nome de tabela fora do padrão permitido.")
    started = time.monotonic()
    output = Path("artifacts/recovery")
    output.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="precisei-recovery-") as temporary:
        key = Path(temporary) / "exercise.key"
        subprocess.run([args.age_keygen, "-o", str(key)], check=True, capture_output=True)
        recipient = subprocess.check_output([args.age_keygen, "-y", str(key)]).decode().strip()
        backup = output / "exercise.sql.age"
        with backup.open("wb") as encrypted, tempfile.TemporaryFile() as errors:
            dump = subprocess.Popen([args.mysqldump, *connection, "--single-transaction",
                "--skip-lock-tables", "--no-tablespaces", "--set-gtid-purged=OFF",
                "--column-statistics=0", args.source], stdout=subprocess.PIPE, stderr=errors)
            try:
                subprocess.run([args.age, "-r", recipient], stdin=dump.stdout,
                               stdout=encrypted, stderr=subprocess.PIPE, check=True)
            finally:
                dump.stdout.close()
                dump.wait()
            if dump.returncode:
                raise RuntimeError("Falha no dump; backup rejeitado.")
        digest = hashlib.sha256(backup.read_bytes()).hexdigest()
        backup.with_suffix(".age.sha256").write_text(digest + "\n", encoding="utf-8")
        if hashlib.sha256(backup.read_bytes()).hexdigest() != digest:
            raise RuntimeError("Checksum do backup não confere.")
        query("CREATE DATABASE `" + args.destination + "` CHARACTER SET utf8mb4;")
        with tempfile.TemporaryFile() as errors:
            decrypt = subprocess.Popen([args.age, "-d", "-i", str(key), str(backup)],
                                       stdout=subprocess.PIPE, stderr=errors)
            try:
                subprocess.run([args.mysql, *connection, args.destination], stdin=decrypt.stdout,
                               stderr=subprocess.PIPE, check=True)
            finally:
                decrypt.stdout.close()
                decrypt.wait()
            if decrypt.returncode:
                raise RuntimeError("Falha de autenticação/descriptografia; restauração rejeitada.")
    counts = {}
    for table in tables:
        before = int(query("SELECT COUNT(*) FROM `" + table + "`;", args.source))
        after = int(query("SELECT COUNT(*) FROM `" + table + "`;", args.destination))
        if before != after:
            raise RuntimeError("Contagens divergentes após restauração.")
        counts[table] = after
    constraints = int(query("SELECT COUNT(*) FROM information_schema.referential_constraints "
                            "WHERE constraint_schema='" + args.destination + "';"))
    versions = query("SELECT version FROM flyway_schema_history WHERE success=1 ORDER BY installed_rank;", args.destination).splitlines()
    evidence = {"source": args.source, "destination": args.destination, "counts": counts,
                "foreignKeys": constraints, "flywayVersions": versions, "sha256": digest,
                "seconds": round(time.monotonic() - started, 2),
                "scope": "synthetic local recovery; temporary private key destroyed"}
    (output / "result.json").write_text(json.dumps(evidence, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(evidence, indent=2))


if __name__ == "__main__":
    main()
