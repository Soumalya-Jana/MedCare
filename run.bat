@echo off
setlocal
title Hospital Management System (MedCare HMS)

echo ========================================================
echo       Starting Hospital Management System (HMS)
echo ========================================================
echo.

:: Set Java Home to JDK 25 if present
if exist "D:\JDK 25" (
    set "JAVA_HOME=D:\JDK 25"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

:: Prefer local tools maven or maven wrapper
if exist "%~dp0.tools\apache-maven-3.9.6\bin" (
    set "PATH=%~dp0.tools\apache-maven-3.9.6\bin;%PATH%"
)

:: Verify Java
echo Checking Java version...
java -version
if %errorlevel% neq 0 (
    echo [ERROR] Java could not be started. Please ensure Java 17+ or JDK 25 is installed.
    pause
    exit /b %errorlevel%
)

:: Run Spring Boot
echo.
echo Launching Spring Boot Application...
echo The app will be available at: http://localhost:8080
echo.

if exist "%~dp0mvnw.cmd" (
    call "%~dp0mvnw.cmd" spring-boot:run
) else (
    call mvn spring-boot:run
)

pause
