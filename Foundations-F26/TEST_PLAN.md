# Step-by-Step Test Plan for Foundations-F26

## Prerequisites
- Start with **empty database** (delete `foundations.db` if exists)
- Run: `java --module-path /home/yogi/javafx-sdk-25.0.4/lib --add-modules javafx.controls,javafx.fxml -cp bin applicationMain.FoundationsMain`

---

## Phase 1: First-Time Admin Setup (guiFirstAdmin)

### 1.1 Launch with Empty Database
**Action:** Start application
**Expected:** First Admin page displays, all fields empty, "Setup Admin Account" button **disabled**, no PasswordStatusPanel visible

### 1.2 Invalid Username Format
**Action:** Enter `ab` (too short), tab to password
**Expected:** Username error alert with header **"Invalid Username"**, message from UserNameRecognizer

### 1.3 Valid Username, Duplicate Check
**Action:** Enter valid username (e.g., `adminuser`), tab to password
**Expected:** No error yet

### 1.4 Password Requirements - Start Typing
**Action:** Click in first password field, type `A`
**Expected:** PasswordStatusPanel **appears below password fields** with 6 rows (all ✗ red):
- ✗ at least 8 characters
- ✗ an uppercase letter  
- ✗ a lowercase letter
- ✗ a number
- ✗ a special character
- ✗ both passwords match
Button remains disabled

### 1.5 Password Requirements - Progressive
**Action:** Continue typing: `Aa1!` → `Aa1!long`
**Expected:** Labels flip to ✓ green progressively:
- `Aa1!` → uppercase ✓, lowercase ✓, number ✓, special ✓, length ✗, match ✗
- `Aa1!long` → length ✓, match ✗

### 1.6 Confirm Password - Mismatch
**Action:** Enter different password in second field
**Expected:** "both passwords match" stays ✗, button **disabled**

### 1.7 Confirm Password - Match
**Action:** Enter same valid password in second field
**Expected:** "both passwords match" flips to ✓, button **enabled**

### 1.8 Click Setup Admin Account
**Action:** Click enabled button
**Expected:** Navigates to UserUpdate page

---

## Phase 2: User Login (guiUserLogin)

### 2.1 Invalid Username
**Action:** Enter non-existent username, any password, click Log In
**Expected:** Alert "Incorrect username/password. Try again!" (no info leak)

### 2.2 Valid Username, Wrong Password
**Action:** Enter `adminuser` (from Phase 1), wrong password
**Expected:** Same generic message

### 2.3 Valid Credentials, Not OTP
**Action:** Enter correct username/password
**Expected:** Dispatches to AdminHome (since admin role)

### 2.4 OTP Flow
**Prereq:** Admin sets OTP for another user (see Phase 5.4)
**Action:** Log in as that user with OTP password
**Expected:** OTP message → navigates to UserUpdate

### 2.5 Length Limits
**Action:** Username >16 chars or password >64 chars
**Expected:** Rejected immediately with generic message, no DB lookup

---

## Phase 3: New Account via Invitation (guiNewAccount)

### 3.1 Invalid Invitation Code
**Action:** Enter fake code on login page, click "Setup Account"
**Expected:** "The invitation code is not valid" alert

### 3.2 Valid Invitation, Invalid Username
**Action:** Valid code, invalid username format
**Expected:** Alert with header **"Invalid Username"**

### 3.3 Valid Invitation, Duplicate Username
**Action:** Valid code, username that exists
**Expected:** Alert with header **"Username Taken"**

### 3.4 Password Mismatch
**Action:** Valid username, two different passwords, click User Setup
**Expected:** Alert with header **"Passwords Do Not Match"**, content "The two passwords must be identical. Please try again."

### 3.5 Password Requirements Panel
**Action:** Type in first password field
**Expected:** PasswordStatusPanel appears below fields, same 6 rows as Phase 1.4

### 3.6 Valid Password + Match
**Action:** Strong password in both fields
**Expected:** All 6 ✓, button enabled, click → creates account → UserUpdate

---

## Phase 4: User Update Page (guiUserUpdate)

### 4.1 Update Username - Own Current Name
**Action:** Click "Update Username", enter same username
**Expected:** Accepts, no "already taken" error

### 4.2 Update Username - Other User's Name
**Action:** Click "Update Username", enter existing different username
**Expected:** Alert "That username is already taken."

### 4.3 Update Username - Invalid Format
**Action:** Click "Update Username", enter invalid format
**Expected:** Username validator error

