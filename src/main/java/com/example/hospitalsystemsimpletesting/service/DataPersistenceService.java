package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Appointment;
import com.example.hospitalsystemsimpletesting.model.Bill;
import com.example.hospitalsystemsimpletesting.model.InventoryItem;
import com.example.hospitalsystemsimpletesting.model.MedicalRecord;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.model.Payment;

import java.util.List;

/**
 * Interface for data persistence operations.
 * Implementations of this interface should handle loading and saving
 * data to/from persistent storage (e.g., CSV files, database, etc.)
 */
public interface DataPersistenceService {
    
    /**
     * Save a list of patients to persistent storage
     * 
     * @param patients the list of patients to save
     */
    void savePatients(List<Patient> patients);
    
    /**
     * Load a list of patients from persistent storage
     * 
     * @return the list of patients
     */
    List<Patient> loadPatients();
    
    /**
     * Save a list of appointments to persistent storage
     * 
     * @param appointments the list of appointments to save
     */
    void saveAppointments(List<Appointment> appointments);
    
    /**
     * Load a list of appointments from persistent storage
     * 
     * @return the list of appointments
     */
    List<Appointment> loadAppointments();
    
    /**
     * Save a list of medical records to persistent storage
     * 
     * @param records the list of medical records to save
     */
    void saveMedicalRecords(List<MedicalRecord> records);
    
    /**
     * Load a list of medical records from persistent storage
     * 
     * @return the list of medical records
     */
    List<MedicalRecord> loadMedicalRecords();
    
    /**
     * Save a list of bills to persistent storage
     * 
     * @param bills the list of bills to save
     */
    void saveBills(List<Bill> bills);
    
    /**
     * Load a list of bills from persistent storage
     * 
     * @return the list of bills
     */
    List<Bill> loadBills();
    
    /**
     * Save a list of payments to persistent storage
     * 
     * @param payments the list of payments to save
     */
    void savePayments(List<Payment> payments);
    
    /**
     * Load a list of payments from persistent storage
     * 
     * @return the list of payments
     */
    List<Payment> loadPayments();
    
    /**
     * Save a list of inventory items to persistent storage
     * 
     * @param items the list of inventory items to save
     */
    void saveInventory(List<InventoryItem> items);
    
    /**
     * Load a list of inventory items from persistent storage
     * 
     * @return the list of inventory items
     */
    List<InventoryItem> loadInventory();
} 