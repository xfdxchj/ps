# 本地构建并运行（用于验证游戏内表现）
#   1) javac 编译 SPD-classes + core -> _javac_out
#   2) 打包 core jar（class）
#   3) 打包 desktop jar（6 个 desktop class + 全部 assets）—— 资源必须在这里，因为 classpath 里 desktop 优先
#   4) 覆盖到 新建文件夹\lib 并启动游戏
#
# 用法: & E:\破碎的地牢\_EndShatteredBuild\tools\runlocal.ps1

$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

$parent = Split-Path -Parent $root
$libDir = Get-ChildItem $parent -Directory -ErrorAction SilentlyContinue |
          ForEach-Object { Join-Path $_.FullName 'lib' } |
          Where-Object { Test-Path (Join-Path $_ 'gdx-1.14.0.jar') } |
          Select-Object -First 1
if (-not $libDir) { Write-Error "找不到依赖 lib 目录"; exit 2 }
$appHome = Split-Path -Parent $libDir

$out    = Join-Path $parent '_javac_out'
$assets = Join-Path $root 'core\src\main\assets'
$deskSrc= Join-Path $root 'desktop\src\main\java'

Write-Host "依赖: $libDir"
Write-Host "游戏: $appHome"
Write-Host "资源: $assets"

# ---------- 1) 编译 ----------
$srcs = Get-ChildItem -Recurse -Filter '*.java' -Path 'SPD-classes\src\main\java','core\src\main\java' |
        ForEach-Object { $_.FullName.Substring($root.Length + 1) }
[System.IO.File]::WriteAllLines("$out\sources.txt", $srcs, (New-Object System.Text.UTF8Encoding($false)))
$libs = (Get-ChildItem $libDir -Filter '*.jar' | ForEach-Object { $_.FullName }) -join ';'
$env:JAVA_TOOL_OPTIONS = '-Duser.language=en -Duser.country=US'

Write-Host "`n[1/4] 编译 $($srcs.Count) 源文件 ..." -ForegroundColor Cyan
if (Test-Path "$out\com") { Remove-Item "$out\com" -Recurse -Force }
$raw = & javac -nowarn -encoding UTF-8 -d $out -cp $libs "@$out\sources.txt" 2>&1 | Out-String
$code = $LASTEXITCODE
$raw | Set-Content -Encoding UTF8 "$out\javac.log"
if ($code -ne 0) {
    Write-Host "编译失败! 见 $out\javac.log" -ForegroundColor Red
    ($raw -split "`r?`n" | Where-Object { $_ -match 'error:' } | Select-Object -First 15) | ForEach-Object { Write-Host "  $_" }
    exit 1
}
Write-Host "编译 OK" -ForegroundColor Green

# ---------- 2) core jar ----------
Write-Host "`n[2/4] 打包 core jar ..." -ForegroundColor Cyan
$coreJar = Join-Path $env:TEMP 'core-end-new.jar'
if (Test-Path $coreJar) { Remove-Item $coreJar -Force }
& jar cf $coreJar -C $out com
Write-Host "  core: $([math]::Round((Get-Item $coreJar).Length/1MB,1)) MB" -ForegroundColor Green

# ---------- 3) desktop jar（含资源！）----------
Write-Host "`n[3/4] 打包 desktop jar（含 assets）..." -ForegroundColor Cyan
$deskJar = Join-Path $env:TEMP 'desktop-end-new.jar'
if (Test-Path $deskJar) { Remove-Item $deskJar -Force }

# 先编译 desktop 模块
$deskOut = Join-Path $env:TEMP 'desk_out'
Remove-Item $deskOut -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $deskOut | Out-Null
if (Test-Path $deskSrc) {
    $dsrc = Get-ChildItem -Recurse -Filter '*.java' -Path $deskSrc | ForEach-Object { $_.FullName }
    & javac -nowarn -encoding UTF-8 -d $deskOut -cp "$coreJar;$libs" $dsrc 2>&1 | Out-Null
    Write-Host "  desktop 模块已编译"
} else {
    Write-Host "  ⚠ 没有 desktop 源码，尝试复用旧 jar 里的 class" -ForegroundColor Yellow
    $old = Join-Path $libDir 'desktop-0.0.1-end.jar'
    if (Test-Path $old) {
        & jar xf $old com 2>&1 | Out-Null
        if (Test-Path ".\com") { Move-Item ".\com" $deskOut -Force }
    }
}

# 组包：desktop class + 全部 assets
& jar cf $deskJar -C $deskOut . -C $assets . 2>&1 | Out-Null
Write-Host "  desktop: $([math]::Round((Get-Item $deskJar).Length/1MB,1)) MB" -ForegroundColor Green

# ---------- 4) 部署 ----------
Write-Host "`n[4/4] 部署 ..." -ForegroundColor Cyan
Copy-Item $coreJar (Join-Path $libDir 'core-0.0.1-end.jar') -Force
Copy-Item $deskJar (Join-Path $libDir 'desktop-0.0.1-end.jar') -Force
Write-Host "已覆盖 core + desktop jar" -ForegroundColor Green

$bat = Join-Path $appHome 'bin\desktop.bat'
if (Test-Path $bat) {
    Write-Host "`n启动游戏 ..." -ForegroundColor Yellow
    & $bat
} else {
    Write-Host "请手动运行: $bat" -ForegroundColor Red
}
