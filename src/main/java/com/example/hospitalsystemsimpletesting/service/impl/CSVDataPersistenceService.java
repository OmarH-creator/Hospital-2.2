package com.example.hospitalsystemsimpletesting.service.impl;

import com.example.hospitalsystemsimpletesting.model.*;
import com.example.hospitalsystemsimpletesting.service.DataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.PatientService;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the DataPersistenceService using CSV files
 */
public class CSVDataPersistenceService implements DataPersistenceService {
    
    private static final String DATA_DIR = "hospital_data";
    private static final String PATIENTS_FILE = "patients.csv";
    private static final String APPOINTMENTS_FILE = "appointments.csv";
    private static final String MEDICAL_RECORDS_FILE = "medical_records.csv";
    private static final String BILLS_FILE = "bills.csv";
    private static final String PAYMENTS_FILE = "payments.csv";
    private static final String INVENTORY_FILE = "inventory.csv";
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Services needed for relationships
    private final PatientService patientService;
    
    public CSVDataPersistenceService(PatientService patientService) {
        this.patientService = patientService;
        initializeDataStorage();
    }
    
    /**
     * Update the patient service reference
     * Used to resolve circular dependencies
     * @param patientService The patient service to use
     */
    public void setPatientService(PatientService patientService) {
        // Use reflection to modify the final field for this special case
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("patientService");
            field.setAccessible(true);
            field.set(this, patientService);
        } catch (Exception e) {
            System.err.println("Error updating patient service: " + e.getMessage());
        }
    }
    
    /**
     * Initialize data storage directory
     */
    private void initializeDataStorage() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectory(dataDir);
                System.out.println("Created data directory: " + dataDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }
    
    @Override
    public void savePatients(List<Patient> patients) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + PATIENTS_FILE))) {
            // Write header
            writer.println("id,firstName,lastName,dob,gender,contactNumber,address,admitted");
            
            // Write data
            for (Patient patient : patients) {
                writer.println(
                    patient.getId() + "," +
                    patient.getFirstName() + "," +
                    patient.getLastName() + "," +
                    patient.getDateOfBirth().format(dateFormatter) + "," +
                    patient.getGender() + "," +
                    patient.getContactNumber() + "," +
                    patient.getAddress().replace(",", ";") + "," +
                    patient.isAdmitted()
                );
            }
            System.out.println("Saved " + patients.size() + " patients to CSV");
        } catch (IOException e) {
            System.err.println("Error saving patients to CSV: " + e.getMessage());
        }
    }
    
    @Override
    public List<Patient> loadPatients() {
        List<Patient> patients = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, PATIENTS_FILE);
        
        if (!Files.exists(filePath)) {
            return patients;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    String id = data[0];
                    String firstName = data[1];
                    String lastName = data[2];
                    LocalDate dob = LocalDate.parse(data[3], dateFormatter);
                    
                    Patient patient = new Patient(id, firstName, lastName, dob);
                    
                    if (data.length >= 5) {
                        patient.setGender(data[4]);
                    }
                    if (data.length >= 6) {
                        patient.setContactNumber(data[5]);
                    }
                    if (data.length >= 7) {
                        patient.setAddress(data[6].replace(";", ","));
                    }
                    if (data.length >= 8) {
                        patient.setAdmitted(Boolean.parseBoolean(data[7]));
                    }
                    
                    patients.add(patient);
                }
            }
            System.out.println("Loaded " + patients.size() + " patients from CSV");
        } catch (IOException e) {
            System.err.println("Error loading patients from CSV: " + e.getMessage());
        }
        
        return patients;
    }
    
    @Override
    public void saveAppointments(List<Appointment> appointments) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + APPOINTMENTS_FILE))) {
            // Write header
            writer.println("id,patientId,type,dateTime,status");
            
            // Write data
            for (Appointment appointment : appointments) {
                writer.println(
                    appointment.getId() + "," +
                    appointment.getPatient().getId() + "," +
                    appointment.getType() + "," +
                    appointment.getDateTime().format(dateTimeFormatter) + "," +
                    appointment.getStatus().name()
                );
            }
            System.out.println("Saved " + appointments.size() + " appointments to CSV");
        } catch (IOException e) {
            System.err.println("Error saving appointments to CSV: " + e.getMessage());
        }
    }
    
    @Override
    public List<Appointment> loadAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, APPOINTMENTS_FILE);
        
        if (!Files.exists(filePath)) {
            return appointments;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    String id = data[0];
                    String patientId = data[1];
                    String type = data[2];
                    LocalDateTime dateTime = LocalDateTime.parse(data[3], dateTimeFormatter);
                    String statusStr = data[4];
                    
                    // Find the patient
                    Patient patient = patientService.findPatientById(patientId).orElse(null);
                    if (patient != null) {
                        Appointment appointment = new Appointment(id, patient, type, dateTime);
                        try {
                            appointment.setStatus(Appointment.Status.valueOf(statusStr));
                        } catch (IllegalArgumentException e) {
                            // Default to SCHEDULED if status can't be parsed
                            appointment.setStatus(Appointment.Status.SCHEDULED);
                        }
                        
                        appointments.add(appointment);
                    }
                }
            }
            System.out.println("Loaded " + appointments.size() + " appointments from CSV");
        } catch (IOException e) {
            System.err.println("Error loading appointments from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing appointment data: " + e.getMessage());
        }
        
        return appointments;
    }
    
    @Override
    public void saveMedicalRecords(List<MedicalRecord> records) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + MEDICAL_RECORDS_FILE))) {
            // Write header
            writer.println("id,patientId,diagnosis,notes,recordDate");
            
            // Write data
            for (MedicalRecord record : records) {
                writer.println(
                    record.getId() + "," +
                    record.getPatient().getId() + "," +
                    record.getDiagnosis().replace(",", ";") + "," +
                    (record.getNotes() != null ? record.getNotes().replace(",", ";") : "") + "," +
                    record.getRecordDate().format(dateFormatter)
                );
            }
            System.out.println("Saved " + records.size() + " medical records to CSV");
        } catch (IOException e) {
            System.err.println("Error saving medical records to CSV: " + e.getMessage());
        }
    }
    
    @Override
    public List<MedicalRecord> loadMedicalRecords() {
        List<MedicalRecord> records = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, MEDICAL_RECORDS_FILE);
        
        if (!Files.exists(filePath)) {
            return records;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    String id = data[0];
                    String patientId = data[1];
                    String diagnosis = data[2].replace(";", ",");
                    String notes = data[3].replace(";", ",");
                    LocalDate recordDate = LocalDate.parse(data[4], dateFormatter);
                    
                    // Find the patient
                    Patient patient = patientService.findPatientById(patientId).orElse(null);
                    if (patient != null) {
                        MedicalRecord record = new MedicalRecord(id, patient, diagnosis, recordDate);
                        record.setNotes(notes);
                        records.add(record);
                    }
                }
            }
            System.out.println("Loaded " + records.size() + " medical records from CSV");
        } catch (IOException e) {
            System.err.println("Error loading medical records from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing medical record data: " + e.getMessage());
        }
        
        return records;
    }
    
    @Override
    public void saveBills(List<Bill> bills) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + BILLS_FILE))) {
            // Write header
            writer.println("id,patientId,dateIssued,datePaid,status,totalAmount,amountPaid,items");
            
            // Write data
            for (Bill bill : bills) {
                // Format items as: description1:amount1|description2:amount2|...
                StringBuilder itemsBuilder = new StringBuilder();
                for (Bill.BillItem item : bill.getItems()) {
                    if (itemsBuilder.length() > 0) {
                        itemsBuilder.append("|");
                    }
                    itemsBuilder.append(item.getDescription().replace(":", "-").replace("|", "/"))
                                .append(":")
                                .append(item.getAmount());
                }
                
                writer.println(
                    bill.getId() + "," +
                    bill.getPatient().getId() + "," +
                    bill.getDateIssued().format(dateFormatter) + "," +
                    (bill.getDatePaid() != null ? bill.getDatePaid().format(dateFormatter) : "NULL") + "," +
                    bill.getStatus() + "," +
                    bill.getTotalAmount() + "," +
                    bill.getAmountPaid() + "," +
                    itemsBuilder.toString()
                );
            }
            System.out.println("Saved " + bills.size() + " bills to CSV");
        } catch (IOException e) {
            System.err.println("Error saving bills to CSV: " + e.getMessage());
        }
    }
    
    @Override
    public List<Bill> loadBills() {
        List<Bill> bills = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, BILLS_FILE);
        
        if (!Files.exists(filePath)) {
            return bills;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    String id = data[0];
                    String patientId = data[1];
                    LocalDate dateIssued = LocalDate.parse(data[2], dateFormatter);
                    LocalDate datePaid = "NULL".equals(data[3]) ? null : LocalDate.parse(data[3], dateFormatter);
                    String status = data[4];
                    double totalAmount = Double.parseDouble(data[5]);
                    double amountPaid = Double.parseDouble(data[6]);
                    
                    // Find the patient
                    Patient patient = patientService.findPatientById(patientId).orElse(null);
                    if (patient != null) {
                        Bill bill = new Bill(id, patient, dateIssued, datePaid, status, totalAmount, amountPaid);
                        
                        // Add items if available
                        if (data.length >= 8 && !data[7].isEmpty()) {
                            String[] items = data[7].split("\\|");
                            for (String item : items) {
                                String[] itemData = item.split(":");
                                if (itemData.length == 2) {
                                    String description = itemData[0].replace("-", ":").replace("/", "|");
                                    double amount = Double.parseDouble(itemData[1]);
                                    bill.addItem(description, amount);
                                }
                            }
                        }
                        
                        bills.add(bill);
                    }
                }
            }
            System.out.println("Loaded " + bills.size() + " bills from CSV");
        } catch (IOException e) {
            System.err.println("Error loading bills from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing bill data: " + e.getMessage());
        }
        
        return bills;
    }
    
    @Override
    public void savePayments(List<Payment> payments) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + PAYMENTS_FILE))) {
            // Write header
            writer.println("id,billId,amount,paymentDateTime,paymentMethod,status");
            
            // Write data
            for (Payment payment : payments) {
                writer.println(
                    payment.getId() + "," +
                    payment.getBillId() + "," +
                    payment.getAmount() + "," +
                    payment.getPaymentDateTime().format(dateTimeFormatter) + "," +
                    payment.getPaymentMethod() + "," +
                    payment.getStatus().name()
                );
            }
            System.out.println("Saved " + payments.size() + " payments to CSV");
        } catch (IOException e) {
            System.err.println("Error saving payments to CSV: " + e.getMessage());
        }
    }
    
    @Override
    public List<Payment> loadPayments() {
        List<Payment> payments = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, PAYMENTS_FILE);
        
        if (!Files.exists(filePath)) {
            return payments;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 6) {
                    String id = data[0];
                    String billId = data[1];
                    BigDecimal amount = new BigDecimal(data[2]);
                    LocalDateTime paymentDateTime = LocalDateTime.parse(data[3], dateTimeFormatter);
                    String paymentMethod = data[4];
                    String statusStr = data[5];
                    
                    // Create payment
                    Payment payment = new Payment(id, billId, amount, paymentMethod);
                    
                    // Set status if available
                    try {
                        payment.setStatus(Payment.PaymentStatus.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        // Status is already set to COMPLETED by default in constructor
                    }
                    
                    payments.add(payment);
                }
            }
            System.out.println("Loaded " + payments.size() + " payments from CSV");
        } catch (IOException e) {
            System.err.println("Error loading payments from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing payment data: " + e.getMessage());
        }
        
        return payments;
    }
    
    @Override
    public void saveInventory(List<InventoryItem> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + INVENTORY_FILE))) {
            // Write header
            writer.println("id,name,quantity,unitPrice,category,minQuantity");
            
            // Write data
            for (InventoryItem item : items) {
                writer.println(
                    item.getId() + "," +
                    item.getName().replace(",", ";") + "," +
                    item.getQuantity() + "," +
                    item.getUnitPrice() + "," +
                    (item.getCategory() != null ? item.getCategory().replace(",", ";") : "") + "," +
                    item.getMinQuantity()
                );
            }
            System.out.println("Saved " + items.size() + " inventory items to CSV");
        } catch (IOException e) {
            System.err.println("Error saving inventory items to CSV: " + e.getMessage());
        }
    }
    
    @Override
    public List<InventoryItem> loadInventory() {
        List<InventoryItem> items = new ArrayList<>();
        Path filePath = Paths.get(DATA_DIR, INVENTORY_FILE);
        
        if (!Files.exists(filePath)) {
            return items;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header
            String line = reader.readLine();
            
            // Read data
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    String id = data[0];
                    String name = data[1].replace(";", ",");
                    int quantity = Integer.parseInt(data[2]);
                    double unitPrice = Double.parseDouble(data[3]);
                    
                    InventoryItem item = new InventoryItem(id, name, quantity, unitPrice);
                    
                    // Set optional fields
                    if (data.length >= 5 && !data[4].isEmpty()) {
                        item.setCategory(data[4].replace(";", ","));
                    }
                    
                    if (data.length >= 6 && !data[5].isEmpty()) {
                        item.setMinQuantity(Integer.parseInt(data[5]));
                    }
                    
                    items.add(item);
                }
            }
            System.out.println("Loaded " + items.size() + " inventory items from CSV");
        } catch (IOException e) {
            System.err.println("Error loading inventory items from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing inventory data: " + e.getMessage());
        }
        
        return items;
    }
} 