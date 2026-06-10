package com.resumeai.pro.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.resumeai.pro.presentation.splash.SplashScreen
import com.resumeai.pro.presentation.home.HomeScreen
import com.resumeai.pro.presentation.builder.BuilderScreen
import com.resumeai.pro.presentation.generation.GenerationScreen
import com.resumeai.pro.presentation.preview.PreviewScreen
import com.resumeai.pro.presentation.templates.TemplatesScreen
import com.resumeai.pro.presentation.myresumes.MyResumesScreen
import com.resumeai.pro.presentation.settings.SettingsScreen
import com.resumeai.pro.presentation.ailoader.AILoaderScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Builder.route) {
            BuilderScreen(navController = navController)
        }
        composable(Screen.Generation.route) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
            GenerationScreen(navController = navController, resumeId = resumeId)
        }
        composable(Screen.Preview.route) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
            PreviewScreen(navController = navController, resumeId = resumeId)
        }
        composable(Screen.Templates.route) {
            TemplatesScreen(navController = navController)
        }
        composable(Screen.MyResumes.route) {
            MyResumesScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.AILoader.route) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("loaderType") ?: "default"
            AILoaderScreen(navController = navController, loaderType = type)
        }
    }
}

