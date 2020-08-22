package com.assignment.mostlytracker.exception

internal class ProjectInvalidDateRangeException : IllegalArgumentException("Project cannot have end date before start date")