# Proxy Pattern - Secure & Lazy-Load Reports: Changes Made

## Overview
Implemented the **Proxy** design pattern to add access control and lazy loading to report access. The proxy acts as a gatekeeper, checking permissions before loading expensive report files.

## Changes Made

### 1. Implemented `RealReport` with Expensive Loading
**Purpose:** Encapsulate the actual report implementation and expensive disk operations.

**Changes:**
- Moved file loading logic from `ReportFile` into `RealReport`
- Implemented `display()` method with:
  - Disk load simulation (120ms delay)
  - Console output showing the report content

**Code:**
```java
@Override
public void display(User user) {
    String content = loadFromDisk();
    System.out.println("REPORT -> id=" + reportId + " title=" + title + 
                       " classification=" + classification + " openedBy=" + user.getName());
    System.out.println("CONTENT: " + content);
}

private String loadFromDisk() {
    System.out.println("[disk] loading report " + reportId + " ...");
    try { Thread.sleep(120); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    return "Internal report body for " + title;
}
```

**Why:**
- `RealReport` is the "real subject" behind the proxy
- Contains all expensive operations that should be deferred until actually needed
- Keeps expensive logic separate from control logic (in the proxy)

### 2. Implemented `ReportProxy` with Access Control & Lazy Loading
**Purpose:** Control access to reports and defer expensive loading until authorized.

**Changes:**
- Added nullable `RealReport` field: `private RealReport realReport;` (initially null)
- Implemented `display()` method with three responsibilities:
  1. Check access control first
  2. Lazy-load the real report if access granted
  3. Cache the loaded report for future calls

**Code:**
```java
@Override
public void display(User user) {
    // 1. Access check
    if (!accessControl.canAccess(user, classification)) {
        System.out.println("ACCESS DENIED: " + user.getName() + 
                          " cannot access " + classification + " report " + reportId);
        return;
    }
    
    // 2. Lazy-load (only on first authorized call)
    if (realReport == null) {
        realReport = new RealReport(reportId, title, classification);
    }
    
    // 3. Use cached real report
    realReport.display(user);
}
```

**Why:**
- **Access Control First:** Denies unauthorized access immediately, before any expensive operations
- **Lazy Loading:** `RealReport` is created only when:
  - User has permission AND
  - The report is actually viewed (not just access attempt)
- **Caching:** Once loaded, the same proxy instance reuses the cached `RealReport` on subsequent calls

### 3. Updated `ReportViewer` to Use `Report` Interface
**Purpose:** Make the viewer depend on abstraction, not concrete implementations.

**Changes:**
- Old signature: `public void open(ReportFile report, User user)`
- New signature: `public void open(Report report, User user)`

**Why:**
- Works transparently with any `Report` implementation (real or proxy)
- Decouples viewer from concrete classes
- Enables proxy pattern without changing viewer code

### 4. Refactored `App` to Use Proxies
**Purpose:** Ensure all report access goes through the proxy layer.

**Changes:**
- Old: `ReportFile publicReport = new ReportFile(...)`
- New: `Report publicReport = new ReportProxy(...)`

**Why:**
- Every report access is now gated by proxy security & caching logic
- Access control happens automatically
- Lazy loading is transparent to the client

## Security & Performance Improvements

### Before (Broken Code):
```
User attempts access → Immediate disk load (every time) → No access check → Anyone can read any report
```

### After (Proxy Pattern):
```
User attempts access → Access check → If denied: stop (no load)
                                    → If allowed: lazy-load (only if first call) → Cache → Display
```

## Key Behaviors Achieved

### 1. **Access Control**
- Students viewing FACULTY reports get denied
- Students viewing ADMIN reports get denied
- Only authorized roles can access their reports

**Example:**
```
viewer.open(facultyReport, student); 
// Output: "ACCESS DENIED: Jasleen cannot access FACULTY report R-202"
// No disk load happens
```

### 2. **Lazy Loading**
- Report file is loaded only when:
  - User has permission AND
  - User actually calls `display()`

**Example:**
```
Report adminReport = new ReportProxy(...); // Nothing loaded yet
adminReport.display(admin); // First call: [disk] loading report ...
adminReport.display(admin); // Second call: REUSES cached real report (no disk load)
```

### 3. **Caching Per Proxy**
- Each `ReportProxy` instance caches its own `RealReport`
- Different proxies maintain separate caches
- Repeated access through the same proxy avoids redundant loads

## Design Benefits

### 1. **Separation of Concerns**
- Proxy handles: access control, lazy loading, caching
- Real report handles: actual display logic and disk I/O

### 2. **Security First**
- Access checks happen before expensive operations
- Prevents unauthorized users from triggering disk loads

### 3. **Performance**
- Lazy loading defers expensive file I/O
- Caching eliminates redundant disk reads
- Authorized users see only one disk load per unique report

### 4. **Transparency**
- Clients code against `Report` interface
- Proxy presence is invisible to callers
- Easy to add/remove proxies without changing client code

### 5. **Easy to Extend**
- Want to add request logging? Proxy can log all access attempts
- Want to add quotas? Proxy can track usage per user
- Want to add metrics? Proxy can measure load times
- All without touching real report or client code

## Comparison: Direct Access vs. Proxy

### Direct Access (No Proxy):
```java
report.display(user); // Always runs, no checks
// Risk: Unauthorized access, repeated loads, no logging
```

### Via Proxy (This Implementation):
```java
proxyReport.display(user); 
// Safe: Access checked → Lazy-loaded → Cached → Can be logged/monitored
```

## Key Takeaway
The Proxy pattern introduces a control layer that sits between clients and expensive resources. It's essential for implementing security, lazy loading, caching, and monitoring without modifying the real subject or client code. The proxy acts as a "surrogate" providing controlled access to the actual resource.
