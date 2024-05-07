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
set "webappsPath=D:\ITU\Server\apache-tomcat-10.1.7\webapps\"

rem suppression de test
rmdir /s /q "%testPath%"

rem re-création de test
mkdir "%testPath%"

rem création de tempJava et binPath
mkdir "%tempJava%"
mkdir "%binPath%"

:: Parcourir récursivement tous les fichiers .java dans src et les copier dans tempJava
for /r "%srcPath%" %%f in (*.java) do (
    echo %%f
    copy "%%f" "!tempJava!" /Y
)

rem compilation des src java vers bin
javac -cp %libPath%* -d %binPath% ..\tempJava\*.java

rem architecture de Test
mkdir "%testPath%\WEB-INF"
mkdir "%testPath%\WEB-INF\lib"

rem archivage en jar du contenu de bin
pushd "%binPath%"
jar cf "%jarName%.jar" *
popd

rem copie de jar vers lib de test Path
xcopy "%binPath%\%jarName%.jar" %testPath%\WEB-INF\lib /Y

rem suppression de tempJava 
rmdir /s /q "%tempJava%"

rem suppression de binPath
rmdir /s /q "%binPath%"

rem archivage du contenu de Test
pushd "%testPath%"
jar cf "%appName%.war" *
popd

xcopy "%testPath%\%appName%.war" %webappsPath% /Y