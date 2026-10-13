@echo off
setlocal
echo ===== Employee Management System - requirements check =====
set MISSING=0
call :chk "Java (need 17+)" "java -version"
call :chk "Maven" "mvn -version"
call :chk "Node.js" "node -v"
call :chk "npm" "npm -v"
call :chk "MySQL client" "mysql --version"
echo.
echo --- Ports (should be free for 8080 and 3000; 3306 should be IN USE by MySQL) ---
for %%P in (8080 3000 3306) do (
  netstat -ano | findstr /R /C:":%%P .*LISTENING" >nul && (echo Port %%P: IN USE) || (echo Port %%P: free)
)
echo.
if %MISSING%==0 (echo RESULT: all tools found.) else (echo RESULT: %MISSING% tool^(s^) missing - install them, then run this again.)
pause
exit /b

:chk
echo.
echo [%~1]
%~2 >nul 2>&1
if errorlevel 1 (echo   MISSING / not on PATH & set /a MISSING+=1) else (%~2 2^>^&1 | findstr /N "^" | findstr "^1:" & echo   OK)
exit /b
