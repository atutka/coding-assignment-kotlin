package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.handler.GlobalErrorWebExceptionHandler
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.project.request.dto.ProjectCreateRequestDTO
import com.assignment.mostlytracker.project.router.ProjectRouter
import com.assignment.mostlytracker.project.service.ProjectServiceImpl
import com.assignment.mostlytracker.projectentry.repository.ProjectEntryRepository
import com.assignment.mostlytracker.projectentry.service.ProjectEntryServiceImpl
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
internal class CreateProjectHandlerTest {

    @MockkBean
    private lateinit var projectRepository: ProjectRepository

    @MockkBean
    private lateinit var projectEntryRepository: ProjectEntryRepository

    @field:Autowired
    private lateinit var applicationContext: ApplicationContext

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun beforeEach() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build()
    }

    @Test
    fun `should create project`() {
        // given
        every { projectRepository.save(any<Project>()) } returns Mono.just(projectWithId)

        // when
        webTestClient.post()
                .uri("/api/project")
                .bodyValue(createRequestDTO)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectHeader()
                .valueEquals("location", projectId.toString())

        // then
        verify { projectRepository.save(eq(project)) }
    }

    @Test
    fun `should not create with project with blank name`() {
        // when
        webTestClient.post()
                .uri("/api/project")
                .bodyValue(createRequestDTO.copy(name = ""))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Request validation failed\",\"errorDetails\":[{\"message\":\"Name cannot be blank or null\"}]}")
    }

    @Test
    fun `should not create with project end date before start date`() {
        // when
        webTestClient.post()
                .uri("/api/project")
                .bodyValue(createRequestDTO.copy(startDate = LocalDate.of(2020, 9, 20)))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project cannot have end date before start date\"}")
    }

    private companion object {
        private const val projectId = -100L
        private const val name = "name"
        private val startDate = LocalDate.of(2020, 8, 19)
        private val endDate = LocalDate.of(2020, 9, 19)
        private val createRequestDTO = ProjectCreateRequestDTO(
                name = name,
                startDate = startDate,
                endDate = endDate
        )
        private val project = Project(
                name = name,
                startDate = startDate,
                endDate = endDate
        )
        private val projectWithId = Project(
                id = projectId,
                name = name,
                startDate = startDate,
                endDate = endDate
        )
    }
}