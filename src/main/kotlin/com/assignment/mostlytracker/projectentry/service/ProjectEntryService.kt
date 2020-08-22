package com.assignment.mostlytracker.projectentry.service

import com.assignment.mostlytracker.projectentry.domain.ProjectEntry
import com.assignment.mostlytracker.projectentry.request.ProjectEntryCreateRequest
import com.assignment.mostlytracker.projectentry.request.ProjectEntrySearchRequest
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal interface ProjectEntryService {

    fun create(createRequest: ProjectEntryCreateRequest): Mono<ProjectEntry>

    fun search(searchRequest: ProjectEntrySearchRequest, pageable: Pageable): Flux<ProjectEntry>

    fun delete(id: Long, projectId: Long): Mono<Void>

}