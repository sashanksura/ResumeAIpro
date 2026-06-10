package com.resumeai.pro.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Builder : Screen("builder")
    object Preview : Screen("preview/{resumeId}") {
        fun createRoute(resumeId: String) = "preview/$resumeId"
    }
    object Generation : Screen("generation/{resumeId}") {
        fun createRoute(resumeId: String) = "generation/$resumeId"
    }
    object Templates : Screen("templates")
    object MyResumes : Screen("my_resumes")
    object Settings : Screen("settings")
    object AILoader : Screen("ai_loader/{loaderType}") {
        fun createRoute(loaderType: String) = "ai_loader/$loaderType"
    }
}

