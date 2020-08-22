package com.assignment.mostlytracker.project.request.dto

import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

internal data class ProjectUpdateRequestDTO(
        @field:NotBlank(message = "Name cannot be blank or null")
        @field:Size(max = 200, message = "Name cannot be longer than 200 characters")
        val name: String,
        val startDate: LocalDate,
        val endDate: LocalDate? = null
)