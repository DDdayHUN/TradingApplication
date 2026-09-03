$envValues = @{}

Get-Content ".env" | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        $envValues[$matches[1].Trim()] = $matches[2].Trim()
    }
}


Write-Host "Strating containers..."
docker compose --env-file .\.env -f ./docker/compose.yaml up -d

Write-Host "Strating IBKR IB Gateway..."
$possiblePaths = @(
    "C:\Jts\ibgateway\latest\ibgateway.exe"
)

$gatewayPath = $possiblePaths | Where-Object {Test-Path $_ } |
        Select-Object -First 1

if (-not $gatewayPath) {
    Write-Host "IB Gateway was not found in the common installation locations."
    Write-Host "Searching C:\Jts..."

    $gatewayPath = Get-ChildItem `
        -Path "C:\Jts" `
        -Filter "ibgateway.exe" `
        -Recurse `
        -ErrorAction SilentlyContinue |
            Select-Object -First 1 |
            Select-Object -ExpandProperty FullName
}

if (-not $gatewayPath) {
    Write-Host "Could not find IB Gateway."
    exit 1
}

Write-Host "Found IB Gateway at:"
Write-Host $gatewayPath

$gatewayProcess = Get-Process -Name "ibgateway" -ErrorAction SilentlyContinue

if ($gatewayProcess) {
    Write-Host "IB Gateway is already running."
    exit 0
}

$process = Start-Process -FilePath $gatewayPath -PassThru

Write-Host "Waiting for IB Gateway loading window..."

Add-Type @"
using System;
using System.Text;
using System.Runtime.InteropServices;

public static class Win32 {
    public delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

    [DllImport("user32.dll")]
    public static extern uint GetWindowThreadProcessId(
        IntPtr hWnd,
        out uint lpdwProcessId
    );

    [DllImport("user32.dll")]
    public static extern bool IsWindowVisible(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool IsWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern int GetWindowTextLength(IntPtr hWnd);

    [DllImport("user32.dll", CharSet = CharSet.Auto)]
    public static extern int GetWindowText(
        IntPtr hWnd,
        StringBuilder lpString,
        int nMaxCount
    );

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);
}
"@

function Get-ProcessWindows {
    param(
        [int]$ProcessId
    )

    $windows = [System.Collections.Generic.List[object]]::new()

    $callback = {
        param(
            [IntPtr]$hWnd,
            [IntPtr]$lParam
        )

        $pidValue = [uint32]0

        [Win32]::GetWindowThreadProcessId(
                $hWnd,
                [ref]$pidValue
        ) | Out-Null

        if (
        $pidValue -eq $ProcessId -and
                [Win32]::IsWindowVisible($hWnd)
        ) {
            $length = [Win32]::GetWindowTextLength($hWnd)
            $builder = New-Object System.Text.StringBuilder ($length + 1)

            [Win32]::GetWindowText(
                    $hWnd,
                    $builder,
                    $builder.Capacity
            ) | Out-Null

            $windows.Add(
                    [PSCustomObject]@{
                        Handle = $hWnd
                        Title  = $builder.ToString()
                    }
            )
        }

        return $true
    }

    [Win32]::EnumWindows($callback, [IntPtr]::Zero) | Out-Null

    return $windows
}

# -------------------------------------------------------
# Wait for FIRST window: loading/splash screen
# -------------------------------------------------------

$timeout = [Diagnostics.Stopwatch]::StartNew()
$loadingWindow = $null

while ($timeout.Elapsed.TotalSeconds -lt 60) {

    $windows = @(Get-ProcessWindows -ProcessId $process.Id)

    if ($windows.Count -gt 0) {
        $loadingWindow = $windows[0]
        break
    }

    Start-Sleep -Milliseconds 250
}

if (-not $loadingWindow) {
    Write-Host "Timed out waiting for IB Gateway loading window."
    exit 1
}

# -------------------------------------------------------
# Wait for SECOND window: actual login GUI
# -------------------------------------------------------

Write-Host "Waiting for IB Gateway login window..."

$loadingHandle = $loadingWindow.Handle

$timeout.Restart()

$loginWindow = $null

while ($timeout.Elapsed.TotalSeconds -lt 60) {

    $windows = @(Get-ProcessWindows -ProcessId $process.Id)

    foreach ($window in $windows) {

        if ($window.Handle -ne $loadingHandle) {
            $loginWindow = $window
            break
        }
    }

    if ($loginWindow) {
        break
    }

    Start-Sleep -Milliseconds 250
}

if (-not $loginWindow) {
    Write-Host "Timed out waiting for IB Gateway login window."
    exit 1
}


# -------------------------------------------------------
# Focus EXACT login window
# -------------------------------------------------------

[Win32]::ShowWindowAsync(
        $loginWindow.Handle,
        9
) | Out-Null

Start-Sleep -Milliseconds 250

[Win32]::SetForegroundWindow(
        $loginWindow.Handle
) | Out-Null

Start-Sleep -Milliseconds 500


# -------------------------------------------------------
# Enter credentials
# -------------------------------------------------------

$wshell = New-Object -ComObject WScript.Shell

$username = $envValues["IBKR_USERNAME"]
$password = $envValues["IBKR_PASSWORD"]

$wshell.SendKeys($username)
$wshell.SendKeys("{TAB}")
$wshell.SendKeys($password)
$wshell.SendKeys("{ENTER}")
