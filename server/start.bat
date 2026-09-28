@echo off
rem Updates the imu plugins from the latest GitHub build, then starts the server.
rem Put this next to paper.jar. If GitHub can't be reached, the server starts with
rem the plugins it already has.
setlocal
cd /d "%~dp0"

set "BASE=https://github.com/imuuu/minecraft-plugins/releases/download/latest"
set "VERSION_FILE=plugins\.imus-version"
set "TMP_DIR=%TEMP%\imus-update"
rem Windows' own curl and tar, not whatever else (Git Bash, ...) is first on PATH
set "CURL=%SystemRoot%\System32\curl.exe"
set "TAR=%SystemRoot%\System32\tar.exe"

if not exist plugins mkdir plugins
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%"
mkdir "%TMP_DIR%"

echo Checking for plugin updates...
"%CURL%" -fsSL -o "%TMP_DIR%\version.txt" "%BASE%/version.txt"
if errorlevel 1 goto offline

set "REMOTE="
set "LOCAL="
set /p REMOTE=<"%TMP_DIR%\version.txt"
if exist "%VERSION_FILE%" set /p LOCAL=<"%VERSION_FILE%"
if "%REMOTE%"=="%LOCAL%" goto uptodate

echo New build %REMOTE:~0,7% found, downloading...
"%CURL%" -fsSL -o "%TMP_DIR%\plugins.zip" "%BASE%/plugins.zip"
if errorlevel 1 goto failed
mkdir "%TMP_DIR%\jars"
"%TAR%" -xf "%TMP_DIR%\plugins.zip" -C "%TMP_DIR%\jars"
if errorlevel 1 goto failed
copy /y "%TMP_DIR%\jars\*.jar" plugins >nul
if errorlevel 1 goto failed
>"%VERSION_FILE%" echo %REMOTE%
echo Plugins updated to %REMOTE:~0,7%.
goto start

:uptodate
echo Plugins are up to date: %LOCAL:~0,7%
goto start

:offline
echo Could not reach GitHub, starting with the current plugins.
goto start

:failed
echo Update failed, starting with the current plugins.
goto start

:start
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%"
java -Xms1G -Xmx2G -jar paper.jar --nogui
