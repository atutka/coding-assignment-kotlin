package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.ValidationException
import com.assignment.mostlytracker.project.request.ProjectUpdateRequest
import com.assignment.mostlytracker.project.request.dto.ProjectUpdateRequestDTO
import com.assignment.mostlytracker.project.router.PROJECT_ID_PATH_VARIABLE
import com.assignment.mostlytracker.project.service.ProjectService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.buildAndAwait
import reactor.core.publisher.Mono
import javax.validation.Validator

@Component
internal class UpdateProjectHandler(
        private val projectService: ProjectService,
        private val validator: Validator
) : HandlerFunction<ServerResponse> {

    private val logger = KotlinLogging.logger {}

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val projectId = serverRequest.pathVariable(PROJECT_ID_PATH_VARIABLE).toLong()
        val requestDTO = serverRequest.awaitBody<ProjectUpdateRequestDTO>()
        validate(requestDTO)
        projectService.update(requestDTO.toProjectUpdateRequest(projectId)).awaitSingle()
        ok().buildAndAwait()
    }

    private suspend fun validate(requestDTO: ProjectUpdateRequestDTO) {
        val constraintViolations = validator.validate(requestDTO)
        if (constraintViolations.isNotEmpty()) {
            logger.error { "Validation errors when updating project: ${constraintViolations.map { it.message }}" }
            throw ValidationException(constraintViolations)
        }
    }

    private suspend fun ProjectUpdateRequestDTO.toProjectUpdateRequest(id: Long) =
            ProjectUpdateRequest(
                    id = id,
                    name = name,
                    startDate = startDate,
                    endDate = endDate
            )
}