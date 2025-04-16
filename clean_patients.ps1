# Clean up patients.csv file by removing duplicates

# Read the CSV file
$patientsFile = "hospital_data\patients.csv"
$patients = Import-Csv -Path $patientsFile

# Track unique combinations of firstName, lastName, dob, gender
$uniquePatients = @{}
$cleanedPatients = @()

# Header for the CSV
$headerRow = "id,firstName,lastName,dob,gender,contactNumber,address,admitted"

# Keep some specific patient IDs that might be referenced elsewhere (P100-P105)
$keepIds = @("P100", "P101", "P102", "P103", "P104", "P105")

Write-Host "Original patient count: $($patients.Count)"

# Filter patients to keep unique records and preserve certain IDs
foreach ($patient in $patients) {
    $key = "$($patient.firstName)_$($patient.lastName)_$($patient.dob)_$($patient.gender)"
    
    # Keep specific IDs or unique patient combinations
    if ($keepIds -contains $patient.id -or -not $uniquePatients.ContainsKey($key)) {
        $uniquePatients[$key] = $patient
        $cleanedPatients += $patient
    }
}

Write-Host "Cleaned patient count: $($cleanedPatients.Count)"

# Prepare new CSV content
$newCsvContent = $headerRow
foreach ($patient in $cleanedPatients) {
    $line = "$($patient.id),$($patient.firstName),$($patient.lastName),$($patient.dob),$($patient.gender),$($patient.contactNumber),$($patient.address),$($patient.admitted)"
    $newCsvContent += "`n$line"
}

# Write to new file
Set-Content -Path "hospital_data\patients_clean.csv" -Value $newCsvContent

# Replace the original file
Move-Item -Path "hospital_data\patients_clean.csv" -Destination $patientsFile -Force

Write-Host "Patients data cleaned successfully." 