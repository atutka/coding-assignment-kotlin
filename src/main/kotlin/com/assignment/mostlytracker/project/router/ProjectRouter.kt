package com.assignment.mostlytracker.project.router

import com.assignment.mostlytracker.project.handler.CreateProjectHandler
import com.assignment.mostlytracker.project.handler.DeleteProjectHandler
import com.assignment.mostlytracker.project.handler.GetProjectHandler
import com.assignment.mostlytracker.project.handler.GetProjectSummaryHandler
import com.assignment.mostlytracker.project.handler.SearchProjectsHandler
import com.assignment.mostlytracker.project.handler.UpdateProjectHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.DELETE
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.RequestPredicates.PUT
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

const val PROJECT_ID_PATH_VARIABLE = "id"

@Component
internal class ProjectRouter(
        private val createProjectHandler: CreateProjectHandler,
        private val getProjectHandler: GetProjectHandler,
        private val searchProjectsHandler: SearchProjectsHandler,
        private val updateProjectHandler: UpdateProjectHandler,
        private val deleteProjectHandler: DeleteProjectHandler,
        private val getProjectSummaryHandler: GetProjectSummaryHandler
) {

    @Bean
    fun projectRoute(): RouterFunction<ServerResponse> = route(
            POST("/api/project"), HandlerFunction<ServerResponse>(createProjectHandler::handle))
            .andRoute(GET("/api/project/{$PROJECT_ID_PATH_VARIABLE}"), HandlerFunction<ServerResponse>(getProjectHandler::handle))
            .andRoute(GET("/api/project"), HandlerFunction<ServerResponse>(searchProjectsHandler::handle))
            .andRoute(PUT("/api/project/{$PROJECT_ID_PATH_VARIABLE}"), HandlerFunction<ServerResponse>(updateProjectHandler::handle))
            .andRoute(DELETE("/api/project/{$PROJECT_ID_PATH_VARIABLE}"), HandlerFunction<ServerResponse>(deleteProjectHandler::handle))
            .andRoute(GET("/api/project/{$PROJECT_ID_PATH_VARIABLE}/summary"), HandlerFunction<ServerResponse>(getProjectSummaryHandler::handle))

}