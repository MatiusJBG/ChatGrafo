@echo off
REM Script para compilar y ejecutar el Social Media Relationship Analyzer con GraphStream

setlocal
set CLASSPATH=librarys\gs-core-2.0.jar;librarys\gs-ui-swing-2.0.jar;bin

REM Compilar todos los .java respetando la estructura de paquetes
javac -cp "%CLASSPATH%" -d bin src/app/Main.java src/app/SocialNetworkApp.java src/user/User.java src/graph/SocialGraph.java src/graph/GraphTraversal.java src/graph/InteractiveGraph.java src/analysis/SocialAnalysis.java src/community/Community.java
if %ERRORLEVEL% neq 0 (
    echo Error en la compilacion.
    exit /b %ERRORLEVEL%
)

java -cp "%CLASSPATH%" app.Main
endlocal
