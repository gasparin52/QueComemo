package com.quecomemo.app.domain.repository

import com.quecomemo.app.domain.model.StartDestination

interface SessionRepository {
    fun getStartDestination(): StartDestination
}
