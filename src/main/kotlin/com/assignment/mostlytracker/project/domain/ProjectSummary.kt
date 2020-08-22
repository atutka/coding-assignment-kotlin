package com.assignment.mostlytracker.project.domain

import java.math.BigDecimal

internal data class ProjectSummary(
        val totalTimeSpent: BigDecimal,
        val totalDays: Int,
        val averageTimeSpentPerDay: BigDecimal
)