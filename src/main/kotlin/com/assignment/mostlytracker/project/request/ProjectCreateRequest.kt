package com.assignment.mostlytracker.project.request

import java.time.LocalDate

internal data class ProjectCreateRequest(
        val name: String,
        val startDate: LocalDate,
        val endDate: LocalDate? = null
)