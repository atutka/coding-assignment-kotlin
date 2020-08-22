package com.assignment.mostlytracker.projectentry.request.dto

import java.math.BigDecimal
import java.time.LocalDate

internal data class ProjectEntryDTO(
        val id: Long? = null,
        val date: LocalDate,
        val timeSpent: BigDecimal,
        val description: String? = null
)