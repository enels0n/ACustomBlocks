@echo off
setlocal
set "MVN_HOME=%~dp0.tools\apache-maven-3.9.9"
"%MVN_HOME%\bin\mvn.cmd" %*
