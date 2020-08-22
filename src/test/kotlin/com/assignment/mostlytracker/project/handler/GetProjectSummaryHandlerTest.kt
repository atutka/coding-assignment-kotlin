package com.assignment.mostlytracker.project.handler

import com.assignment.mostlytracker.exception.handler.GlobalErrorWebExceptionHandler
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.dto.ProjectSummaryDTO
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.project.router.ProjectRouter
import com.assignment.mostlytracker.project.service.ProjectServiceImpl
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
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
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.TEN
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ProjectRouter::class, CreateProjectHandler::class,
    DeleteProjectHandler::class, GetProjectHandler::class, GetProjectSummaryHandler::class,
    SearchProjectsHandler::class, UpdateProjectHandler::class, ProjectServiceImpl::class,
    ProjectEntryServiceImpl::class, GlobalErrorWebExceptionHandler::class
])
@WebFluxTest
internal class GetProjectSummaryHandlerTest {

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
    fun `should get project summary`() {
        // given
        every { projectRepository.existsById(any<Long>()) } returns Mono.just(true)
        every { projectEntryRepository.findByIdNotNullAndProjectId(any(), any()) }returns Flux.fromIterable(listOf(projectEntry1, projectEntry2))

        // when
        webTestClient.get()
                .uri("/api/project/{id}/summary", projectId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(objectMapper.writeValueAsString(projectSummary))

        // then
        verify { projectRepository.existsById(eq(projectId)) }
        verify { projectEntryRepository.findByIdNotNullAndProjectId(eq(projectId), eq(Pageable.unpaged())) }
    }

    @Test
    fun `should not get project summary when project id not found`() {
        // given
        every { projectRepository.existsById(any<Long>()) } returns Mono.just(false)

        // when
        webTestClient.get()
                .uri("/api/project/{id}/summary", projectId)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project with given id was not found\"}")

        // then
        verify { projectRepository.existsById(eq(projectId)) }
    }

    private companion object {
        private const val projectId = -100L
        private val project = Project(
                id = projectId,
                name = "name",
                startDate = LocalDate.of(2020, 8, 19)
        )
        private val projectEntry1 = ProjectEntry(
                timeSpent = ONE,
                date = LocalDate.of(2020, 8, 19),
                projectId = projectId
        )
        private val projectEntry2 = projectEntry1.copy(timeSpent = TEN)
        private val projectSummary = ProjectSummaryDTO(
                totalTimeSpent = BigDecimal(11),
                totalDays = 1,
                averageTimeSpentPerDay = BigDecimal(11)
        )
    }
}