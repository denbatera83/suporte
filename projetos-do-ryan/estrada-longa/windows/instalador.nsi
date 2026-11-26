Unicode true
SetCompressor /SOLID lzma
!include "MUI2.nsh"
Name "Estrada Longa"
OutFile "Instalar-Estrada-Longa.exe"
InstallDir "$LOCALAPPDATA\EstradaLonga"
RequestExecutionLevel user
!define MUI_ICON "icone.ico"
!define MUI_UNICON "icone.ico"
!define MUI_FINISHPAGE_RUN "$INSTDIR\EstradaLonga.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Abrir o Estrada Longa agora"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "PortugueseBR"
Section "Jogo"
  SetOutPath "$INSTDIR"
  File /r "dist\EstradaLonga\*.*"
  CreateShortCut "$DESKTOP\Estrada Longa.lnk" "$INSTDIR\EstradaLonga.exe" "" "$INSTDIR\icone.ico"
  CreateDirectory "$SMPROGRAMS\Estrada Longa"
  CreateShortCut "$SMPROGRAMS\Estrada Longa\Estrada Longa.lnk" "$INSTDIR\EstradaLonga.exe" "" "$INSTDIR\icone.ico"
  CreateShortCut "$SMPROGRAMS\Estrada Longa\Desinstalar.lnk" "$INSTDIR\Desinstalar.exe"
  WriteUninstaller "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\EstradaLonga" "DisplayName" "Estrada Longa"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\EstradaLonga" "UninstallString" "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\EstradaLonga" "DisplayIcon" "$INSTDIR\icone.ico"
SectionEnd
Section "Uninstall"
  Delete "$DESKTOP\Estrada Longa.lnk"
  RMDir /r "$SMPROGRAMS\Estrada Longa"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\EstradaLonga"
SectionEnd
