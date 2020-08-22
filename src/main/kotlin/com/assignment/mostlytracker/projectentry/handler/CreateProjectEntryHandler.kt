package com.assignment.mostlytracker.projectentry.handler

import com.assignment.mostlytracker.exception.ValidationException
import com.assignment.mostlytracker.projectentry.request.ProjectEntryCreateRequest
import com.assignment.mostlytracker.projectentry.request.dto.ProjectEntryCreateRequestDTO
import com.assignment.mostlytracker.projectentry.router.PROJECT_ID_PATH_VARIABLE
import com.assignment.mostlytracker.projectentry.service.ProjectEntryService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.buildAndAwait
import reactor.core.publisher.Mono
import java.net.URI
import javax.validation.Validator

@Component
internal class CreateProjectEntryHandler(
        private val projectEntryService: ProjectEntryService,
        private val validator: Validator
) : HandlerFunction<ServerResponse> {

    private val logger = KotlinLogging.logger {}

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val requestDTO = serverRequest.awaitBody<ProjectEntryCreateRequestDTO>()
        val projectId = serverRequest.pathVariable(PROJECT_ID_PATH_VARIABLE).toLong()
        validate(requestDTO)
        val projectEntry = projectEntryService.create(requestDTO.toProjectEntryCreateRequest(projectId)).awaitSingle()
        created(URI.create(projectEntry.id!!.toString())).buildAndAwait()
    }

    private suspend fun validate(requestDTO: ProjectEntryCreateRequestDTO) {
        val constraintViolations = validator.validate(requestDTO)
        if (constraintViolations.isNotEmpty()) {
            logger.error { "Validation errors when creating project entry: ${constraintViolations.map { it.message }}" }
            throw ValidationException(constraintViolations)
        }
    }

    private suspend fun ProjectEntryCreateRequestDTO.toProjectEntryCreateRequest(projectId: Long) =
            ProjectEntryCreateRequest(
                    date = date,
                    timeSpent = timeSpent,
                    description = description,
                    projectId = projectId
            )
}