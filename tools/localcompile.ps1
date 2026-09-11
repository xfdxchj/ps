# Local static compile check for the End fork (bypasses Gradle / Android SDK / network).
# Compiles SPD-classes + core sources with javac against the jar set in `新建文件夹\lib`.
# Usage:  pwsh -File tools\localcompile.ps1
# Exit 0 = compile clean. Any javac error is a real CI blocker.

$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

# The jar set lives in a sibling folder whose name is non-ASCII; locate it by content
# rather than hardcoding the name (Windows PowerShell 5.1 reads this file as ANSI).
$parent = Split-Path -Parent $root
$libDir = Get-ChildItem $parent -Directory -ErrorAction SilentlyContinue |
          ForEach-Object { Join-Path $_.FullName 'lib' } |
          Where-Object { Test-Path (Join-Path $_ 'gdx-1.14.0.jar') } |
          Select-Object -First 1
if (-not $libDir) { Write-Error "jar lib (containing gdx-1.14.0.jar) not found under $parent"; exit 2 }

$out = Join-Path $parent '_javac_out'
New-Item -ItemType Directory -Force -Path $out | Out-Null

$srcs = Get-ChildItem -Recurse -Filter '*.java' -Path 'SPD-classes\src\main\java', 'core\src\main\java' |
        ForEach-Object { $_.FullName.Substring($root.Length + 1) }
[System.IO.File]::WriteAllLines("$out\sources.txt", $srcs, (New-Object System.Text.UTF8Encoding($false)))

$libs = (Get-ChildItem $libDir -Filter '*.jar' | ForEach-Object { $_.FullName }) -join ';'

# English diagnostics so the output is parseable regardless of host codepage.
$env:JAVA_TOOL_OPTIONS = '-Duser.language=en -Duser.country=US'

Write-Host "compiling $($srcs.Count) sources ..." -ForegroundColor Cyan
$raw = & javac -nowarn -encoding UTF-8 -d $out -cp $libs "@$out\sources.txt" 2>&1 | Out-String
$code = $LASTEXITCODE

$raw | Set-Content -Encoding UTF8 "$out\javac.log"

if ($code -eq 0) {
    Write-Host "COMPILE OK" -ForegroundColor Green
    exit 0
}

# Summarize: one line per error with file:line and the message.
$lines = $raw -split "`r?`n"
$errs = $lines | Where-Object { $_ -match '^(.+\.java):(\d+): error: (.*)$' }
Write-Host "COMPILE FAILED - $($errs.Count) errors" -ForegroundColor Red
$errs | ForEach-Object {
    if ($_ -match '^(.+\.java):(\d+): error: (.*)$') {
        "{0}:{1}`n    {2}" -f $matches[1], $matches[2], $matches[3]
    }
}
Write-Host "(full log: $out\javac.log)"
exit 1
