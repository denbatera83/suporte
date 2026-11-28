Unicode true
SetCompressor /SOLID lzma
!include "MUI2.nsh"
Name "RYAN STATION"
OutFile "Instalar-RYAN-STATION.exe"
InstallDir "$LOCALAPPDATA\RyanStation"
RequestExecutionLevel user
!define MUI_ICON "icone.ico"
!define MUI_UNICON "icone.ico"
!define MUI_FINISHPAGE_RUN "$INSTDIR\RyanStation.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Abrir o RYAN STATION agora"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "PortugueseBR"
Section "Jogo"
  SetOutPath "$INSTDIR"
  File /r "dist\RyanStation\*.*"
  CreateShortCut "$DESKTOP\RYAN STATION.lnk" "$INSTDIR\RyanStation.exe" "" "$INSTDIR\icone.ico"
  CreateDirectory "$SMPROGRAMS\RYAN STATION"
  CreateShortCut "$SMPROGRAMS\RYAN STATION\RYAN STATION.lnk" "$INSTDIR\RyanStation.exe" "" "$INSTDIR\icone.ico"
  CreateShortCut "$SMPROGRAMS\RYAN STATION\Desinstalar.lnk" "$INSTDIR\Desinstalar.exe"
  WriteUninstaller "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\RyanStation" "DisplayName" "RYAN STATION"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\RyanStation" "UninstallString" "$INSTDIR\Desinstalar.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\RyanStation" "DisplayIcon" "$INSTDIR\icone.ico"
SectionEnd
Section "Uninstall"
  Delete "$DESKTOP\RYAN STATION.lnk"
  RMDir /r "$SMPROGRAMS\RYAN STATION"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\RyanStation"
SectionEnd
