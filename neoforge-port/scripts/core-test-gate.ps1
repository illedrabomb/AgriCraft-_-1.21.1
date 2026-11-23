# Windows PowerShell equivalent of core-test-gate.sh; never invoked automatically.
param([switch]$ApproveCoreTests)
$ErrorActionPreference = 'Stop'

if (-not $ApproveCoreTests) {
    Write-Error 'GESPERRT: Nur nach ausdruecklicher Nutzerfreigabe mit -ApproveCoreTests.'
    exit 2
}

$projectDir = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $projectDir
try {
    if (-not (Test-Path 'build.gradle') -or -not (Test-Path 'settings.gradle')) {
        throw 'Hauptmod-Projektverzeichnis nicht gefunden.'
    }
    if (Test-Path '.\gradlew.bat') {
        $gradleExecutable = (Resolve-Path '.\gradlew.bat').Path
    } else {
        $gradleCommand = Get-Command gradle -ErrorAction SilentlyContinue
        if (-not $gradleCommand) { throw 'Weder gradlew.bat noch Gradle im PATH gefunden. Kein Test gestartet.' }
        $gradleExecutable = $gradleCommand.Source
    }

    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java nicht gefunden. Kein Test gestartet.'
    }
    # Windows PowerShell treats native stderr as ErrorRecords under Stop.
    $ErrorActionPreference = 'Continue'
    $javaVersion = (& java -version 2>&1 | Select-Object -First 1).ToString()
    $ErrorActionPreference = 'Stop'
    if ($javaVersion -notmatch '(?:version\s+"21(?:\.|"))|(?:openjdk\s+21\s+)') {
        throw "Java 21 erforderlich; gefunden: $javaVersion. Kein Test gestartet."
    }
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
        throw 'Git nicht gefunden. Kein Test gestartet.'
    }
    $branch = (& git branch --show-current).Trim()
    if ($branch -ne 'integration/neoforge-1211-consolidation') {
        throw "Falscher Branch: $branch. Kein Test gestartet."
    }
    if (& git status --porcelain) {
        throw 'Arbeitsbaum nicht sauber. Erst committen und GitHub-HEAD abgleichen.'
    }

    # This checks the selected Gradle without running any build/test task.
    # Gradle 8.5+ is required to run under Java 21.
    $ErrorActionPreference = 'Continue'
    $gradleVersionOutput = & $gradleExecutable '--version' 2>&1
    $gradleVersionExitCode = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    $gradleVersionLine = $gradleVersionOutput |
        Where-Object { $_.ToString() -match '^Gradle\s+[0-9]+\.[0-9]+' } |
        Select-Object -First 1
    if ($gradleVersionExitCode -ne 0 -or -not $gradleVersionLine) {
        throw 'Gradle-Version nicht lesbar. Keine Tests oder Build-Tasks gestartet.'
    }
    $gradleVersion = ($gradleVersionLine.ToString() -replace '^Gradle\s+', '').Trim()
    if ($gradleVersion -notmatch '^(\d+)\.(\d+)(?:\.\d+)?$') {
        throw "Ungueltige Gradle-Version: $gradleVersion. Kein Test gestartet."
    }
    $gradleMajor = [int]$Matches[1]
    $gradleMinor = [int]$Matches[2]
    if ($gradleMajor -lt 8 -or ($gradleMajor -eq 8 -and $gradleMinor -lt 5)) {
        throw "Gradle $gradleVersion unterstuetzt Java 21 nicht (mindestens 8.5 erforderlich). Alten Forge-7.4-Wrapper nicht verwenden."
    }
    $sha = (& git rev-parse HEAD).Trim()
    $timestamp = (Get-Date).ToUniversalTime().ToString('yyyyMMddTHHmmssZ')
    $reportDir = Join-Path $projectDir "build/core-gate-reports/$timestamp-$sha"
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    [System.IO.File]::WriteAllBytes((Join-Path $reportDir 'run-start.marker'), [byte[]]@())
    @(
        "UTC=$timestamp"
        "BRANCH=$branch"
        "COMMIT=$sha"
        "JAVA=$javaVersion"
        "GRADLE=$gradleVersion"
        'SCOPE=core-only :test :build (no Minecraft run, no addon tasks)'
        'APPROVAL=explicit CLI flag; user approval must exist separately'
    ) | Set-Content (Join-Path $reportDir 'manifest.txt') -Encoding UTF8

    $ErrorActionPreference = 'Continue'
    & $gradleExecutable ':test' ':build' '--no-daemon' '--console=plain' *> (Join-Path $reportDir 'gradle.log')
    $result = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    Set-Content (Join-Path $reportDir 'exit-code.txt') -Value $result -Encoding ASCII

    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) {
        & $python.Source 'scripts/summarize-core-gate.py' $reportDir
        if ($LASTEXITCODE -ne 0) { Write-Warning 'Kurzbericht fehlgeschlagen; Rohlogs sind erhalten.' }
    } else {
        Write-Warning 'Python fehlt; Rohlogs sind erhalten, keine Kurzfassung.'
    }
    Write-Output "Protokoll: $reportDir"
    if ($result -ne 0) { Write-Warning 'Gradle-Tasks fehlgeschlagen. Kein Minecraft-Test freigegeben.' }
    exit $result
} catch {
    Write-Warning "Core-Gate abgebrochen: $($_.Exception.Message)"
    exit 2
} finally {
    Pop-Location
}
