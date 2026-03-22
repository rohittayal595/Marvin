# Project Plan

Create an android app called marvin (screenshots attached) .

Mini PRD for this app -

1-Pager Justification
Marvin solves the "uncertainty gap" in weight loss by providing a data-driven, multi-week roadmap based on the sustainable 1% weekly reduction rule. It replaces vague goals with a high-transparency list of weekly targets and a "Weekly Pivot" mechanism that adjusts to the user’s real-world data.
P0: Context, Executive Summary, & Vision
Context: Most weight loss apps fail users by being either too calorie-obsessed or too vague about the timeline. Marvin is a mobile-first tracking and forecasting tool designed for users who value sustainability and data-driven planning. By utilizing the user’s height and BMI as a baseline, the app generates a multi-week projection of their journey, applying a 1% weekly reduction rate (ramping down to 0.8% once in a healthy BMI range). The core value proposition is the "Weekly Reset": at the end of every week, the app compares actual weight averages against the plan and empowers the user to choose their path forward—be it adjusting intake, increasing activity, or recalculating the timeline. Our goal is to eliminate the "guessing game" and provide a realistic, visual ETA for a user’s target weight.
P0: Critical User Journeys (CUJs)
CUJ 1: The Blueprint (Onboarding & Projection)
User Story: As a motivated user looking for a sustainable plan, I want to see a realistic timeline for my weight loss so that I can commit to a long-term journey rather than a crash diet.
The Journey:
Entry: User inputs height and current weight.
The Range: Marvin calculates the "Healthy Range" (BMI 18.5–24.9) and asks the user to tap a target weight within this range.
The Math: Marvin applies the "1% Rule" (until BMI 24.9) then the "0.8% Rule" to generate a multi-week list.
The Vision: User is presented with their "ETA" (e.g., "You will reach your goal in 14 weeks on Oct 12th") and a visualization showing the projected dotted line vs. their starting point.
CUJ 2: The Daily Pulse (Input & Friction Management)
User Story: I want to log my data throughout the week so that my weekly averages remain accurate without me forgetting about it.
The Journey:
The Trigger: User receives a scheduled notification (every hour until they check-in) reminding them to "track their goal"
The Input: User taps the Floating Action Button (FAB) and enters weight and/or calories in a single, thumb-friendly modal.
Immediate Feedback: The daily view within the current week’s row updates instantly.
CUJ 3: The Weekly Pivot (Reflection & Recalculation)
User Story: As a user who might have had an "off" week, I want the app to help me adjust my plan without feeling like I've failed.
The Journey:
The Analysis: At the end of Day 7, Marvin calculates the average weight and calorie intake for the week.
The Verdict: Marvin compares the "Actual Average" to the "Target Weight" for that week.
The Choice: If the target wasn't met, the user is presented with three clear paths:
Option A (Intake): Reduce calorie ceiling for the next week.
Option B (Burn): Maintain calories but set a step/activity goal.
Option C (Plan): Recalculate the entire future timeline based on the new current weight, moving the ETA out.
Value Realization: The user feels in control and the roadmap remains "live" and honest.
CUJ 4: The Progress Ledger (Long-term Visualization)
User Story: As a data-driven user, I want to see my history and future at a glance so that I stay motivated by my overall trend.
The Journey:
The View: User opens the app to the main "Roadmap" screen—a scrollable list of weeks.
The Drill-down: User taps a previous week; the row expands to show a 7-day breakdown of every calorie and weight entry.
The Graph: User toggles to the graph view to see the solid "Actual" line chasing the dotted "Projected" line, providing a visual sense of "closing the gap."

## Project Brief

# Project Brief: Marvin

## Features
1. **Smart Onboarding & Goal Projection (The Blueprint):** Calculates a personalized, sustainable weight loss timeline using the user's height and starting weight. It applies a 1% weekly reduction rule to generate a realistic, multi-week ETA for reaching their target weight within a healthy BMI range.
2. **Frictionless Daily Tracking (The Daily Pulse):** A simple, quick-input modal accessible via a Floating Action Button (FAB) allowing users to log their daily weight and caloric intake. Includes local scheduled reminders to ensure consistent tracking.
3. **Adaptive Weekly Pivot:** An end-of-week review mechanism that compares the user's actual 7-day average against their target. If off-track, it empowers the user to dynamically adjust their plan by reducing calorie intake, increasing activity, or recalculating the entire future timeline.
4. **Interactive Progress Ledger:** A comprehensive, scrollable roadmap detailing week-by-week progress. Features expandable daily drill-downs and a visual chart overlaying the actual weight trend against the projected dotted line.

