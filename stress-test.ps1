#requires -Version 5.1
# Comprehensive stress + functional test against Project-Task Description.txt
# and the rubric in ITPU_EstimateTemplate_Java_sem4.xlsx.

$ErrorActionPreference = 'Continue'
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
$mvn = "$env:USERPROFILE\.vscode\extensions\oracle.oracle-java-25.1.0\nbcode\java\maven\bin\mvn.cmd"
Set-Location $PSScriptRoot

# ====== HELPERS ======================================================
$Script:results = New-Object System.Collections.Generic.List[hashtable]
function PassT($name) {
    $Script:results.Add(@{name=$name; pass=$true})
    Write-Host ("  PASS  " + $name) -ForegroundColor Green
}
function FailT($name, $reason) {
    $Script:results.Add(@{name=$name; pass=$false; reason=$reason})
    Write-Host ("  FAIL  " + $name + "  ::  " + $reason) -ForegroundColor Red
}
function Section($title) {
    Write-Host ""
    Write-Host ("=== " + $title + " ===") -ForegroundColor Cyan
}

function NewConn {
    $c = New-Object System.Net.Sockets.TcpClient
    $c.Connect("localhost", 7070)
    $s = $c.GetStream()
    $w = New-Object System.IO.StreamWriter($s)
    $w.AutoFlush = $true
    return [pscustomobject]@{
        client = $c
        reader = (New-Object System.IO.StreamReader($s))
        writer = $w
    }
}
function ReadResp($conn) {
    $sb = New-Object System.Text.StringBuilder
    while ($true) {
        $line = $conn.reader.ReadLine()
        if ($null -eq $line) { break }
        if ($line -eq "<<END>>") { break }
        [void]$sb.AppendLine($line)
    }
    return $sb.ToString()
}
function Send($conn, $cmd) {
    $conn.writer.WriteLine($cmd)
    return ReadResp $conn
}
function CloseConn($conn) { try { $conn.client.Close() } catch {} }

