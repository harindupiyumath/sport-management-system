$results = [System.Collections.Generic.List[PSCustomObject]]::new()

function Record-Test($category, $name, $expected, $actual, $passed, $details) {
    $results.Add([PSCustomObject]@{
        Category = $category
        TestCase = $name
        Expected = $expected
        Actual   = $actual
        Status   = if ($passed) { "PASS" } else { "FAIL" }
        Details  = $details
    })
    $statusText = if ($passed) { "[PASS]" } else { "[FAIL]" }
    Write-Host "$statusText $category - $name"
}

# 1-6 Frontend UI HTTP Availability Checks
$frontendFiles = @("index.html", "login.html", "signup.html", "forgot-password.html", "dashboard.html", "admin-dashboard.html")
foreach ($f in $frontendFiles) {
    try {
        $res = Invoke-WebRequest -Uri "http://localhost:5500/$f" -UseBasicParsing
        Record-Test "Frontend UI" "HTTP 200: $f" "200 OK" "$($res.StatusCode)" ($res.StatusCode -eq 200) "File served successfully by HTTP server"
    } catch {
        Record-Test "Frontend UI" "HTTP 200: $f" "200 OK" "$($_.Exception.Message)" $false "$_"
    }
}

# 7. Invalid Faculty Validation (Negative Test)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242399"; faculty="AGRICULTURE"; password="Password@123"; confirmPassword="Password@123"
    } | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Validation" "Invalid Faculty Rejected" "400 Bad Request" "200 OK" $false "Allowed non-existent faculty"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Validation" "Invalid Faculty Rejected" "400 Bad Request" "$statusCode" ($statusCode -eq 400) "Correctly rejected non-existent faculty"
}

# 8. Student 2 Registration & Login (Faculty: APPLIED_SCIENCES)
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242301"; faculty="APPLIED_SCIENCES"; password="Password@123"; confirmPassword="Password@123"
    } | ConvertTo-Json) -ContentType "application/json"
    $stu2Login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="242301"; password="Password@123"} | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Register & Login Student (APPLIED_SCIENCES)" "Role STUDENT & Token" "$($stu2Login.user.role) / $($stu2Login.user.faculty)" ($stu2Login.user.role -eq "STUDENT" -and $stu2Login.user.faculty -eq "APPLIED_SCIENCES") "Student logged in successfully"
} catch {
    Record-Test "Authentication" "Register & Login Student (APPLIED_SCIENCES)" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# 9. Lecturer 2 Registration & Login (Faculty: BUSINESS_STUDIES)
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="LECTURER"; userId="L002"; faculty="BUSINESS_STUDIES"; password="Lecturer@456"; confirmPassword="Lecturer@456"
    } | ConvertTo-Json) -ContentType "application/json"
    $lec2Login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="L002"; password="Lecturer@456"} | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Register & Login Lecturer (BUSINESS_STUDIES)" "Role LECTURER & Token" "$($lec2Login.user.role) / $($lec2Login.user.faculty)" ($lec2Login.user.role -eq "LECTURER" -and $lec2Login.user.faculty -eq "BUSINESS_STUDIES") "Lecturer logged in successfully"
} catch {
    Record-Test "Authentication" "Register & Login Lecturer (BUSINESS_STUDIES)" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# 10. Outdoor Sport Booking (Football, Sport ID 6)
try {
    $testDate = (Get-Date).AddDays(4).ToString("yyyy-MM-dd")
    $outdoorBookBody = @{ sportId = 6; date = $testDate; start = "16:00"; end = "17:00" } | ConvertTo-Json
    $bOutdoor = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $($stu2Login.token)" } -Body $outdoorBookBody -ContentType "application/json"
    Record-Test "Outdoor Sports" "Student Books Outdoor Football Slot" "201 Created & status=PENDING" "Status: $($bOutdoor.status), Sport: $($bOutdoor.sportName)" ($bOutdoor.status -eq "PENDING" -and $bOutdoor.sportName -eq "Football") "Outdoor booking created"
} catch {
    Record-Test "Outdoor Sports" "Student Books Outdoor Football Slot" "201 Created" "$($_.Exception.Message)" $false "$_"
}

# 11. Admin Rejects Booking -> Notification Delivered & Slot Released
try {
    $adminLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="admin"; password="Admin@123"} | ConvertTo-Json) -ContentType "application/json"
    $adminToken = $adminLogin.token
    
    $rejRes = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bookings/$($bOutdoor.id)/reject" -Method Put -Headers @{ Authorization="Bearer $adminToken" }
    
    # Check student notification for rejection
    $stuNotifs = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications" -Method Get -Headers @{ Authorization="Bearer $($stu2Login.token)" }
    $rejNotif = $stuNotifs | Where-Object { $_.message -like "*rejected*" }
    
    # Check slot is freed back to AVAILABLE
    $slotsRes = Invoke-RestMethod -Uri "http://localhost:8080/api/sports/6/slots?date=$testDate" -Method Get -Headers @{ Authorization="Bearer $($stu2Login.token)" }
    $s16 = $slotsRes.slots | Where-Object { $_.start -eq "16:00" }
    
    $passed = ($rejRes.status -eq "REJECTED" -and $rejNotif -ne $null -and $s16.status -eq "AVAILABLE")
    Record-Test "Admin Operations" "Admin Rejects Booking -> Notification Sent & Slot Released" "Status REJECTED, Notif Sent, Slot AVAILABLE" "RejStatus=$($rejRes.status), NotifFound=$($rejNotif -ne $null), Slot=$($s16.status)" $passed "Rejection workflow validated"
} catch {
    Record-Test "Admin Operations" "Admin Rejects Booking -> Notification Sent & Slot Released" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# 12. Verify Security: Cannot access with invalid token
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/my" -Method Get -Headers @{ Authorization="Bearer invalid-token-12345" }
    Record-Test "Security / RBAC" "Reject Invalid JWT Token" "401/403" "200 OK" $false "Invalid token allowed"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Security / RBAC" "Reject Invalid JWT Token" "401 or 403" "$statusCode" ($statusCode -eq 401 -or $statusCode -eq 403) "Rejected invalid token"
}

# 13. Password Hash Privacy in Profile
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8080/api/users/me" -Method Get -Headers @{ Authorization="Bearer $($stu2Login.token)" }
    $hasPassword = ($profile.PSObject.Properties.Name -contains "password" -or $profile.PSObject.Properties.Name -contains "passwordHash")
    Record-Test "Security / Privacy" "Profile API Excludes Password Field" "No password field in response" "hasPassword: $hasPassword" (!$hasPassword) "Password is never leaked in DTO"
} catch {
    Record-Test "Security / Privacy" "Profile API Excludes Password Field" "No password" "$($_.Exception.Message)" $false "$_"
}

Write-Host "=========================================="
$total = $results.Count
$passedCount = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failedCount = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "EXTENDED TEST EXECUTION SUMMARY: Total: $total | Passed: $passedCount | Failed: $failedCount"
Write-Host "=========================================="
