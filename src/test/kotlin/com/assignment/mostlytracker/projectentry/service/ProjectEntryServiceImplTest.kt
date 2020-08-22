package com.assignment.mostlytracker.projectentry.service

import com.assignment.mostlytracker.exception.EntityNotFoundException
import com.assignment.mostlytracker.exception.ProjectEntryDateNotInProjectDateRangeException
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.repository.ProjectEntryRepository
import com.assignment.mostlytracker.projectentry.request.ProjectEntryCreateRequest
import com.assignment.mostlytracker.projectentry.request.ProjectEntrySearchRequest
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal.ONE
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
internal class ProjectEntryServiceImplTest {

    @MockK
    private lateinit var projectEntryRepository: ProjectEntryRepository

    @MockK
    private lateinit var projectRepository: ProjectRepository

    @InjectMockKs
    private lateinit var projectEntryService: ProjectEntryServiceImpl

    @Test
    fun `should delete project entry`() {
        // given
        every { projectEntryRepository.findByIdAndProjectId(any(), any()) } returns Mono.just(projectEntry)
        every { projectEntryRepository.delete(any()) } returns Mono.empty()

        // when
        projectEntryService.delete(projectEntryId, projectId).block()

        // then
        verify { projectEntryRepository.findByIdAndProjectId(eq(projectEntryId), eq(projectId)) }
        verify { projectEntryRepository.delete(eq(projectEntry)) }
    }

    @Test
    fun `should throw exception on deleting when project entry not found`() {
        // given
        every { projectEntryRepository.findByIdAndProjectId(any(), any()) } returns Mono.empty()

        // when
        assertThatThrownBy { projectEntryService.delete(projectEntryId, projectId).block() }
                .isExactlyInstanceOf(EntityNotFoundException::class.java)
                .hasMessage("Project entry was not found")

        // then
        verify { projectEntryRepository.findByIdAndProjectId(eq(projectEntryId), eq(projectId)) }
    }

    @Test
    fun `should search project entries`() {
        // given
        every { projectEntryRepository.findByIdNotNullAndProjectId(any(), any()) } returns Flux.fromIterable(listOf(projectEntry))

        // when
        val result = projectEntryService.search(ProjectEntrySearchRequest(projectId), PageRequest.of(0, 10)).collectList().block()

        // then
        verify { projectEntryRepository.findByIdNotNullAndProjectId(eq(projectId), eq(PageRequest.of(0, 10))) }

        assertThat(result).containsExactly(projectEntry)
    }

    @Test
    fun `should create project`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)
        every { projectEntryRepository.save(any<ProjectEntry>()) } returns Mono.just(projectEntry)

        // when
        val result = projectEntryService.create(createProjectEntryRequest).block()

        // then
        verify { projectRepository.findById(eq(projectId)) }
        verify { projectEntryRepository.save(eq(projectEntry)) }

        assertThat(result).isEqualTo(projectEntry)
    }

    @Test
    fun `should throw exception on create project entry when project not exists`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        assertThatThrownBy {
            projectEntryService.create(createProjectEntryRequest)
                    .block()
        }
                .isExactlyInstanceOf(EntityNotFoundException::class.java)
                .hasMessage("Project with given id was not found")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should throw exception on create project entry when date after end date`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)

        // when
        assertThatThrownBy {
            projectEntryService.create(createProjectEntryRequest.copy(date = LocalDate.of(2020, 10, 23)))
                    .block()
        }
                .isExactlyInstanceOf(ProjectEntryDateNotInProjectDateRangeException::class.java)
                .hasMessage("Project entry date need to be between project start and end dates")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should throw exception on create project when end date before start date`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)

        // when
        assertThatThrownBy {
            projectEntryService.create(createProjectEntryRequest.copy(date = LocalDate.of(2020, 8, 17)))
                    .block()
        }
                .isExactlyInstanceOf(ProjectEntryDateNotInProjectDateRangeException::class.java)
                .hasMessage("Project entry date need to be between project start and end dates")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    private companion object {
        private const val projectId = -100L
        private const val projectEntryId = -200L
        private const val description = "description"
        private val date = LocalDate.of(2020, 8, 20)
        private val projectEntry = ProjectEntry(
                timeSpent = ONE,
                date = date,
                description = description,
                projectId = projectId
        )
        private val project = Project(
                name = "name",
                startDate = LocalDate.of(2020, 8, 18),
                endDate = LocalDate.of(2020, 8, 22)
        )
        private val createProjectEntryRequest = ProjectEntryCreateRequest(
                timeSpent = ONE,
                date = date,
                description = description,
                projectId = projectId
        )
    }
}