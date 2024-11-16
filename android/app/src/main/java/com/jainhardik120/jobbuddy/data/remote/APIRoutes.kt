package com.jainhardik120.jobbuddy.data.remote

import com.jainhardik120.jobbuddy.Constants

object APIRoutes {
    private const val HOST = Constants.HOST_NAME
    private const val API_BASE_URL = "https://$HOST/api"
    const val LOGIN_ROUTE = "$API_BASE_URL/login"
    const val RESUME_ROUTE = "$API_BASE_URL/resume"
    const val PROFILE_ROUTE = "$API_BASE_URL/profile"
    const val GENERATE_PROFILE_RESUME_ROUTE = "$API_BASE_URL/profile/generateFromResume"
    const val SIMPLIFY_JOB_ROUTE = "$API_BASE_URL/job/simplify"
    const val CHECK_PROFILE_SCORE_ROUTE = "$API_BASE_URL/job/checkProfileScore"
    const val GENERATE_TAILORED_RESUME_ROUTE = "$API_BASE_URL/job/generateTailoredResume"
    const val FLASH_CARDS_ROUTE = "$API_BASE_URL/job/flashCards"
    const val GENERATE_QUESTIONS_ROUTE = "$API_BASE_URL/interview/generateQuestions"
    const val INTERVIEW_EVALUATE_ROUTE = "$API_BASE_URL/interview/evaluateResponses"
}