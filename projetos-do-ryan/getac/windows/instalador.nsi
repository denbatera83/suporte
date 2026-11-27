Unicode true
SetCompressor /SOLID lzma
!include "MUI2.nsh"
Name "GETAC"
OutFile "Instalar-GETAC.exe"
InstallDir "$LOCALAPPDATA\Getac"
RequestExecutionLevel user
!define MUI_ICON "icone.ico"
!define MUI_UNICON "icone.ico"
!define MUI_FINISHPAGE_RUN "$INSTDIR\Getac.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Abrir o GETAC agora"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "PortugueseBR"
Section "Jogo"
  SetOutPath "$INSTDIR"
  File /r "dist\Getac\*.*"
  CreateShortCut "$DESKTOP\GETAC.lnk" "$INSTDIR\Getac.exe" "" "$INSTDIR\icone.ico"
  CreateDirectory "$SMPROGRAMS\GETAC"
  CreateShortCut "$SMPROGRAMS\GETAC\GETAC.lnk" "$INSTDIR\Getac.exe" "" "$INSTDIR\icone.ico"
  CreateShortCut "$SMPROGRAMS\GETAC\Desinstalar.lnk" "$INSTDIR\Desinstalar.exe"
  WriteUninstaller "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Getac" "DisplayName" "GETAC"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Getac" "UninstallString" "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Getac" "DisplayIcon" "$INSTDIR\icone.ico"
SectionEnd
Section "Uninstall"
  Delete "$DESKTOP\GETAC.lnk"
  RMDir /r "$SMPROGRAMS\GETAC"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Getac"
SectionEnd
