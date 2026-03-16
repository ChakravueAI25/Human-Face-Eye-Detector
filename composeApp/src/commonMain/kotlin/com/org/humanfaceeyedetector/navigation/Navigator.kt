package com.org.humanfaceeyedetector.navigation

import androidx.compose.runtime.Composable
import com.org.humanfaceeyedetector.state.AppStateHolder

/**
 * Expected implementation of navigation for platform-specific code
 * Android implementation handles ML inference managers
 */
@Composable
expect fun AppNavigation(appState: AppStateHolder)
