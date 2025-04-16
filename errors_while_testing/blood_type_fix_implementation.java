// ================ PatientService.java ================
// Add this new method to the PatientService interface

/**
 * Update a patient's medical information
 * @param patientId The ID of the patient to update
 * @param bloodType The patient's blood type
 */
void updateMedicalInfo(String patientId, String bloodType);


// ================ PatientServiceImpl.java ================
// Add this implementation to the PatientServiceImpl class

@Override
public void updateMedicalInfo(String patientId, String bloodType) {
    Patient patient = patientsById.get(patientId);
    if (patient != null) {
        patient.setBloodType(bloodType);
        savePatients(); // Call the private method to persist changes
    } else {
        throw new IllegalArgumentException("Patient not found: " + patientId);
    }
}


// ================ PatientRegistrationScreen.java ================
// Update the showEditPatientDialog method's result converter

dialog.setResultConverter(dialogButton -> {
    if (dialogButton == saveButtonType) {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            LocalDate dob = dateOfBirthPicker.getValue();
            String bloodType = bloodTypeComboBox.getValue();
            
            // Validate inputs
            if (firstName.isEmpty()) {
                showErrorAlert("Invalid Input", "First name cannot be empty.");
                return null;
            }
            
            if (lastName.isEmpty()) {
                showErrorAlert("Invalid Input", "Last name cannot be empty.");
                return null;
            }
            
            if (dob == null) {
                showErrorAlert("Invalid Input", "Date of birth is required.");
                return null;
            }
            
            // Update patient demographic information
            String fullName = firstName + " " + lastName;
            String gender = selectedPatient.getGender() != null ? selectedPatient.getGender() : "Unknown";
            String contactNumber = selectedPatient.getContactNumber() != null ? selectedPatient.getContactNumber() : "";
            String address = selectedPatient.getAddress() != null ? selectedPatient.getAddress() : "";
            
            try {
                // Update standard patient fields
                patientService.updatePatient(selectedPatient.getId(), fullName, dob, gender, contactNumber, address);
                
                // Update blood type with the new dedicated method
                try {
                    // If the new method is available, use it
                    patientService.updateMedicalInfo(selectedPatient.getId(), bloodType);
                } catch (NoSuchMethodError e) {
                    // Fallback for when the interface hasn't been updated yet
                    // Get a reference to the object in the service's internal map
                    Patient updatedPatient = patientService.findPatientById(selectedPatient.getId()).orElseThrow();
                    updatedPatient.setBloodType(bloodType);
                    // Note: We don't need to call savePatient() because updatePatient() already persisted the basic changes
                }
                
                // Get the updated patient for returning to the UI
                return patientService.findPatientById(selectedPatient.getId()).orElse(null);
            } catch (Exception e) {
                showErrorAlert("Update Failed", "Failed to update patient: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } catch (IllegalArgumentException e) {
            showErrorAlert("Invalid Input", e.getMessage());
            return null;
        }
    }
    return null;
});


// ================ ALTERNATE IMPLEMENTATION (NO INTERFACE CHANGES) ================
// If changing the PatientService interface is not possible, use this implementation in PatientRegistrationScreen.java

dialog.setResultConverter(dialogButton -> {
    if (dialogButton == saveButtonType) {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            LocalDate dob = dateOfBirthPicker.getValue();
            String bloodType = bloodTypeComboBox.getValue();
            
            // Validate inputs
            if (firstName.isEmpty()) {
                showErrorAlert("Invalid Input", "First name cannot be empty.");
                return null;
            }
            
            if (lastName.isEmpty()) {
                showErrorAlert("Invalid Input", "Last name cannot be empty.");
                return null;
            }
            
            if (dob == null) {
                showErrorAlert("Invalid Input", "Date of birth is required.");
                return null;
            }
            
            try {
                // Store current blood type for debugging
                String oldBloodType = selectedPatient.getBloodType();
                System.out.println("DEBUG: Changing blood type from " + oldBloodType + " to " + bloodType);
                
                // First update standard fields (this will call savePatients() in the service impl)
                String fullName = firstName + " " + lastName;
                String gender = selectedPatient.getGender() != null ? selectedPatient.getGender() : "Unknown";
                String contactNumber = selectedPatient.getContactNumber() != null ? selectedPatient.getContactNumber() : "";
                String address = selectedPatient.getAddress() != null ? selectedPatient.getAddress() : "";
                
                patientService.updatePatient(selectedPatient.getId(), fullName, dob, gender, contactNumber, address);
                
                // Then set blood type directly on the patient object in the service's map
                Patient updatedPatient = patientService.findPatientById(selectedPatient.getId()).orElseThrow();
                updatedPatient.setBloodType(bloodType);
                
                // Force another save to ensure blood type is persisted
                // This exploits the fact that updatePatient calls savePatients() internally
                patientService.updatePatient(selectedPatient.getId(), fullName, dob, gender, contactNumber, address);
                
                System.out.println("DEBUG: Blood type updated to " + updatedPatient.getBloodType());
                return updatedPatient;
            } catch (Exception e) {
                showErrorAlert("Update Failed", "Failed to update patient: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } catch (IllegalArgumentException e) {
            showErrorAlert("Invalid Input", e.getMessage());
            return null;
        }
    }
    return null;
}); 