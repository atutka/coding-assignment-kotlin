package com.assignment.mostlytracker.projectentry.request.dto

import javax.validation.constraints.Positive

internal data class ProjectEntrySearchRequestDTO(
        @field:Positive(message = "ProjectId cannot be negative value")
        val projectId: Long
)