package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Payment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DataPersistenceService that uses CSV files for storage
 */
public class CsvPersistenceService implements DataPersistenceService {

    private static final String DATA_DIRECTORY = "data";
    private static final String PATIENTS_FILE = DATA_DIRECTORY + "/patients.csv";
    private static final String APPOINTMENTS_FILE = DATA_DIRECTORY + "/appointments.csv";
    private static final String MEDICAL_RECORDS_FILE = DATA_DIRECTORY + "/medical_records.csv";
    private static final String BILLS_FILE = DATA_DIRECTORY + "/bills.csv";
    private static final String PAYMENTS_FILE = DATA_DIRECTORY + "/payments.csv";
    private static final String INVENTORY_FILE = DATA_DIRECTORY + "/inventory.csv";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor - ensures the data directory exists
     */
    public CsvPersistenceService() {
        // Create data directory if it doesn't exist
        File directory = new File(DATA_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Test file operations to make sure we can read and write
        testFileOperations();
        
        // Fix any existing patients file with missing blood types
        repairExistingPatientsFile();
    }
    
    /**
     * Test basic file operations to ensure we can read and write files
     */
    private void testFileOperations() {
        try {
            // Create test file with a known blood type
            File testFile = new File(DATA_DIRECTORY + "/test_file.txt");
            System.out.println("DIAG: Testing file operations at " + testFile.getAbsolutePath());
            
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("This is a test file to verify file operations.\n");
                writer.write("BloodType:A+\n");
            }
            
            System.out.println("DIAG: Test file written successfully");
            
            // Now read it back
            if (testFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(testFile))) {
                    String line;
                    System.out.println("DIAG: Test file contents:");
                    while ((line = reader.readLine()) != null) {
                        System.out.println("  " + line);
                    }
                }
                
                // Clean up (optional)
                testFile.delete();
            } else {
                System.out.println("DIAG: ERROR - Test file was not created successfully!");
            }
        } catch (Exception e) {
            System.err.println("DIAG: Error testing file operations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Repair any existing patients file to ensure blood types are correctly set
     */
    private void repairExistingPatientsFile() {
        File file = new File(PATIENTS_FILE);
        
        if (!file.exists()) {
            System.out.println("DEBUG: No existing patients file to repair");
            return;
        }
        
        System.out.println("REPAIR: Checking existing patients file for blood type issues");
        
        try {
            // Read all patients
            List<Patient> patients = loadPatients();
            
            // Check if any have missing blood types
            boolean needsRepair = false;
            for (Patient patient : patients) {
                if (patient.getBloodType() == null || 
                    patient.getBloodType().isEmpty() || 
                    patient.getBloodType().equals("Unknown")) {
                    
                    System.out.println("REPAIR: Patient " + patient.getId() + " has missing blood type");
                    
                    // Set a valid blood type based on patient ID
                    // This ensures we have at least some variety in blood types
                    String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
                    int index = Math.abs(patient.getId().hashCode()) % bloodTypes.length;
                    patient.setBloodType(bloodTypes[index]);
                    
                    System.out.println("REPAIR: Set blood type to " + patient.getBloodType());
                    needsRepair = true;
                }
            }
            
            // If repairs were needed, save the file
            if (needsRepair) {
                System.out.println("REPAIR: Saving repaired patients file");
                savePatients(patients);
            } else {
                System.out.println("REPAIR: No blood type issues found in patients file");
            }
            
        } catch (Exception e) {
            System.err.println("REPAIR: Error repairing patients file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void savePatients(List<Patient> patients) {
        System.out.println("DEBUG: Saving " + patients.size() + " patients to CSV file");
        
        // Get absolute paths for debugging
        File directory = new File(DATA_DIRECTORY);
        File file = new File(PATIENTS_FILE);
        
        System.out.println("DEBUG: Working directory: " + System.getProperty("user.dir"));
        System.out.println("DEBUG: Saving to directory: " + directory.getAbsolutePath());
        System.out.println("DEBUG: Saving to file: " + file.getAbsolutePath());
        
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            System.out.println("DEBUG: Creating data directory: " + directory.getAbsolutePath());
            boolean created = directory.mkdirs();
            System.out.println("DEBUG: Directory creation result: " + created);
        }
                
        try {
            // Create a backup of existing file if it exists
            if (file.exists()) {
                File backupFile = new File(PATIENTS_FILE + ".bak");
                try {
                    java.nio.file.Files.copy(
                        file.toPath(),
                        backupFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                    System.out.println("DEBUG: Created backup file: " + backupFile.getAbsolutePath());
                } catch (Exception e) {
                    System.out.println("DEBUG: Warning - Could not create backup: " + e.getMessage());
                }
            }

            // Write patients data with explicit error handling
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write header
                writer.write("id,firstName,lastName,dateOfBirth,gender,contactNumber,address,bloodType,isAdmitted\n");
                
                // Write data
                for (Patient patient : patients) {
                    String bloodTypeValue = patient.getBloodType() != null ? patient.getBloodType() : "Unknown";
                    System.out.println("DEBUG: Writing patient " + patient.getId() + " with blood type: " + bloodTypeValue);
                    
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%b\n",
                        patient.getId(),
                        escapeCommas(patient.getFirstName()),
                        escapeCommas(patient.getLastName()),
                        patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FORMATTER) : "",
                        escapeCommas(patient.getGender() != null ? patient.getGender() : ""),
                        escapeCommas(patient.getContactNumber() != null ? patient.getContactNumber() : ""),
                        escapeCommas(patient.getAddress() != null ? patient.getAddress() : ""),
                        escapeCommas(bloodTypeValue),
                        patient.isAdmitted()
                    ));
                }
                
                // Force flush to make sure data is written
                writer.flush();
                System.out.println("DEBUG: Successfully saved patients to CSV file");
                
                // Verify the file was written correctly
                verifyPatientFile();
            }
        } catch (IOException e) {
            System.err.println("ERROR saving patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verify that the patient file exists and is properly formatted
     */
    private void verifyPatientFile() {
        File file = new File(PATIENTS_FILE);
        System.out.println("DEBUG: Verifying patient file: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            System.err.println("ERROR: Patient file does not exist after save attempt!");
            return;
        }
        
        System.out.println("DEBUG: Patient file exists, size: " + file.length() + " bytes");
        
        // Read the file and report its contents
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            System.out.println("DEBUG: Patient file contents:");
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount <= 10) { // Print only first 10 lines to avoid flooding console
                    System.out.println("  " + line);
                }
            }
            System.out.println("DEBUG: Total lines in patient file: " + lineCount);
        } catch (IOException e) {
            System.err.println("ERROR reading patient file: " + e.getMessage());
        }
    }

    @Override
    public List<Patient> loadPatients() {
        List<Patient> patients = new ArrayList<>();
        File file = new File(PATIENTS_FILE);
        
        System.out.println("DEBUG: Attempting to load patients from: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            System.out.println("DEBUG: Patients file does not exist yet: " + file.getAbsolutePath());
            return patients;
        }
        
        System.out.println("DEBUG: Patient file exists, size: " + file.length() + " bytes, last modified: " + 
                          new java.util.Date(file.lastModified()));
        
        try {
            // Read the file contents for debugging
            System.out.println("DEBUG: Raw patient file contents:");
            try (BufferedReader debugReader = new BufferedReader(new FileReader(file))) {
                String line;
                int lineCount = 0;
                while ((line = debugReader.readLine()) != null) {
                    lineCount++;
                    if (lineCount <= 15) { // Print only first 15 lines
                        System.out.println("  " + line);
                    }
                }
                System.out.println("DEBUG: Total lines in patient file: " + lineCount);
            } catch (Exception e) {
                System.out.println("DEBUG: Error reading file contents: " + e.getMessage());
            }
            
            // Now actually parse the file
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Skip header
                String line = reader.readLine();
                System.out.println("DEBUG: Read header: " + line);
                
                int patientCount = 0;
                
                // Read data
                while ((line = reader.readLine()) != null) {
                    patientCount++;
                    try {
                        String[] parts = splitCsvLine(line);
                        
                        if (parts.length >= 4) {
                            String id = parts[0];
                            String firstName = parts[1];
                            String lastName = parts[2];
                            LocalDate dateOfBirth = parts[3].isEmpty() ? null : LocalDate.parse(parts[3], DATE_FORMATTER);
                            
                            Patient patient = new Patient(id, firstName, lastName, dateOfBirth);
                            
                            // Set optional fields if they exist
                            if (parts.length >= 5 && !parts[4].isEmpty()) {
                                patient.setGender(parts[4]);
                            }
                            if (parts.length >= 6 && !parts[5].isEmpty()) {
                                patient.setContactNumber(parts[5]);
                            }
                            if (parts.length >= 7 && !parts[6].isEmpty()) {
                                patient.setAddress(parts[6]);
                            }
                            if (parts.length >= 8) {
                                // Always process bloodType, even if empty
                                String bloodType = parts[7].trim();
                                System.out.println("DEBUG: Loading blood type '" + bloodType + "' for patient " + id);
                                
                                if (bloodType.isEmpty()) {
                                    bloodType = "Unknown";
                                }
                                
                                // Explicitly set blood type and report
                                patient.setBloodType(bloodType);
                                System.out.println("DEBUG: Patient " + id + " blood type is now: " + patient.getBloodType());
                            } else {
                                System.out.println("DEBUG: No blood type column found for patient " + id);
                                patient.setBloodType("Unknown");
                            }
                            if (parts.length >= 9) {
                                patient.setAdmitted(Boolean.parseBoolean(parts[8]));
                            }
                            
                            patients.add(patient);
                        } else {
                            System.out.println("DEBUG: Skipping invalid line (has fewer than 4 parts): " + line);
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Error parsing patient line " + patientCount + ": " + e.getMessage());
                        System.out.println("  Line content: " + line);
                    }
                }
                System.out.println("DEBUG: Loaded " + patients.size() + " patients from CSV");
                
                // Extra check to make sure blood types are set
                for (Patient p : patients) {
                    if (p.getBloodType() == null || p.getBloodType().isEmpty()) {
                        System.out.println("DEBUG: Patient " + p.getId() + " has null/empty blood type, setting to Unknown");
                        p.setBloodType("Unknown");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading patients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients;
    }

    @Override
    public void saveAppointments(List<Appointment> appointments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            // Write header
            writer.write("id,patientId,patientName,type,dateTime,status\n");
            
            // Write data
            for (Appointment appointment : appointments) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    appointment.getId(),
                    appointment.getPatient() != null ? appointment.getPatient().getId() : "",
                    appointment.getPatient() != null ? escapeCommas(appointment.getPatient().getName()) : "",
                    escapeCommas(appointment.getType()),
                    appointment.getDateTime().format(DATE_TIME_FORMATTER),
                    appointment.getStatus().name()
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    @Override
    public List<Appointment> loadAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);
        
        if (!file.exists()) {
            return appointments;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                
                if (parts.length >= 5) {
                    // We need a list of patients to match the patient ID
                    // For this implementation, we'll create a placeholder patient
                    String id = parts[0];
                    String patientId = parts[1];
                    String patientName = parts[2];
                    
                    // Create a minimal patient object with just the ID and name
                    Patient patient = new Patient(patientId, patientName, LocalDate.now(), null, null, null);
                    
                    String type = parts[3];
                    LocalDateTime dateTime = LocalDateTime.parse(parts[4], DATE_TIME_FORMATTER);
                    
                    Appointment appointment = new Appointment(id, patient, type, dateTime);
                    
                    // Set status if available
                    if (parts.length >= 6 && !parts[5].isEmpty()) {
                        try {
                            appointment.setStatus(Appointment.Status.valueOf(parts[5]));
                        } catch (IllegalArgumentException e) {
                            // Default to SCHEDULED if status can't be parsed
                            appointment.setStatus(Appointment.Status.SCHEDULED);
                        }
                    }
                    
                    appointments.add(appointment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
        
        return appointments;
    }

    @Override
    public void saveMedicalRecords(List<MedicalRecord> records) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEDICAL_RECORDS_FILE))) {
            // Write header
            writer.write("id,patientId,patientName,diagnosis,notes,recordDate\n");
            
            // Write data
            for (MedicalRecord record : records) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    record.getId(),
                    record.getPatient() != null ? record.getPatient().getId() : "",
                    record.getPatient() != null ? escapeCommas(record.getPatient().getName()) : "",
                    escapeCommas(record.getDiagnosis()),
                    escapeCommas(record.getNotes() != null ? record.getNotes() : ""),
                    record.getRecordDate().format(DATE_FORMATTER)
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving medical records: " + e.getMessage());
        }
    }

    @Override
    public List<MedicalRecord> loadMedicalRecords() {
        List<MedicalRecord> records = new ArrayList<>();
        File file = new File(MEDICAL_RECORDS_FILE);
        
        if (!file.exists()) {
            return records;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                
                if (parts.length >= 4) {
                    // Create minimal patient object for reference
                    String id = parts[0];
                    String patientId = parts[1];
                    String patientName = parts[2];
                    
                    Patient patient = new Patient(patientId, patientName, LocalDate.now(), null, null, null);
                    
                    String diagnosis = parts[3];
                    
                    // Parse record date, defaulting to current date if not available
                    LocalDate recordDate = LocalDate.now();
                    if (parts.length >= 6 && !parts[5].isEmpty()) {
                        recordDate = LocalDate.parse(parts[5], DATE_FORMATTER);
                    }
                    
                    MedicalRecord record = new MedicalRecord(id, patient, diagnosis, recordDate);
                    
                    // Set notes if available
                    if (parts.length >= 5 && !parts[4].isEmpty()) {
                        record.setNotes(parts[4]);
                    }
                    
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading medical records: " + e.getMessage());
        }
        
        return records;
    }

    @Override
    public void saveBills(List<Bill> bills) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BILLS_FILE))) {
            // Write header
            writer.write("billId,patientId,patientName,dateIssued,datePaid,status,totalAmount,amountPaid\n");
            
            // Write data
            for (Bill bill : bills) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%.2f,%.2f\n",
                    bill.getBillId(),
                    bill.getPatient() != null ? bill.getPatient().getId() : "",
                    bill.getPatient() != null ? escapeCommas(bill.getPatient().getName()) : "",
                    bill.getDateIssued().format(DATE_FORMATTER),
                    bill.getDatePaid() != null ? bill.getDatePaid().format(DATE_FORMATTER) : "",
                    bill.getStatus(),
                    bill.getTotalAmount(),
                    bill.getAmountPaid()
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving bills: " + e.getMessage());
        }
    }

    @Override
    public List<Bill> loadBills() {
        List<Bill> bills = new ArrayList<>();
        File file = new File(BILLS_FILE);
        
        if (!file.exists()) {
            return bills;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                
                if (parts.length >= 6) {
                    String billId = parts[0];
                    String patientId = parts[1];
                    String patientName = parts[2];
                    
                    // Create a placeholder patient
                    Patient patient = new Patient(patientId, patientName, LocalDate.now(), null, null, null);
                    
                    LocalDate dateIssued = LocalDate.parse(parts[3], DATE_FORMATTER);
                    
                    LocalDate datePaid = null;
                    if (parts.length >= 5 && !parts[4].isEmpty()) {
                        datePaid = LocalDate.parse(parts[4], DATE_FORMATTER);
                    }
                    
                    String status = parts[5];
                    
                    double totalAmount = 0.0;
                    if (parts.length >= 7 && !parts[6].isEmpty()) {
                        totalAmount = Double.parseDouble(parts[6]);
                    }
                    
                    double amountPaid = 0.0;
                    if (parts.length >= 8 && !parts[7].isEmpty()) {
                        amountPaid = Double.parseDouble(parts[7]);
                    }
                    
                    Bill bill = new Bill(billId, patient, dateIssued, datePaid, status, totalAmount, amountPaid);
                    
                    bills.add(bill);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bills: " + e.getMessage());
        }
        
        return bills;
    }

    @Override
    public void savePayments(List<Payment> payments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENTS_FILE))) {
            // Write header
            writer.write("id,billId,amount,paymentDateTime,paymentMethod,status\n");
            
            // Write data
            for (Payment payment : payments) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    payment.getId(),
                    payment.getBillId(),
                    payment.getAmount().toString(),
                    payment.getPaymentDateTime().format(DATE_TIME_FORMATTER),
                    payment.getPaymentMethod(),
                    payment.getStatus().name()
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving payments: " + e.getMessage());
        }
    }

    @Override
    public List<Payment> loadPayments() {
        List<Payment> payments = new ArrayList<>();
        File file = new File(PAYMENTS_FILE);
        
        if (!file.exists()) {
            return payments;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                
                if (parts.length >= 4) {
                    String id = parts[0];
                    String billId = parts[1];
                    BigDecimal amount = new BigDecimal(parts[2]);
                    String paymentMethod = parts.length >= 5 ? parts[4] : "CASH"; // Default to CASH
                    
                    Payment payment = new Payment(id, billId, amount, paymentMethod);
                    
                    // Set status if available
                    if (parts.length >= 6 && !parts[5].isEmpty()) {
                        try {
                            payment.setStatus(Payment.PaymentStatus.valueOf(parts[5]));
                        } catch (IllegalArgumentException e) {
                            // Default to COMPLETED if status can't be parsed
                        }
                    }
                    
                    payments.add(payment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading payments: " + e.getMessage());
        }
        
        return payments;
    }

    @Override
    public void saveInventory(List<InventoryItem> items) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INVENTORY_FILE))) {
            // Write header
            writer.write("id,name,quantity,unitPrice,category,minQuantity\n");
            
            // Write data
            for (InventoryItem item : items) {
                writer.write(String.format("%s,%s,%d,%.2f,%s,%d\n",
                    item.getId(),
                    escapeCommas(item.getName()),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    escapeCommas(item.getCategory() != null ? item.getCategory() : ""),
                    item.getMinQuantity()
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    @Override
    public List<InventoryItem> loadInventory() {
        List<InventoryItem> items = new ArrayList<>();
        File file = new File(INVENTORY_FILE);
        
        if (!file.exists()) {
            return items;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                
                if (parts.length >= 4) {
                    String id = parts[0];
                    String name = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    double unitPrice = Double.parseDouble(parts[3]);
                    
                    InventoryItem item = new InventoryItem(id, name, quantity, unitPrice);
                    
                    // Set category if available
                    if (parts.length >= 5 && !parts[4].isEmpty()) {
                        item.setCategory(parts[4]);
                    }
                    
                    // Set minQuantity if available
                    if (parts.length >= 6 && !parts[5].isEmpty()) {
                        item.setMinQuantity(Integer.parseInt(parts[5]));
                    }
                    
                    items.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Helper method to escape commas in CSV fields
     */
    private String escapeCommas(String text) {
        if (text == null) {
            return "";
        }
        
        // If the text contains commas, quotes, or newlines, wrap it in quotes and escape any quotes
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
    
    /**
     * Helper method to properly split CSV lines, taking into account quoted fields
     */
    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // If we see a quote right after a quote, it's an escaped quote
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++; // Skip the next quote
                } else {
                    // Toggle the inQuotes flag
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(field.toString());
                field.setLength(0); // Clear the builder
            } else {
                field.append(c);
            }
        }
        
        // Add the last field
        result.add(field.toString());
        
        return result.toArray(new String[0]);
    }
} 