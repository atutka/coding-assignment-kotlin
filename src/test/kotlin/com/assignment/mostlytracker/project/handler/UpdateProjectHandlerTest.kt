package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.handler.GlobalErrorWebExceptionHandler
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.project.request.dto.ProjectUpdateRequestDTO
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
internal class UpdateProjectHandlerTest {

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
    fun `should update project`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)
        every { projectRepository.save(any<Project>()) } returns Mono.just(project)

        // when
        webTestClient.put()
                .uri("/api/project/{id}", projectId)
                .bodyValue(updateRequestDTO)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk

        // then
        verify { projectRepository.findById(eq(projectId)) }
        verify { projectRepository.save(eq(project)) }
    }

    @Test
    fun `should not update with project with blank name`() {
        // when
        webTestClient.put()
                .uri("/api/project/{id}", projectId)
                .bodyValue(updateRequestDTO.copy(name = ""))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Request validation failed\",\"errorDetails\":[{\"message\":\"Name cannot be blank or null\"}]}")
    }

    @Test
    fun `should not update with project end date before start date`() {
        // when
        webTestClient.put()
                .uri("/api/project/{id}", projectId)
                .bodyValue(updateRequestDTO.copy(startDate = LocalDate.of(2020, 9, 20)))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project cannot have end date before start date\"}")
    }

    @Test
    fun `should not update with project id not exist`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        webTestClient.put()
                .uri("/api/project/{id}", 200)
                .bodyValue(updateRequestDTO)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project with given id was not found\",\"errorDetails\":[]}")

        // then
        verify { projectRepository.findById(eq(200L)) }
    }

    private companion object {
        private const val projectId = -100L
        private const val name = "name"
        private val startDate = LocalDate.of(2020, 8, 19)
        private val endDate = LocalDate.of(2020, 9, 19)
        private val updateRequestDTO = ProjectUpdateRequestDTO(
                name = name,
                startDate = startDate,
                endDate = endDate
        )
        private val project = Project(
                id = projectId,
                name = name,
                startDate = startDate,
                endDate = endDate
        )
    }
}