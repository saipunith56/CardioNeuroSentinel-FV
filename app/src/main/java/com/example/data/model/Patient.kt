package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mrn: String,
    val name: String,
    val age: Int,
    val gender: String, // "Male", "Female", "Other"
    val bloodPressure: String, // e.g. "115/76"
    val cholesterol: Int, // e.g. 170
    val bloodGroup: String, // e.g. "O+"
    val conditionTag: String, // e.g. "Hypertension & Atherosclerosis"
    val activeCase: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
