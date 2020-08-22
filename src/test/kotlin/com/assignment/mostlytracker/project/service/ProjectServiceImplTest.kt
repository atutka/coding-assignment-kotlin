package com.assignment.mostlytracker.project.service

import com.assignment.mostlytracker.exception.EntityNotFoundException
import com.assignment.mostlytracker.exception.ProjectInvalidDateRangeException
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.project.request.ProjectCreateRequest
import com.assignment.mostlytracker.project.request.ProjectSearchRequest
import com.assignment.mostlytracker.project.request.ProjectUpdateRequest
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.request.ProjectEntrySearchRequest
import com.assignment.mostlytracker.projectentry.service.ProjectEntryService
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
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
internal class ProjectServiceImplTest {

    @MockK
    private lateinit var projectRepository: ProjectRepository

    @MockK
    private lateinit var projectEntryService: ProjectEntryService

    @InjectMockKs
    private lateinit var projectService: ProjectServiceImpl

    @Test
    fun `should delete project`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)
        every { projectRepository.delete(any()) } returns Mono.empty()

        // when
        projectService.delete(projectId).block()

        // then
        verify { projectRepository.findById(eq(projectId)) }
        verify { projectRepository.delete(eq(project)) }
    }

    @Test
    fun `should throw exception on deleting when project not found`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        assertThatThrownBy { projectService.delete(projectId).block() }
                .isExactlyInstanceOf(EntityNotFoundException::class.java)
                .hasMessage("Project with given id was not found")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should get project`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(project)

        // when
        val result = projectService.get(projectId).block()

        // then
        verify { projectRepository.findById(eq(projectId)) }

        assertThat(result).isEqualTo(project)
    }

    @Test
    fun `should throw exception on getting project when not found`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        assertThatThrownBy { projectService.delete(projectId).block() }
                .isExactlyInstanceOf(EntityNotFoundException::class.java)
                .hasMessage("Project with given id was not found")

        // then
        verify { projectRepository.findById(eq(projectId)) }
    }

    @Test
    fun `should search projects`() {
        // given
        every { projectRepository.findByIdNotNull(any()) } returns Flux.fromIterable(listOf(project))

        // when
        val result = projectService.search(ProjectSearchRequest(), PageRequest.of(0, 10)).collectList().block()

        // then
        verify { projectRepository.findByIdNotNull(eq(PageRequest.of(0, 10))) }

        assertThat(result).containsExactly(project)
    }

    @Test
    fun `should create project`() {
        // given
        every { projectRepository.save(any<Project>()) } returns Mono.just(project)

        // when
        val result = projectService.create(createRequest).block()

        // then
        verify { projectRepository.save(eq(project)) }

        assertThat(result).isEqualTo(project)
    }

    @Test
    fun `should throw exception on create project when start date after end date`() {
        // when
        assertThatThrownBy {
            projectService.create(createRequest.copy(startDate = LocalDate.of(2020, 10, 19)))
                    .block()
        }
                .isExactlyInstanceOf(ProjectInvalidDateRangeException::class.java)
                .hasMessage("Project cannot have end date before start date")
    }

    @Test
    fun `should throw exception on create project when end date before start date`() {
        // when
        assertThatThrownBy {
            projectService.create(createRequest.copy(endDate = LocalDate.of(2020, 8, 18)))
                    .block()
        }
                .isExactlyInstanceOf(ProjectInvalidDateRangeException::class.java)
                .hasMessage("Project cannot have end date before start date")
    }

    @Test
    fun `should update project`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.just(projectWithId)
        every { projectRepository.save(any<Project>()) } returns Mono.just(projectWithId)

        // when
        val result = projectService.update(updateRequest).block()

        // then
        verify { projectRepository.findById(eq(updateRequest.id)) }
        verify { projectRepository.save(eq(projectWithId)) }

        assertThat(result).isEqualTo(projectWithId)
    }

    @Test
    fun `should throw exception on update project when project not found`() {
        // given
        every { projectRepository.findById(any<Long>()) } returns Mono.empty()

        // when
        assertThatThrownBy {
            projectService.update(updateRequest).block()
        }
                .isExactlyInstanceOf(EntityNotFoundException::class.java)
                .hasMessage("Project with given id was not found")

        // then
        verify { projectRepository.findById(eq(updateRequest.id)) }

    }

    @Test
    fun `should throw exception on update project when start date after end date`() {
        // when
        assertThatThrownBy {
            projectService.update(updateRequest.copy(startDate = LocalDate.of(2020, 10, 19)))
                    .block()
        }
                .isExactlyInstanceOf(ProjectInvalidDateRangeException::class.java)
                .hasMessage("Project cannot have end date before start date")
    }

    @Test
    fun `should throw exception on update project when end date before start date`() {
        // when
        assertThatThrownBy {
            projectService.update(updateRequest.copy(endDate = LocalDate.of(2020, 8, 18)))
                    .block()
        }
                .isExactlyInstanceOf(ProjectInvalidDateRangeException::class.java)
                .hasMessage("Project cannot have end date before start date")
    }

    @Test
    fun `should get project summary`() {
        // given
        every { projectRepository.existsById(any<Long>()) } returns Mono.just(true)
        every { projectEntryService.search(any(), any()) } returns Flux.fromIterable(projectEntries)

        // when
        val result = projectService.getSummary(projectId).block()!!

        // then
        verify { projectRepository.existsById(eq(projectId)) }
        verify { projectEntryService.search(eq(ProjectEntrySearchRequest(projectId)), eq(Pageable.unpaged())) }

        assertThat(result.totalTimeSpent).isEqualTo(BigDecimal("6.98"))
        assertThat(result.totalDays).isEqualTo(2)
        assertThat(result.averageTimeSpentPerDay).isEqualTo(BigDecimal("3.49"))
    }

    @Test
    fun `should throw exception on getting project summary when project not exists`() {
        // given
        every { projectRepository.existsById(any<Long>()) } returns Mono.just(false)

        // when
        assertThatThrownBy { projectService.getSummary(projectId).block()!! }
                .isExactlyInstanceOf(EntityNotFoundException::class.java)
                .hasMessage("Project with given id was not found")

        // then
        verify { projectRepository.existsById(eq(projectId)) }
    }

    companion object {
        private const val projectId = -100L
        private const val name = "name"
        private val startDate = LocalDate.of(2020, 8, 19)
        private val endDate = LocalDate.of(2020, 9, 19)
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
        private val createRequest = ProjectCreateRequest(
                name = name,
                startDate = startDate,
                endDate = endDate
        )
        private val updateRequest = ProjectUpdateRequest(
                id = projectId,
                name = name,
                startDate = startDate,
                endDate = endDate
        )
        private val projectEntry1 = ProjectEntry(
                id = -200,
                timeSpent = ONE,
                date = LocalDate.of(2020, 8, 20),
                description = "description",
                projectId = projectId
        )
        private val projectEntry2 = projectEntry1.copy(
                timeSpent = ONE,
                date = LocalDate.of(2020, 8, 20)
        )
        private val projectEntry3 = projectEntry1.copy(
                timeSpent = BigDecimal("4.98"),
                date = LocalDate.of(2020, 8, 21)
        )
        private val projectEntries = listOf(projectEntry1, projectEntry2, projectEntry3)
    }

}