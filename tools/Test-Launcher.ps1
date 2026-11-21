[CmdletBinding()]
param()

# This checker never runs Maven, Java, a GUI, or a live network request.
$ErrorActionPreference = 'Stop'
$taskOriginalPath = $env:PATH
$taskOriginalExitCode = $global:LASTEXITCODE
$taskTempBase = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
$taskFixture = Join-Path $taskTempBase ('wordtrail-launcher-test-' + [Guid]::NewGuid().ToString('N'))
$taskFixture = [System.IO.Path]::GetFullPath($taskFixture)
$taskChecks = 0

function Assert-LauncherCheck([bool]$Condition, [string]$Message) {
    if (-not $Condition) { throw "Launcher regression: $Message" }
    $script:taskChecks++
}

function Get-StubCalls([string]$Command) {
    $taskLogPath = Join-Path $taskFixture "state/$Command.jsonl"
    if (Test-Path -LiteralPath $taskLogPath) {
        foreach ($taskLine in Get-Content -LiteralPath $taskLogPath) {
            # Keep each argument array as a single recorded call.
            [string[]]$taskDecodedArguments = ConvertFrom-Json -InputObject $taskLine
            ,$taskDecodedArguments
        }
    }
}

function Assert-CallCounts([int]$Builds, [int]$Launches, [string]$Context) {
    Assert-LauncherCheck (@(Get-StubCalls 'mvn').Count -eq $Builds) "$Context build count"
    Assert-LauncherCheck (@(Get-StubCalls 'java').Count -eq $Launches) "$Context launch count"
}

function Invoke-ExpectedLauncherFailure([string]$Message) {
    $taskFailed = $false
    try { & $taskLauncher -Build -Check }
    catch {
        $taskFailed = $true
        Assert-LauncherCheck ($_.Exception.Message.Contains($Message)) "expected failure: $Message"
    }
    Assert-LauncherCheck $taskFailed 'the launcher must fail'
    Assert-LauncherCheck (-not (Test-Path -LiteralPath $taskStamp)) 'failed build must not retain a success stamp'
}

function Invoke-ExpectedDataGuard([hashtable]$LauncherOptions, [string]$Message) {
    $taskBuildsBefore = @(Get-StubCalls 'mvn').Count
    $taskLaunchesBefore = @(Get-StubCalls 'java').Count
    $taskFailed = $false
    try { & $taskLauncher @LauncherOptions }
    catch {
        $taskFailed = $true
        Assert-LauncherCheck ($_.Exception.Message.Contains($Message)) "expected data guard: $Message"
    }
    Assert-LauncherCheck $taskFailed 'unsafe data configuration must stop the launcher'
    Assert-CallCounts $taskBuildsBefore $taskLaunchesBefore 'data guard must run before Maven or Java'
}

