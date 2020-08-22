package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.handler.GlobalErrorWebExceptionHandler
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.dto.ProjectDTO
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.project.router.ProjectRouter
import com.assignment.mostlytracker.project.service.ProjectServiceImpl
import com.assignment.mostlytracker.projectentry.repository.ProjectEntryRepository
import com.assignment.mostlytracker.projectentry.service.ProjectEntryServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ProjectRouter::class, CreateProjectHandler::class,
    DeleteProjectHandler::class, GetProjectHandler::class, GetProjectSummaryHandler::class,
    SearchProjectsHandler::class, UpdateProjectHandler::class, ProjectServiceImpl::class,
    ProjectEntryServiceImpl::class, GlobalErrorWebExceptionHandler::class
])
@WebFluxTest
internal class GetProjectHandlerTest {

    @MockkBean
    private lateinit var projectRepository: ProjectRepository

    @MockkBean
    private lateinit var projectEntryRepository: ProjectEntryRepository

    @field:Autowired
    private lateinit var applicationContext: ApplicationContext

    @field:Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun beforeEach() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build()
    }

    @Test
    fun `should get project`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)

        // when
        webTestClient.get()
                .uri("/api/project/{id}", projectId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(objectMapper.writeValueAsString(projectDTO))

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should not get project when project not found`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        webTestClient.get()
                .uri("/api/project/{id}", projectId)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project with given id was not found\"}")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    private companion object {
        private const val projectId = -100L
        private val project = Project(
                id = projectId,
                name = "name",
                startDate = LocalDate.of(2020, 8, 19)
        )
        private val projectDTO = ProjectDTO(
                id = projectId,
                name = project.name,
                startDate = project.startDate,
                endDate = null
        )
    }
}