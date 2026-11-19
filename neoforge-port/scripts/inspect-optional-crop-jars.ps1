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
        $missingProduceModels = 0
        foreach ($seed in $pamSeeds) {
            $crop = $seed.Substring(0, $seed.Length - 'seeditem'.Length)
            $produceModel = 'assets/pamhc2crops/models/item/' + $crop + 'item.json'
            $present = $allItemModels -contains $produceModel
            if (-not $present) { $missingProduceModels++ }
            Write-Output "  pamhc2crops:$seed -> pamhc2crops:${crop}item ; produce model: $present"
        }
        Write-Output "Pam's seeditem candidates without matching produce model: $missingProduceModels"
        Write-Output "Pam's plural crop-name probes (these are seeds whose crop name itself ends in 'seeds'):"
        foreach ($crop in @('mustardseeds', 'sesameseeds')) {
            $seedModel = 'assets/pamhc2crops/models/item/' + $crop + 'seeditem.json'
            $produceModel = 'assets/pamhc2crops/models/item/' + $crop + 'item.json'
            Write-Output "  $crop ; seed model: $($allItemModels -contains $seedModel) ; produce model: $($allItemModels -contains $produceModel)"
        }

        # From the publicly visible OLD Pam's ItemRegistry, not a verified
        # 1.21.1 contract. Item models alone cannot reveal use-on-block logic.
        $pamHistoricalDirectFood = @(
            'cotton', 'flax', 'kenaf', 'jute', 'sisal',
            'coffeebean', 'tealeaf', 'agave', 'ginger'
        )
        Write-Output "Pam's historically self-plantable produce candidates (1.21.1 behavior UNVERIFIED):"
        foreach ($crop in $pamHistoricalDirectFood) {
            $foodModel = 'assets/pamhc2crops/models/item/' + $crop + 'item.json'
            $seedModel = 'assets/pamhc2crops/models/item/' + $crop + 'seeditem.json'
            Write-Output "  pamhc2crops:${crop}item ; food model: $($allItemModels -contains $foodModel) ; separate seed model: $($allItemModels -contains $seedModel)"
        }
        Write-Output 'NOTE: A self-plantable food model is NOT proof that the food item can be planted in Crop Sticks.'

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
