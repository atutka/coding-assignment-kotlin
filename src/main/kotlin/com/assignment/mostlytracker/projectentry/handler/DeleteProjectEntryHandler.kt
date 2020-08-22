package com.assignment.mostlytracker.projectentry.handler

import com.assignment.mostlytracker.projectentry.router.PROJECT_ENTRY_ID_PATH_VARIABLE
import com.assignment.mostlytracker.projectentry.router.PROJECT_ID_PATH_VARIABLE
import com.assignment.mostlytracker.projectentry.service.ProjectEntryService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.buildAndAwait
import reactor.core.publisher.Mono

@Component
internal class DeleteProjectEntryHandler(
        private val projectEntryService: ProjectEntryService
) : HandlerFunction<ServerResponse> {

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> = mono {
        val projectId = serverRequest.pathVariable(PROJECT_ID_PATH_VARIABLE).toLong()
        val projectEntryId = serverRequest.pathVariable(PROJECT_ENTRY_ID_PATH_VARIABLE).toLong()
        projectEntryService.delete(projectEntryId, projectId).awaitFirstOrNull()
        ok().buildAndAwait()
    }
}