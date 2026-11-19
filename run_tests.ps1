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

# ==========================================
# 1. FRONTEND AVAILABILITY CHECKS
# ==========================================
$frontendFiles = @("index.html", "login.html", "signup.html", "forgot-password.html", "dashboard.html", "admin-dashboard.html")
foreach ($f in $frontendFiles) {
    try {
        $res = Invoke-WebRequest -Uri "http://localhost:5500/$f" -UseBasicParsing
        Record-Test "Frontend UI" "HTTP 200: $f" "200 OK" "$($res.StatusCode)" ($res.StatusCode -eq 200) "File served successfully by HTTP server"
    } catch {
        Record-Test "Frontend UI" "HTTP 200: $f" "200 OK" "$($_.Exception.Message)" $false "$_"
    }
}

# ==========================================
# 2. AUTHENTICATION & REGISTRATION TESTS
# ==========================================
# Test 7: Super Admin Default Login
try {
    $adminLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="admin"; password="Admin@123"} | ConvertTo-Json) -ContentType "application/json"
    $adminToken = $adminLogin.token
    Record-Test "Authentication" "Super Admin Default Login" "200 OK & JWT with ADMIN role" "$($adminLogin.user.role)" ($adminLogin.user.role -eq "ADMIN" -and $adminToken) "Token received successfully"
} catch {
    Record-Test "Authentication" "Super Admin Default Login" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 8: Login with Invalid Password
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="admin"; password="WrongPassword"} | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Invalid Password Rejection" "401 Unauthorized" "200 OK" $false "Should have failed"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Authentication" "Invalid Password Rejection" "401" "$statusCode" ($statusCode -eq 401) "401 returned as expected"
}

# Test 9: Student Signup - Valid (Faculty: TECHNOLOGY)
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242203"; faculty="TECHNOLOGY"; password="Password@123"; confirmPassword="Password@123"
    } | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Student Registration (Valid)" "201 Created" "$($res.message)" ($res.userId -eq "242203") "$($res.message)"
} catch {
    Record-Test "Authentication" "Student Registration (Valid)" "201 Created" "$($_.Exception.Message)" $false "$_"
}

# Test 10: Student Signup - Password Mismatch
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242204"; faculty="TECHNOLOGY"; password="Password@123"; confirmPassword="DifferentPassword@123"
    } | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Password Mismatch Validation" "400 Bad Request" "200 OK" $false "Allowed mismatch"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Authentication" "Password Mismatch Validation" "400" "$statusCode" ($statusCode -eq 400) "400 returned for mismatch"
}

# Test 11: Student Signup - Weak Password
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242205"; faculty="TECHNOLOGY"; password="weak"; confirmPassword="weak"
    } | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Weak Password Validation" "400 Bad Request" "200 OK" $false "Allowed weak password"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Authentication" "Weak Password Validation" "400" "$statusCode" ($statusCode -eq 400) "400 returned for regex violation"
}

# Test 12: Student Signup - Duplicate ID
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242203"; faculty="TECHNOLOGY"; password="Password@123"; confirmPassword="Password@123"
    } | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Duplicate ID Conflict" "409 Conflict" "200 OK" $false "Allowed duplicate"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Authentication" "Duplicate ID Conflict" "409" "$statusCode" ($statusCode -eq 409) "409 returned for existing user"
}

# Test 13: Student Signup - Invalid Faculty Validation
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242299"; faculty="INVALID_FACULTY"; password="Password@123"; confirmPassword="Password@123"
    } | ConvertTo-Json) -ContentType "application/json"
    Record-Test "Authentication" "Invalid Faculty Rejected" "400 Bad Request" "200 OK" $false "Allowed invalid faculty"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Authentication" "Invalid Faculty Rejected" "400" "$statusCode" ($statusCode -eq 400) "400 returned for non-existent faculty"
}

