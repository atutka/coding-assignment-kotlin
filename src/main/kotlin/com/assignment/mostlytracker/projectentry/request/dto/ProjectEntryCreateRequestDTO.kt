package com.assignment.mostlytracker.projectentry.request.dto

import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.Size

internal data class ProjectEntryCreateRequestDTO(
        val date: LocalDate,
        @field:DecimalMin("0", message = "Value for timeSpent must be greater than 0", inclusive = false)
        @field:DecimalMax("10", message = "Maximum value for timeSpent is 10", inclusive = true)
        @field:Digits(fraction = 2, integer = 2, message = "Value for timeSpent be must within 0-10 range with maximum of two fraction digits")
        val timeSpent: BigDecimal,
        @field:Size(max = 3000, message = "Description cannot be longer than 3000 characters")
        val description: String? = null
)