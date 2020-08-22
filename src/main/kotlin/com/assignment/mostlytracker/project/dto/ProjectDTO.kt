package com.assignment.mostlytracker.project.dto

import java.time.LocalDate

internal data class ProjectDTO(
        val id: Long,
        val name: String,
        val startDate: LocalDate,
        val endDate: LocalDate?
)