package com.assignment.mostlytracker.projectentry.handler

import com.assignment.mostlytracker.exception.MissingRequirementParameter
import com.assignment.mostlytracker.exception.SizeLimitGreaterThanMaxException
import com.assignment.mostlytracker.exception.ValidationException
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.request.ProjectEntrySearchRequest
import com.assignment.mostlytracker.projectentry.request.dto.ProjectEntryDTO
import com.assignment.mostlytracker.projectentry.request.dto.ProjectEntrySearchRequestDTO
import com.assignment.mostlytracker.projectentry.router.PROJECT_ID_PATH_VARIABLE
import com.assignment.mostlytracker.projectentry.service.ProjectEntryService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import reactor.core.publisher.Mono
import javax.validation.Validator

@Component
internal class SearchProjectEntriesHandler(
        private val projectEntryService: ProjectEntryService,
        private val validator: Validator
) : HandlerFunction<ServerResponse> {

    private val logger = KotlinLogging.logger {}

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val projectId = serverRequest.pathVariable(PROJECT_ID_PATH_VARIABLE).toLong()
        val requestDTO = createProjectEntrySearchRequestDTO(projectId)
        validate(requestDTO)
        val projectEntries = projectEntryService.search(requestDTO.toProjectEntrySearchRequest(), serverRequest.toPageable())
                .asFlow()
                .map { it.toDTO() }
                .toList()
        ok().bodyValueAndAwait(projectEntries)
    }

    private suspend fun validate(requestDTO: ProjectEntrySearchRequestDTO) {
        val constraintViolations = validator.validate(requestDTO)
        if (constraintViolations.isNotEmpty()) {
            logger.error { "Validation errors when searching projects: ${constraintViolations.map { it.message }}" }
            throw ValidationException(constraintViolations)
        }
    }

    private suspend fun createProjectEntrySearchRequestDTO(projectId: Long): ProjectEntrySearchRequestDTO =
            ProjectEntrySearchRequestDTO(
                    projectId = projectId
            )

    private suspend fun ServerRequest.toPageable(): Pageable {
        val size = queryParam(SIZE_QUERY_PARAM)
                .map { it.toInt() }
                .orElseThrow {
                    logger.error { "Missing query parameter: $SIZE_QUERY_PARAM" }
                    MissingRequirementParameter(SIZE_QUERY_PARAM)
                }
        if (size > SIZE_MAX) {
            throw SizeLimitGreaterThanMaxException()
        }
        return PageRequest.of(
                queryParam(PAGE_QUERY_PARAM)
                        .map { it.toInt() }
                        .orElseThrow {
                            logger.error { "Missing query parameter: $PAGE_QUERY_PARAM" }
                            MissingRequirementParameter(PAGE_QUERY_PARAM)
                        },
                size
        )
    }

    private suspend fun ProjectEntrySearchRequestDTO.toProjectEntrySearchRequest() =
            ProjectEntrySearchRequest(
                    projectId = projectId
            )

    private suspend fun ProjectEntry.toDTO() =
            ProjectEntryDTO(
                    id = id!!,
                    date = date,
                    timeSpent = timeSpent,
                    description = description
            )

    companion object {
        private const val PAGE_QUERY_PARAM = "page"
        private const val SIZE_QUERY_PARAM = "size"
        private const val SIZE_MAX = 200
    }
}