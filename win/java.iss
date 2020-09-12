#include "idp.iss"
#include "version.iss"

[Run]
Filename: "{code:GetJavaDownloadPath}"; Parameters: "/s"; Description: "Install Java runtime"; StatusMsg: "Installing Java runtime"; Check: "NeedToInstallJava";

[Code]
const RequiredVersion = '1.6';
type TJREResult = (None, Has32, Has64);

function CheckJRE() : TJREResult;
var
    JavaVer : String;
    CheckResult : TJREResult;
begin
    Result := false;
    CheckResult := None;
    if IsWin64 then
        begin
            RegQueryStringValue(HKLM64, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVer);
            CheckResult := Has64;
        end;

    if Length(JavaVer) = 0 then
    begin
        RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVer);
        CheckResult := Has32;
    end;

    if Length(JavaVer) > 0 then
    begin
    	Log('An existing Java version ' + JavaVer + ' was found.')
    	if CompareVersion(JavaVer,RequiredVersion) >= 0 then
    	begin
            Log('The found Java version is not new enough. Required minimum ' + RequiredVersion);
    	    CheckResult := None;
    	end;
    end
    else
    begin
        CheckResult := None;
        Log('No existing Java version was found.');
    end;
    Result := CheckResult;
end;

function GetJavaDownloadUrl() : String;
var
    pagePath : String;
    pageContents : String;
    searchString : String;
    startIndex : Integer;
begin
    pagePath := AddBackslash(ExpandConstant('{tmp}')) + '\jre_page.htm'
    Log('Saving JRE download page to: ' + pagePath);
    idpDownloadFile(
        'https://www.java.com/en/download/manual.jsp',
        pagePath);
    LoadStringFromFile(pagePath, pageContents);
    if IsWin64 then
        searchString := 'Download Java software for Windows (64-bit)" href="'
    else
        searchString := 'Download Java software for Windows Offline" href="';
    pageContents := Copy(pageContents, Pos(searchString, pageContents) + Length(searchString), 1000)
    pageContents := Copy(pageContents, 0, Pos('"', pageContents) - 1)
    Log('Java download URL is ' + pageContents);
    Result := pageContents;
end;

function GetJavaDownloadPath(ParamUnused : String) : String;
begin
    Result := AddBackslash(ExpandConstant('{tmp}')) + '\jreinstall.exe';
end;

function NeedToInstallJava() : Boolean;
begin
    Result := (CheckJRE = None);
end;

function InitializeSetup() : Boolean;
begin
    if NeedToInstallJava() then
    begin
    	MsgBox('This application requires the Java Runtime Environment v' + RequiredVersion + ' or newer to run. As part of the installation of this software, the newest version of Java will be downloaded and installed. This may take several minutes.',
    	  mbConfirmation, MB_OK);
    end;
    Result := true;
end;

procedure InitializeWizard();
var
    downloadURL : String;
begin
    if NeedToInstallJava then
    begin
        downloadURL := GetJavaDownloadUrl()
        idpAddFile(
            downloadURL,
            GetJavaDownloadPath(''));
        idpDownloadAfter(wpReady);
    end;
end;
