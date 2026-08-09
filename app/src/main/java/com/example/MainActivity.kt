package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.UroViewModel
import com.example.ui.screens.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.utils.FeedbackHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: UroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()
            MyApplicationTheme(theme = appTheme, fontScale = fontScale) {
                MainLayout(viewModel)
            }
        }
    }
}

sealed class NavigationTab(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Calculadoras : NavigationTab("calculadoras", "Calculadoras", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    object Historial : NavigationTab("historial", "Historial", Icons.Filled.History, Icons.Outlined.History)
    object Casos : NavigationTab("casos", "Casos Clínicos", Icons.Filled.FolderShared, Icons.Outlined.FolderShared)
    object Informacion : NavigationTab("informacion", "Información", Icons.Filled.Info, Icons.Outlined.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: UroViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val view = LocalView.current

    // Tabs inside the navigation drawer
    val tabs = listOf(
        NavigationTab.Calculadoras,
        NavigationTab.Historial,
        NavigationTab.Casos,
        NavigationTab.Informacion
    )

    // Show top bar only on the 4 main hub screens
    val showTopAppBar = currentRoute in tabs.map { it.route }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentTheme by viewModel.appTheme.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(310.dp)
            ) {
                // Doctor Profile Header with custom background gradient matching active theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column {
                        // Profile Avatar Placeholder mimicking the medical UI mockup
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil del Doctor",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Dr. Reynel Busto",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "reynelbusto@gmail.com",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UroCálculos",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation drawer items
                tabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            scope.launch { drawerState.close() }
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_tab_${tab.route}"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Interactive Quick Theme Selector in bottom of the Hamburger menu
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Tema de la Interfaz",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple("BLUE", "Azul", Color(0xFF005DAC)),
                            Triple("GREEN", "Verde", Color(0xFF0A7E64)),
                            Triple("PURPLE", "Púrpura", Color(0xFF6F35A5)),
                            Triple("DARK", "Carbón", Color(0xFF2B2D31))
                        ).forEach { (themeId, label, color) ->
                            val isThemeSelected = currentTheme == themeId
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        FeedbackHelper.triggerTap(view, viewModel)
                                        viewModel.setTheme(themeId)
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isThemeSelected) 3.dp else 0.dp,
                                            color = if (isThemeSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isThemeSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isThemeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showTopAppBar) {
                    CenterAlignedTopAppBar(
                        title = {
                            val titleText = when (currentRoute) {
                                NavigationTab.Calculadoras.route -> "UroCálculos"
                                NavigationTab.Historial.route -> "Historial"
                                NavigationTab.Casos.route -> "Casos Clínicos"
                                NavigationTab.Informacion.route -> "Información"
                                else -> "UroCálculos"
                            }
                            Text(
                                text = titleText,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    FeedbackHelper.triggerTap(view, viewModel)
                                    scope.launch { drawerState.open() }
                                },
                                modifier = Modifier.testTag("hamburger_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú principal",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1080.dp)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavigationTab.Calculadoras.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                // Main Tab Hubs
                composable(NavigationTab.Calculadoras.route) {
                    CalculatorsScreen(navController = navController, viewModel = viewModel)
                }
                composable(NavigationTab.Historial.route) {
                    HistoryScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Casos.route) {
                    CasesScreen(navController = navController, viewModel = viewModel)
                }
                composable(NavigationTab.Informacion.route) {
                    AboutScreen(viewModel = viewModel)
                }

                // Sub-screens: Calculators details
                composable("calc_imc") {
                    ImcScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_hidratacion") {
                    HidratacionScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_ipss") {
                    IpssScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_psa") {
                    PsaScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_gleason") {
                    GleasonScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_renal") {
                    RenalScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_creatinina") {
                    CreatininaScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_clavien") {
                    ClavienScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_stone") {
                    StoneScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_shim") {
                    ShimScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_nih_cpsi") {
                    NihCpsiScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_ecog") {
                    EcogScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_kps") {
                    KpsScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_senic") {
                    SenicScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_nnis") {
                    NnisScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_tnm") {
                    TnmScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_psadt") {
                    PsadtScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_volumen_prostata_vejiga") {
                    ProstateBladderVolumeScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_partin") {
                    PartinTablesScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_capra") {
                    CapraScoreScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_eortc") {
                    EortcRiskScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_oab_q") {
                    OabqScreen(navController = navController, viewModel = viewModel)
                }
                composable("calc_iciq_sf") {
                    IciqSfScreen(navController = navController, viewModel = viewModel)
                }

                // Case detail screen
                composable("case_detail") {
                    CaseDetailScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}
    }
}

@Preview(showBackground = true, name = "Layout Principal", showSystemUi = true)
@Composable
fun MainLayoutPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    MyApplicationTheme {
        MainLayout(viewModel = UroViewModel(context.applicationContext as android.app.Application))
    }
}