## High-Level Tech Stack
* **Kotlin:** Primary programming language.
* **Jetpack Compose:** For building modern, reactive, edge-to-edge Material Design 3 UI.
* **Kotlin Coroutines:** For asynchronous operations and background task handling.
* **Room (with KSP):** Local SQLite database required for persisting daily weight, caloric logs, and the generated multi-week roadmap. KSP (Kotlin Symbol Processing) is used for efficient code generation.
* **Jetpack DataStore (Preferences):** For lightweight storage of user profile metrics (height, baseline weight, target weight) and onboarding state.
* **WorkManager:** Required to handle the scheduling and delivery of local hourly check-in reminders.
* **ViewModel & StateFlow:** Core architectural components to manage UI state effectively.

## UI Design Image
![UI Design](/Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_4.png)
Image path = /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_4.png

## Implementation Steps

### Task_1_DataLayerAndOnboarding: Set up Room Database for logs/roadmap and DataStore for user preferences. Implement the Onboarding flow to collect height, current weight, and target weight, calculating the initial 1% reduction multi-week roadmap.
- **Status:** COMPLETED
- **Updates:** Fixed OnboardingViewModel logic. Healthy Weight Range now calculates properly and Start My Journey button triggers the onboarding completion. Added window insets padding for edge-to-edge UI. Completed refinement loop.
- **Acceptance Criteria:**
  - Room DB and DataStore created successfully
  - Onboarding flow calculates realistic 1% multi-week projection
  - The implemented UI must match the design provided in /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_9.png, /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_8.png, and /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_6.png.

### Task_2_RoadmapAndDailyTracking: Build the main Roadmap screen showing the scrollable list of weeks and the Daily Check-in modal triggered by the FAB.
- **Status:** COMPLETED
- **Updates:** Main Screen & Roadmap Data implemented with RoadmapScreen.kt. Built reactive LazyColumn for displaying WeeklyPlan data from Room. Expandable Week View added using AnimatedVisibility. Daily Check-in Modal created with a FAB that opens a custom Compose Dialog to log weight and calories, saved via DailyLogDao in RoadmapViewModel. Linked UI state in MainActivity.kt. Completed.
- **Acceptance Criteria:**
  - Main screen displays weekly roadmap that expands to show days
  - FAB opens the daily input modal and saves weight/calories to DB
  - The implemented UI must match the design provided in /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_5.png, /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_4.png, and /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_2.png.

### Task_3_ChartsAndWeeklyPivot: Implement the Progress Visualization screen with an actual vs projected chart, and the Weekly Pivot modal for end-of-week plan adjustment.
- **Status:** COMPLETED
- **Updates:** Progress Visualization screen with actual vs projected chart implemented using Canvas. Weekly Pivot modal created matching image_0.png with options for Adjust Intake, Increase Activity, Recalculate Plan. Linked ProgressScreen to Roadmap top bar. Completed.
- **Acceptance Criteria:**
  - Progress Chart renders projected dotted line vs actual solid line
  - Weekly Pivot modal triggers correctly and updates the roadmap DB based on user selection
  - The implemented UI must match the design provided in /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_1.png and /Users/rohittayal/AndroidStudioProjects/Marvin/input_images/image_0.png.

### Task_4_RemindersAndAppIcon: Integrate WorkManager for scheduled daily tracking reminders, apply vibrant Material Design 3 styling throughout, and create an adaptive app icon.
- **Status:** COMPLETED
- **Updates:** Fixed crash on launch. The issue was the lack of the kotlin-android plugin in build.gradle.kts and inconsistent JVM targets. App now launches successfully. Completed Task 4 refinement.
- **Acceptance Criteria:**
  - WorkManager successfully schedules and triggers local notifications
  - Material 3 theme applied with vibrant, energetic colors
  - Adaptive App Icon created and applied

### Task_5_RunAndVerify: Run the app, test all user journeys, and instruct critic_agent to verify application stability and UI alignment.
- **Status:** BLOCKED
- **Updates:** The same core feature (Start My Journey button) is still broken after a refinement loop. Stopping process and reporting failure to the user.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - UI closely matches all provided design images
- **Duration:** N/A

