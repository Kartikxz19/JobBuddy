package com.jainhardik120.jobbuddy.data.remote

import com.jainhardik120.jobbuddy.Constants


object APIRoutes {
    private const val HOST = Constants.HOST_NAME

    private const val AUTH_BASE_URL = "https://$HOST"
    const val LOGIN_ROUTE = "$AUTH_BASE_URL/login"
}