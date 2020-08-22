package com.assignment.mostlytracker.project.service

import com.assignment.mostlytracker.project.domain.Project
import com.assignment.mostlytracker.project.domain.ProjectSummary
import com.assignment.mostlytracker.project.request.ProjectCreateRequest
import com.assignment.mostlytracker.project.request.ProjectSearchRequest
import com.assignment.mostlytracker.project.request.ProjectUpdateRequest
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal interface ProjectService {

    fun create(createRequest: ProjectCreateRequest): Mono<Project>

    fun get(id: Long): Mono<Project>

    fun update(updateRequest: ProjectUpdateRequest): Mono<Project>

    fun search(searchRequest: ProjectSearchRequest, pageable: Pageable): Flux<Project>

    fun delete(id: Long): Mono<Void>

    fun getSummary(id: Long): Mono<ProjectSummary>

}