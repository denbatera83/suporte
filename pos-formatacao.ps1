# ============================================================
#  Denilson Suporte Remoto - Script de pos-formatacao
#  Instala os programas padrao e atualiza o Windows recem-formatado.
#  Uso (PowerShell como Administrador):
#    irm https://raw.githubusercontent.com/denbatera83/suporte/main/pos-formatacao.ps1 | iex
# ============================================================

$ErrorActionPreference = "Continue"
$log = "$env:PUBLIC\Desktop\pos-formatacao-log.txt"
Start-Transcript -Path $log -Append | Out-Null

function Titulo($texto) { Write-Host "`n=== $texto ===" -ForegroundColor Cyan }

# --- 1. Precisa ser administrador ---------------------------------
$admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()
  ).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $admin) {
  Write-Host "Abra o PowerShell como Administrador e rode de novo." -ForegroundColor Red
  Stop-Transcript | Out-Null
  return
}

# --- 2. Impede o PC de dormir durante a instalacao ----------------
powercfg /change standby-timeout-ac 0
powercfg /change monitor-timeout-ac 0

# --- 3. Confere o winget ------------------------------------------
Titulo "Verificando o winget"
if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
  Write-Host "winget nao encontrado. Atualize o 'Instalador de Aplicativo' pela Microsoft Store e rode de novo." -ForegroundColor Red
  Stop-Transcript | Out-Null
  return
}
winget source update | Out-Null

# --- 4. Lista de programas (edite aqui) ---------------------------
#  Para achar o ID de outro programa: winget search nome
$programas = @(
  @{ Nome = "Google Chrome";          Id = "Google.Chrome" },
  @{ Nome = "Mozilla Firefox";        Id = "Mozilla.Firefox.pt-BR" },
  @{ Nome = "7-Zip";                  Id = "7zip.7zip" },
  @{ Nome = "VLC";                    Id = "VideoLAN.VLC" },
  @{ Nome = "Adobe Acrobat Reader";   Id = "Adobe.Acrobat.Reader.64-bit" },
  @{ Nome = "LibreOffice";            Id = "TheDocumentFoundation.LibreOffice" },
  @{ Nome = "Visual C++ Runtimes";    Id = "Microsoft.VCRedist.2015+.x64" },
  @{ Nome = ".NET Desktop Runtime 8"; Id = "Microsoft.DotNet.DesktopRuntime.8" },
  @{ Nome = "RustDesk";               Id = "RustDesk.RustDesk" }
)

# --- 5. Instalacao ------------------------------------------------
$ok = @(); $falhou = @()
foreach ($p in $programas) {
  Titulo "Instalando $($p.Nome)"
  winget install --id $p.Id --exact --silent --accept-package-agreements --accept-source-agreements
  if ($LASTEXITCODE -eq 0 -or $LASTEXITCODE -eq -1978335189) { $ok += $p.Nome }  # 0 = ok / ja instalado
  else { $falhou += $p.Nome }
}

# WhatsApp (vem da Microsoft Store)
Titulo "Instalando WhatsApp"
winget install --id 9NKSQGP7F2NH --source msstore --accept-package-agreements --accept-source-agreements
if ($LASTEXITCODE -eq 0) { $ok += "WhatsApp" } else { $falhou += "WhatsApp" }

# --- 6. Atualiza o que ja veio no Windows -------------------------
Titulo "Atualizando programas instalados"
winget upgrade --all --silent --accept-package-agreements --accept-source-agreements

# --- 7. Abre o Windows Update para conferencia --------------------
Start-Process "ms-settings:windowsupdate"

# --- 8. Volta a configuracao de energia ---------------------------
powercfg /change standby-timeout-ac 30
powercfg /change monitor-timeout-ac 10

# --- 9. Resumo ----------------------------------------------------
Titulo "RESUMO"
Write-Host "Instalados: $($ok -join ', ')" -ForegroundColor Green
if ($falhou.Count -gt 0) { Write-Host "Falharam (instalar manualmente): $($falhou -join ', ')" -ForegroundColor Yellow }
Write-Host "`nLog salvo na Area de Trabalho: pos-formatacao-log.txt"
Write-Host "Confira o Windows Update na janela que abriu."
Stop-Transcript | Out-Null
