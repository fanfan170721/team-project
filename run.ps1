[CmdletBinding()]
param(
    [switch]$Build,
    [switch]$Check,
    [string]$DataDirectory
)

$ErrorActionPreference = 'Stop'

function Get-LauncherInputFingerprint {
    $taskInputPaths = [System.Collections.Generic.List[string]]::new()
    $taskInputPaths.Add('pom.xml')
    $taskInputPaths.Add('run.ps1')
    foreach ($taskInputDirectory in @('src', 'config/checkstyle')) {
        $taskDirectoryPath = Join-Path $PSScriptRoot $taskInputDirectory
        if (Test-Path -LiteralPath $taskDirectoryPath -PathType Container) {
            foreach ($taskInputFile in Get-ChildItem -LiteralPath $taskDirectoryPath -Recurse -File -Force) {
                $taskRelativePath = $taskInputFile.FullName.Substring($PSScriptRoot.Length + 1).Replace('\', '/')
                $taskInputPaths.Add($taskRelativePath)
            }
        }
    }
    $taskSortedPaths = $taskInputPaths.ToArray()
    [Array]::Sort($taskSortedPaths, [StringComparer]::Ordinal)
    $taskManifest = [System.Text.StringBuilder]::new()
    [void]$taskManifest.Append("wordtrail-launcher-inputs-v1`n")
    foreach ($taskInputPath in $taskSortedPaths) {
        $taskAbsolutePath = Join-Path $PSScriptRoot $taskInputPath
        $taskHash = 'MISSING'
        if (Test-Path -LiteralPath $taskAbsolutePath -PathType Leaf) {
            $taskHash = (Get-FileHash -LiteralPath $taskAbsolutePath -Algorithm SHA256).Hash
        }
        [void]$taskManifest.Append($taskInputPath).Append([char]0).Append($taskHash).Append("`n")
    }
    $taskHasher = [System.Security.Cryptography.SHA256]::Create()
    try {
        $taskBytes = [System.Text.Encoding]::UTF8.GetBytes($taskManifest.ToString())
        return [BitConverter]::ToString($taskHasher.ComputeHash($taskBytes)).Replace('-', '')
    }
    finally {
        $taskHasher.Dispose()
    }
}

Push-Location -LiteralPath $PSScriptRoot
try {
    $taskJarPath = Join-Path $PSScriptRoot 'target/wordtrail.jar'
    $taskStampPath = Join-Path $PSScriptRoot 'target/launcher-inputs.sha256'
    $taskInputFingerprint = Get-LauncherInputFingerprint
    $taskJarFingerprint = ''
    if (Test-Path -LiteralPath $taskJarPath -PathType Leaf) {
        $taskJarFingerprint = (Get-FileHash -LiteralPath $taskJarPath -Algorithm SHA256).Hash
    }
    $taskSavedFingerprint = ''
    if (Test-Path -LiteralPath $taskStampPath -PathType Leaf) {
        $taskSavedFingerprint = [System.IO.File]::ReadAllText($taskStampPath).Trim()
    }
    $taskNeedsBuild = $Build -or -not (Test-Path -LiteralPath $taskJarPath -PathType Leaf) -or
        $taskSavedFingerprint -ne ($taskInputFingerprint + ':' + $taskJarFingerprint)

    if ($taskNeedsBuild) {
        # Invalidate the previous stamp even if Maven fails before its clean phase.
        if (Test-Path -LiteralPath $taskStampPath) { Remove-Item -LiteralPath $taskStampPath -Force }
        Get-Command mvn -ErrorAction Stop | Out-Null
        & mvn -B --no-transfer-progress clean verify
        if ($LASTEXITCODE -ne 0) { throw 'Build failed. See the Maven output above.' }
        if (-not (Test-Path -LiteralPath $taskJarPath -PathType Leaf)) {
            throw 'Build completed without target/wordtrail.jar. WordTrail was not started.'
        }
        if ((Get-LauncherInputFingerprint) -ne $taskInputFingerprint) {
            throw 'Project files changed during the build. Run the launcher again to rebuild them.'
        }
        $taskJarFingerprint = (Get-FileHash -LiteralPath $taskJarPath -Algorithm SHA256).Hash
        [System.IO.File]::WriteAllText($taskStampPath, ($taskInputFingerprint + ':' + $taskJarFingerprint),
            [System.Text.Encoding]::ASCII)
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
