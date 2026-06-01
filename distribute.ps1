# توزيع إصدار جديد على المختبرين
param([string]$Notes = "")

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"

if ($Notes -ne "") {
    Set-Content -Path "$PSScriptRoot\release-notes.txt" -Value $Notes -Encoding UTF8
}

Write-Host "جاري الرفع..."
& "$PSScriptRoot\gradlew.bat" appDistributionUploadDebug
