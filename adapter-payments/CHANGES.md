# Adapter Pattern - Payments: Changes Made

## Overview
Introduced the **Adapter** design pattern to decouple `OrderService` from specific payment SDK implementations. This allows the system to support multiple payment providers without modifying client code.

## Changes Made

### 1. Created `FastPayAdapter.java`
**Purpose:** Adapts the `FastPayClient` SDK to implement the `PaymentGateway` interface.

**Why:**
- `FastPayClient` uses `payNow(String custId, int amountCents)` method signature
- The system needs a unified `PaymentGateway` interface with `charge(String customerId, int amountCents)`
- The adapter bridges this mismatch, translating calls from the interface to the SDK's specific method

**Implementation:**
- Wraps `FastPayClient` instance
- Implements `PaymentGateway` interface
- Delegates `charge()` calls to `payNow()` with proper parameter mapping
- Validates inputs using `Objects.requireNonNull()`

### 2. Created `SafeCashAdapter.java`
**Purpose:** Adapts the `SafeCashClient` SDK to implement the `PaymentGateway` interface.

**Why:**
- `SafeCashClient` uses a two-step process: `createPayment()` then `confirm()`
- Different interface than `FastPayClient`
- Adapter pattern hides this complexity behind the unified `PaymentGateway` interface

**Implementation:**
- Wraps `SafeCashClient` instance
- Implements `PaymentGateway` interface
- Orchestrates the two-step process: creates payment object and calls `confirm()` in one `charge()` call
- Maintains consistency with the interface contract

### 3. Updated `App.java`
**Purpose:** Register adapters in the gateway map instead of raw SDK instances.

**Changes:**
- Replaced TODO comments with actual adapter instantiation
- `gateways.put("fastpay", new FastPayAdapter(new FastPayClient()));`
- `gateways.put("safecash", new SafeCashAdapter(new SafeCashClient()));`

**Why:**
- `OrderService` now receives pre-configured adapters, not SDKs
- The service can call `gateways.get(provider)` and work with any `PaymentGateway` implementation
- Enables easy addition of new providers without changing `OrderService`

## Design Benefits

### 1. **Decoupling**
- `OrderService` depends only on `PaymentGateway` interface
- Changes to SDK implementations don't affect business logic

### 2. **Single Responsibility**
- Each adapter is responsible for translating one SDK's interface to the target interface
- Easy to maintain and test in isolation

### 3. **Open/Closed Principle**
- System is open for extension (add new adapters)
- But closed for modification (no changes to `OrderService`)

### 4. **Vendor Independence**
- Adding a new payment provider requires only creating a new adapter
- No changes to existing code paths

## External Behavior
- Same transaction IDs are printed
- Same output format is maintained
- No changes to how `OrderService` client code looks

## Key Takeaway
The Adapter pattern allows incompatible interfaces to work together through a translation layer, enabling flexible integration of third-party libraries while keeping business logic clean and decoupled.
