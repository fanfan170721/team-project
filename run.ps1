[CmdletBinding()]
param(
    [switch]$Build,
    [switch]$Check,
    [string]$DataDirectory
)

$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    $taskJarPath = Join-Path $PSScriptRoot 'target/wordtrail.jar'
    $taskNeedsBuild = $Build -or -not (Test-Path -LiteralPath $taskJarPath)
    if (-not $taskNeedsBuild) {
        $taskJarTime = (Get-Item -LiteralPath $taskJarPath).LastWriteTimeUtc
        $taskBuildInputs = @(Get-Item -LiteralPath (Join-Path $PSScriptRoot 'pom.xml'))
        $taskBuildInputs += @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src') -Recurse -File)
        $taskNeedsBuild = @($taskBuildInputs | Where-Object { $_.LastWriteTimeUtc -gt $taskJarTime }).Count -gt 0
    }

    if ($taskNeedsBuild) {
        Get-Command mvn -ErrorAction Stop | Out-Null
        & mvn -B --no-transfer-progress verify
        if ($LASTEXITCODE -ne 0) { throw 'Build failed. See the Maven output above.' }
    }

    Get-Command java -ErrorAction Stop | Out-Null
    $taskJavaArguments = @('-jar', $taskJarPath)
    if ($Check) {
        $taskJavaArguments += '--help'
    }
    elseif (-not [string]::IsNullOrWhiteSpace($DataDirectory)) {
        $taskJavaArguments += @('--data-dir', $DataDirectory)
    }
    & java @taskJavaArguments
    if ($LASTEXITCODE -ne 0) { throw 'WordTrail could not start. See the message above.' }
}
finally {
    Pop-Location
}
