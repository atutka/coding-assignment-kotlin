package com.assignment.mostlytracker.projectentry.service

import com.assignment.mostlytracker.exception.EntityNotFoundException
import com.assignment.mostlytracker.exception.ProjectEntryDateNotInProjectDateRangeException
import com.assignment.mostlytracker.project.repository.ProjectRepository
import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.repository.ProjectEntryRepository
import com.assignment.mostlytracker.projectentry.request.ProjectEntryCreateRequest
import com.assignment.mostlytracker.projectentry.request.ProjectEntrySearchRequest
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
@Transactional
internal class ProjectEntryServiceImpl(
        private val projectEntryRepository: ProjectEntryRepository,
        private val projectRepository: ProjectRepository
) : ProjectEntryService {

    private val logger = KotlinLogging.logger {}

    private val scheduler = Schedulers.newElastic("projectEntry")

    override fun create(createRequest: ProjectEntryCreateRequest): Mono<ProjectEntry> = mono {
        val project = projectRepository.findById(createRequest.projectId)
                .awaitFirstOrElse {
                    logger.error { "Project not found for id: ${createRequest.projectId}" }
                    throw EntityNotFoundException("Project with given id was not found")
                }
        if (project.startDate.isAfter(createRequest.date) || project.endDate?.isBefore(createRequest.date) == true) {
            logger.error { "Entry date ${createRequest.date} is outside project with id ${createRequest.projectId} range dates" }
            throw ProjectEntryDateNotInProjectDateRangeException()
        }
        projectEntryRepository.save(createRequest.toProjectEntry())
                .subscribeOn(scheduler).awaitSingle()
    }

    override fun search(searchRequest: ProjectEntrySearchRequest, pageable: Pageable): Flux<ProjectEntry> =
            projectEntryRepository.findByIdNotNullAndProjectId(
                    projectId = searchRequest.projectId,
                    pageable = pageable
            )

    override fun delete(id: Long, projectId: Long): Mono<Void> = mono {
        val projectEntry = projectEntryRepository.findByIdAndProjectId(id, projectId)
                .awaitFirstOrElse {
                    logger.error { "Project entry not found for id: $id and project id: $projectId" }
                    throw EntityNotFoundException("Project entry was not found")
                }
        projectEntryRepository.delete(projectEntry)
                .subscribeOn(scheduler)
                .awaitFirstOrNull()
    }

    private suspend fun ProjectEntryCreateRequest.toProjectEntry() =
            ProjectEntry(
                    date = date,
                    timeSpent = timeSpent,
                    description = description,
                    projectId = projectId
            )

}
