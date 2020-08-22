package com.assignment.mostlytracker.project.dto

import java.math.BigDecimal

internal data class ProjectSummaryDTO(
        val totalTimeSpent: BigDecimal,
        val totalDays: Int,
        val averageTimeSpentPerDay: BigDecimal
)