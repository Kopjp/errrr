package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.InvoiceDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PressingScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TreasuryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OilGold
import com.example.ui.theme.OlivePrimary
import com.example.ui.viewmodel.PressViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val viewModel: PressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure RTL layout for authentic Arabic display
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OlivePressApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OlivePressApp(viewModel: PressViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val invoicePreview by viewModel.invoicePreview.collectAsStateWithLifecycle()

    // BackHandler: Return to Dashboard if user is on any other tab
    BackHandler(enabled = currentTab != ScreenTab.DASHBOARD) {
        viewModel.setTab(ScreenTab.DASHBOARD)
    }

    val mainNavItems = listOf(
        ScreenTab.DASHBOARD to ("الرئيسية" to Icons.Default.Dashboard),
        ScreenTab.PRESSING to ("عصر الزبائن" to Icons.Default.WaterDrop),
        ScreenTab.PURCHASES to ("شراء الزيتون" to Icons.Default.Agriculture),
        ScreenTab.SALES to ("بيع الزيت" to Icons.Default.PointOfSale),
        ScreenTab.INVENTORY to ("الإدارة والتقارير" to Icons.Default.Storage)
    )

    val isManagementSection = currentTab in listOf(
        ScreenTab.INVENTORY,
        ScreenTab.TREASURY,
        ScreenTab.REPORTS,
        ScreenTab.SETTINGS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_press_logo),
                                contentDescription = "شعار المعصرة",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = settings.pressName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OlivePrimary
                                )
                                Text(
                                    text = currentTab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Sub-tabs for management & operations section
                if (isManagementSection) {
                    val subTabs = listOf(
                        ScreenTab.INVENTORY to "المخزون والخزانات",
                        ScreenTab.TREASURY to "الصندوق والمصاريف",
                        ScreenTab.REPORTS to "التقارير المالية",
                        ScreenTab.SETTINGS to "الإعدادات والتدقيق"
                    )
                    val selectedSubIndex = subTabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

                    ScrollableTabRow(
                        selectedTabIndex = selectedSubIndex,
                        edgePadding = 12.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = OlivePrimary,
                        indicator = { tabPositions ->
                            if (selectedSubIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubIndex]),
                                    color = OlivePrimary,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        subTabs.forEach { (tab, title) ->
                            val isSelected = tab == currentTab
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.setTab(tab) },
                                modifier = Modifier.testTag("sub_nav_${tab.name}"),
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) OlivePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OlivePrimary,
                tonalElevation = 6.dp
            ) {
                mainNavItems.forEach { (tab, labelAndIcon) ->
                    val (label, icon) = labelAndIcon
                    val isSelected = if (tab == ScreenTab.INVENTORY) {
                        isManagementSection
                    } else {
                        currentTab == tab
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab == ScreenTab.INVENTORY) {
                                if (!isManagementSection) viewModel.setTab(ScreenTab.INVENTORY)
                            } else {
                                viewModel.setTab(tab)
                            }
                        },
                        modifier = Modifier.testTag("main_nav_${tab.name}"),
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = OlivePrimary,
                            indicatorColor = OlivePrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "ScreenTransition") { targetTab ->
                when (targetTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { viewModel.setTab(it) },
                        onQuickAction = { /* handled via navigation */ }
                    )
                    ScreenTab.PURCHASES -> PurchasesScreen(viewModel = viewModel)
                    ScreenTab.PRESSING -> PressingScreen(viewModel = viewModel)
                    ScreenTab.SALES -> SalesScreen(viewModel = viewModel)
                    ScreenTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                    ScreenTab.TREASURY -> TreasuryScreen(viewModel = viewModel)
                    ScreenTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Invoice/Receipt preview
    invoicePreview?.let { previewData ->
        InvoiceDialog(
            data = previewData,
            viewModel = viewModel,
            settings = settings,
            onDismiss = { viewModel.dismissInvoicePreview() }
        )
    }
}
