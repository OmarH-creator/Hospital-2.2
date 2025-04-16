# Fix medical records to reference only existing patients

# Read the patients file to get valid patient IDs
$patientsFile = "hospital_data\patients.csv"
$patients = Import-Csv -Path $patientsFile
$validPatientIds = $patients | ForEach-Object { $_.id }

Write-Host "Valid patient IDs: $($validPatientIds -join ', ')"

# Read the medical records file
$recordsFile = "hospital_data\medical_records.csv"
$records = Import-Csv -Path $recordsFile

# Filter records to keep only those with valid patient IDs
$validRecords = @()
$invalidRecords = @()

foreach ($record in $records) {
    if ($validPatientIds -contains $record.patientId) {
        $validRecords += $record
    } else {
        $invalidRecords += $record
    }
}

Write-Host "Total medical records: $($records.Count)"
Write-Host "Valid medical records: $($validRecords.Count)"
Write-Host "Invalid medical records: $($invalidRecords.Count)"

# If there are invalid records, reassign them to valid patients
if ($invalidRecords.Count -gt 0) {
    foreach ($record in $invalidRecords) {
        # Assign to a random valid patient ID
        $randomPatientId = $validPatientIds | Get-Random
        $record.patientId = $randomPatientId
        $validRecords += $record
        
        Write-Host "Reassigned record $($record.id) from $($record.patientId) to $randomPatientId"
    }
}

# Header for the CSV
$headerRow = "id,patientId,diagnosis,notes,recordDate"

# Prepare new CSV content
$newCsvContent = $headerRow
foreach ($record in $validRecords) {
    $line = "$($record.id),$($record.patientId),$($record.diagnosis),$($record.notes),$($record.recordDate)"
    $newCsvContent += "`n$line"
}

# Write to new file
Set-Content -Path "hospital_data\medical_records_fixed.csv" -Value $newCsvContent

# Replace the original file
Move-Item -Path "hospital_data\medical_records_fixed.csv" -Destination $recordsFile -Force

Write-Host "Medical records references fixed successfully." 