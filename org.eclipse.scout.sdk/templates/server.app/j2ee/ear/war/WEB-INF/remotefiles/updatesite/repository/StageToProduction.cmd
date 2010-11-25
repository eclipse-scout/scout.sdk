@echo off
set JAVA_HOME=C:\j2sdk1.5.0_12
for %%f in (com.bsiag.fenix_*.jar) do %JAVA_HOME%\bin\java -jar %%f stage @@ALIAS@@_local.xml X:\Tomcat_5_5_prod\webapps\@@ALIAS@@\@@ALIAS@@_1.0.0.xml
pause
