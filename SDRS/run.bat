@echo off
cd /d "%~dp0"
if not exist out\sdrs\Main.class (
    echo Application not compiled yet. Run compile.bat first.
    pause
    exit /b 1
)
java -cp out sdrs.Main
pause
