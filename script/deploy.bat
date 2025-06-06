@echo off
setlocal enabledelayedexpansion

rem definition des variables
set "appName=myTest"
set "jarName=mySpring_Framework"
set "srcPath=..\src\"
set "libPath=..\lib\"
set "binPath=..\bin\"
set "tempJava=..\tempJava\"
set "testPath=D:\Studies\ITU\S6\Framework\POC\app\"
set "errorHandler=..\error_manager\error.jsp"

rem création de tempJava et binPath
mkdir "%tempJava%"
mkdir "%binPath%"

:: Parcourir récursivement tous les fichiers .java dans src et les copier dans tempJava
for /r "%srcPath%" %%f in (*.java) do (
    copy "%%f" "!tempJava!" /Y
)

rem compilation des src java vers bin
javac -cp %libPath%* -d %binPath% ..\tempJava\*.java

rem Vérifier si la compilation a échoué
if %errorlevel% neq 0 (
    echo Erreur de compilation.
    exit /b %errorlevel%
)

rem archivage en jar du contenu de bin
pushd "%binPath%"
jar cf "%jarName%.jar" *
popd

rem copie de jar vers lib de test Path
xcopy "%binPath%\%jarName%.jar" %testPath%\lib /Y
xcopy "%libPath%" %testPath%\lib /Y

rem copie de error manager vers test Path 
xcopy "%errorHandler%" %testPath%\lib /Y

rem suppression de tempJava et binPath
rmdir /s /q "%tempJava%"
rmdir /s /q "%binPath%"

echo Framework built successfully !