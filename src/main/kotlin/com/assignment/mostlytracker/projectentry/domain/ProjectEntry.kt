package com.assignment.mostlytracker.projectentry.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("tracker.project_entry")
internal data class ProjectEntry(
        @Id
        val id: Long? = null,
        @Column("date")
        val date: LocalDate,
        @Column("time_spent")
        val timeSpent: BigDecimal,
        @Column("description")
        val description: String? = null,
        @Column("project_id")
        val projectId: Long
)