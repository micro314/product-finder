$ErrorActionPreference = 'Stop'

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$composeFile = Join-Path $projectDir 'compose.yaml'

docker build --tag product-finder-backend:local (Join-Path $projectDir 'backend')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

docker build --tag product-finder-frontend:local (Join-Path $projectDir 'frontend')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

docker compose --project-directory $projectDir --file $composeFile up --detach --no-build @args
exit $LASTEXITCODE
