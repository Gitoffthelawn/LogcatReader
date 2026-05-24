package com.dp.logcatapp.ui.screens

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.dp.logcatapp.ui.MainActivityViewModel

@Composable
fun App(
  initialScreen: ScreenKey,
  modifier: Modifier = Modifier,
) {
  val activity = LocalActivity.current
  val mainActivityViewModel = viewModel<MainActivityViewModel>()
  val backStack = remember { mutableStateListOf(initialScreen) }

  LaunchedEffect(mainActivityViewModel) {
    mainActivityViewModel.viewLogs.collect { uri ->
      val lastOrNull = backStack.lastOrNull()
      val screenKey = ScreenKey.SavedLogsViewer(uri)
      if (lastOrNull is ScreenKey.SavedLogsViewer) {
        backStack[backStack.lastIndex] = screenKey
      } else {
        backStack += screenKey
      }
    }
  }

  fun popBackStackOrFinish() {
    if (backStack.size == 1) {
      activity?.finish()
    } else {
      backStack.removeLastOrNull()
    }
  }

  NavDisplay(
    backStack = backStack,
    modifier = modifier,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    transitionSpec = {
      slideInHorizontally(initialOffsetX = { it }) togetherWith
        slideOutHorizontally(targetOffsetX = { -it })
    },
    popTransitionSpec = {
      slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })
    },
    predictivePopTransitionSpec = {
      slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })
    },
    entryProvider = { key ->
      when (key) {
        ScreenKey.DeviceLogs -> NavEntry(key) {
          DeviceLogsScreen(
            modifier = Modifier.fillMaxSize(),
            onShowFiltersScreen = { prePopulateFilterInfo ->
              backStack += ScreenKey.Filters(prepopulateFilterInfo = prePopulateFilterInfo)
            },
            onShowSavedLogsScreen = { backStack += ScreenKey.SavedLogs },
            onShowSettingsScreen = { backStack += ScreenKey.Settings },
            onShowSavedLogsViewerScreen = { uri ->
              backStack += ScreenKey.SavedLogsViewer(uri)
            },
            onNavBack = { popBackStackOrFinish() }
          )
        }
        is ScreenKey.Filters -> NavEntry(key) {
          FiltersScreen(
            modifier = Modifier.fillMaxSize(),
            prepopulateFilterInfo = key.prepopulateFilterInfo,
            onNavBack = { popBackStackOrFinish() }
          )
        }
        ScreenKey.SavedLogs -> NavEntry(key) {
          SavedLogsScreen(
            modifier = Modifier.fillMaxSize(),
            onClickLogFile = { uri ->
              backStack += ScreenKey.SavedLogsViewer(uri)
            },
            onNavBack = { popBackStackOrFinish() }
          )
        }
        is ScreenKey.SavedLogsViewer -> NavEntry(key) {
          SavedLogsViewerScreen(
            modifier = Modifier.fillMaxSize(),
            uri = key.uri,
            onNavBack = { popBackStackOrFinish() }
          )
        }
        is ScreenKey.Settings -> NavEntry(key) {
          SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onNavBack = { popBackStackOrFinish() }
          )
        }
      }
    }
  )
}

sealed interface ScreenKey {
  data object DeviceLogs : ScreenKey
  data class Filters(
    val prepopulateFilterInfo: PrepopulateFilterInfo? = null,
  ) : ScreenKey

  data object SavedLogs : ScreenKey
  data class SavedLogsViewer(val uri: Uri) : ScreenKey
  data object Settings : ScreenKey
}


