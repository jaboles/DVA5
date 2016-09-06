#include "isxdl.iss"
#include "version.iss"

[Run]
Filename: "{code:GetJavaDownloadPath}"; Parameters: "/s"; Description: "Install Java runtime"; StatusMsg: "Installing Java runtime"; Check: "NeedToInstallJava"; BeforeInstall: "DownloadJava";

[Code]
function CheckJRE(MinVersion : String) : Boolean;
var
  JavaVer : String;
begin
    Result := false;
    if IsWin64 then
      begin
          RegQueryStringValue(HKLM64, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVer);
      end
    else
      begin
          RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVer);
      end;                                                                                                                  
    if Length( JavaVer ) > 0 then
    begin
    	if CompareVersion(JavaVer,MinVersion) >= 0 then
    	begin
    		Result := true;
    	end;
    end;
end;

function GetJavaDownloadUrl() : String;
var
    pagePath : String;
    pageContents : String;
    searchString : String;
    startIndex : Integer;
begin
    pagePath := AddBackslash(ExpandConstant('{tmp}')) + '\jre_page.htm'
    isxdl_Download(
        StrToInt(ExpandConstant('{wizardhwnd}')),
        'http://www.java.com/en/download/manual.jsp',
        pagePath);
    LoadStringFromFile(pagePath, pageContents);
    if IsWin64 then
        searchString := 'Download Java software for Windows (64-bit)" href="'
    else
        searchString := 'Download Java software for Windows Offline" href="';
    pageContents := Copy(pageContents, Pos(searchString, pageContents) + Length(searchString), 1000)
    pageContents := Copy(pageContents, 0, Pos('"', pageContents) - 1)
    Result := pageContents;
end;

function GetJavaDownloadPath(ParamUnused : String) : String;
begin
    Result := AddBackslash(ExpandConstant('{tmp}')) + '\jreinstall.exe';
end;

procedure DownloadJava();
var
    downloadURL : String;
begin
    downloadURL := GetJavaDownloadUrl()
    isxdl_Download(
        StrToInt(ExpandConstant('{wizardhwnd}')),
        downloadURL,
        GetJavaDownloadPath(''));
end;

function NeedToInstallJava() : Boolean;
begin
    Result := (not CheckJRE('1.6'));
end;
