@echo off
setlocal enabledelayedexpansion

rem definition des variables
set "appName=myTest"
set "jarName=Kiady2375"
set "srcPath=..\src\"
set "libPath=..\lib\"
set "binPath=..\bin\"
set "tempJava=..\tempJava\"
set "testPath=D:\ITU\S4\Web_Dynamique(Mr_Naina)\mySpring_Framework\Test\"

rem création de tempJava et binPath
mkdir "%tempJava%"

:: Parcourir récursivement tous les fichiers .java dans src et les copier dans tempJava
for /r "%srcPath%" %%f in (*.java) do (
    echo %%f
    copy "%%f" "!tempJava!" /Y
)

rem compilation des src java vers bin
javac -cp %libPath%* -d %binPath% ..\tempJava\*.java

rem archivage en jar du contenu de bin
pushd "%binPath%"
jar cf "%jarName%.jar" *
popd

rem copie de jar vers lib de test Path
xcopy "%binPath%\%jarName%.jar" %testPath%\lib /Y

rem suppression de tempJava 
rmdir /s /q "%tempJava%"
