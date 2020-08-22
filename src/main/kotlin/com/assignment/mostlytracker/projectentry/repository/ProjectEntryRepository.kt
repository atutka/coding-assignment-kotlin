package com.assignment.mostlytracker.projectentry.repository

import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
internal interface ProjectEntryRepository : ReactiveCrudRepository<ProjectEntry, Long> {

    fun findByIdNotNullAndProjectId(projectId: Long, pageable: Pageable) : Flux<ProjectEntry>

    fun findByIdAndProjectId(id: Long, projectId: Long): Mono<ProjectEntry>
}