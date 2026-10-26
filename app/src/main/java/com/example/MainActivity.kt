package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.FinancesScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.VentaAlPasoTheme
import com.example.ui.viewmodel.SalesViewModel
import com.example.util.ErrorLogger
import kotlinx.coroutines.flow.collectLatest

enum class AppScreen(val label: String, val icon: ImageVector) {
    POS("Ventas", Icons.Default.Storefront),
    FINANCES("Finanzas", Icons.Default.AccountBalanceWallet),
    REPORTS("Reportes", Icons.Default.Assessment),
    CATALOG("Productos", Icons.Default.Inventory2),
    DIAGNOSTIC("Diagnóstico", Icons.Default.BugReport)
}

class MainActivity : ComponentActivity() {

    private val salesViewModel: SalesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Global uncaught exception logger hook for error diagnostics
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            ErrorLogger.error(
                tag = "CrashLogger",
                message = "Excepción no capturada en hilo ${thread.name}: ${throwable.localizedMessage}",
                throwable = throwable
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        setContent {
            VentaAlPasoTheme {
                MainAppContent(viewModel = salesViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: SalesViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.POS) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back button to return to POS if on another tab
    if (currentScreen != AppScreen.POS) {
        BackHandler {
            currentScreen = AppScreen.POS
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberPrimary,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.POS -> PosScreen(viewModel = viewModel)
                AppScreen.FINANCES -> FinancesScreen(viewModel = viewModel)
                AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                AppScreen.CATALOG -> CatalogScreen(viewModel = viewModel)
                AppScreen.DIAGNOSTIC -> DiagnosticScreen(viewModel = viewModel)
            }
        }
    }
}
