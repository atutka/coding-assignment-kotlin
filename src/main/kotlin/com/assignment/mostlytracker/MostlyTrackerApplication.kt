package com.assignment.mostlytracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableR2dbcRepositories
@EnableTransactionManagement
class MostlyTrackerApplication

fun main(args: Array<String>) {
	runApplication<MostlyTrackerApplication>(*args)
}
