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
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ProjectRouter::class, CreateProjectHandler::class,
    DeleteProjectHandler::class, GetProjectHandler::class, GetProjectSummaryHandler::class,
    SearchProjectsHandler::class, UpdateProjectHandler::class, ProjectServiceImpl::class,
    ProjectEntryServiceImpl::class, GlobalErrorWebExceptionHandler::class
])
@WebFluxTest
internal class SearchProjectsHandlerTest {

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
    fun `should search projects`() {
        // given
        every { projectRepository.findByIdNotNull(any()) } returns Flux.fromIterable(listOf(project))

        // when
        webTestClient.get()
                .uri {
                    it.path("/api/project")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build()
                }
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(objectMapper.writeValueAsString(listOf(projectDTO)))

        // then
        verify { projectRepository.findByIdNotNull(eq(PageRequest.of(page, size))) }
    }

    @Test
    fun `should return error when size query param is missing`() {

        // when
        webTestClient.get()
                .uri {
                    it.path("/api/project")
                            .queryParam("page", page)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Missing required parameter: size\"}")

    }

    @Test
    fun `should return error when page query param is missing`() {

        // when
        webTestClient.get()
                .uri {
                    it.path("/api/project")
                            .queryParam("size", size)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Missing required parameter: page\"}")

    }

    @Test
    fun `should return error when size query param is greater than max`() {

        // when
        webTestClient.get()
                .uri {
                    it.path("/api/project")
                            .queryParam("size", 201)
                            .queryParam("page", page)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .json("{\"message\":\"Param 'size' value is greater than maximum\",\"errorDetails\":[]}")

    }

    private companion object {
        private const val size = 10
        private const val page = 0
        private val project = Project(
                id = -100L,
                name = "name",
                startDate = LocalDate.of(2020, 8, 19)
        )
        private val projectDTO = ProjectDTO(
                id = project.id!!,
                name = project.name,
                startDate = project.startDate,
                endDate = null
        )
    }
}