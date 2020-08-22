package com.assignment.mostlytracker.projectentry.handler

import com.assignment.mostlytracker.exception.handler.GlobalErrorWebExceptionHandler
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.repository.ProjectEntryRepository
import com.assignment.mostlytracker.projectentry.request.dto.ProjectEntryCreateRequestDTO
import com.assignment.mostlytracker.projectentry.router.ProjectEntryRouter
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
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ProjectEntryRouter::class, CreateProjectEntryHandler::class,
    DeleteProjectEntryHandler::class, SearchProjectEntriesHandler::class,
    ProjectEntryServiceImpl::class, GlobalErrorWebExceptionHandler::class
])
@WebFluxTest
internal class CreateProjectEntryHandlerTest {

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
    fun `should create project entry`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)
        every { projectEntryRepository.save(any<ProjectEntry>()) } returns Mono.just(projectEntry)

        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectHeader()
                .valueEquals("location", projectEntryId.toString())

        // then
        verify { projectRepository.findById(eq(projectId)) }
        verify { projectEntryRepository.save(eq(projectEntry.copy(id = null))) }
    }

    @Test
    fun `should not create project when time spent equals zero`() {
        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest.copy(timeSpent = ZERO))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Request validation failed\",\"errorDetails\":[{\"message\":\"Value for timeSpent must be greater than 0\"}]}")
    }

    @Test
    fun `should not create project when time spent less than zero`() {
        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest.copy(timeSpent = BigDecimal(-1)))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Request validation failed\",\"errorDetails\":[{\"message\":\"Value for timeSpent must be greater than 0\"}]}")
    }

    @Test
    fun `should not create project when time spent greater than ten`() {
        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest.copy(timeSpent = BigDecimal("10.01")))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Request validation failed\",\"errorDetails\":[{\"message\":\"Maximum value for timeSpent is 10\"}]}")
    }

    @Test
    fun `should not create project when time spent has more than two fractions`() {
        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest.copy(timeSpent = BigDecimal("9.001")))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Request validation failed\",\"errorDetails\":[{\"message\":\"Value for timeSpent be must within 0-10 range with maximum of two fraction digits\"}]}")
    }

    @Test
    fun `should not create project when project not found`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project with given id was not found\"}")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should not create project when entry date before project start date`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)

        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest.copy(date = LocalDate.of(2020, 8, 9)))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project entry date need to be between project start and end dates\"}")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should not create project when entry date after project end date`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)

        // when
        webTestClient.post()
                .uri("/api/project/{projectId}/entry", projectId)
                .bodyValue(createProjectEntryRequest.copy(date = LocalDate.of(2020, 8, 23)))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project entry date need to be between project start and end dates\"}")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    private companion object {
        private const val projectId = -100L
        private const val projectEntryId = -200L
        private const val description = "description"
        private val date = LocalDate.of(2020, 8, 19)
        private val createProjectEntryRequest = ProjectEntryCreateRequestDTO(
                timeSpent = ONE,
                date = date,
                description = description
        )
        private val projectEntry = ProjectEntry(
                id = projectEntryId,
                timeSpent = ONE,
                date = date,
                description = description,
                projectId = projectId
        )
        private val project = Project(
                name = "name",
                startDate = LocalDate.of(2020, 8, 10),
                endDate = LocalDate.of(2020, 8, 22)
        )
    }
}