function StartServer($outName) {
    $cp = "target\warehouse-multiclient-2.0.0.jar;$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
    return Start-Process java `
        -ArgumentList "-cp", $cp, "com.adxam.warehouse.app.ServerMain" `
        -PassThru `
        -RedirectStandardOutput ($outName + ".out.log") `
        -RedirectStandardError  ($outName + ".err.log") `
        -NoNewWindow
}

# ====== PHASE 1: STATIC ==============================================
Section "Phase 1 - Static checks (mandatory rubric items)"

$pom = Get-Content pom.xml -Raw
$banned = @('springframework','hibernate','quarkus','micronaut','jakarta.persistence','zaxxer.hikari')
$bannedHit = $banned | Where-Object { $pom -match $_ }
if ($bannedHit) { FailT "No side framework (mandatory)" ("found: " + $bannedHit) }
else            { PassT "No side framework (mandatory)" }

$srcRoot = "src\main\java\com\adxam\warehouse"
$layerPresence = [ordered]@{
    entity     = (Test-Path "$srcRoot\entity")
    dal        = (Test-Path "$srcRoot\dal")
    service    = (Test-Path "$srcRoot\service")
    controller = (Test-Path "$srcRoot\controller")
    net        = (Test-Path "$srcRoot\net")
    app        = (Test-Path "$srcRoot\app")
    config     = (Test-Path "$srcRoot\config")
}
$missing = $layerPresence.GetEnumerator() | Where-Object { -not $_.Value } | ForEach-Object { $_.Key }
if ($missing) { FailT "Layered architecture (mandatory)" ("missing: " + ($missing -join ',')) }
else          { PassT "Layered architecture (mandatory) - all layers present" }

$mains = Get-ChildItem -Recurse $srcRoot -Filter *.java | Select-String "public static void main" | Select-Object -ExpandProperty Path -Unique
if ($mains.Count -eq 2) {
    $names = $mains | ForEach-Object { Split-Path $_ -Leaf }
    PassT ("Two main entry points only: " + ($names -join ', '))
} else { FailT "Entry points" ("expected 2 mains, found " + $mains.Count) }

# ====== PHASE 2: BUILD + TESTS =======================================
Section "Phase 2 - Clean build + full test suite (unit + integration)"
Write-Host "  Running: mvn clean verify (this takes a moment)..."
$verifyLog = & $mvn clean verify 2>&1 | Out-String
if ($verifyLog -match 'BUILD SUCCESS') { PassT "mvn clean verify: BUILD SUCCESS" }
else {
    $verifyLog | Out-File stress-verify.log
    FailT "mvn clean verify" "see stress-verify.log"
}

$totals = [regex]::Matches($verifyLog, 'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)$', 'Multiline')
if ($totals.Count -ge 2) {
    $unit = $totals[$totals.Count - 2]
    $it   = $totals[$totals.Count - 1]
    PassT ("Unit tests: " + $unit.Groups[1].Value + " run, " + $unit.Groups[2].Value + " failures, " + $unit.Groups[3].Value + " errors")
    PassT ("Integration tests: " + $it.Groups[1].Value + " run, " + $it.Groups[2].Value + " failures, " + $it.Groups[3].Value + " errors")
}

# ====== PHASE 3: SERVER STARTUP ======================================
Section "Phase 3 - Server startup (fresh DB)"
Remove-Item data -Recurse -Force -ErrorAction SilentlyContinue
$server = StartServer "stress-srv1"
Start-Sleep -Seconds 5
$srvLog = Get-Content "stress-srv1.err.log" -Raw -ErrorAction SilentlyContinue
if ($srvLog -match 'Server listening on port 7070') { PassT "Server listens on port 7070" }
else { FailT "Server startup" "no listen log" }
if ($srvLog -match 'Default admin seeded: admin') { PassT "Default admin seeded on fresh DB" }
else { FailT "Admin seeding" "seed log missing" }
if ($srvLog -match 'ConnectionPool opened.*size=5') { PassT "Connection pool initialized (size=5, bonus +10)" }
else { FailT "Connection pool init" "no pool log" }

# ====== PHASE 4: AUTHORIZATION =======================================
Section "Phase 4 - Authorization (mandatory: functionality only after login)"
$c = NewConn
[void](ReadResp $c)

$r = Send $c "find laptops"
if ($r -match "Login required") { PassT "Unauthenticated read rejected" } else { FailT "Auth gating (read)" $r }
$r = Send $c "add laptop x 1 1 1 a b 1"
if ($r -match "Login required") { PassT "Unauthenticated write rejected" } else { FailT "Auth gating (write)" $r }
$r = Send $c "users list"
if ($r -match "Login required") { PassT "Unauthenticated admin op rejected" } else { FailT "Auth gating (admin)" $r }

$r = Send $c "login admin wrongpass"
if ($r -match "Invalid credentials") { PassT "Bad password rejected" } else { FailT "Bad password" $r }

$r = Send $c "login admin admin"
if ($r -match "ADMIN") { PassT "Admin login succeeds" } else { FailT "Admin login" $r }

$r = Send $c "whoami"
if ($r -match "admin.*ADMIN") { PassT "whoami reflects session" } else { FailT "whoami" $r }

[void](Send $c "logout")
$r = Send $c "find laptops"
if ($r -match "Login required") { PassT "Post-logout requires re-auth" } else { FailT "Logout" $r }
CloseConn $c

# ====== PHASE 5: ROLES ===============================================
Section "Phase 5 - Two mandatory roles + visitor (bonus +20)"

$c = NewConn; [void](ReadResp $c)
[void](Send $c "login admin admin")
[void](Send $c "users add normaluser pass123 USER")
CloseConn $c

$c = NewConn; [void](ReadResp $c)
[void](Send $c "login normaluser pass123")
$r = Send $c "whoami"
if ($r -match "normaluser.*USER") { PassT "USER can log in" } else { FailT "USER login" $r }
$r = Send $c "find laptops"
if ($r -notmatch "Permission denied") { PassT "USER can read" } else { FailT "USER read" $r }
$r = Send $c "add laptop UserLap 1.0 999 2 Linux Intel 300"
if ($r -match "Added") { PassT "USER can add items" } else { FailT "USER add" $r }
$r = Send $c "users list"
if ($r -match "Permission denied") { PassT "USER cannot manage users (role gating)" } else { FailT "USER role gating" $r }
$r = Send $c "users add hacker pass ADMIN"
if ($r -match "Permission denied") { PassT "USER cannot escalate to ADMIN" } else { FailT "USER escalation block" $r }
CloseConn $c

$c = NewConn; [void](ReadResp $c)
[void](Send $c "visit")
$r = Send $c "whoami"
if ($r -match "VISITOR") { PassT "VISITOR session established" } else { FailT "Visitor session" $r }
$r = Send $c "find laptops"
if ($r -notmatch "Permission denied") { PassT "VISITOR can read items" } else { FailT "Visitor read" $r }
$r = Send $c "add laptop X 1 1 1 a b 1"
if ($r -match "Permission denied") { PassT "VISITOR blocked from add" } else { FailT "Visitor add block" $r }
$r = Send $c "users list"
if ($r -match "Permission denied") { PassT "VISITOR blocked from admin ops" } else { FailT "Visitor admin block" $r }
CloseConn $c

# ====== PHASE 6: ADMIN USER MANAGEMENT ===============================
Section "Phase 6 - Admin user management (list/add/delete)"
$c = NewConn; [void](ReadResp $c)
[void](Send $c "login admin admin")

$r = Send $c "users list"
if (($r -match "admin") -and ($r -match "normaluser")) { PassT "users list returns all users" } else { FailT "users list" $r }

$r = Send $c "users add temp1 pass USER"
if ($r -match "User created") { PassT "users add USER" } else { FailT "users add" $r }
$r = Send $c "users add temp2 pass ADMIN"
if ($r -match "User created") { PassT "users add ADMIN" } else { FailT "users add admin" $r }

$listResp = Send $c "users list"
$temp1pattern = "id=(\d+),\s+username=" + [char]39 + "temp1" + [char]39
$temp1id = $null
foreach ($line in ($listResp -split "`n")) {
    if ($line -match $temp1pattern) { $temp1id = $matches[1]; break }
}
if ($temp1id) {
    $r = Send $c ("users delete " + $temp1id)
    if ($r -match "User deleted") { PassT "users delete by id" } else { FailT "users delete" $r }
} else { FailT "users delete" "couldn't find temp1 id" }