# Test 14: Lecturer 1 Signup & Login (Faculty: APPLIED_SCIENCES)
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="LECTURER"; userId="L001"; faculty="APPLIED_SCIENCES"; password="Lecturer@123"; confirmPassword="Lecturer@123"
    } | ConvertTo-Json) -ContentType "application/json"
    $lecLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="L001"; password="Lecturer@123"} | ConvertTo-Json) -ContentType "application/json"
    $lecToken = $lecLogin.token
    Record-Test "Authentication" "Lecturer Signup & Login" "200 OK & LECTURER role" "$($lecLogin.user.role)" ($lecLogin.user.role -eq "LECTURER") "Lecturer token acquired"
} catch {
    Record-Test "Authentication" "Lecturer Signup & Login" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 15: Lecturer 2 Signup & Login (Faculty: BUSINESS_STUDIES)
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="LECTURER"; userId="L002"; faculty="BUSINESS_STUDIES"; password="Lecturer@456"; confirmPassword="Lecturer@456"
    } | ConvertTo-Json) -ContentType "application/json"
    $lec2Login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="L002"; password="Lecturer@456"} | ConvertTo-Json) -ContentType "application/json"
    $lec2Token = $lec2Login.token
    Record-Test "Authentication" "Lecturer 2 (BUSINESS_STUDIES) Signup & Login" "200 OK & LECTURER role" "$($lec2Login.user.role) / $($lec2Login.user.faculty)" ($lec2Login.user.role -eq "LECTURER" -and $lec2Login.user.faculty -eq "BUSINESS_STUDIES") "Lecturer 2 token acquired"
} catch {
    Record-Test "Authentication" "Lecturer 2 (BUSINESS_STUDIES) Signup & Login" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 16: Student 2 Signup & Login (Faculty: APPLIED_SCIENCES)
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Body (@{
        role="STUDENT"; userId="242301"; faculty="APPLIED_SCIENCES"; password="Password@123"; confirmPassword="Password@123"
    } | ConvertTo-Json) -ContentType "application/json"
    $stu2Login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="242301"; password="Password@123"} | ConvertTo-Json) -ContentType "application/json"
    $stu2Token = $stu2Login.token
    Record-Test "Authentication" "Student 2 (APPLIED_SCIENCES) Signup & Login" "200 OK & STUDENT role" "$($stu2Login.user.role) / $($stu2Login.user.faculty)" ($stu2Login.user.role -eq "STUDENT" -and $stu2Login.user.faculty -eq "APPLIED_SCIENCES") "Student 2 token acquired"
} catch {
    Record-Test "Authentication" "Student 2 (APPLIED_SCIENCES) Signup & Login" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 17: Forgot Password & New Password Authentication
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/forgot-password" -Method Post -Body (@{
        userId="242203"; newPassword="UpdatedPassword@123"; confirmPassword="UpdatedPassword@123"
    } | ConvertTo-Json) -ContentType "application/json"
    $newLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{userId="242203"; password="UpdatedPassword@123"} | ConvertTo-Json) -ContentType "application/json"
    $studentToken = $newLogin.token
    Record-Test "Authentication" "Forgot Password Reset & Re-login" "200 OK & new token" "Token acquired" ($studentToken -ne $null) "Password reset successful"
} catch {
    Record-Test "Authentication" "Forgot Password Reset & Re-login" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# ==========================================
# 3. USER PROFILE & SECURITY PRIVACY TESTS
# ==========================================
# Test 18: Get User Profile (/api/users/me)
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8080/api/users/me" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    Record-Test "User Profile" "GET /users/me (Own Profile)" "242203 / STUDENT / TECHNOLOGY" "$($profile.userId) / $($profile.role) / $($profile.faculty)" ($profile.userId -eq "242203" -and $profile.role -eq "STUDENT") "Profile fetched without password"
} catch {
    Record-Test "User Profile" "GET /users/me" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 19: Profile API Excludes Password Field
try {
    $hasPassword = ($profile.PSObject.Properties.Name -contains "password" -or $profile.PSObject.Properties.Name -contains "passwordHash")
    Record-Test "Security / Privacy" "Profile API Excludes Password Field" "No password in response" "hasPassword: $hasPassword" (!$hasPassword) "Password is never leaked in DTO"
} catch {
    Record-Test "Security / Privacy" "Profile API Excludes Password Field" "No password" "$($_.Exception.Message)" $false "$_"
}

# ==========================================
# 4. SPORTS & FACILITIES TESTS
# ==========================================
# Test 20: Query Indoor Sports
try {
    $indoor = Invoke-RestMethod -Uri "http://localhost:8080/api/sports?type=INDOOR" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $names = ($indoor | ForEach-Object { $_.name }) -join ", "
    Record-Test "Sports & Facilities" "Query Indoor Sports" "4 sports (Badminton, Volleyball, Table Tennis, Carrom)" "$($indoor.Count) sports: $names" ($indoor.Count -eq 4) "Correct facility mapping"
} catch {
    Record-Test "Sports & Facilities" "Query Indoor Sports" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 21: Query Outdoor Sports
try {
    $outdoor = Invoke-RestMethod -Uri "http://localhost:8080/api/sports?type=OUTDOOR" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $names = ($outdoor | ForEach-Object { $_.name }) -join ", "
    Record-Test "Sports & Facilities" "Query Outdoor Sports" "4 sports (Cricket, Football, Netball, Athletics)" "$($outdoor.Count) sports: $names" ($outdoor.Count -eq 4) "Correct facility mapping"
} catch {
    Record-Test "Sports & Facilities" "Query Outdoor Sports" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 22: Query Initial Time Slots (07:00 - 19:00 = 12 slots, all AVAILABLE)
try {
    $testDate = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
    $slotsRes = Invoke-RestMethod -Uri "http://localhost:8080/api/sports/1/slots?date=$testDate" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $availCount = ($slotsRes.slots | Where-Object { $_.status -eq "AVAILABLE" }).Count
    Record-Test "Slot Management" "Query Initial Slots (12 slots 07:00-19:00)" "12 AVAILABLE slots" "$($slotsRes.slots.Count) slots, $availCount available" ($slotsRes.slots.Count -eq 12 -and $availCount -eq 12) "Full working day covered"
} catch {
    Record-Test "Slot Management" "Query Initial Slots" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# ==========================================
# 5. INDOOR BOOKING & LECTURER PRIORITY
# ==========================================
# Test 23: Student Books Available Slot (10:00 - 11:00)
try {
    $bookBody = @{ sportId = 1; date = $testDate; start = "10:00"; end = "11:00" } | ConvertTo-Json
    $b1 = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $studentToken" } -Body $bookBody -ContentType "application/json"
    Record-Test "Booking Operations" "Student Books Available Slot" "201 Created & status=PENDING" "Status: $($b1.status)" ($b1.status -eq "PENDING") "Booking ID: $($b1.id)"
} catch {
    Record-Test "Booking Operations" "Student Books Available Slot" "201 Created" "$($_.Exception.Message)" $false "$_"
}

# Test 24: Verify Slot Status is PENDING
try {
    $slotsRes = Invoke-RestMethod -Uri "http://localhost:8080/api/sports/1/slots?date=$testDate" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $s10 = $slotsRes.slots | Where-Object { $_.start -eq "10:00" }
    Record-Test "Slot Management" "Slot Status reflects PENDING" "PENDING" "$($s10.status)" ($s10.status -eq "PENDING") "Slot held under review"
} catch {
    Record-Test "Slot Management" "Slot Status reflects PENDING" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 25: Reject Duplicate Booking by Same User
try {
    $bookBody = @{ sportId = 1; date = $testDate; start = "10:00"; end = "11:00" } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $studentToken" } -Body $bookBody -ContentType "application/json"
    Record-Test "Booking Operations" "Prevent Duplicate Booking" "409 Conflict" "200 OK" $false "Allowed duplicate"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Booking Operations" "Prevent Duplicate Booking" "409" "$statusCode" ($statusCode -eq 409) "409 returned for duplicate"
}

# Test 26: Lecturer Priority: Lecturer Requests Slot Held by PENDING Student
try {
    $bookBody = @{ sportId = 1; date = $testDate; start = "10:00"; end = "11:00" } | ConvertTo-Json
    $bLec = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $lecToken" } -Body $bookBody -ContentType "application/json"
    
    # Check student booking status - should now be REJECTED
    $myBookings = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/my" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $stBooking = $myBookings | Where-Object { $_.id -eq $b1.id }
    
    # Check student notification
    $stNotifs = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $bumpNotif = $stNotifs | Where-Object { $_.message -like "*given to a lecturer*" }
    
    $passed = ($bLec.status -eq "PENDING" -and $stBooking.status -eq "REJECTED" -and $bumpNotif -ne $null)
    Record-Test "Lecturer Priority" "Lecturer Overrides Pending Student Request" "Lecturer=PENDING, Student=REJECTED, Notification Sent" "Lec=$($bLec.status), Student=$($stBooking.status), BumpNotifFound=$($bumpNotif -ne $null)" $passed "Priority rule cleanly enforced"
} catch {
    Record-Test "Lecturer Priority" "Lecturer Overrides Pending Student Request" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 27: Super Admin Approves Booking -> Slot BOOKED
try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bookings/$($bLec.id)/approve" -Method Put -Headers @{ Authorization="Bearer $adminToken" }
    $slotsRes = Invoke-RestMethod -Uri "http://localhost:8080/api/sports/1/slots?date=$testDate" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $s10 = $slotsRes.slots | Where-Object { $_.start -eq "10:00" }
    Record-Test "Admin Operations" "Admin Approves Booking -> Slot BOOKED" "Slot status BOOKED" "$($s10.status)" ($s10.status -eq "BOOKED") "Approved booking locks slot"
} catch {
    Record-Test "Admin Operations" "Admin Approves Booking" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 28: Lecturer Cannot be Bumped Once APPROVED
try {
    $bookBody = @{ sportId = 1; date = $testDate; start = "10:00"; end = "11:00" } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $lecToken" } -Body $bookBody -ContentType "application/json"
    Record-Test "Lecturer Priority" "Approved Slot is Non-Overridable" "409 Conflict" "200 OK" $false "Overrode approved slot"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Lecturer Priority" "Approved Slot is Non-Overridable" "409" "$statusCode" ($statusCode -eq 409) "409 returned when slot is BOOKED"
}

# ==========================================
# 6. OUTDOOR BOOKING & ADMIN REJECTION
# ==========================================
# Test 29: Student Books Outdoor Football Slot
try {
    $testDateOutdoor = (Get-Date).AddDays(5).ToString("yyyy-MM-dd")
    $outdoorBookBody = @{ sportId = 6; date = $testDateOutdoor; start = "16:00"; end = "17:00" } | ConvertTo-Json
    $bOutdoor = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $stu2Token" } -Body $outdoorBookBody -ContentType "application/json"
    Record-Test "Outdoor Sports" "Student Books Outdoor Football Slot" "201 Created & status=PENDING" "Status: $($bOutdoor.status), Sport: $($bOutdoor.sportName)" ($bOutdoor.status -eq "PENDING" -and $bOutdoor.sportName -eq "Football") "Outdoor booking created"
} catch {
    Record-Test "Outdoor Sports" "Student Books Outdoor Football Slot" "201 Created" "$($_.Exception.Message)" $false "$_"
}

# Test 30: Admin Rejects Booking -> Notification Sent & Slot Released
try {
    $rejRes = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bookings/$($bOutdoor.id)/reject" -Method Put -Headers @{ Authorization="Bearer $adminToken" }
    
    # Check student notification for rejection
    $stuNotifs = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications" -Method Get -Headers @{ Authorization="Bearer $stu2Token" }
    $rejNotif = $stuNotifs | Where-Object { $_.message -like "*rejected*" }
    
    # Check slot is freed back to AVAILABLE
    $slotsRes = Invoke-RestMethod -Uri "http://localhost:8080/api/sports/6/slots?date=$testDateOutdoor" -Method Get -Headers @{ Authorization="Bearer $stu2Token" }
    $s16 = $slotsRes.slots | Where-Object { $_.start -eq "16:00" }
    
    $passed = ($rejRes.message -eq "Booking rejected" -and $rejNotif -ne $null -and $s16.status -eq "AVAILABLE")
    Record-Test "Admin Operations" "Admin Rejects Booking -> Notification Sent & Slot Released" "Status REJECTED, Notif Sent, Slot AVAILABLE" "Message=$($rejRes.message), NotifFound=$($rejNotif -ne $null), Slot=$($s16.status)" $passed "Rejection workflow validated"
} catch {
    Record-Test "Admin Operations" "Admin Rejects Booking -> Notification Sent & Slot Released" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# ==========================================
# 7. CANCELLATION, SECURITY & NOTIFICATIONS
# ==========================================
# Test 31: User Cancels Own Booking (Slot Released)
try {
    $bookBody = @{ sportId = 1; date = $testDate; start = "14:00"; end = "15:00" } | ConvertTo-Json
    $cancelTestBooking = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Post -Headers @{ Authorization="Bearer $studentToken" } -Body $bookBody -ContentType "application/json"
    Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/$($cancelTestBooking.id)" -Method Delete -Headers @{ Authorization="Bearer $studentToken" }
    
    $myBookings = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/my" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $bObj = $myBookings | Where-Object { $_.id -eq $cancelTestBooking.id }
    
    $slotsRes = Invoke-RestMethod -Uri "http://localhost:8080/api/sports/1/slots?date=$testDate" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $s14 = $slotsRes.slots | Where-Object { $_.start -eq "14:00" }
    
    $passed = ($bObj.status -eq "CANCELLED" -and $s14.status -eq "AVAILABLE")
    Record-Test "Booking Operations" "User Cancels Own Booking (Slot Released)" "Status CANCELLED & Slot AVAILABLE" "Status=$($bObj.status), Slot=$($s14.status)" $passed "Slot properly restored"
} catch {
    Record-Test "Booking Operations" "User Cancels Own Booking" "204 No Content" "$($_.Exception.Message)" $false "$_"
}

# Test 32: Non-Admin Blocked from /api/admin/**
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    Record-Test "Security / RBAC" "Block Student from Admin Endpoints" "403 Forbidden" "200 OK" $false "Student accessed admin area"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Security / RBAC" "Block Student from Admin Endpoints" "403" "$statusCode" ($statusCode -eq 403) "Access denied correctly enforced"
}

# Test 33: Block Invalid JWT Token
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/my" -Method Get -Headers @{ Authorization="Bearer invalid-token-xyz" }
    Record-Test "Security / RBAC" "Reject Invalid JWT Token" "401 or 403" "200 OK" $false "Invalid token allowed"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Record-Test "Security / RBAC" "Reject Invalid JWT Token" "401 or 403" "$statusCode" ($statusCode -eq 401 -or $statusCode -eq 403) "Rejected invalid token"
}

# Test 34: Super Admin View All Users
try {
    $users = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Get -Headers @{ Authorization="Bearer $adminToken" }
    Record-Test "Admin Operations" "Admin Lists All Registered Users" "Multiple users (admin, 242203, L001, L002, 242301)" "$($users.Count) users" ($users.Count -ge 4) "Users list verified"
} catch {
    Record-Test "Admin Operations" "Admin Lists All Registered Users" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Test 35: Mark Notification as Read
try {
    $notifs = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $targetNotif = $notifs[0]
    $readRes = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications/$($targetNotif.id)/read" -Method Put -Headers @{ Authorization="Bearer $studentToken" }
    $updatedNotifs = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications" -Method Get -Headers @{ Authorization="Bearer $studentToken" }
    $updatedTarget = $updatedNotifs | Where-Object { $_.id -eq $targetNotif.id }
    Record-Test "Notification System" "Mark Notification as Read" "read=true" "read=$($updatedTarget.read)" ($updatedTarget.read -eq $true) "State updated"
} catch {
    Record-Test "Notification System" "Mark Notification as Read" "200 OK" "$($_.Exception.Message)" $false "$_"
}

# Output Summary
$total = $results.Count
$passedCount = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failedCount = ($results | Where-Object { $_.Status -eq "FAIL" }).Count

Write-Host "=========================================="
Write-Host "TOTAL TESTS: $total | PASSED: $passedCount | FAILED: $failedCount"
Write-Host "=========================================="

$results | ConvertTo-Json -Depth 4 | Out-File -FilePath "test_results.json" -Encoding utf8
