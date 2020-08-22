package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.ValidationException
import com.assignment.mostlytracker.project.request.ProjectCreateRequest
import com.assignment.mostlytracker.project.request.dto.ProjectCreateRequestDTO
import com.assignment.mostlytracker.project.service.ProjectService
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
internal class CreateProjectHandler(
        private val projectService: ProjectService,
        private val validator: Validator
) : HandlerFunction<ServerResponse> {

    private val logger = KotlinLogging.logger {}

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val requestDTO = serverRequest.awaitBody<ProjectCreateRequestDTO>()
        validate(requestDTO)
        val project = projectService.create(requestDTO.toProjectCreateRequest()).awaitSingle()
        created(URI.create(project.id!!.toString())).buildAndAwait()
    }

    private suspend fun validate(requestDTO: ProjectCreateRequestDTO) {
        val constraintViolations = validator.validate(requestDTO)
        if (constraintViolations.isNotEmpty()) {
            logger.error { "Validation errors when creating project: ${constraintViolations.map { it.message }}" }
            throw ValidationException(constraintViolations)
        }
    }

    private suspend fun ProjectCreateRequestDTO.toProjectCreateRequest() =
            ProjectCreateRequest(
                    name = name,
                    startDate = startDate,
                    endDate = endDate
            )
}