$r = Send $c "users delete 1"
if ($r -match "Refusing to delete") { PassT "Self-delete protection on admin" } else { FailT "Self-delete protection" $r }

$r = Send $c "users add normaluser dup USER"
if ($r -match "already exists") { PassT "Duplicate username rejected" } else { FailT "Duplicate user" $r }
CloseConn $c

# ====== PHASE 7: USER ITEM REQUIREMENTS ==============================
Section "Phase 7 - User can list / add / remove items (task req)"
$c = NewConn; [void](ReadResp $c)
[void](Send $c "login admin admin")

$r = Send $c "add laptop ItemA 1.0 500 2 Linux Intel 300"
if ($r -match "Added") { PassT "Add laptop" } else { FailT "Add laptop" $r }
$r = Send $c "add oven OvenA 20.0 800 3 2200 35.0"
if ($r -match "Added") { PassT "Add oven" } else { FailT "Add oven" $r }

$r = Send $c "find laptops"
$laptopId = $null
foreach ($line in ($r -split "`n")) { if ($line -match "id=(\d+).*ItemA") { $laptopId = $matches[1]; break } }
$r = Send $c "find ovens"
$ovenId = $null
foreach ($line in ($r -split "`n")) { if ($line -match "id=(\d+).*OvenA") { $ovenId = $matches[1]; break } }

