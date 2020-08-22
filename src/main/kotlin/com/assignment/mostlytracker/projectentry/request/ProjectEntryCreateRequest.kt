package com.assignment.mostlytracker.projectentry.request

import java.math.BigDecimal
import java.time.LocalDate

internal data class ProjectEntryCreateRequest(
        val date: LocalDate,
        val timeSpent: BigDecimal,
        val description: String? = null,
        val projectId: Long
)