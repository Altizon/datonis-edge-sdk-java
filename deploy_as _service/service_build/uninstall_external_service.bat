@echo off

REM Set variables
set SERVICE_NAME=Datonis_Network_Monitoring

REM Define NSSM executable path
set NSSM_EXE=%~dp0\nssm.exe

REM Stop the service
"%NSSM_EXE%" stop %SERVICE_NAME% > nul 2>&1

REM Uninstall the service
"%NSSM_EXE%" remove %SERVICE_NAME% confirm

echo Service %SERVICE_NAME% stopped and uninstalled successfully.

pause
