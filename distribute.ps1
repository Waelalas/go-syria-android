# distribute.ps1 - رفع اصدار جديد
# الاستخدام: .\distribute.ps1
# ملاحظات الاصدار تُكتب في release-notes.txt مباشرة

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
Write-Host "Uploading to Firebase App Distribution..."
& "$PSScriptRoot\gradlew.bat" appDistributionUploadDebug
