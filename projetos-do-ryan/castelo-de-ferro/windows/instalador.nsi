Unicode true
SetCompressor /SOLID lzma
!include "MUI2.nsh"
Name "Castelo de Ferro"
OutFile "Instalar-Castelo-de-Ferro.exe"
InstallDir "$LOCALAPPDATA\CasteloDeFerro"
RequestExecutionLevel user
!define MUI_ICON "icone.ico"
!define MUI_UNICON "icone.ico"
!define MUI_FINISHPAGE_RUN "$INSTDIR\CasteloDeFerro.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Abrir o Castelo de Ferro agora"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "PortugueseBR"
Section "Jogo"
  SetOutPath "$INSTDIR"
  File /r "dist\CasteloDeFerro\*.*"
  CreateShortCut "$DESKTOP\Castelo de Ferro.lnk" "$INSTDIR\CasteloDeFerro.exe" "" "$INSTDIR\icone.ico"
  CreateDirectory "$SMPROGRAMS\Castelo de Ferro"
  CreateShortCut "$SMPROGRAMS\Castelo de Ferro\Castelo de Ferro.lnk" "$INSTDIR\CasteloDeFerro.exe" "" "$INSTDIR\icone.ico"
  CreateShortCut "$SMPROGRAMS\Castelo de Ferro\Desinstalar.lnk" "$INSTDIR\Desinstalar.exe"
  WriteUninstaller "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CasteloDeFerro" "DisplayName" "Castelo de Ferro"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CasteloDeFerro" "UninstallString" "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CasteloDeFerro" "DisplayIcon" "$INSTDIR\icone.ico"
SectionEnd
Section "Uninstall"
  Delete "$DESKTOP\Castelo de Ferro.lnk"
  RMDir /r "$SMPROGRAMS\Castelo de Ferro"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\CasteloDeFerro"
SectionEnd
