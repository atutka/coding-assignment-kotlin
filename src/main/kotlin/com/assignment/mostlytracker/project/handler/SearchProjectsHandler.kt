package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.MissingRequirementParameter
import com.assignment.mostlytracker.exception.SizeLimitGreaterThanMaxException
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.dto.ProjectDTO
import com.assignment.mostlytracker.project.request.ProjectSearchRequest
import com.assignment.mostlytracker.project.service.ProjectService
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

@Component
internal class SearchProjectsHandler(
        private val projectService: ProjectService
) : HandlerFunction<ServerResponse> {

    private val logger = KotlinLogging.logger {}

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val projects = projectService.search(ProjectSearchRequest(), serverRequest.toPageable())
                .asFlow()
                .map { it.toDTO() }
                .toList();
        ok().bodyValueAndAwait(projects)
    }

    private suspend fun ServerRequest.toPageable(): Pageable {
        val size = queryParam(SIZE_QUERY_PARAM)
                .map { it.toInt() }
                .orElseThrow {
                    logger.error { "Missing query parameter: $SIZE_QUERY_PARAM" }
                    MissingRequirementParameter(SIZE_QUERY_PARAM)
                }
        if(size > SIZE_MAX) {
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

    private suspend fun Project.toDTO() =
            ProjectDTO(
                    id = id!!,
                    name = name,
                    startDate = startDate,
                    endDate = endDate
            )

    private companion object {
        private const val PAGE_QUERY_PARAM = "page"
        private const val SIZE_QUERY_PARAM = "size"
        private const val SIZE_MAX = 200
    }
}