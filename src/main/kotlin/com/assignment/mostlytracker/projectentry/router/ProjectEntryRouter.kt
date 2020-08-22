package com.assignment.mostlytracker.projectentry.router

import com.assignment.mostlytracker.projectentry.handler.CreateProjectEntryHandler
import com.assignment.mostlytracker.projectentry.handler.DeleteProjectEntryHandler
import com.assignment.mostlytracker.projectentry.handler.SearchProjectEntriesHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.DELETE
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

const val PROJECT_ENTRY_ID_PATH_VARIABLE = "id"
const val PROJECT_ID_PATH_VARIABLE = "projectId"

@Component
internal class ProjectEntryRouter(
        private val createProjectEntryHandler: CreateProjectEntryHandler,
        private val searchProjectEntriesHandler: SearchProjectEntriesHandler,
        private val deleteProjectEntryHandler: DeleteProjectEntryHandler
) {

    @Bean
    fun projectEntryRoute(): RouterFunction<ServerResponse> = route(
            POST("/api/project/{$PROJECT_ID_PATH_VARIABLE}/entry"), HandlerFunction<ServerResponse>(createProjectEntryHandler::handle))
            .andRoute(GET("/api/project/{$PROJECT_ID_PATH_VARIABLE}/entry"), HandlerFunction<ServerResponse>(searchProjectEntriesHandler::handle))
            .andRoute(DELETE("/api/project/{$PROJECT_ID_PATH_VARIABLE}/entry/{$PROJECT_ENTRY_ID_PATH_VARIABLE}"), HandlerFunction<ServerResponse>(deleteProjectEntryHandler::handle))

}