try {
    foreach ($taskChild in @('bin', 'state', 'src/main/resources', 'config/checkstyle')) {
        [void][System.IO.Directory]::CreateDirectory((Join-Path $taskFixture $taskChild))
    }
    $taskLauncher = Join-Path $taskFixture 'run.ps1'
    Copy-Item -LiteralPath (Join-Path (Split-Path -Parent $PSScriptRoot) 'run.ps1') -Destination $taskLauncher
    $taskSource = Join-Path $taskFixture 'src/main/resources/seed.json'
    $taskPom = Join-Path $taskFixture 'pom.xml'
    $taskConfig = Join-Path $taskFixture 'config/checkstyle/checkstyle.xml'
    $taskStamp = Join-Path $taskFixture 'target/launcher-inputs.sha256'
    $taskJar = Join-Path $taskFixture 'target/wordtrail.jar'
    $taskMode = Join-Path $taskFixture 'state/build-mode'
    [System.IO.File]::WriteAllText($taskSource, '{"version":1}')
    [System.IO.File]::WriteAllText($taskPom, '<project />')
    [System.IO.File]::WriteAllText($taskConfig, '<module />')
    $taskMavenStub = @'
$stubRoot = Split-Path -Parent $PSScriptRoot
$stubLog = Join-Path $stubRoot 'state/mvn.jsonl'
[System.IO.File]::AppendAllText($stubLog, (ConvertTo-Json -InputObject @($args) -Compress) + "`n")
$stubModePath = Join-Path $stubRoot 'state/build-mode'
$stubMode = if (Test-Path -LiteralPath $stubModePath) { [System.IO.File]::ReadAllText($stubModePath) } else { '' }
if ($stubMode -eq 'fail') { $global:LASTEXITCODE = 19; return }
$stubTarget = Join-Path $stubRoot 'target'
[void][System.IO.Directory]::CreateDirectory($stubTarget)
$stubJar = Join-Path $stubTarget 'wordtrail.jar'
if ($stubMode -eq 'no-jar') {
    if (Test-Path -LiteralPath $stubJar) { Remove-Item -LiteralPath $stubJar -Force }
} else {
    [System.IO.File]::WriteAllText($stubJar, 'stub executable; never invoke real Java')
}
if ($stubMode -eq 'change-input') {
    [System.IO.File]::AppendAllText((Join-Path $stubRoot 'pom.xml'), ' changed during build')
}
$global:LASTEXITCODE = 0
'@
    $taskJavaStub = @'
$stubRoot = Split-Path -Parent $PSScriptRoot
$stubLog = Join-Path $stubRoot 'state/java.jsonl'
[System.IO.File]::AppendAllText($stubLog, (ConvertTo-Json -InputObject @($args) -Compress) + "`n")
$global:LASTEXITCODE = 0
'@
    [System.IO.File]::WriteAllText((Join-Path $taskFixture 'bin/mvn.ps1'), $taskMavenStub)
    [System.IO.File]::WriteAllText((Join-Path $taskFixture 'bin/java.ps1'), $taskJavaStub)
    $env:PATH = (Join-Path $taskFixture 'bin') + [System.IO.Path]::PathSeparator + $taskOriginalPath
    foreach ($taskCommand in @('mvn', 'java')) {
        $taskResolved = (Get-Command $taskCommand -ErrorAction Stop).Source
        $taskExpected = Join-Path $taskFixture "bin/$taskCommand.ps1"
        Assert-LauncherCheck ($taskResolved -eq $taskExpected) "$taskCommand must resolve only to the isolated stub"
    }

    & $taskLauncher -Check
    Assert-CallCounts 1 1 'initial build'
    Assert-LauncherCheck (Test-Path -LiteralPath $taskStamp) 'successful build creates the stamp'
    & $taskLauncher -Check
    Assert-CallCounts 1 2 'unchanged inputs'

    $taskOriginalWriteTime = (Get-Item -LiteralPath $taskSource).LastWriteTimeUtc
    (Get-Item -LiteralPath $taskSource).LastWriteTimeUtc = $taskOriginalWriteTime.AddMinutes(1)
    & $taskLauncher -Check
    Assert-CallCounts 1 3 'timestamp-only change'
    [System.IO.File]::WriteAllText($taskSource, '{"version":2}')
    (Get-Item -LiteralPath $taskSource).LastWriteTimeUtc = $taskOriginalWriteTime
    & $taskLauncher -Check
    Assert-CallCounts 2 4 'content change with preserved timestamp'

    $taskAddedSource = Join-Path $taskFixture 'src/main/resources/added file.json'
    [System.IO.File]::WriteAllText($taskAddedSource, '{}')
    & $taskLauncher -Check
    Assert-CallCounts 3 5 'added source'
    Remove-Item -LiteralPath $taskAddedSource
    & $taskLauncher -Check
    Assert-CallCounts 4 6 'deleted source'

    [System.IO.File]::AppendAllText($taskConfig, '<!-- changed -->')
    & $taskLauncher -Check
    Assert-CallCounts 5 7 'checkstyle configuration change'
    [System.IO.File]::AppendAllText($taskPom, '<!-- changed -->')
    & $taskLauncher -Check
    Assert-CallCounts 6 8 'POM change'
    [System.IO.File]::AppendAllText($taskLauncher, "`n# changed launcher input`n")
    & $taskLauncher -Check
    Assert-CallCounts 7 9 'launcher change'

    Remove-Item -LiteralPath $taskStamp
    & $taskLauncher -Check
    Assert-CallCounts 8 10 'missing stamp'
    Remove-Item -LiteralPath $taskJar
    & $taskLauncher -Check
    Assert-CallCounts 9 11 'missing JAR'
    & $taskLauncher -Build -Check
    Assert-CallCounts 10 12 'forced build'

    [System.IO.File]::WriteAllText($taskMode, 'fail')
    Invoke-ExpectedLauncherFailure 'Build failed'
    Assert-CallCounts 11 12 'failed build'
    Remove-Item -LiteralPath $taskMode
    & $taskLauncher -Check
    Assert-CallCounts 12 13 'retry after failure'

    [System.IO.File]::WriteAllText($taskMode, 'no-jar')
    Invoke-ExpectedLauncherFailure 'without target/wordtrail.jar'
    Assert-CallCounts 13 13 'missing build output'
    [System.IO.File]::WriteAllText($taskMode, 'change-input')
    Invoke-ExpectedLauncherFailure 'changed during the build'
    Assert-CallCounts 14 13 'inputs changed during build'
    Remove-Item -LiteralPath $taskMode

    $taskDataDirectory = Join-Path $taskFixture 'data with spaces'
    & $taskLauncher -DataDirectory $taskDataDirectory
    Assert-CallCounts 15 14 'data directory forwarding'
    $taskLaunchCalls = @(Get-StubCalls 'java')
    $taskArguments = $taskLaunchCalls[-1]
    Assert-LauncherCheck ($taskArguments.Count -eq 4) 'data directory stays one argument'
    Assert-LauncherCheck ($taskArguments[2] -eq '--data-dir' -and $taskArguments[3] -eq $taskDataDirectory) 'data directory is preserved'
    & $taskLauncher -Check -DataDirectory $taskDataDirectory
    Assert-CallCounts 15 15 'check remains offline'
    $taskLaunchCalls = @(Get-StubCalls 'java')
    foreach ($taskArguments in $taskLaunchCalls) {
        Assert-LauncherCheck ($taskArguments[0] -eq '-jar' -and $taskArguments[1] -eq $taskJar) 'only the fixture JAR is launched'
    }
    $taskArguments = $taskLaunchCalls[-1]
    Assert-LauncherCheck ($taskArguments.Count -eq 3 -and $taskArguments[2] -eq '--help') 'check overrides the data directory and only requests help'
    Assert-LauncherCheck (-not (Test-Path -LiteralPath $taskDataDirectory)) 'check creates no data folder'
    foreach ($taskArguments in @(Get-StubCalls 'mvn')) {
        Assert-LauncherCheck (($taskArguments -join ' ') -eq '-B --no-transfer-progress clean verify') 'every rebuild performs clean verify'
    }
    # Model switching branches, building externally without clean, then restoring the old inputs.
    $taskOldPom = [System.IO.File]::ReadAllText($taskPom)
    [System.IO.File]::WriteAllText($taskPom, '<project>another branch</project>')
    [System.IO.File]::WriteAllText($taskJar, 'JAR replaced by an external build of another branch')
    [System.IO.File]::WriteAllText($taskPom, $taskOldPom)
    & $taskLauncher -Check
    Assert-CallCounts 16 16 'restored inputs with externally replaced JAR'
    $taskCurrentJarHash = (Get-FileHash -LiteralPath $taskJar -Algorithm SHA256).Hash
    Assert-LauncherCheck ([System.IO.File]::ReadAllText($taskStamp).EndsWith(':' + $taskCurrentJarHash)) 'stamp binds the exact JAR'
    & $taskLauncher -Check
    Assert-CallCounts 16 17 'matching inputs and JAR skip rebuilding'

    # Existing profiles must survive both forced and automatic clean builds, including -Check.
    $taskProfileDirectory = Join-Path $taskFixture 'target/presentation-data/nested'
    [void][System.IO.Directory]::CreateDirectory($taskProfileDirectory)
    $taskProtectedProfile = Join-Path $taskProfileDirectory 'wordtrail.json'
    $taskProfileBytes = [byte[]](0, 10, 13, 127, 255)
    [System.IO.File]::WriteAllBytes($taskProtectedProfile, $taskProfileBytes)
    $taskStampBeforeGuard = [System.IO.File]::ReadAllText($taskStamp)
    $taskDataMessage = 'Refusing to rebuild because WordTrail data exists under target'
    Invoke-ExpectedDataGuard @{ Build = $true } $taskDataMessage
    Invoke-ExpectedDataGuard @{ Build = $true; Check = $true } $taskDataMessage
    [System.IO.File]::AppendAllText($taskPom, '<!-- automatic rebuild required -->')
    Invoke-ExpectedDataGuard @{} $taskDataMessage
    Invoke-ExpectedDataGuard @{ Check = $true; DataDirectory = 'target/ignored-by-check' } $taskDataMessage
    Assert-LauncherCheck ([Convert]::ToBase64String([System.IO.File]::ReadAllBytes($taskProtectedProfile)) -eq
        [Convert]::ToBase64String($taskProfileBytes)) 'nested profile remains byte-identical after every blocked build'
    Assert-LauncherCheck ([System.IO.File]::ReadAllText($taskStamp) -eq $taskStampBeforeGuard) 'data guard does not alter the successful build stamp'
    Remove-Item -LiteralPath $taskProtectedProfile

    $taskRootProfile = Join-Path $taskFixture 'target/wordtrail.json'
    [System.IO.File]::WriteAllBytes($taskRootProfile, $taskProfileBytes)
    Invoke-ExpectedDataGuard @{ Check = $true } $taskDataMessage
    Assert-LauncherCheck ([Convert]::ToBase64String([System.IO.File]::ReadAllBytes($taskRootProfile)) -eq
        [Convert]::ToBase64String($taskProfileBytes)) 'profile directly in target remains byte-identical'
    Remove-Item -LiteralPath $taskRootProfile

    # Explicit data paths cannot create future profiles inside the disposable build tree.
    $taskUnsafeDataPaths = @('target', './target/', 'target/new-profile', 'target/child/..',
        'target/../target/new-profile', 'target-other/../target/new-profile',
        (Join-Path $taskFixture 'target/new-profile'))
    if ([System.IO.Path]::DirectorySeparatorChar -eq '\') { $taskUnsafeDataPaths += 'TARGET/new-profile' }
    foreach ($taskUnsafeDataPath in $taskUnsafeDataPaths) {
        Invoke-ExpectedDataGuard @{ DataDirectory = $taskUnsafeDataPath } 'DataDirectory must be outside target'
    }
    Assert-LauncherCheck (-not (Test-Path -LiteralPath (Join-Path $taskFixture 'target/new-profile'))) 'rejected data path is never created'

    & $taskLauncher -Check -DataDirectory 'target/ignored-by-check'
    Assert-CallCounts 17 18 'check ignores the explicit path after existing data has been removed'
    $taskLaunchCalls = @(Get-StubCalls 'java')
    $taskArguments = $taskLaunchCalls[-1]
    Assert-LauncherCheck ($taskArguments.Count -eq 3 -and $taskArguments[2] -eq '--help') 'check still forwards only help'
    Assert-LauncherCheck (-not (Test-Path -LiteralPath (Join-Path $taskFixture 'target/ignored-by-check'))) 'check does not create the ignored profile'
    foreach ($taskSafeDataPath in @('target-other', '.wordtrail/demo', 'target/../.wordtrail/demo with spaces')) {
        & $taskLauncher -DataDirectory $taskSafeDataPath
        $taskLaunchCalls = @(Get-StubCalls 'java')
        $taskArguments = $taskLaunchCalls[-1]
        Assert-LauncherCheck ($taskArguments.Count -eq 4 -and $taskArguments[2] -eq '--data-dir' -and
            $taskArguments[3] -eq $taskSafeDataPath) 'safe persistent data path is forwarded unchanged'
    }
    Assert-CallCounts 17 21 'safe paths neither trigger a build nor collide with target prefix'
    Write-Output "Launcher regression checks passed: $taskChecks assertions; stubbed Maven/Java only."
}
finally {
    $env:PATH = $taskOriginalPath
    $global:LASTEXITCODE = $taskOriginalExitCode
    # Delete only this unique fixture after checking its absolute location and name.
    $taskResolvedFixture = [System.IO.Path]::GetFullPath($taskFixture)
    $taskTempPrefix = $taskTempBase.TrimEnd('\', '/') + [System.IO.Path]::DirectorySeparatorChar
    if (-not $taskResolvedFixture.StartsWith($taskTempPrefix, [StringComparison]::OrdinalIgnoreCase) -or
        (Split-Path -Leaf $taskResolvedFixture) -notmatch '^wordtrail-launcher-test-[0-9a-f]{32}$') {
        throw 'Refusing to clean up an unexpected launcher-test directory.'
    }
    if (Test-Path -LiteralPath $taskResolvedFixture) {
        Remove-Item -LiteralPath $taskResolvedFixture -Recurse -Force
    }
}
