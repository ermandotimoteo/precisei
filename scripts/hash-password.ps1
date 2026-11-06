$ErrorActionPreference = 'Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    & .\mvnw.cmd -q compile dependency:build-classpath '-Dmdep.outputFile=target/classpath.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar o utilitário.' }
    $taskClasspath = 'target/classes;' + (Get-Content -Raw -LiteralPath target/classpath.txt).Trim()
    & "$env:JAVA_HOME\bin\java.exe" -cp $taskClasspath com.precisei.tools.SenhaTool @args
} finally { Pop-Location }
