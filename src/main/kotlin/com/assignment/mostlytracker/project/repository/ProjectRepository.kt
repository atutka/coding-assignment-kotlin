package com.assignment.mostlytracker.project.repository

import com.assignment.mostlytracker.project.domain.Project
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
internal interface ProjectRepository : ReactiveCrudRepository<Project, Long> {

    fun findByIdNotNull(pageable: Pageable) : Flux<Project>
}