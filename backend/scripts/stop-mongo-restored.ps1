$ErrorActionPreference = 'SilentlyContinue'

$mongoProcess = Get-Process mongod
if ($mongoProcess) {
    $mongoProcess | Stop-Process -Force
}
