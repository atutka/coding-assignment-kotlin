package com.assignment.mostlytracker.projectentry.handler

import com.assignment.mostlytracker.exception.handler.GlobalErrorWebExceptionHandler
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.repository.ProjectEntryRepository
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
import java.math.BigDecimal.ONE
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ProjectEntryRouter::class, CreateProjectEntryHandler::class,
    DeleteProjectEntryHandler::class, SearchProjectEntriesHandler::class,
    ProjectEntryServiceImpl::class, GlobalErrorWebExceptionHandler::class
])
@WebFluxTest
internal class DeleteProjectEntryHandlerTest {

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
    fun `should delete project entry`() {
        // given
        every { projectEntryRepository.findByIdAndProjectId(any(), any()) } returns Mono.just(projectEntry)
        every { projectEntryRepository.delete(any()) } returns Mono.empty<Void>()

        // when
        webTestClient.delete()
                .uri("/api/project/{projectId}/entry/{id}", projectId, projectEntryId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk

        // then
        verify { projectEntryRepository.findByIdAndProjectId(eq(projectEntryId), eq(projectId)) }
        verify { projectEntryRepository.delete(eq(projectEntry)) }
    }

    @Test
    fun `should not delete project entry when not found`() {
        // given
        every { projectEntryRepository.findByIdAndProjectId(any(), any()) } returns Mono.empty()

        // when
        webTestClient.delete()
                .uri("/api/project/{projectId}/entry/{id}", projectId, projectEntryId)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Project entry was not found\"}")

        // then
        verify { projectEntryRepository.findByIdAndProjectId(eq(projectEntryId), eq(projectId)) }
    }

    private companion object {
        private const val projectId = -100L
        private const val projectEntryId = -200L
        private val projectEntry = ProjectEntry(
                id = projectEntryId,
                timeSpent = ONE,
                date = LocalDate.of(2020, 8, 19),
                projectId = projectId
        )
    }
}