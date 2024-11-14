package com.jainhardik120.jobbuddy.data.remote

import com.jainhardik120.jobbuddy.Constants

object APIRoutes {
    private const val HOST = Constants.HOST_NAME
    private const val AUTH_BASE_URL = "https://$HOST"
    const val LOGIN_ROUTE = "$AUTH_BASE_URL/login"
    const val RESUME_UPLOAD_ROUTE = "$AUTH_BASE_URL/uploadResume"
    const val RESUME_SCORE_ROUTE = "$AUTH_BASE_URL/checkResumeScore"
    const val RESUME_STATUS_ROUTE = "$AUTH_BASE_URL/getResumeStatus"
}