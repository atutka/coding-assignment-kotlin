package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.dto.ProjectDTO
import com.assignment.mostlytracker.project.router.PROJECT_ID_PATH_VARIABLE
import com.assignment.mostlytracker.project.service.ProjectService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import reactor.core.publisher.Mono

@Component
internal class GetProjectHandler(
        private val projectService: ProjectService
) : HandlerFunction<ServerResponse> {

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val projectId = serverRequest.pathVariable(PROJECT_ID_PATH_VARIABLE).toLong()
        val project = projectService.get(projectId).awaitSingle()
        ok().bodyValueAndAwait(project.toDTO())
    }

    private suspend fun Project.toDTO() =
            ProjectDTO(
                    id = id!!,
                    name = name,
                    startDate = startDate,
                    endDate = endDate
            )
}