param(
    [Parameter(Position = 0)]
    [string] $DataFile,
    [switch] $Clear
)

$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$composeFile = Join-Path $projectDir 'compose.yaml'
$database = 'product_finder'
$collection = 'catalog_products'
$mongoPassword = if ($env:MONGO_PASSWORD) { $env:MONGO_PASSWORD } else { 'product-finder' }

function Invoke-Compose {
    param([Parameter(ValueFromRemainingArguments = $true)] [string[]] $CommandArgs)
    docker compose --project-directory $projectDir --file $composeFile @CommandArgs
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose command failed with exit code $LASTEXITCODE." }
}

if ($Clear) {
    $dataFile = $null
} elseif (-not $DataFile) {
    $DataFile = Join-Path $projectDir 'backend/sample-data/catalog_products.json'
} else {
    $DataFile = (Resolve-Path $DataFile).Path
}

if ($Clear) {
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        docker compose --project-directory $projectDir --file $composeFile exec -T mongo mongosh --quiet `
            --username product_finder --password $mongoPassword --authenticationDatabase admin $database `
            --eval 'db.runCommand({ping: 1}).ok' *> $null
        if ($LASTEXITCODE -eq 0) { break }
        if ($attempt -eq 30) { throw 'MongoDB did not become ready within 60 seconds.' }
        Start-Sleep -Seconds 2
    }
    Invoke-Compose exec -T mongo mongosh --quiet --username product_finder --password $mongoPassword `
        --authenticationDatabase admin $database --eval "db.$collection.deleteMany({})"
    exit 0
}

if (-not (Test-Path -LiteralPath $DataFile -PathType Leaf)) {
    throw "JSON file not found: $DataFile"
}

for ($attempt = 1; $attempt -le 30; $attempt++) {
    docker compose --project-directory $projectDir --file $composeFile exec -T mongo mongosh --quiet `
        --username product_finder --password $mongoPassword --authenticationDatabase admin $database `
        --eval 'db.runCommand({ping: 1}).ok' *> $null
    if ($LASTEXITCODE -eq 0) { break }
    if ($attempt -eq 30) { throw 'MongoDB did not become ready within 60 seconds.' }
    Start-Sleep -Seconds 2
}

$containerFile = "/tmp/product-finder-catalog-products-$PID.json"
try {
    Invoke-Compose cp $DataFile "mongo:$containerFile"
    Invoke-Compose exec -T mongo mongoimport --username product_finder --password $mongoPassword `
        --authenticationDatabase admin --db $database --collection $collection --type json --jsonArray `
        --mode upsert --upsertFields _id --file $containerFile
} finally {
    docker compose --project-directory $projectDir --file $composeFile exec -T mongo rm -f $containerFile *> $null
}
