@echo off
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED - see errors above.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo.
echo Compiled successfully. Run the application with run.bat
pause
