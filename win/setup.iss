[Setup]
AppName=DVA
AppVerName=DVA
AppVersion={#version}
AppPublisher=Jonathan Boles
CloseApplications=yes
CloseApplicationsFilter=*.exe;*.dll;*.chm;*.jar
DefaultDirName={autopf}\DVA 5
DefaultGroupName=DVA 5
SourceDir=.
OutputDir=..\build\Output\dist
OutputBaseFilename=DVA5Setup
Compression={#innosetupcompression}
SolidCompression=yes
UninstallDisplayIcon={app}\dva.ico
ArchitecturesInstallIn64BitMode=x64compatible

WindowVisible=no
WindowShowCaption=no

[InstallDelete]
Type: filesandordirs; Name: "{app}\jre"

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
; DVA launchers
Source: "..\build\Output\win\i386\DVA.exe"; DestDir: "{app}"; Flags: replacesameversion; Check: IsX86OS
Source: "..\build\Output\win\i386\DVA.scr"; DestDir: "{sys}"; Flags: replacesameversion; Check: IsX86OS
Source: "..\build\Output\win\amd64\DVA.exe"; DestDir: "{app}"; Flags: replacesameversion; Check: IsX64OS
Source: "..\build\Output\win\amd64\DVA.scr"; DestDir: "{sys}"; Flags: replacesameversion; Check: IsX64OS
; JRE
Source: "..\build\Tools\jre\win32\*"; DestDir: "{app}\jre"; Flags: recursesubdirs replacesameversion; Check: IsX86OS
Source: "..\build\Tools\jre\win64\*"; DestDir: "{app}\jre"; Flags: recursesubdirs replacesameversion; Check: IsX64OS
Source: "..\build\Tools\jre\winarm64\*"; DestDir: "{app}\jre"; Flags: recursesubdirs replacesameversion; Check: IsARM64
; ffmpeg
Source: "..\ffmpeg\win\i686\*"; DestDir: "{app}"; Flags: replacesameversion; Check: IsX86OS
Source: "..\ffmpeg\win\amd64\*"; DestDir: "{app}"; Flags: replacesameversion; Check: IsX64Compatible
; jars
Source: "dva.ico"; DestDir: "{app}"
Source: "..\jars\*.jar"; DestDir: "{app}"
Source: "..\build\Output\*.jar"; DestDir: "{app}"
Source: "..\build\Output\*.txt"; DestDir: "{app}"

[Icons]
Name: "{group}\{cm:UninstallProgram,DVA}"; Filename: "{uninstallexe}"
; x86/x64
Name: "{group}\DVA 5"; Filename: "{app}\DVA.exe"; AppUserModelID: "jb.DVA"; Check: not IsArm64
Name: "{autodesktop}\DVA 5"; Filename: "{app}\DVA.exe"; Tasks: desktopicon; AppUserModelID: "jb.DVA"; Check: not IsArm64
; ARM64
Name: "{group}\DVA 5"; Filename: "{app}\jre\bin\javaw.exe"; Parameters: "-cp {code:CalculateClasspath} -Dsun.java2d.dpiaware=true jb.dvacommon.DVA"; WorkingDir: "{app}"; AppUserModelID: "jb.DVA"; IconFilename: "{app}\dva.ico"; Check: IsArm64
Name: "{autodesktop}\DVA 5"; Filename: "{app}\jre\bin\javaw.exe"; Parameters: "-cp {code:CalculateClasspath} -Dsun.java2d.dpiaware=true jb.dvacommon.DVA"; WorkingDir: "{app}"; AppUserModelID: "jb.DVA"; IconFilename: "{app}\dva.ico"; Tasks: desktopicon; Check: IsArm64

[Registry]
Root: HKLM; Subkey: "Software\DVA"; ValueType: string; ValueName: "working.directory"; ValueData: "{app}"; Flags: deletevalue

[Run]
; x86/x64
Filename: "{app}\DVA.exe"; Description: "{cm:LaunchProgram,DVA}"; Flags: nowait postinstall; Check: not IsArm64
Filename: "{app}\DVA.exe"; Parameters: "/x"; StatusMsg: "Updating sound libraries"; Check: not IsArm64
; ARM64
Filename: "{app}\jre\bin\javaw.exe"; Description: "{cm:LaunchProgram,DVA}"; Parameters: "-cp {code:CalculateClasspath} -Dsun.java2d.dpiaware=true jb.dvacommon.DVA"; WorkingDir: "{app}"; Flags: nowait postinstall; Check: IsArm64
Filename: "{app}\jre\bin\java.exe"; Parameters: "-cp {code:CalculateClasspath} -Dsun.java2d.dpiaware=true jb.dvacommon.DVA /x"; StatusMsg: "Updating sound libraries"; WorkingDir: "{app}"; Flags: runhidden; Check: IsArm64

[Code]
function CalculateClasspath(Param: String): String;
var
  FindRec: TFindRec;
  Str: String;
begin
  Str := '';
  if FindFirst(ExpandConstant('{app}') + '\*.jar', FindRec) then
  begin
    try
      repeat
        Str := Str + FindRec.Name + ';';
      until not FindNext(FindRec);
    finally
      FindClose(FindRec);
    end;
  end;
  Result := Str;
end;