if ($laptopId) {
    $r = Send $c ("remove laptop " + $laptopId)
    if ($r -match "Removed laptop") { PassT "Remove laptop by id" } else { FailT "Remove laptop" $r }
} else { FailT "Find laptop id" "ItemA not found" }
if ($ovenId) {
    $r = Send $c ("remove oven " + $ovenId)
    if ($r -match "Removed oven") { PassT "Remove oven by id" } else { FailT "Remove oven" $r }
} else { FailT "Find oven id" "OvenA not found" }

$r = Send $c "remove laptop 9999999"
if ($r -match "No laptop found") { PassT "Remove non-existent returns notice" } else { FailT "Remove notice" $r }

$r = Send $c "find all"
if ($r) { PassT "find all" } else { FailT "find all" "empty" }
$r = Send $c "find all price=0;10000"
if ($r) { PassT "find all with price range" } else { FailT "find with range" "empty" }
$r = Send $c "cost laptops"
if ($r -match "Total inventory") { PassT "cost laptops" } else { FailT "cost laptops" $r }
$r = Send $c "cost all"
if ($r -match "Total inventory") { PassT "cost all" } else { FailT "cost all" $r }
$r = Send $c "cheapest"
if ($r -match "Cheapest|No appliances") { PassT "cheapest" } else { FailT "cheapest" $r }
CloseConn $c

# ====== PHASE 8: INPUT VALIDATION (BONUS +20) ========================
Section "Phase 8 - OOP-style input validation"
$c = NewConn; [void](ReadResp $c)
[void](Send $c "login admin admin")

$r = Send $c "add laptop"
if ($r -match "Invalid input.*Missing") { PassT "Missing args produce validation error" } else { FailT "Missing args" $r }
$r = Send $c "add laptop X notanumber 100 1 L I 100"
if ($r -match "Invalid input.*number") { PassT "Numeric validation" } else { FailT "Numeric validation" $r }
$r = Send $c "add laptop X 1 1 1 L I notint"
if ($r -match "Invalid input") { PassT "Int validation on battery" } else { FailT "Int validation" $r }
$r = Send $c "users add x y NOTAROLE"
if ($r -match "Invalid input.*Unknown role") { PassT "Role enum validation" } else { FailT "Role validation" $r }
$r = Send $c "find badcategory"
if ($r -match "Invalid input|Unknown target") { PassT "Unknown find target rejected" } else { FailT "Unknown target" $r }
$r = Send $c "bogus-command"
if ($r -match "Unknown command") { PassT "Unknown command rejected" } else { FailT "Unknown command" $r }
$r = Send $c ""
if ($r -match "Type 'help'") { PassT "Empty line shows help hint" } else { FailT "Empty line" $r }
CloseConn $c

# ====== PHASE 9: CONCURRENT MULTI-CLIENT =============================
Section "Phase 9 - Multi-client concurrent (task req: one thread per client)"

$jobs = 1..6 | ForEach-Object {
    Start-Job -ArgumentList $_ -ScriptBlock {
        param($id)
        try {
            $c = New-Object System.Net.Sockets.TcpClient
            $c.Connect("localhost", 7070)
            $s = $c.GetStream()
            $r = New-Object System.IO.StreamReader($s)
            $w = New-Object System.IO.StreamWriter($s); $w.AutoFlush = $true
            function Drain { param($rd) while($true) { $l = $rd.ReadLine(); if ($null -eq $l -or $l -eq "<<END>>") { return } } }
            Drain $r
            $w.WriteLine("login admin admin"); Drain $r
            for ($i = 0; $i -lt 5; $i++) {
                $w.WriteLine("add laptop Concurrent" + $id + "-" + $i + " 1 100 1 L I 100")
                Drain $r
            }
            $w.WriteLine("exit"); Drain $r
            $c.Close()
            return "client-$id ok"
        } catch { return "client-$id ERROR: $_" }
    }
}
$concurrentOut = $jobs | Wait-Job -Timeout 60 | Receive-Job
$jobs | Remove-Job -Force
$okCount = ($concurrentOut | Where-Object { $_ -match 'ok$' }).Count
if ($okCount -eq 6) { PassT "6 concurrent clients all completed successfully" }
else { FailT "Concurrent clients" ("only " + $okCount + "/6 ok") }

