package com.assignment.mostlytracker.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("tracker.project")
internal data class Project(
        @Id
        val id: Long? = null,
        @Column("name")
        val name: String,
        @Column("start_date")
        val startDate: LocalDate,
        @Column("end_date")
        val endDate: LocalDate? = null
)