package com.assignment.mostlytracker.exception

internal class ProjectEntryDateNotInProjectDateRangeException
    : IllegalArgumentException("Project entry date need to be between project start and end dates")