Start-Sleep -Seconds 1
$srvLog = Get-Content "stress-srv1.err.log" -Raw
$threadIds = ([regex]::Matches($srvLog, "thread-(\d+)") | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
$clientThreads = $threadIds | Where-Object { [int]$_ -gt 3 }
if ($clientThreads.Count -ge 6) { PassT ("Server spawned " + $clientThreads.Count + " distinct client threads (per-thread requirement)") }
else { FailT "Per-thread server" ("only " + $clientThreads.Count + " client threads spawned") }

# ====== PHASE 10: POOL SATURATION ===================================
Section "Phase 10 - Connection pool under load (pool size = 5, 20 clients)"
$pjobs = 1..20 | ForEach-Object {
    Start-Job -ArgumentList $_ -ScriptBlock {
        param($id)
        try {
            $c = New-Object System.Net.Sockets.TcpClient
            $c.Connect("localhost", 7070)
            $s = $c.GetStream()
            $r = New-Object System.IO.StreamReader($s)
            $w = New-Object System.IO.StreamWriter($s); $w.AutoFlush = $true
            function Drain { param($rd) while($true) { $l = $rd.ReadLine(); if ($null -eq $l -or $l -eq "<<END>>") { return } } }
            Drain $r
            $w.WriteLine("login admin admin"); Drain $r
            $w.WriteLine("find all"); Drain $r
            $w.WriteLine("cost all"); Drain $r
            $w.WriteLine("exit"); Drain $r
            $c.Close()
            return "ok-$id"
        } catch { return "ERR-$id" }
    }
}
$satOut = $pjobs | Wait-Job -Timeout 120 | Receive-Job
$pjobs | Remove-Job -Force
$satOk = ($satOut | Where-Object { $_ -match '^ok' }).Count
if ($satOk -eq 20) { PassT "20 clients vs pool=5: all completed (pool serializes correctly)" }
else { FailT "Pool saturation" ("only " + $satOk + "/20 completed") }

# ====== PHASE 11: PERSISTENCE ========================================
Section "Phase 11 - Persistence across server restart (server-side storage)"

$c = NewConn; [void](ReadResp $c)
[void](Send $c "login admin admin")
[void](Send $c "add laptop PersistMarker 1 12345 1 L I 100")
CloseConn $c

Stop-Process -Id $server.Id -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

if (Test-Path "data\warehouse.mv.db") { PassT "Server-side H2 file persisted to disk" }
else { FailT "Server-side storage" "no data file" }

$server2 = StartServer "stress-srv2"
Start-Sleep -Seconds 5

$c = NewConn; [void](ReadResp $c)
[void](Send $c "login admin admin")
$r = Send $c "find laptops"
if ($r -match "PersistMarker") { PassT "Data persisted across restart" }
else { FailT "Persistence" "marker missing after restart" }
CloseConn $c

Stop-Process -Id $server2.Id -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

# ====== REPORT =======================================================
Section "SUMMARY"
$total  = $Script:results.Count
$passed = ($Script:results | Where-Object { $_.pass }).Count
$failed = $total - $passed
Write-Host ""
Write-Host ("Total: " + $total + "  Passed: " + $passed + "  Failed: " + $failed) -ForegroundColor Yellow
if ($failed -gt 0) {
    Write-Host ""
    Write-Host "FAILED CHECKS:" -ForegroundColor Red
    $Script:results | Where-Object { -not $_.pass } | ForEach-Object {
        Write-Host ("  - " + $_.name + " :: " + $_.reason) -ForegroundColor Red
    }
    Write-Host ""
    Write-Host ($failed.ToString() + " CHECK(S) FAILED") -ForegroundColor Red
} else {
    Write-Host ""
    Write-Host "ALL CHECKS PASSED" -ForegroundColor Green
}
