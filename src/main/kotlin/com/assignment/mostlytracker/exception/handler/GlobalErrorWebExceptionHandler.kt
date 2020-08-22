package com.assignment.mostlytracker.exception.handler

import com.assignment.mostlytracker.exception.EntityNotFoundException
import com.assignment.mostlytracker.exception.MissingRequirementParameter
import com.assignment.mostlytracker.exception.ProjectEntryDateNotInProjectDateRangeException
import com.assignment.mostlytracker.exception.ProjectInvalidDateRangeException
import com.assignment.mostlytracker.exception.SizeLimitGreaterThanMaxException
import com.assignment.mostlytracker.exception.ValidationException
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just

@Component
@Order(-2)
internal class GlobalErrorWebExceptionHandler(
        val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {

    private val logger = KotlinLogging.logger {}

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> = mono {
        logger.error(ex) { "Unexpected behaviour, please check:" }
        val response = when (ex) {
            is MissingRequirementParameter -> handle(ex)
            is EntityNotFoundException -> handle(ex)
            is ValidationException -> handle(ex)
            is ServerWebInputException -> handle(ex)
            is ProjectInvalidDateRangeException -> handle(ex)
            is SizeLimitGreaterThanMaxException -> handle(ex)
            is ProjectEntryDateNotInProjectDateRangeException -> handle(ex)
            else -> handle(ex)
        }

        exchange.response.headers.contentType = APPLICATION_JSON
        exchange.response.statusCode = response.statusCode
        val bytes = getBytes(response.body)
        val buffer = exchange.response.bufferFactory().wrap(bytes)
        exchange.response.writeWith(just(buffer)).awaitFirstOrNull()
    }

    private suspend fun handle(exception: MissingRequirementParameter): ResponseEntity<ServerError> =
            ResponseEntity(ServerError(message = exception.message), BAD_REQUEST)

    private suspend fun handle(exception: EntityNotFoundException): ResponseEntity<ServerError> =
            ResponseEntity(ServerError(message = exception.message), BAD_REQUEST)

    private suspend fun handle(exception: ProjectEntryDateNotInProjectDateRangeException): ResponseEntity<ServerError> =
            ResponseEntity(ServerError(message = exception.message), BAD_REQUEST)

    private suspend fun handle(exception: ProjectInvalidDateRangeException): ResponseEntity<ServerError> =
            ResponseEntity(ServerError(message = exception.message), BAD_REQUEST)

    private suspend fun handle(exception: SizeLimitGreaterThanMaxException): ResponseEntity<ServerError> =
            ResponseEntity(ServerError(message = exception.message), BAD_REQUEST)

    private suspend fun handle(exception: ValidationException): ResponseEntity<ServerError> {
        val errors = exception.violations.map { ErrorDetails(it.message) }.toList()
        return ResponseEntity(
                ServerError(message = "Request validation failed", errorDetails = errors),
                BAD_REQUEST)
    }

    private suspend fun handle(exception: ServerWebInputException): ResponseEntity<ServerError> = ResponseEntity(
            ServerError(message = "${exception.reason}. Please check if your request body is correct"),
            BAD_REQUEST)

    private suspend fun handle(exception: Throwable): ResponseEntity<ServerError> = ResponseEntity(
            ServerError(message = "There was some problem with application"),
            INTERNAL_SERVER_ERROR)

    private suspend fun getBytes(value: Any?): ByteArray =
            withContext(Dispatchers.IO) { objectMapper.writeValueAsBytes(value) }

}

internal data class ServerError(val message: String?, val errorDetails: List<ErrorDetails> = emptyList())
internal data class ErrorDetails(val message: String)