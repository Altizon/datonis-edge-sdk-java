@echo off

REM Set variables
set SERVICE_NAME=Datonis_Network_Monitoring
set JAR_FILE=datonis-edge-examples-5.0.0-jar-with-dependencies.jar

REM Get the directory of this batch file
for %%I in ("%~dp0.") do (
    set "SERVICE_STARTUP_DIR=%%~fI"
    set "SERVICE_JAR_PATH=%SERVICE_STARTUP_DIR%\%JAR_FILE%"
)

echo Service JAR path is %SERVICE_JAR_PATH%

REM Define NSSM executable path
set NSSM_EXE=%~dp0\nssm.exe

REM Check if NSSM executable exists
if not exist "%NSSM_EXE%" (
    echo NSSM not found. Ensure nssm.exe is in the same directory as this batch file.
    pause
    exit /b 1
)

REM Install the service
"%NSSM_EXE%" install %SERVICE_NAME% java -Dlog4j.properties=log4j.properties -Ddatonis-edge.properties=datonis-edge.properties -jar "%SERVICE_JAR_PATH%"

REM Set the startup directory
"%NSSM_EXE%" set %SERVICE_NAME% AppDirectory "%SERVICE_STARTUP_DIR%"

REM Set service description (optional)
"%NSSM_EXE%" set %SERVICE_NAME% Description "Datonis Edge Network Monitoring Service to send data externally to Datonis"

REM Start service after 2 seconds interval
timeout /t 2 /nobreak >nul

REM Start the service immediately
"%NSSM_EXE%" start %SERVICE_NAME%

echo Service installation complete. Service started.
pause