### 4.4 Update Password - Weak
**Action:** Click "Update Password", type weak password in dialog
**Expected:** Save **disabled**, PasswordStatusPanel shows ✗ for missing requirements

### 4.5 Update Password - Strong
**Action:** Type strong password in dialog
**Expected:** Save **enabled**, all ✓, click Save → updates, refreshes label

### 4.6 Cancel Password Update
**Action:** Click "Update Password", click Cancel
**Expected:** No change to password

### 4.7 OTP User Blocked
**Prereq:** User has OTP flag set
**Action:** Click "Proceed to the User Home Page"
**Expected:** Warning "You must set a new password before continuing", stays on page

### 4.8 Normal User Proceeds
**Action:** Normal user clicks "Proceed to the User Home Page"
**Expected:** Dispatches via GUISingleRoleDispatch to correct home page

---

## Phase 5: Admin Home Page (guiAdminHome)

### 5.1 Send Invitation - Invalid Email
**Action:** Enter invalid email, click "Send Invitation"
**Expected:** Email validator error

### 5.2 Send Invitation - Duplicate Email
**Action:** Send invitation to same email twice
**Expected:** "An invitation has already been sent to this email address"

### 5.3 Send Invitation - Valid
**Action:** Valid email, select role, click "Send Invitation"
**Expected:** Shows code in alert, invitation count increments

### 5.4 Manage Invitations
**Action:** Click "Manage Invitations"
**Expected:** Modal table with columns: Code, Email, Role, Deadline
- Select row, click "Cancel Selected Invitation" → confirms, deletes, refreshes

### 5.5 Set One-Time Password
**Action:** Click "Set a One-Time Password"
**Expected:** Username dialog → password dialog
- Invalid username → "No account exists with that username"
- Invalid password format → Password validator error
- Valid → Sets OTP flag, success message

### 5.6 Delete User
**Action:** Click "Delete a User"
**Expected:** UserListView with Delete button
- Select self → "An admin cannot delete their own account"
- Select other, confirm → deletes, refreshes table

### 5.7 List All Users
**Action:** Click "List All Users"
**Expected:** UserListView read-only (no Delete button)

### 5.8 Add/Remove Roles
**Action:** Click "Add/Remove Roles"
**Expected:** Opens ViewAddRemoveRoles (see Phase 6)

---

## Phase 6: Add/Remove Roles (guiAddRemoveRoles)

### 6.1 No User Selected
**Action:** Open page
**Expected:** Only user selector ComboBox visible

### 6.2 Select User
**Action:** Select user from ComboBox
**Expected:** Shows current roles, add/remove ComboBoxes (filtered - only roles user doesn't have in add, only roles user has in remove)

### 6.3 Add Role
**Action:** Select role to add, click "Add This Role"
**Expected:** DB updated, UI refreshes, role moves from add list to remove list

### 6.4 Remove Role
**Action:** Select role to remove, click "Remove This Role"
**Expected:** DB updated, UI refreshes, role moves from remove list to add list

### 6.5 Return
**Action:** Click "Return"
**Expected:** Back to AdminHome

---

## Phase 7: OTP End-to-End Flow

### 7.1 Admin Sets OTP
**Action:** Admin Home → "Set a One-Time Password" → enter username + temp password
**Expected:** Success message, OTP flag set in DB

### 7.2 User Logs In with OTP
**Action:** Log in as that user with temp password
**Expected:** OTP message → UserUpdate page

### 7.3 User Tries Proceed Without Password Change
**Action:** Click "Proceed to the User Home Page"
**Expected:** Blocked with warning

### 7.4 User Updates Password
**Action:** "Update Password" → PasswordEntryDialog → strong password → Save
**Expected:** OTP flag cleared → redirected to Login page

### 7.5 User Logs In with New Password
**Action:** Log in with new password
**Expected:** Normal dispatch to home page

---

## Quick Verification Checklist

| Feature | FirstAdmin | NewAccount | PasswordEntryDialog |
|---------|------------|------------|---------------------|
| Panel hidden initially | ✓ | ✓ | ✓ (shows on dialog open) |
| Panel below password fields | ✓ (50, 295) | ✓ (50, 295) | In dialog VBox |
| 5 requirement labels match validator | ✓ | ✓ | ✓ |
| "both passwords match" label | ✓ | ✓ | N/A (single field) |
| Labels flip ✗→✓ on typing | ✓ | ✓ | ✓ |
| Button disabled until all ✓ + match | ✓ | ✓ | Save disabled until all ✓ |
| Reset on page reuse | N/A | `reset()` in displayNewAccount | New panel per dialog |