package com.pulsar.marvin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pulsar.marvin.data.local.AppDatabase
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import com.pulsar.marvin.data.prefs.dataStore
import com.pulsar.marvin.ui.onboarding.OnboardingScreen
import com.pulsar.marvin.ui.onboarding.OnboardingViewModel
import com.pulsar.marvin.ui.onboarding.OnboardingViewModelFactory
import com.pulsar.marvin.ui.pivot.PivotOption
import com.pulsar.marvin.ui.pivot.WeeklyPivotDialog
import com.pulsar.marvin.ui.progress.ProgressScreen
import com.pulsar.marvin.ui.roadmap.RoadmapScreen
import com.pulsar.marvin.ui.roadmap.RoadmapViewModel
import com.pulsar.marvin.ui.roadmap.RoadmapViewModelFactory
import com.pulsar.marvin.ui.theme.MarvinTheme

enum class Screen {
    ONBOARDING, ROADMAP, PROGRESS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val prefsRepo = UserPreferencesRepository(this.dataStore)
        
        val onboardingFactory = OnboardingViewModelFactory(prefsRepo, database.weeklyPlanDao())
        val onboardingViewModel = ViewModelProvider(this, onboardingFactory)[OnboardingViewModel::class.java]

        val roadmapFactory = RoadmapViewModelFactory(prefsRepo, database.weeklyPlanDao(), database.dailyLogDao())
        val roadmapViewModel = ViewModelProvider(this, roadmapFactory)[RoadmapViewModel::class.java]

        setupDailyReminder()

        setContent {
            MarvinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prefs by prefsRepo.userPreferencesFlow.collectAsState(initial = null)
                    var currentScreen by remember { mutableStateOf(Screen.ONBOARDING) }
                    var showPivot by remember { mutableStateOf(false) }
                    var pivotWeek by remember { mutableIntStateOf(1) }

                    LaunchedEffect(prefs) {
                        if (prefs?.isOnboardingComplete == true && currentScreen == Screen.ONBOARDING) {
                            currentScreen = Screen.ROADMAP
                        }
                    }

                    when (currentScreen) {
                        Screen.ONBOARDING -> {
                            if (prefs != null) {
                                OnboardingScreen(
                                    viewModel = onboardingViewModel,
                                    onComplete = { currentScreen = Screen.ROADMAP }
                                )
                            }
                        }
                        Screen.ROADMAP -> {
                            RoadmapScreen(
                                viewModel = roadmapViewModel,
                                onNavigateToProgress = { currentScreen = Screen.PROGRESS },
                                onPivotRequested = { week ->
                                    pivotWeek = week
                                    showPivot = true
                                }
                            )

                            if (showPivot) {
                                val state by roadmapViewModel.state.collectAsState()
                                val plan = state.weeklyPlans.find { it.weekNumber == pivotWeek }
                                val targetWeight = plan?.targetWeight ?: 0f

                                val startOfWeekMillis = plan?.startOfWeekMillis ?: 0L
                                val endOfWeekMillis = startOfWeekMillis + java.time.Duration.ofDays(6).toMillis()
                                val logsThisWeek = state.dailyLogs.filter { log ->
                                    log.dateMillis in startOfWeekMillis..endOfWeekMillis
                                }
                                val actualWeight = logsThisWeek.mapNotNull { it.weight }.average().let { if (it.isNaN()) 0f else it.toFloat() }
                                val actualCalories = logsThisWeek.mapNotNull { it.calories }.average().let { if (it.isNaN()) 0 else it.toInt() }
                                
                                WeeklyPivotDialog(
                                    weekNumber = pivotWeek,
                                    targetWeight = targetWeight,
                                    actualWeight = actualWeight,
                                    averageCalories = actualCalories,
                                    onDismiss = { showPivot = false },
                                    onApply = { option ->
                                        if (option == PivotOption.RECALCULATE) {
                                            // TODO: Update next weeks with current week's last reading as starting point.
                                        }
                                        showPivot = false
                                    }
                                )
                            }
                        }
                        Screen.PROGRESS -> {
                            val state by roadmapViewModel.state.collectAsState()
                            ProgressScreen(
                                weeklyPlans = state.weeklyPlans,
                                dailyLogs = state.dailyLogs,
                                startingWeight = prefs?.startingWeight ?: 0f,
                                targetWeight = prefs?.targetWeight ?: 0f,
                                onBack = { currentScreen = Screen.ROADMAP }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<com.pulsar.marvin.worker.ReminderWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setInitialDelay(24, java.util.concurrent.TimeUnit.HOURS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
