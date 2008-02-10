; Note: VERSION and TMPDIR are defined from the command line in the Makefile
;!define VERSION "3.1.0-svn3025"
;!define TMPDIR "/home/uckelman/projects/VASSAL/uckelman-working/tmp"
!define SRCDIR "${TMPDIR}/VASSAL-${VERSION}"
!define UROOT "Software\Microsoft\Windows\CurrentVersion\Uninstall\VASSAL-${VERSION}"

Name "VASSAL-${VERSION}"
OutFile "${TMPDIR}/VASSAL-${VERSION}-windows.exe"

; SetCompress auto ; (can be off or force)
; SetDatablockOptimize on ; (can be off)
; CRCCheck on ; (can be off)
; AutoCloseWindow false ; (can be true for the window go away automatically at end)
; ShowInstDetails hide ; (can be show to have them shown, or nevershow to disable)
; SetDateSave off ; (can be on to have files restored to their orginal date)

InstallDir "$PROGRAMFILES\VASSAL\VASSAL-${VERSION}"
InstallDirRegKey HKLM "SOFTWARE\vassalengine.org\VASSAL-${VERSION}" ""
DirText "Select the directory in which to install VASSAL-${VERSION}:"

Section "" 
  SetOutPath "$INSTDIR"
  File "${SRCDIR}/*.exe"
  File "${SRCDIR}/*.bat"
    
  SetOutPath "$INSTDIR\lib"
  File /r "${SRCDIR}/lib/*"

  WriteRegStr HKLM "SOFTWARE\vassalengine.org\VASSAL-${VERSION}" "" "$INSTDIR"

  WriteRegStr HKLM ${UROOT} "DisplayName" "VASSAL (${VERSION})"
  WriteRegStr HKLM ${UROOT} "DisplayVersion" "${VERSION}"
  WriteRegStr HKLM ${UROOT} "InstallLocation" "$INSTDIR"
  WriteRegStr HKLM ${UROOT} "UninstallString" '"$INSTDIR\uninst.exe"'
  WriteRegStr HKLM ${UROOT} "Publisher" "vassalengine.org"
  WriteRegStr HKLM ${UROOT} "URLInfoAbout" "http://www.vassalengine.org"
  WriteRegStr HKLM ${UROOT} "URLUpdateInfo" "http://www.vassalengine.org"
  WriteRegDWORD HKLM ${UROOT} "NoModify" 0x00000001
  WriteRegDWORD HKLM ${UROOT} "NoRepair" 0x00000001

  WriteUninstaller "$INSTDIR\uninst.exe"
SectionEnd 

UninstallText "This will uninstall VASSAL-${VERSION} from your system."

Section Uninstall
  ; delete whatever files/registry keys/etc you installed here.
  Delete "$INSTDIR\uninst.exe"
  DeleteRegKey HKLM "SOFTWARE\vassalengine.org\VASSAL-${VERSION}"
  DeleteRegKey HKLM ${UROOT}
  RMDir /r "$INSTDIR"
  RMDir "$PROGRAMFILES\VASSAL" 
SectionEnd 
