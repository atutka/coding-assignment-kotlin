package com.assignment.mostlytracker.exception

import javax.validation.ConstraintViolation

internal data class MissingRequirementParameter(val parameterName: String)
    : IllegalStateException("Missing required parameter: $parameterName")

internal class SizeLimitGreaterThanMaxException : IllegalArgumentException("Param 'size' value is greater than maximum")

internal data class ValidationException(val violations: Set<ConstraintViolation<out Any>>) : RuntimeException()
