@echo off
chcp 65001 >nul
cd /d "%~dp0"

rem 本机 JAVA_HOME 未指向 JDK 17 时兜底（其他机器请装 JDK 17 并配置 JAVA_HOME）
if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME=C:\Users\37549\Tools\jdk-17"

rem 已有实例在监听 18080 时，clean 会被日志句柄挡住；先检测
netstat -ano | findstr ":18080" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo 检测到 18080 已被占用：已有轻享图文实例在运行（停止 = 关闭 Qingxiang-Tomcat 窗口）。
    start "" http://127.0.0.1:18080/qingxiang/login
    pause
    exit /b 0
)

echo [1/3] 打包（跳过测试）...
call mvn -B -DskipTests clean package
if errorlevel 1 (
    echo 打包失败，看上面的报错。
    pause
    exit /b 1
)

echo [2/3] 新窗口启动 Tomcat（停止 = 关闭该窗口或 Ctrl+C）...
start "Qingxiang-Tomcat" cmd /k mvn -B -Prun cargo:run

echo [3/3] 等待 http://127.0.0.1:18080/qingxiang/login 就绪...
set /a n=0
:wait
ping -n 3 127.0.0.1 >nul
curl -sf -o NUL --max-time 3 http://127.0.0.1:18080/qingxiang/login && goto ready
set /a n+=1
if %n% lss 45 goto wait
echo 两分钟还没起来，去 Qingxiang-Tomcat 窗口看报错（常见：MySQL 没启动、db.properties 没配）。
pause
exit /b 1

:ready
start "" http://127.0.0.1:18080/qingxiang/
echo.
echo 已打开 http://127.0.0.1:18080/qingxiang/
echo 演示账号：reviewer/123456、creator/123456、reader/123456
echo 停止服务 = 关闭 Qingxiang-Tomcat 窗口。
pause
