# Read-only offline inventory of optional crop mod jars.
# This does NOT prove registry availability; models/assets are hints only.
# Example:
#   .\scripts\inspect-optional-crop-jars.ps1 -JarPaths @(
#       'C:\path\pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar',
#       'C:\path\farmersdelight-1.21.1.jar'
#   )
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string[]] $JarPaths
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

foreach ($jarPath in $JarPaths) {
    $resolved = (Resolve-Path -LiteralPath $jarPath -ErrorAction Stop).ProviderPath
    if (-not [System.IO.File]::Exists($resolved) -or
        -not $resolved.EndsWith('.jar', [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Expected an existing .jar file: $jarPath"
    }

    $checksum = (Get-FileHash -LiteralPath $resolved -Algorithm SHA256).Hash
    Write-Output "=== $([System.IO.Path]::GetFileName($resolved)) ==="
    Write-Output "Path: $resolved"
    Write-Output "SHA256: $checksum"

    $zip = [System.IO.Compression.ZipFile]::OpenRead($resolved)
    try {
        $names = @($zip.Entries | ForEach-Object { $_.FullName })
        $modMetadata = @($names | Where-Object {
            $_ -eq 'META-INF/neoforge.mods.toml' -or
            $_ -eq 'META-INF/mods.toml'
        } | Sort-Object)
        Write-Output "Mod metadata files: $($modMetadata -join ', ')"

        $allItemModels = @($names | Where-Object {
            $_ -match '^assets/(pamhc2crops|farmersdelight)/models/item/[a-z0-9_./-]+\.json$'
        } | Sort-Object -Unique)
        Write-Output "Optional crop item model assets found: $($allItemModels.Count)"

        $pamSeeds = @($allItemModels | Where-Object {
            $_ -match '^assets/pamhc2crops/models/item/([a-z0-9_]+)seeditem\.json$'
        } | ForEach-Object {
            [System.IO.Path]::GetFileNameWithoutExtension($_)
        } | Sort-Object -Unique)
        Write-Output "Pam's seeditem model candidates ($($pamSeeds.Count)):"
        foreach ($seed in $pamSeeds) {
            $crop = $seed.Substring(0, $seed.Length - 'seeditem'.Length)
            $produceModel = 'assets/pamhc2crops/models/item/' + $crop + 'item.json'
            $present = $allItemModels -contains $produceModel
            Write-Output "  pamhc2crops:$seed -> pamhc2crops:${crop}item ; produce model: $present"
        }

        $farmerNames = @('cabbage_seeds', 'cabbage', 'tomato_seeds',
                         'tomato', 'onion', 'rice', 'rice_panicle')
        Write-Output "Farmer's Delight item model candidates:"
        foreach ($name in $farmerNames) {
            $asset = 'assets/farmersdelight/models/item/' + $name + '.json'
            Write-Output "  farmersdelight:$name ; model: $($allItemModels -contains $asset)"
        }
        Write-Output 'NOTE: Missing models do not prove missing registry items.'
        Write-Output 'NOTE: Present models do not prove matching NeoForge item registrations.'
        Write-Output 'NOTE: Confirm live IDs using /cropgenetics data commands in the later Minecraft test.'
    }
    finally {
        $zip.Dispose()
    }
}
