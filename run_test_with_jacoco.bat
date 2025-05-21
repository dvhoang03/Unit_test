@echo off
setlocal

:: Đường dẫn thư viện JaCoCo
set JACOCO_AGENT=lib\jacocoagent.jar
set JACOCO_CLI=lib\jacococli.jar

:: Tạo thư mục build nếu chưa có
if not exist build\classes mkdir build\classes

echo ========================================
echo Biên dịch mã nguồn và mã kiểm thử
echo ========================================

:: Lấy danh sách tất cả các file .java
dir /b /s src\*.java > sources.txt
dir /b /s test\*.java >> sources.txt

:: Biên dịch
javac -encoding UTF-8 -d build\classes -cp "lib/*" @sources.txt
if %errorlevel% neq 0 (
    echo ❌ Biên dịch thất bại!
    pause
    exit /b
)

echo ========================================
echo Chạy test kèm theo JaCoCo Agent
echo ========================================
java -javaagent:"%JACOCO_AGENT%"=destfile=jacoco.exec ^
 -cp "build/classes;lib/*" org.junit.runner.JUnitCore ^
 RTDRestaurant.Controller.Service.ServiceUserIT ^
 RTDRestaurant.Controller.Service.ServiceMailIT ^
 RTDRestaurant.Controller.Service.ServiceStaffTest ^
 RTDRestaurant.Controller.Service.ServiceAdminTest ^
 RTDRestaurant.Controller.Service.ServiceCustomerTest

echo ========================================
echo Tạo báo cáo HTML bằng JaCoCo
echo ========================================
java -jar "%JACOCO_CLI%" report jacoco.exec ^
  --classfiles build\classes\RTDRestaurant\Controller\Service ^
  --sourcefiles src\RTDRestaurant\Controller\Service ^
  --html report

echo ========================================
echo Mở báo cáo
echo ========================================
start report\index.html
pause
