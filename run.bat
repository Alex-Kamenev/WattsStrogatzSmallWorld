@echo off
setlocal
if not exist "%~dp0target\SmallWorld.jar" (
  echo Build the application first: python build.py
  exit /b 1
)
java -Xmx512m -jar "%~dp0target\SmallWorld.jar"
