@echo off
REM Delegates Gradle wrapper calls from the workspace root to the notes_frontend module.
setlocal
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%\notes_frontend"
call .\gradlew.bat %*
endlocal
