# Clean up medical_records.csv file by removing duplicates and fake data

# Read the CSV file
$recordsFile = "hospital_data\medical_records.csv"
$records = Import-Csv -Path $recordsFile

# Track unique combinations to avoid duplicates
$uniqueRecords = @{}
$cleanedRecords = @()

# Header for the CSV
$headerRow = "id,patientId,diagnosis,notes,recordDate"

# Keywords for obviously fake or test data
$fakeKeywords = @("test", "batnyyyyy", "saldkdn", "fake")

Write-Host "Original medical records count: $($records.Count)"

# Filter out duplicate records and records with fake data
foreach ($record in $records) {
    # Skip records with obvious fake data in diagnosis or notes
    $hasFakeData = $false
    foreach ($keyword in $fakeKeywords) {
        if ($record.diagnosis -match $keyword -or $record.notes -match $keyword) {
            $hasFakeData = $true
            break
        }
    }
    
    if ($hasFakeData) {
        continue
    }
    
    # Create a key based on patientId, diagnosis, and recordDate
    $key = "$($record.patientId)_$($record.diagnosis)_$($record.recordDate)"
    
    # Only keep unique combinations
    if (-not $uniqueRecords.ContainsKey($key)) {
        $uniqueRecords[$key] = $record
        $cleanedRecords += $record
    }
}

Write-Host "Cleaned medical records count: $($cleanedRecords.Count)"

# Prepare new CSV content
$newCsvContent = $headerRow
foreach ($record in $cleanedRecords) {
    $line = "$($record.id),$($record.patientId),$($record.diagnosis),$($record.notes),$($record.recordDate)"
    $newCsvContent += "`n$line"
}

# Write to new file
Set-Content -Path "hospital_data\medical_records_clean.csv" -Value $newCsvContent

# Replace the original file
Move-Item -Path "hospital_data\medical_records_clean.csv" -Destination $recordsFile -Force

Write-Host "Medical records data cleaned successfully." 