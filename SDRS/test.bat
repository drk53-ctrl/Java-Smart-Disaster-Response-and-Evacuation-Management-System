@echo off
cd /d "%~dp0"
if not exist out\sdrs\test\TestHarness.class (
    echo Project not compiled yet. Run compile.bat first.
    pause
    exit /b 1
)
java -cp out sdrs.test.TestHarness
pause
