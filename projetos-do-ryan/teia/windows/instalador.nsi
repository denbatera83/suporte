Unicode true
SetCompressor /SOLID lzma
!include "MUI2.nsh"
Name "TEIA"
OutFile "Instalar-TEIA.exe"
InstallDir "$LOCALAPPDATA\Teia"
RequestExecutionLevel user
!define MUI_ICON "icone.ico"
!define MUI_UNICON "icone.ico"
!define MUI_FINISHPAGE_RUN "$INSTDIR\Teia.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Abrir o TEIA agora"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "PortugueseBR"
Section "Jogo"
  SetOutPath "$INSTDIR"
  File /r "dist\Teia\*.*"
  CreateShortCut "$DESKTOP\TEIA.lnk" "$INSTDIR\Teia.exe" "" "$INSTDIR\icone.ico"
  CreateDirectory "$SMPROGRAMS\TEIA"
  CreateShortCut "$SMPROGRAMS\TEIA\TEIA.lnk" "$INSTDIR\Teia.exe" "" "$INSTDIR\icone.ico"
  CreateShortCut "$SMPROGRAMS\TEIA\Desinstalar.lnk" "$INSTDIR\Desinstalar.exe"
  WriteUninstaller "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Teia" "DisplayName" "TEIA"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Teia" "UninstallString" "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Teia" "DisplayIcon" "$INSTDIR\icone.ico"
SectionEnd
Section "Uninstall"
  Delete "$DESKTOP\TEIA.lnk"
  RMDir /r "$SMPROGRAMS\TEIA"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Teia"
SectionEnd
