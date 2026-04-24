$ErrorActionPreference = 'Stop'

$mongoExe = 'C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe'
$dbPath = "$env:USERPROFILE\mongodb-data-restored"
$logDir = "$env:USERPROFILE\mongodb-log"
$logPath = "$logDir\mongod-restored.log"
$port = 27017

# If Mongo is already listening, do nothing.
$test = Test-NetConnection -ComputerName 127.0.0.1 -Port $port -WarningAction SilentlyContinue
if ($test.TcpTestSucceeded) {
    exit 0
}

if (-not (Test-Path $mongoExe)) {
    throw "mongod introuvable: $mongoExe"
}

if (-not (Test-Path $dbPath)) {
    throw "dbPath introuvable: $dbPath"
}

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

Start-Process -FilePath $mongoExe -ArgumentList @(
    '--dbpath', $dbPath,
    '--logpath', $logPath,
    '--bind_ip', '127.0.0.1',
    '--port', "$port"
) -WindowStyle Hidden
