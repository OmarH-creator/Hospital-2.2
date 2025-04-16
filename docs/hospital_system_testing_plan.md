# Hospital Management System - Testing Plan

## Project Overview
This document outlines the testing approach for our minimal Hospital Management System. The system will be implemented using Java and JavaFX, focusing on simplicity while maintaining full functionality for basic hospital operations.

## System Scope (Minimalist Approach)

### Core Functionality
- Patient registration
- Appointment scheduling
- Medical records management
- Billing
- Inventory control

## System Architecture

### Model
- `Patient`: Basic patient information
- `Appointment`: Simple appointment tracking
- `MedicalRecord`: Basic medical history
- `Bill`: Simple billing information
- `InventoryItem`: Basic inventory tracking

### Service
- `PatientService`: Patient registration and retrieval
- `AppointmentService`: Scheduling appointments
- `MedicalRecordService`: Managing medical records
- `BillingService`: Basic billing operations
- `InventoryService`: Inventory tracking

### Controller
- `HospitalController`: Core business logic connecting all services

### View
- JavaFX minimal UI for all operations

## Testing Strategy Checklist

### 1. JUnit Testing Approaches ✓

- [ ] **Unit Tests for Model Layer**
  - [ ] Test `Patient` class
  - [ ] Test `Appointment` class
  - [ ] Test `MedicalRecord` class
  - [ ] Test `Bill` class
  - [ ] Test `InventoryItem` class

- [ ] **Unit Tests for Service Layer**
  - [ ] Test `PatientService`
  - [ ] Test `AppointmentService`
  - [ ] Test `MedicalRecordService`
  - [ ] Test `BillingService`
  - [ ] Test `InventoryService`

- [ ] **Unit Tests for Controller Layer**
  - [ ] Test `HospitalController` (with mocked services)
  - [ ] Test core business workflows

### 2. White Box Testing (3 Classes) ✓

- [ ] **Class 1: `Patient`**
  - [ ] Ensure complete code coverage
  - [ ] Test all conditional branches

- [ ] **Class 2: `AppointmentService`**
  - [ ] Ensure complete code coverage
  - [ ] Test all conditional branches

- [ ] **Class 3: `HospitalController`**
  - [ ] Ensure complete code coverage
  - [ ] Test all conditional branches

### 3. GUI Testing with FSM ✓

- [ ] **Define Minimal UI States**
  - [ ] Home screen
  - [ ] Patient registration
  - [ ] Appointment scheduling
  - [ ] Medical records view
  - [ ] Billing screen
  - [ ] Inventory screen

- [ ] **Create Simple FSM**
  - [ ] Document basic transitions between screens
  - [ ] Create minimal FSM diagram

- [ ] **Implement Basic GUI Tests**
  - [ ] Test core UI flows
  - [ ] Verify basic data entry and display

### 4. Integration Testing ✓

- [ ] **Test Key Integration Points**
  - [ ] Patient registration + Appointment scheduling
  - [ ] Appointment + Medical records
  - [ ] Medical records + Billing
  - [ ] Full system workflow tests

## Implementation Plan

### Phase 1: Core Implementation
- [ ] Implement model classes
- [ ] Implement service interfaces and implementations
- [ ] Implement controller layer

### Phase 2: Unit and White Box Testing
- [ ] Create minimal test cases for all layers
- [ ] Ensure basic coverage of critical paths

### Phase 3: Simple UI Implementation
- [ ] Create basic JavaFX screens
- [ ] Connect UI to controller
- [ ] Create simple FSM

### Phase 4: GUI and Integration Testing
- [ ] Test basic UI flows
- [ ] Verify fundamental integrations

## Metrics for Success
- [ ] Minimal but complete test coverage
- [ ] All core features functioning
- [ ] Simple documentation
- [ ] Easy to understand codebase 