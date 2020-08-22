package com.assignment.mostlytracker.project.service

import com.assignment.mostlytracker.exception.EntityNotFoundException
import com.assignment.mostlytracker.exception.ProjectInvalidDateRangeException
import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.domain.ProjectSummary
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.project.request.ProjectCreateRequest
import com.assignment.mostlytracker.project.request.ProjectSearchRequest
import com.assignment.mostlytracker.project.request.ProjectUpdateRequest
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.request.ProjectEntrySearchRequest
import com.assignment.mostlytracker.projectentry.service.ProjectEntryService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Pageable.unpaged
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_EVEN

@Service
@Transactional
internal class ProjectServiceImpl(
        private val projectRepository: ProjectRepository,
        private val projectEntryService: ProjectEntryService
) : ProjectService {

    private val logger = KotlinLogging.logger {}

    private val scheduler = Schedulers.newElastic("project")

    override fun create(createRequest: ProjectCreateRequest): Mono<Project> = mono {
        if (createRequest.endDate?.isBefore(createRequest.startDate) == true) {
            logger.error { "End date ${createRequest.endDate} was before start date ${createRequest.startDate}" }
            throw ProjectInvalidDateRangeException()
        }
        projectRepository.save(createRequest.toProject())
                .subscribeOn(scheduler)
                .awaitSingle();
    }

    override fun get(id: Long): Mono<Project> = mono {
        projectRepository.findById(id)
                .subscribeOn(scheduler)
                .awaitFirstOrElse {
                    logger.error { "Project not found for id: $id" }
                    throw EntityNotFoundException("Project with given id was not found")
                }
    }

    override fun update(updateRequest: ProjectUpdateRequest): Mono<Project> = mono {
        if (updateRequest.endDate?.isBefore(updateRequest.startDate) == true) {
            logger.error { "End date ${updateRequest.endDate} was before start date ${updateRequest.startDate}" }
            throw ProjectInvalidDateRangeException()
        }
        val project = get(updateRequest.id).awaitSingle()
        projectRepository.save(project.copy(
                name = updateRequest.name,
                startDate = updateRequest.startDate,
                endDate = updateRequest.endDate))
                .subscribeOn(scheduler)
                .awaitSingle()
    }

    override fun search(searchRequest: ProjectSearchRequest, pageable: Pageable): Flux<Project> =
            projectRepository.findByIdNotNull(pageable)

    override fun delete(id: Long): Mono<Void> = mono {
        val project = get(id).awaitSingle()
        projectRepository.delete(project).subscribeOn(scheduler).awaitFirstOrNull()
    }

    override fun getSummary(id: Long): Mono<ProjectSummary> = mono {
        val projectExists = projectRepository.existsById(id)
                .subscribeOn(scheduler)
                .awaitSingle()
        if (!projectExists) {
            logger.error { "Project not found for id: $id" }
            throw EntityNotFoundException("Project with given id was not found");
        }
        val projectEntries = projectEntryService.search(ProjectEntrySearchRequest(projectId = id), unpaged())
                .asFlow()
                .toList()
        val totalTimeSpent = calculateTotalTimeSpent(projectEntries)
        val totalDays = calculateTotalDays(projectEntries)
        ProjectSummary(
                totalTimeSpent = totalTimeSpent,
                totalDays = totalDays,
                averageTimeSpentPerDay = calculateAverageTimeSpentPerDay(totalTimeSpent, totalDays))
    }

    private suspend fun calculateTotalTimeSpent(projectEntries: List<ProjectEntry>): BigDecimal =
            projectEntries
                    .map { it.timeSpent }
                    .fold(ZERO, BigDecimal::add)

    private suspend fun calculateTotalDays(projectEntries: List<ProjectEntry>): Int =
            projectEntries
                    .groupBy { it.date }
                    .size

    private suspend fun calculateAverageTimeSpentPerDay(totalTimeSpend: BigDecimal, totalDays: Int): BigDecimal {
        if (totalTimeSpend == ZERO || totalDays == 0) {
            return ZERO
        }
        return totalTimeSpend.divide(BigDecimal(totalDays))
                .setScale(2, HALF_EVEN)
    }

    private suspend fun ProjectCreateRequest.toProject() =
            Project(
                    name = name,
                    startDate = startDate,
                    endDate = endDate
            )

    private suspend fun ProjectUpdateRequest.toProject() =
            Project(
                    id = id,
                    name = name,
                    startDate = startDate,
                    endDate = endDate
            )
}