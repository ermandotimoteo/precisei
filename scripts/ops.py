"""Valida entradas de operação sem eval e sem imprimir credenciais."""
import json
import os
import re
import sys
from pathlib import Path

def manifest(file):
    data = json.loads(Path(file).read_text(encoding="utf-8"))
    repo = os.environ.get("ALLOWED_IMAGE_REPOSITORY", "ghcr.io/ermandotimoteo/precisei")
    if type(data.get("version")) is not int or data.get("version") != 1 or data.get("dirty") is not False:
        raise ValueError("Candidato sem origem limpa/verificável")
    if data.get("image") != repo or not re.fullmatch(r"sha256:[a-f0-9]{64}", data.get("digest", "")):
        raise ValueError("Imagem ou digest não permitido")
    if not re.fullmatch(r"[a-f0-9]{40}", data.get("commit", "")):
        raise ValueError("Commit inválido")
    if not re.fullmatch(r"[a-f0-9]{64}", data.get("jarSha256", "")):
        raise ValueError("Checksum do JAR ausente/inválido")
    schema = data.get("schema", {})
    if schema.get("lineage") != "production" or not all(type(schema.get(k)) is int for k in ["minimum", "maximum"]):
        raise ValueError("Compatibilidade de esquema não declarada")
    if schema["minimum"] < 1 or schema["maximum"] < schema["minimum"]:
        raise ValueError("Intervalo de esquema inválido")
    return data

def require_same(first, second):
    if any(first.get(key) != second.get(key) for key in ["commit", "image", "digest", "jarSha256", "schema"]):
        raise ValueError("Candidato diferente do homologado")

if __name__ == "__main__":
    operation, file = sys.argv[1:3]
    try:
        data = manifest(file)
        if operation == "image":
            print(data["image"] + "@" + data["digest"])
        elif operation == "commit":
            print(data["commit"])
        elif operation == "compatible":
            current = int(sys.argv[3])
            if not data["schema"]["minimum"] <= current <= data["schema"]["maximum"]:
                raise ValueError("Imagem incompatível com o esquema atual")
        elif operation == "same":
            staged = manifest(sys.argv[3])
            require_same(data, staged)
        else:
            raise ValueError("Operação desconhecida")
    except (ValueError, KeyError, json.JSONDecodeError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
