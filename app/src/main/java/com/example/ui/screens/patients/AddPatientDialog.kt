package com.example.ui.screens.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalCardBorder
import com.example.ui.theme.MedicalSubtleBg
import com.example.ui.theme.MedicalSurface
import com.example.ui.theme.MedicalTextPrimary
import com.example.ui.theme.MedicalTextSecondary

@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        mrn: String,
        age: Int,
        gender: String,
        bp: String,
        chol: Int,
        bloodGroup: String,
        condition: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mrn by remember { mutableStateOf("MRN-" + (100000..999999).random()) }
    var age by remember { mutableStateOf("45") }
    var gender by remember { mutableStateOf("Male") }
    var bp by remember { mutableStateOf("120/80") }
    var chol by remember { mutableStateOf("190") }
    var bloodGroup by remember { mutableStateOf("A+") }
    var condition by remember { mutableStateOf("Cardiovascular Monitoring") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MedicalSubtleBg,
        unfocusedContainerColor = MedicalSubtleBg,
        focusedBorderColor = MedicalBlue,
        unfocusedBorderColor = MedicalCardBorder,
        focusedLabelColor = MedicalBlue,
        unfocusedLabelColor = MedicalTextSecondary,
        focusedTextColor = MedicalTextPrimary,
        unfocusedTextColor = MedicalTextPrimary,
        cursorColor = MedicalBlue
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MedicalSurface,
        title = {
            Text(
                text = "Register New Patient",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = fieldColors,
                    textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_patient_name")
                )
                OutlinedTextField(
                    value = mrn,
                    onValueChange = { mrn = it },
                    label = { Text("MRN ID") },
                    singleLine = true,
                    colors = fieldColors,
                    textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_patient_mrn")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        singleLine = true,
                        colors = fieldColors,
                        textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        singleLine = true,
                        colors = fieldColors,
                        textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bp,
                        onValueChange = { bp = it },
                        label = { Text("Blood Pressure") },
                        singleLine = true,
                        colors = fieldColors,
                        textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = chol,
                        onValueChange = { chol = it },
                        label = { Text("Cholesterol") },
                        singleLine = true,
                        colors = fieldColors,
                        textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group") },
                        singleLine = true,
                        colors = fieldColors,
                        textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Primary Diagnosis") },
                        singleLine = true,
                        colors = fieldColors,
                        textStyle = TextStyle(fontSize = 14.sp, color = MedicalTextPrimary),
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            mrn.trim(),
                            age.toIntOrNull() ?: 40,
                            gender.trim(),
                            bp.trim(),
                            chol.toIntOrNull() ?: 180,
                            bloodGroup.trim(),
                            condition.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_add_patient_button")
            ) {
                Text("Register Patient", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MedicalTextSecondary)
            }
        }
    )
}
