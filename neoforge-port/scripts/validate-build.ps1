# Developer validation for the Minecraft 1.21.1 / NeoForge integration branch.
# Run from any directory with: powershell -ExecutionPolicy Bypass -File .\neoforge-port\scripts\validate-build.ps1
# Does NOT launch Minecraft, use test-mod JARs, or merge branches.
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$expectedBranch = 'integration/neoforge-1211-consolidation'
$portDirectory = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$repoDirectory = (Resolve-Path (Join-Path $portDirectory '..')).Path
$logDirectory = Join-Path $portDirectory 'build\validation-logs'
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$logPath = Join-Path $logDirectory "gradle-test-build-$stamp.log"
$summaryPath = Join-Path $logDirectory "validation-summary-$stamp.txt"

function Invoke-ToolLines([string]$exe, [string[]]$toolArgs) {
    # Native stdout and stderr may be useful even on successful commands.
    $output = & $exe @toolArgs 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) { throw "$exe failed: $output" }
    return $output.Trim()
}

try {
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
        throw 'git not found on PATH'
    }
    $actualBranch = (Invoke-ToolLines 'git' @('-C', $repoDirectory, 'branch', '--show-current')).Trim()
    $commit = (Invoke-ToolLines 'git' @('-C', $repoDirectory, 'rev-parse', 'HEAD')).Trim()
    if ($actualBranch -ne $expectedBranch) {
        throw "Wrong branch '$actualBranch'. Required: '$expectedBranch'. No build was started."
    }
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java missing on PATH; expected JDK 21'
    }
    if (-not (Get-Command gradle -ErrorAction SilentlyContinue)) {
        throw 'Gradle missing on PATH; expected local Gradle 9.2.1 (do not use root wrapper)'
    }

    $javaInfo = (& java -version 2>&1 | Out-String).Trim()
    $gradleInfo = (Invoke-ToolLines 'gradle' @('--version'))
    $summary = @(
        "Branch: $actualBranch",
        "Commit: $commit",
        "Date: $(Get-Date -Format o)",
        "Java:",
        $javaInfo,
        "Gradle:",
        $gradleInfo,
        "Command: gradle --no-daemon test build",
        "Project: $portDirectory",
        "Build log: $logPath"
    )
    $summary | Set-Content -Path $summaryPath -Encoding UTF8
    Write-Host "Validating $actualBranch at $commit"
    Write-Host "Output: $logPath"

    Push-Location $portDirectory
    try {
        # Don't pass -PloadTestMods, -PpamJar, -PidentifyJar or -PjeiJar.
        $ErrorActionPreference = 'Continue'
        & gradle --no-daemon test build 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
        $ErrorActionPreference = 'Stop'
    } finally {
        Pop-Location
    }

    "Exit code: $exitCode" | Add-Content -Path $summaryPath -Encoding UTF8
    "JUnit XML directory: $(Join-Path $portDirectory 'build\test-results\test')" |
        Add-Content -Path $summaryPath -Encoding UTF8
    "JAR directory: $(Join-Path $portDirectory 'build\libs')" |
        Add-Content -Path $summaryPath -Encoding UTF8
    Write-Host "Summary: $summaryPath"
    if ($exitCode -ne 0) {
        Write-Error "Gradle reported exit code $exitCode. Share the log and summary files."
        exit $exitCode
    }
    Write-Host 'Gradle test build succeeded. Minecraft gameplay has not been tested.'
    exit 0
} catch {
    "Preparation failure: $($_.Exception.Message)" |
        Set-Content -Path $summaryPath -Encoding UTF8
    Write-Error $_.Exception.Message
    exit 1
}
