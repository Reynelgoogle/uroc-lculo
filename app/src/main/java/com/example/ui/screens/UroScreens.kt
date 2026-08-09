package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.CalculationRecord
import com.example.data.PatientCase
import com.example.ui.UroViewModel
import com.example.ui.utils.FeedbackHelper
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

// ----------------------------------------------------
// PREVIEWS
// ----------------------------------------------------

@Preview(showBackground = true, name = "Tarjeta de Calculadora (Claro)")
@Composable
fun CalculatorCardPreview() {
    MyApplicationTheme(theme = "BLUE") {
        Box(modifier = Modifier.padding(16.dp)) {
            CalculatorCard(
                calc = CalculatorItem(
                    route = "calc_psa",
                    title = "PSA y Densidad",
                    description = "Densidad de PSA (PSAD), Relación PSA Libre/Total y estratificación de riesgo.",
                    icon = Icons.Default.Science,
                    category = "ONCO"
                ),
                categoryColor = Color(0xFF6F35A5),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Pantalla IMC", showSystemUi = true)
@Composable
fun ImcScreenPreview() {
    val context = LocalContext.current
    // Usamos un ViewModel falso o nulo si la pantalla lo permite para el preview
    MyApplicationTheme(theme = "BLUE") {
        ImcScreen(
            navController = NavController(context),
            viewModel = UroViewModel(context.applicationContext as android.app.Application)
        )
    }
}

// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Intelligent navigation pop that prioritizes returning straight to the patient case detail
fun smartPopBack(navController: NavController) {
    try {
        val popped = navController.popBackStack("case_detail", false)
        if (!popped) {
            navController.popBackStack()
        }
    } catch (e: Exception) {
        navController.popBackStack()
    }
}

// Dialog that allows a user to save their calculation either general/anonymously, associated to an existing case, or under a new case!
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveCalculationDialog(
    onDismiss: () -> Unit,
    onSave: (caseId: Long?) -> Unit,
    viewModel: UroViewModel
) {
    val view = LocalView.current
    val cases by viewModel.cases.collectAsStateWithLifecycle()
    val activeCaseId by viewModel.selectedCaseId.collectAsStateWithLifecycle()
    var selectedOption by remember {
        mutableStateOf(if (activeCaseId != null) "EXISTING" else "ANONYMOUS")
    }
    var selectedCaseId by remember {
        mutableStateOf<Long?>(activeCaseId ?: cases.firstOrNull()?.id)
    }
    var newCaseTitle by remember { mutableStateOf("") }

    LaunchedEffect(cases, activeCaseId) {
        if (activeCaseId != null) {
            selectedCaseId = activeCaseId
            selectedOption = "EXISTING"
        } else if (selectedCaseId == null && cases.isNotEmpty() && selectedOption == "EXISTING") {
            selectedCaseId = cases.first().id
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Guardar Cálculo",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = SpacerWithMargin(8)
            ) {
                Text(
                    text = "Seleccione cómo desea almacenar este registro médico de forma anónima:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: General Anonymous
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            FeedbackHelper.triggerTap(view, viewModel)
                            selectedOption = "ANONYMOUS"
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == "ANONYMOUS"),
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            selectedOption = "ANONYMOUS"
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("General / Sin Caso", fontWeight = FontWeight.Bold)
                        Text(
                            "Guardar de forma suelta en el historial general",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                // Option 2: Associate to Existing Case
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            FeedbackHelper.triggerTap(view, viewModel)
                            selectedOption = "EXISTING"
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == "EXISTING"),
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            selectedOption = "EXISTING"
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Asociar a Caso Clínico Existente", fontWeight = FontWeight.Bold)
                        Text(
                            "Agrupar con otros cálculos del mismo caso",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (selectedOption == "EXISTING") {
                    if (cases.isEmpty()) {
                        Text(
                            text = "No tienes ningún caso clínico creado. Elige 'Crear Nuevo Caso'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                        )
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        val currentSelection = cases.find { it.id == selectedCaseId }?.title ?: "Selecciona un caso..."

                        Box(modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 8.dp)) {
                            Button(
                                onClick = {
                                    FeedbackHelper.triggerTap(view, viewModel)
                                    expanded = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(currentSelection)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                cases.forEach { pCase ->
                                    DropdownMenuItem(
                                        text = { Text(pCase.title) },
                                        onClick = {
                                            FeedbackHelper.triggerTap(view, viewModel)
                                            selectedCaseId = pCase.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Divider()

                // Option 3: Create New Case
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            FeedbackHelper.triggerTap(view, viewModel)
                            selectedOption = "NEW"
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == "NEW"),
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            selectedOption = "NEW"
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Crear Nuevo Caso y Asociar", fontWeight = FontWeight.Bold)
                        Text(
                            "Generar una nueva carpeta de caso clínico anónimo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (selectedOption == "NEW") {
                    OutlinedTextField(
                        value = newCaseTitle,
                        onValueChange = { newCaseTitle = it },
                        label = { Text("Código o Identificador del Caso") },
                        placeholder = { Text("Ej: Caso Hiperplasia #304") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 4.dp, bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    FeedbackHelper.triggerConfirm(view, viewModel)
                    when (selectedOption) {
                        "ANONYMOUS" -> onSave(null)
                        "EXISTING" -> {
                            if (selectedCaseId != null) {
                                onSave(selectedCaseId)
                            }
                        }
                        "NEW" -> {
                            viewModel.createCase(newCaseTitle) { newId ->
                                onSave(newId)
                            }
                        }
                    }
                },
                enabled = (selectedOption == "ANONYMOUS" ||
                        (selectedOption == "EXISTING" && selectedCaseId != null) ||
                        (selectedOption == "NEW" && newCaseTitle.trim().isNotEmpty())),
                modifier = Modifier.testTag("dialog_confirm_save_button")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    FeedbackHelper.triggerTap(view, viewModel)
                    onDismiss()
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

// Spacer utility helper
private fun SpacerWithMargin(dp: Int): Arrangement.HorizontalOrVertical {
    return Arrangement.spacedBy(dp.dp)
}

// ----------------------------------------------------
// 1. CALCULATORS DASHBOARD / HOMEPAGE SCREEN
// ----------------------------------------------------
@Composable
// Helper function to resolve category-specific colors
fun getCategoryColor(category: String, isDark: Boolean): Color {
    return when (category) {
        "ONCO" -> if (isDark) Color(0xFFE040FB) else Color(0xFF6F35A5) // Purple
        "URO" -> if (isDark) Color(0xFF26A69A) else Color(0xFF00796B)  // Teal
        "CLINICA" -> if (isDark) Color(0xFF64B5F6) else Color(0xFF005DAC)// Blue
        "QUIRURGICO" -> if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)// Orange
        else -> if (isDark) Color(0xFF90A4AE) else Color(0xFF455A64)  // Gray
    }
}

@Composable
fun CalculatorCard(
    calc: CalculatorItem,
    categoryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("calculator_${calc.route}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color-coded left-accent boundary line for clinical categorization
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(categoryColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Soft color container for standard medical icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.08f))
                        .border(1.dp, categoryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = calc.icon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Metadata details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = calc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = calc.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                // Navigational Chevron
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CalculatorsScreen(
    navController: NavController,
    viewModel: UroViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showThemeDialog by remember { mutableStateOf(false) }
    val activeCase by viewModel.selectedCase.collectAsStateWithLifecycle()
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val isDark = appTheme == "DARK"

    var selectedCategory by remember { mutableStateOf("TODOS") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Case Header / Banner if selected
        activeCase?.let { pCase ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("active_case_banner"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Asociando cálculos a Caso Clínico",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pCase.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                navController.navigate("case_detail")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Ver Caso",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.selectCase(null)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Deseleccionar Caso",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Compact Dashboard Header Bar
        val heroGradient = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "UroCálculos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% Local",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = "Soporte de decisiones clínicas en urología",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .testTag("quick_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cambiar Tema",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Subtitle section
        Text(
            text = "Herramientas Clínicas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Categorized filtering row (Horizontal Scroll of Filter Chips)
        val categories = listOf(
            Triple("TODOS", "Todos", Icons.Default.Apps),
            Triple("ONCO", "Onco-Urología", Icons.Default.Biotech),
            Triple("URO", "Urología General", Icons.Default.Quiz),
            Triple("CLINICA", "Función & Clínica", Icons.Default.MonitorWeight),
            Triple("QUIRURGICO", "Riesgo Quirúrgico", Icons.Default.HealthAndSafety)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { (catId, catLabel, catIcon) ->
                val isSelected = selectedCategory == catId
                val catColor = if (catId == "TODOS") MaterialTheme.colorScheme.primary else getCategoryColor(catId, isDark)
                
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        FeedbackHelper.triggerTap(view, viewModel)
                        selectedCategory = catId
                    },
                    label = { 
                        Text(
                            text = catLabel, 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else catColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor.copy(alpha = 0.15f),
                        selectedLabelColor = catColor,
                        selectedLeadingIconColor = catColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        selectedBorderColor = catColor,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 2.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Database/List of Clinical calculators with explicit categories
        val calculators = listOf(
            CalculatorItem("calc_psa", "PSA y Densidad", "Densidad de PSA (PSAD), Relación PSA Libre/Total y estratificación de riesgo.", Icons.Default.Science, "ONCO"),
            CalculatorItem("calc_psadt", "Tiempo Duplicación PSA", "Velocidad de elevación y tiempo de duplicación del PSA (PSADT) en cáncer de próstata.", Icons.Default.Timeline, "ONCO"),
            CalculatorItem("calc_partin", "Tablas de Partin", "Nomograma para predecir invasión capsular, vesículas seminales y ganglios.", Icons.Default.TableChart, "ONCO"),
            CalculatorItem("calc_capra", "Puntaje UCSF-CAPRA", "Estratificación de riesgo y pronóstico prequirúrgico en cáncer de próstata.", Icons.Default.Score, "ONCO"),
            CalculatorItem("calc_eortc", "Riesgo EORTC (Vejiga)", "Predicción de recurrencia y progresión a 1 y 5 años en cáncer vesical NMIBC.", Icons.Default.Analytics, "ONCO"),
            CalculatorItem("calc_gleason", "Score de Gleason / ISUP", "Estratificación de grado histológico de cáncer de próstata según criterios de la ISUP.", Icons.Default.Biotech, "ONCO"),
            CalculatorItem("calc_renal", "Nefrometría R.E.N.A.L.", "Evaluación de complejidad anatómica y riesgo quirúrgico de masas renales.", Icons.Default.Layers, "ONCO"),
            CalculatorItem("calc_ecog", "Escala ECOG", "Estado funcional oncológico según la Eastern Cooperative Oncology Group.", Icons.Default.DirectionsRun, "ONCO"),
            CalculatorItem("calc_kps", "Escala Karnofsky (KPS)", "Índice de capacidad funcional y requerimiento de asistencia médica.", Icons.Default.EscalatorWarning, "ONCO"),
            CalculatorItem("calc_tnm", "Clasificación TNM Urológica", "Estadificación TNM (AJCC 8va Ed) para tumores de riñón, vías superiores, vejiga, próstata, testículo y pene.", Icons.Default.AccountTree, "ONCO"),
            
            CalculatorItem("calc_ipss", "IPSS (Próstata)", "International Prostate Symptom Score + Calidad de vida.", Icons.Default.Quiz, "URO"),
            CalculatorItem("calc_volumen_prostata_vejiga", "Volumen Prostático y PVR", "Fórmula elipsoide para próstata, vejiga y residuo postmiccional (PVR).", Icons.Default.Straighten, "URO"),
            CalculatorItem("calc_oab_q", "Vejiga Hiperactiva (OABSS)", "Cuestionario de síntomas y severidad de vejiga hiperactiva.", Icons.Default.Tune, "URO"),
            CalculatorItem("calc_iciq_sf", "Incontinencia (ICIQ-SF)", "Cuestionario internacional validado para evaluación corta de incontinencia urinaria.", Icons.Default.Water, "URO"),
            CalculatorItem("calc_stone", "Escala STONE", "Probabilidad de cálculo ureteral obstructivo no complicado.", Icons.Default.GridOn, "URO"),
            CalculatorItem("calc_shim", "Escala SHIM (IIEF-5)", "Cuestionario de salud sexual e índice de disfunción eréctil.", Icons.Default.Favorite, "URO"),
            CalculatorItem("calc_nih_cpsi", "Índice NIH-CPSI", "Evaluación de síntomas, dolor y calidad de vida en prostatitis crónica.", Icons.Default.FactCheck, "URO"),
            
            CalculatorItem("calc_imc", "IMC (BMI)", "Índice de Masa Corporal. Clasificación OMS de estado nutricional.", Icons.Default.MonitorWeight, "CLINICA"),
            CalculatorItem("calc_hidratacion", "Hidratación", "Cálculo Holliday-Segar (4-2-1) y reposición de ayuno.", Icons.Default.WaterDrop, "CLINICA"),
            CalculatorItem("calc_creatinina", "Depuración de Creatinina", "Cálculo de aclaramiento de creatinina (Cockcroft-Gault) para función renal.", Icons.Default.Assessment, "CLINICA"),
            
            CalculatorItem("calc_clavien", "Clasificación Clavien-Dindo", "Escala estándar internacional para clasificar complicaciones posoperatorias.", Icons.Default.Assignment, "QUIRURGICO"),
            CalculatorItem("calc_senic", "Estudio SENIC", "Prepredicción del riesgo de infección nosocomial quirúrgica.", Icons.Default.HealthAndSafety, "QUIRURGICO"),
            CalculatorItem("calc_nnis", "Calculadora NNIS/NHSN", "Índice de riesgo quirúrgico y porcentaje de infección sitio quirúrgico.", Icons.Default.Coronavirus, "QUIRURGICO")
        )

        val filteredCalculators = if (selectedCategory == "TODOS") {
            calculators
        } else {
            calculators.filter { it.category == selectedCategory }
        }

        // Rendered filtered list with beautiful custom items
        filteredCalculators.forEach { calc ->
            val cardColor = getCategoryColor(calc.category, isDark)
            CalculatorCard(
                calc = calc,
                categoryColor = cardColor,
                onClick = {
                    FeedbackHelper.triggerTap(view, viewModel)
                    navController.navigate(calc.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Call to action: Cases Assistant
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    FeedbackHelper.triggerTap(view, viewModel)
                    navController.navigate("casos")
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Carpeta de Casos Clínicos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agrupa múltiples cálculos bajo un código anónimo y genera un reporte clínico estructurado de forma segura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ver Casos",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            onDismiss = { showThemeDialog = false },
            viewModel = viewModel
        )
    }
}

data class CalculatorItem(
    val route: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: String
)

// ----------------------------------------------------
// 2. IMC SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImcScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var pesoStr by remember { mutableStateOf("") }
    var tallaStr by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val peso = pesoStr.toFloatOrNull() ?: 0f
    val tallaCm = tallaStr.toFloatOrNull() ?: 0f
    val tallaM = tallaCm / 100f

    val imc = if (peso > 0f && tallaM > 0f) peso / (tallaM * tallaM) else 0f

    val (classification, classificationColor) = when {
        imc <= 0f -> "" to Color.Gray
        imc < 18.5f -> "Bajo Peso (< 18.5 kg/m²)" to Color(0xFFE65100) // Orange/Warning
        imc < 25.0f -> "Normopeso (18.5 - 24.9 kg/m²)" to Color(0xFF2E7D32) // Green/Success
        imc < 30.0f -> "Sobrepeso (25.0 - 29.9 kg/m²)" to Color(0xFF1565C0) // Blue/Secondary
        else -> "Obesidad (≥ 30.0 kg/m²)" to Color(0xFFC62828) // Red/Alert
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cálculo de IMC") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Determinación del Índice de Masa Corporal para la evaluación clínica basal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Parámetros del Paciente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = pesoStr,
                        onValueChange = { pesoStr = it },
                        label = { Text("Peso (kg)") },
                        placeholder = { Text("Ej: 75") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("imc_weight_input"),
                        trailingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = tallaStr,
                        onValueChange = { tallaStr = it },
                        label = { Text("Talla / Altura (cm)") },
                        placeholder = { Text("Ej: 175") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("imc_height_input"),
                        trailingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                    )
                }
            }

            if (imc > 0f) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("ÍNDICE DE MASA CORPORAL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%.1f", imc),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(classificationColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = classification,
                                color = classificationColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("save_imc_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Cálculo")
                        }
                    }
                }
            }

            // Clinical Guideline Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text("Nota Clínica", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "El IMC es un indicador indirecto de adiposidad. Debe interpretarse con precaución en pacientes con retención de líquidos masiva, ascitis, o gran masa muscular.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = String.format("%.1f kg/m² - %s", imc, classification)
                val paramsText = "Peso: $pesoStr kg, Talla: $tallaStr cm"
                viewModel.saveCalculation(
                    calculatorType = "IMC",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 3. HIDRATACIÓN PERIOPERATORIA SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HidratacionScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Balance Hidroelectrolítico, 1 = Pauta Holliday-Segar

    // Common State
    var pesoStr by remember { mutableStateOf("") }
    val peso = pesoStr.toFloatOrNull() ?: 0f

    // Tab 1: Balance Hidroelectrolítico States
    var horasStr by remember { mutableStateOf("24") }
    val horas = horasStr.toFloatOrNull() ?: 24f

    var ingOralStr by remember { mutableStateOf("") }
    var ingParenteralStr by remember { mutableStateOf("") }
    var ingTransfusionesStr by remember { mutableStateOf("") }

    var egrDiuresisStr by remember { mutableStateOf("") }
    var egrVomitosStr by remember { mutableStateOf("") }
    var egrDrenajesStr by remember { mutableStateOf("") }
    var egrDeposicionesStr by remember { mutableStateOf("") }
    var tieneFiebre by remember { mutableStateOf(false) }

    // Tab 2: Holliday-Segar States
    var ayunoHours by remember { mutableStateOf(8f) }

    var showSaveDialog by remember { mutableStateOf(false) }

    // Computations
    // Rule 4-2-1 logic
    val hourlyRate = when {
        peso <= 0f -> 0f
        peso <= 10f -> peso * 4f
        peso <= 20f -> 40f + (peso - 10f) * 2f
        else -> 60f + (peso - 20f) * 1f
    }
    val dailyRate = hourlyRate * 24f
    val totalDeficit = hourlyRate * ayunoHours

    // Balance Computations
    val ingOral = ingOralStr.toFloatOrNull() ?: 0f
    val ingParenteral = ingParenteralStr.toFloatOrNull() ?: 0f
    val ingTransfusiones = ingTransfusionesStr.toFloatOrNull() ?: 0f
    val aguaMetabolica = if (peso > 0f && horas > 0f) 0.3f * peso * horas else 0f
    val totalIngresos = ingOral + ingParenteral + ingTransfusiones + aguaMetabolica

    val egrDiuresis = egrDiuresisStr.toFloatOrNull() ?: 0f
    val egrVomitos = egrVomitosStr.toFloatOrNull() ?: 0f
    val egrDrenajes = egrDrenajesStr.toFloatOrNull() ?: 0f
    val egrDeposiciones = egrDeposicionesStr.toFloatOrNull() ?: 0f
    val factorInsensibles = if (tieneFiebre) 1.0f else 0.5f
    val perdidasInsensibles = if (peso > 0f && horas > 0f) factorInsensibles * peso * horas else 0f
    val totalEgresos = egrDiuresis + perdidasInsensibles + egrVomitos + egrDrenajes + egrDeposiciones
    val balanceNeto = totalIngresos - totalEgresos

    val diuresisRate = if (peso > 0f && horas > 0f) egrDiuresis / peso / horas else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hidratación y Balance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Módulo clínico avanzado para el cálculo de necesidades de hidratación perioperatoria (regla Holliday-Segar) o el control continuo de balance hidroelectrolítico de pacientes quirúrgicos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Weight Input Card (Shared by both models)
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Datos Generales del Paciente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = pesoStr,
                        onValueChange = { pesoStr = it },
                        label = { Text("Peso del Paciente (kg)") },
                        placeholder = { Text("Ej: 70") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("hidratacion_weight_input"),
                        trailingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
                    )
                }
            }

            // TabRow to toggle between modes
            TabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Balance Hídrico", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Holliday-Segar (Ayuno)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (activeTab == 0) {
                // TAB 1: BALANCE HIDROELECTROLÍTICO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Período de Tiempo del Balance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        
                        OutlinedTextField(
                            value = horasStr,
                            onValueChange = { horasStr = it },
                            label = { Text("Horas de Control") },
                            placeholder = { Text("Ej: 24") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("balance_hours_input"),
                            trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                        )
                    }
                }

                // Inputs Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Ingresos (mL)", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) // Soft green

                        OutlinedTextField(
                            value = ingOralStr,
                            onValueChange = { ingOralStr = it },
                            label = { Text("Vía Oral / Enteral (Nutrición, agua)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = ingParenteralStr,
                            onValueChange = { ingParenteralStr = it },
                            label = { Text("Vía Parenteral (Sueros, medicación IV)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = ingTransfusionesStr,
                            onValueChange = { ingTransfusionesStr = it },
                            label = { Text("Transfusiones / Coloides (Sangre, plasma)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Agua Metabólica (Autocalculada):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text("${aguaMetabolica.toInt()} mL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Egresos (mL)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)

                        OutlinedTextField(
                            value = egrDiuresisStr,
                            onValueChange = { egrDiuresisStr = it },
                            label = { Text("Orina (Diuresis)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = egrVomitosStr,
                            onValueChange = { egrVomitosStr = it },
                            label = { Text("Vómitos / Sonda Nasogástrica") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = egrDrenajesStr,
                            onValueChange = { egrDrenajesStr = it },
                            label = { Text("Drenajes Quirúrgicos / Fístulas") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = egrDeposicionesStr,
                            onValueChange = { egrDeposicionesStr = it },
                            label = { Text("Deposiciones (Heces)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Insensible losses and fever toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pérdidas Insensibles (Autocalculadas):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text("${perdidasInsensibles.toInt()} mL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tieneFiebre = !tieneFiebre }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tieneFiebre,
                                onCheckedChange = { tieneFiebre = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Fiebre (>38°C), Taquipnea o Sudoración severa", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Duplica la tasa de pérdidas insensibles de 0.5 a 1.0 mL/kg/h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Display Balance Results if inputs exist
                if (peso > 0f) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (balanceNeto >= 0f) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "RESULTADOS DE BALANCE DE LÍQUIDOS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (balanceNeto >= 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Ingresos:")
                                Text("${totalIngresos.toInt()} mL", fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Egresos:")
                                Text("${totalEgresos.toInt()} mL", fontWeight = FontWeight.Bold)
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Balance Neto:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (balanceNeto >= 0) "+${balanceNeto.toInt()} mL" else "${balanceNeto.toInt()} mL",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (balanceNeto >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }

                            if (horas > 0f) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                
                                Text("Análisis de Ritmo Diurético:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ritmo diurético estimado:")
                                    Text("${String.format(Locale.US, "%.2f", diuresisRate)} mL/kg/h", fontWeight = FontWeight.Bold)
                                }

                                val (badgeColor, badgeText) = when {
                                    diuresisRate < 0.5f -> MaterialTheme.colorScheme.errorContainer to "Oliguria (< 0.5 mL/kg/h)"
                                    diuresisRate <= 1.5f -> Color(0xFFC8E6C9) to "Normal (0.5 - 1.5 mL/kg/h)"
                                    diuresisRate <= 3.0f -> MaterialTheme.colorScheme.primaryContainer to "Abundante (1.5 - 3.0 mL/kg/h)"
                                    else -> MaterialTheme.colorScheme.secondaryContainer to "Poliuria (> 3.0 mL/kg/h)"
                                }

                                val badgeTextColor = when {
                                    diuresisRate < 0.5f -> MaterialTheme.colorScheme.onErrorContainer
                                    diuresisRate <= 1.5f -> Color(0xFF1B5E20)
                                    diuresisRate <= 3.0f -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(badgeColor, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = badgeTextColor
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            Text("Requerimientos Basales Diarios de Electrólitos:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sodio (Na⁺) estimado (1-2 mEq/kg):")
                                Text("${(1.0 * peso).toInt()} - ${(2.0 * peso).toInt()} mEq/24h", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Potasio (K⁺) estimado (0.5-1 mEq/kg):")
                                Text("${(0.5 * peso).toInt()} - ${(1.0 * peso).toInt()} mEq/24h", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { showSaveDialog = true },
                                modifier = Modifier.fillMaxWidth().testTag("save_balance_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar Balance Hidroelectrolítico")
                            }
                        }
                    }
                }
            } else {
                // TAB 2: HOLLIDAY-SEGAR (PERIOPERATORIO)
                if (peso <= 0f) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Por favor, introduzca el peso del paciente para visualizar los requerimientos perioperatorios.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Parámetros de Ayuno", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            Text("Horas de Ayuno Preoperatorio: ${ayunoHours.toInt()} h", fontWeight = FontWeight.Bold)
                            Slider(
                                value = ayunoHours,
                                onValueChange = { ayunoHours = it },
                                valueRange = 0f..24f,
                                steps = 24,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (hourlyRate > 0f) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "REQUERIMIENTOS HÍDRICOS CALCULADOS (4-2-1)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Aporte Basal por Hora:")
                                    Text("${hourlyRate.toInt()} mL/h", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Aporte Basal por Día:")
                                    Text("${dailyRate.toInt()} mL/día", fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Déficit Acumulado de Ayuno:")
                                    Text("${totalDeficit.toInt()} mL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp))

                                Text("Pauta de Reposición del Déficit sugerida:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("• 1ª hora de cirugía: ${(hourlyRate + totalDeficit * 0.5f).toInt()} mL (Basal + 50% déficit)")
                                Text("• 2ª hora de cirugía: ${(hourlyRate + totalDeficit * 0.25f).toInt()} mL (Basal + 25% déficit)")
                                Text("• 3ª hora de cirugía: ${(hourlyRate + totalDeficit * 0.25f).toInt()} mL (Basal + 25% déficit)")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { showSaveDialog = true },
                                    modifier = Modifier.fillMaxWidth().testTag("save_hydration_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardar Pauta Holliday-Segar")
                                }
                            }
                        }
                    }

                    // Regla Guidance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Guía Regla 4-2-1 (Holliday-Segar):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Primeros 10 kg: 4 mL/kg/h", style = MaterialTheme.typography.bodySmall)
                            Text("• Segundos 10 kg (11-20 kg): 2 mL/kg/h", style = MaterialTheme.typography.bodySmall)
                            Text("• Por cada kg adicional (>20 kg): 1 mL/kg/h", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = if (activeTab == 0) {
                    "Balance: ${if (balanceNeto >= 0) "+" else ""}${balanceNeto.toInt()} mL (Ing: ${totalIngresos.toInt()} mL / Egr: ${totalEgresos.toInt()} mL), Diuresis: ${String.format(Locale.US, "%.2f", diuresisRate)} mL/kg/h"
                } else {
                    "Basal: ${hourlyRate.toInt()} mL/h, Déficit: ${totalDeficit.toInt()} mL (en ${ayunoHours.toInt()}h de ayuno)"
                }

                val paramsText = if (activeTab == 0) {
                    "Peso: $pesoStr kg, Horas: ${horas.toInt()}h, IngOral: ${ingOral.toInt()}mL, IngIV: ${ingParenteral.toInt()}mL, Diuresis: ${egrDiuresis.toInt()}mL"
                } else {
                    "Peso: $pesoStr kg, Ayuno: ${ayunoHours.toInt()} h"
                }

                val calcLabel = if (activeTab == 0) "Balance Hidroelectrolítico" else "Hidratación Perioperatoria"

                viewModel.saveCalculation(
                    calculatorType = calcLabel,
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 4. IPSS SCREEN (INTERNATIONAL PROSTATE SYMPTOM SCORE)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpssScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    val questions = listOf(
        IpssQuestion("Q1: Vaciado Incompleto", "¿Con qué frecuencia ha tenido la sensación de no vaciar completamente la vejiga al terminar de orinar?", "SÍNTOMA OBSTRUCTIVO"),
        IpssQuestion("Q2: Frecuencia", "¿Con qué frecuencia ha tenido que volver a orinar antes de dos horas de haber orinado?", "SÍNTOMA IRRITATIVO"),
        IpssQuestion("Q3: Intermitencia", "¿Con qué frecuencia ha observado que, al orinar, el chorro se detenía y comenzaba de nuevo varias veces?", "SÍNTOMA OBSTRUCTIVO"),
        IpssQuestion("Q4: Urgencia", "¿Con qué frecuencia ha tenido dificultad para aguantar las ganas de orinar?", "SÍNTOMA IRRITATIVO"),
        IpssQuestion("Q5: Chorro Débil", "¿Con qué frecuencia ha observado que el chorro de orina es poco fuerte?", "SÍNTOMA OBSTRUCTIVO"),
        IpssQuestion("Q6: Esfuerzo", "¿Con qué frecuencia ha tenido que apretar o hacer esfuerzo para comenzar a orinar?", "SÍNTOMA OBSTRUCTIVO"),
        IpssQuestion("Q7: Nicturia", "¿Cuántas veces suele levantarse a orinar por la noche desde que se acuesta hasta que se levanta por la mañana?", "SÍNTOMA IRRITATIVO")
    )

    // Scores 0-5 for Q1-Q7
    val scores = remember { mutableStateListOf(-1, -1, -1, -1, -1, -1, -1) }
    // Q8 Quality of life question score 0-6
    var q8Score by remember { mutableStateOf(-1) }
    var currentStep by remember { mutableStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val totalScore = scores.filter { it >= 0 }.sum()
    val isComplete = scores.all { it >= 0 } && q8Score >= 0

    val severity = when {
        totalScore <= 7 -> "Leve (0-7 puntos)"
        totalScore <= 19 -> "Moderada (8-19 puntos)"
        else -> "Severa (20-35 puntos)"
    }

    val qolDescription = when (q8Score) {
        0 -> "Muy Satisfecho"
        1 -> "Satisfecho"
        2 -> "Más bien Satisfecho"
        3 -> "Tan Satisfecho como Insatisfecho"
        4 -> "Más bien Insatisfecho"
        5 -> "Infeliz"
        6 -> "Fatal"
        else -> "Sin evaluar"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora IPSS") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Steps indicator
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pregunta ${currentStep + 1} de 8", fontWeight = FontWeight.Bold)
                        Text(if (currentStep < 7) questions[currentStep].category else "CALIDAD DE VIDA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = (currentStep + 1) / 8f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "step_animation"
            ) { step ->
                if (step < 7) {
                    val q = questions[step]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(q.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Text(q.text, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(8.dp))

                            val options = if (step == 6) {
                                listOf("Ninguna vez", "1 vez", "2 veces", "3 veces", "4 veces", "5 o más veces")
                            } else {
                                listOf("Ninguna vez", "Menos de 1 de cada 5 veces", "Menos de la mitad de las veces", "Aproximadamente la mitad", "Más de la mitad de las veces", "Casi siempre")
                            }

                            options.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scores[step] = index
                                            if (currentStep < 7) {
                                                currentStep++
                                            }
                                        }
                                        .background(
                                            if (scores[step] == index) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (scores[step] == index),
                                        onClick = {
                                            scores[step] = index
                                            if (currentStep < 7) {
                                                currentStep++
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("$index - $option", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Q8: Calidad de Vida", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                            Text("¿Cómo se sentiría si tuviera que pasar el resto de su vida con los síntomas prostáticos tal como los tiene ahora?", style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(8.dp))

                            val qolOptions = listOf(
                                "0 - Muy Satisfecho",
                                "1 - Satisfecho",
                                "2 - Más bien Satisfecho",
                                "3 - Tan Satisfecho como Insatisfecho",
                                "4 - Más bien Insatisfecho",
                                "5 - Infeliz",
                                "6 - Fatal"
                            )

                            qolOptions.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            q8Score = index
                                        }
                                        .background(
                                            if (q8Score == index) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (q8Score == index),
                                        onClick = { q8Score = index }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(option, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Back/Next Stepper controllers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { if (currentStep > 0) currentStep-- },
                    enabled = currentStep > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Anterior")
                }

                if (currentStep < 7) {
                    Button(
                        onClick = { currentStep++ },
                        enabled = scores[currentStep] >= 0
                    ) {
                        Text("Siguiente")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }

            // Results presentation once all steps are checked
            if (isComplete) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("RESULTADOS EVALUACIÓN IPSS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Puntuación Total:")
                                Text("$totalScore / 35", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(severity, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Calidad de Vida:")
                                Text("$q8Score / 6", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(qolDescription, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("save_ipss_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar IPSS")
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Puntuación: $totalScore/35 ($severity), Calidad de Vida: $q8Score/6 ($qolDescription)"
                val paramsText = "Scores: ${scores.joinToString(",")}, Q8: $q8Score"
                viewModel.saveCalculation(
                    calculatorType = "IPSS",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

data class IpssQuestion(
    val title: String,
    val text: String,
    val category: String
)

// ----------------------------------------------------
// 5. ESCALA ECOG SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcogScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var selectedGrade by remember { mutableStateOf(-1) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val ecogOptions = listOf(
        EcogGrade(0, "Totalmente activo", "Capaz de realizar todas las actividades previas a la enfermedad sin restricciones."),
        EcogGrade(1, "Restricción de actividad física intensa", "Capaz de realizar trabajos ligeros o de naturaleza sedentaria (p. ej., trabajo doméstico ligero, trabajo de oficina)."),
        EcogGrade(2, "Capaz de autocuidado, incapaz de trabajar", "Ambulatorio y capaz de realizar todos los autocuidados, pero incapaz de llevar a cabo cualquier actividad laboral. Fuera de la cama más del 50% de las horas de vigilia."),
        EcogGrade(3, "Autocuidado limitado", "Capaz de realizar solo autocuidados limitados; confinado a la cama o silla más del 50% de las horas de vigilia."),
        EcogGrade(4, "Completamente discapacitado", "No puede realizar ningún autocuidado. Totalmente confinado a la cama o silla.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escala ECOG") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Evaluación del estado funcional en pacientes oncológicos según los criterios de la Eastern Cooperative Oncology Group.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ecogOptions.forEach { opt ->
                val isSelected = selectedGrade == opt.grade
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGrade = opt.grade }
                        .testTag("ecog_grade_${opt.grade}"),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.grade.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(opt.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(opt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedGrade = opt.grade }
                        )
                    }
                }
            }

            if (selectedGrade >= 0) {
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("save_ecog_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Grado ECOG")
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val matching = ecogOptions.find { it.grade == selectedGrade }
                val resultText = "ECOG: Grado $selectedGrade (${matching?.title})"
                val paramsText = "ECOG Grade Selected: $selectedGrade"
                viewModel.saveCalculation(
                    calculatorType = "ECOG",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

data class EcogGrade(
    val grade: Int,
    val title: String,
    val description: String
)

// ----------------------------------------------------
// 6. ESCALA KARNOFSKY (KPS) SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpsScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var selectedPercent by remember { mutableStateOf(-1) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val kpsOptions = listOf(
        KpsItem(100, "Normal", "Sin quejas. Sin evidencia de enfermedad.", "Capaz de realizar actividad normal"),
        KpsItem(90, "Actividad normal", "Capaz de realizar actividad normal. Signos o síntomas leves de enfermedad.", "Capaz de realizar actividad normal"),
        KpsItem(80, "Actividad normal con esfuerzo", "Actividad normal con esfuerzo. Algunos signos o síntomas de enfermedad.", "Capaz de realizar actividad normal"),
        KpsItem(70, "Autocuidado", "Capaz de cuidarse a sí mismo. Incapaz de llevar a cabo una actividad normal o de realizar un trabajo activo.", "Incapaz de trabajar, pero puede vivir en casa"),
        KpsItem(60, "Asistencia ocasional", "Requiere atención ocasional, pero puede atender la mayoría de sus necesidades personales.", "Incapaz de trabajar, pero puede vivir en casa"),
        KpsItem(50, "Asistencia considerable", "Requiere gran atención y cuidados médicos frecuentes.", "Incapaz de trabajar, pero puede vivir en casa"),
        KpsItem(40, "Incapacitado", "Requiere atención y asistencia especial.", "Incapaz de cuidarse, requiere hospitalización"),
        KpsItem(30, "Incapacitado severo", "Gravemente incapacitado. Se indica hospitalización, aunque la muerte no es inminente.", "Incapaz de cuidarse, requiere hospitalización"),
        KpsItem(20, "Muy enfermo", "Muy enfermo. Es necesario el tratamiento hospitalario activo.", "Incapaz de cuidarse, requiere hospitalización"),
        KpsItem(10, "Moribundo", "Procesos fatales que progresan rápidamente.", "Incapaz de cuidarse, requiere hospitalización"),
        KpsItem(0, "Fallecido", "Muerte.", "Incapaz de cuidarse, requiere hospitalización")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escala Karnofsky (KPS)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Índice de estado funcional del paciente (0-100%). Evalúa la capacidad de realizar actividades cotidianas y la necesidad de soporte médico.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Grouping by clinical category
            val grouped = kpsOptions.groupBy { it.category }
            grouped.forEach { (category, items) ->
                item {
                    Text(
                        text = category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(items) { opt ->
                    val isSelected = selectedPercent == opt.percent
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPercent = opt.percent }
                            .testTag("kps_percent_${opt.percent}"),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${opt.percent}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(64.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(opt.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(opt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPercent = opt.percent }
                            )
                        }
                    }
                }
            }

            item {
                if (selectedPercent >= 0) {
                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("save_kps_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Karnofsky ($selectedPercent%)")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val matching = kpsOptions.find { it.percent == selectedPercent }
                val resultText = "KPS: $selectedPercent% (${matching?.title})"
                val paramsText = "KPS Percent Selected: $selectedPercent%"
                viewModel.saveCalculation(
                    calculatorType = "Karnofsky",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

data class KpsItem(
    val percent: Int,
    val title: String,
    val description: String,
    val category: String
)

// ----------------------------------------------------
// 7. ESTUDIO SENIC SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenicScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var c1Abdominal by remember { mutableStateOf(false) }
    var c2Duracion by remember { mutableStateOf(false) }
    var c3Contaminada by remember { mutableStateOf(false) }
    var c4Diagnosticos by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    var score = 0
    if (c1Abdominal) score++
    if (c2Duracion) score++
    if (c3Contaminada) score++
    if (c4Diagnosticos) score++

    val infectionRisk = when (score) {
        0 -> 1.0f
        1 -> 3.6f
        2 -> 8.9f
        3 -> 17.2f
        else -> 27.0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estudio SENIC") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Índice SENIC (Study on the Efficacy of Nosocomial Infection Control) para la predicción de riesgo de infección en el sitio quirúrgico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Criterios de Evaluación", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cirugía abdominal o torácica", fontWeight = FontWeight.Bold)
                            Text("Intervención realizada en tórax o abdomen", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = c1Abdominal,
                            onCheckedChange = { c1Abdominal = it },
                            modifier = Modifier.testTag("senic_c1")
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Duración de la intervención > 2 horas", fontWeight = FontWeight.Bold)
                            Text("Cirugía con tiempo operatorio mayor a 120 mins", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = c2Duracion,
                            onCheckedChange = { c2Duracion = it },
                            modifier = Modifier.testTag("senic_c2")
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Herida contaminada o infectada", fontWeight = FontWeight.Bold)
                            Text("Herida clase III (Contaminada) o clase IV (Sucia)", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = c3Contaminada,
                            onCheckedChange = { c3Contaminada = it },
                            modifier = Modifier.testTag("senic_c3")
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("> 2 diagnósticos al alta", fontWeight = FontWeight.Bold)
                            Text("Paciente tiene más de dos códigos diagnósticos al egreso", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = c4Diagnosticos,
                            onCheckedChange = { c4Diagnosticos = it },
                            modifier = Modifier.testTag("senic_c4")
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("PUNTUACIÓN DE FACTORES: $score / 4", fontWeight = FontWeight.Bold)
                    Text(
                        text = "${infectionRisk}%",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Riesgo Estimado de Infección Nosocomial", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_senic_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar SENIC")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Puntos: $score/4 (Riesgo: $infectionRisk%)"
                val paramsText = "Cirugía Abd/Tor: $c1Abdominal, Duración >2h: $c2Duracion, Herida Contaminada: $c3Contaminada, Diags >2: $c4Diagnosticos"
                viewModel.saveCalculation(
                    calculatorType = "Estudio SENIC",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 8. CALCULADORA NNIS/NHSN SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NnisScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var asaFactor by remember { mutableStateOf(false) } // true if ASA 3-5, false if ASA 1-2
    var woundFactor by remember { mutableStateOf(false) } // true if Contaminated/Dirty III/IV, false if Clean/Clean-Contaminated I/II
    var durationFactor by remember { mutableStateOf(false) } // true if > 75th percentile, false if <= 75th percentile
    var showSaveDialog by remember { mutableStateOf(false) }

    var index = 0
    if (asaFactor) index++
    if (woundFactor) index++
    if (durationFactor) index++

    val infectionRisk = when (index) {
        0 -> 1.5f
        1 -> 2.9f
        2 -> 6.8f
        else -> 13.0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora NNIS/NHSN") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Índice de riesgo del National Nosocomial Infections Surveillance (NNIS) / NHSN basado en puntuación ASA, clase de herida y tiempo quirúrgico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Puntuación ASA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ASA es 3, 4 o 5", fontWeight = FontWeight.Bold)
                            Text("Enfermedad sistémica grave, incapacitante o riesgo vital", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = asaFactor,
                            onCheckedChange = { asaFactor = it },
                            modifier = Modifier.testTag("nnis_asa")
                        )
                    }

                    Divider()

                    Text("Clase de Herida", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Herida Contaminada o Sucia", fontWeight = FontWeight.Bold)
                            Text("Clase III o Clase IV (inflamación aguda, pus, trauma)", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = woundFactor,
                            onCheckedChange = { woundFactor = it },
                            modifier = Modifier.testTag("nnis_wound")
                        )
                    }

                    Divider()

                    Text("Duración de la Intervención", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Duración > Percentil 75", fontWeight = FontWeight.Bold)
                            Text("Supera el tiempo de corte estándar para la técnica quirúrgica", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = durationFactor,
                            onCheckedChange = { durationFactor = it },
                            modifier = Modifier.testTag("nnis_duration")
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ÍNDICE DE RIESGO NNIS: $index", fontWeight = FontWeight.Bold)
                    Text(
                        text = "${infectionRisk}%",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Riesgo Estimado de Infección del Sitio Quirúrgico (ISQ)", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_nnis_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar NNIS/NHSN")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Índice: $index/3 (Riesgo ISQ: $infectionRisk%)"
                val paramsText = "ASA >=3: $asaFactor, Herida III/IV: $woundFactor, Duración >P75: $durationFactor"
                viewModel.saveCalculation(
                    calculatorType = "NNIS/NHSN",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
                smartPopBack(navController)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun ClinicalStatsCard(
    records: List<CalculationRecord>
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("clinical_stats_card"),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estadísticas Clínicas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            val total = records.size
            Text(
                text = "Total de cálculos registrados: $total",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            val counts = records.groupBy { it.calculatorType }.mapValues { it.value.size }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                counts.forEach { (type, count) ->
                    val pct = if (total > 0) count.toFloat() / total else 0f
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(type, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("$count (${(pct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = pct,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 9. HISTORY SCREEN (ANONYMOUS RECENT CALCULATIONS)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: UroViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val records by viewModel.records.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historial",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Registros anónimos guardados localmente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (records.isNotEmpty()) {
                IconButton(
                    onClick = {
                        FeedbackHelper.triggerTap(view, viewModel)
                        showClearConfirm = true
                    },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Borrar todo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text("No hay registros", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tus cálculos aparecerán aquí de forma anónima una vez los guardes.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            ClinicalStatsCard(
                records = records
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { record ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                FeedbackHelper.triggerTap(view, viewModel)
                                isExpanded = !isExpanded
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = record.calculatorType,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Text(
                                    text = formatTimestamp(record.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Resultado Principal:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = record.result,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Collapsible detail of clinical input parameters
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Parámetros Clínicos Introducidos:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = record.inputParameters,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (record.caseId != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Caso Asociado ID: #${record.caseId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            FeedbackHelper.triggerReject(view, viewModel)
                                            viewModel.deleteRecord(record.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                        modifier = Modifier.align(Alignment.End),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eliminar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("¿Borrar todo el historial?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es irreversible y eliminará todos los registros de cálculos y casos clínicos almacenados localmente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FeedbackHelper.triggerReject(view, viewModel)
                        viewModel.clearAllHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar Todo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    FeedbackHelper.triggerTap(view, viewModel)
                    showClearConfirm = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ----------------------------------------------------
// 10. CASES SCREENS (LIST & DETAILED ANONYMOUS CASES)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(
    navController: NavController,
    viewModel: UroViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val cases by viewModel.cases.collectAsStateWithLifecycle()
    var newCaseName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Casos Clínicos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gestión de pacientes agrupando cálculos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FloatingActionButton(
                onClick = {
                    FeedbackHelper.triggerTap(view, viewModel)
                    showCreateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp).testTag("create_case_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Caso")
            }
        }

        if (cases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text("No hay casos creados", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Puedes crear un caso clínico anónimo para agrupar los cálculos del paciente y obtener un diagnóstico por IA.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cases) { pCase ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                FeedbackHelper.triggerTap(view, viewModel)
                                viewModel.selectCase(pCase.id)
                                navController.navigate("case_detail")
                            }
                            .testTag("case_item_${pCase.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        ListItem(
                            headlineContent = { Text(pCase.title, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Text(
                                    text = "Creado: ${formatTimestamp(pCase.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.FolderShared,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (pCase.aiSummaryStatus == "SUCCESS") {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Resumen disponible",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        FeedbackHelper.triggerReject(view, viewModel)
                                        viewModel.deleteCase(pCase.id)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuevo Caso Clínico Anónimo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = SpacerWithMargin(8)) {
                    Text("Escriba un identificador para el caso (sin nombres personales):", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newCaseName,
                        onValueChange = { newCaseName = it },
                        placeholder = { Text("Ej: Caso Próstata #1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_case_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        FeedbackHelper.triggerConfirm(view, viewModel)
                        viewModel.createCase(newCaseName)
                        newCaseName = ""
                        showCreateDialog = false
                    },
                    enabled = newCaseName.trim().isNotEmpty()
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    FeedbackHelper.triggerTap(view, viewModel)
                    showCreateDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Helper function to build and trigger native Android sharing for the patient case clinical report
fun shareCaseReport(context: android.content.Context, pCase: PatientCase, records: List<CalculationRecord>) {
    val sb = StringBuilder()
    sb.append("========================================\n")
    sb.append("      UROCÁLCULOS - REPORTE CLÍNICO     \n")
    sb.append("========================================\n")
    sb.append("Identificador: ${pCase.title}\n")
    sb.append("Fecha de Creación: ${formatTimestamp(pCase.timestamp)}\n")
    sb.append("Estado de Privacidad: Datos 100% Anónimos\n")
    sb.append("========================================\n\n")

    sb.append("SÍNTESIS CLÍNICA POR IA:\n")
    if (pCase.aiSummary != null) {
        sb.append(pCase.aiSummary)
    } else {
        sb.append("No se ha generado síntesis por IA aún.")
    }
    sb.append("\n\n----------------------------------------\n")
    sb.append("CÁLCULOS ASOCIADOS (${records.size}):\n\n")

    records.forEachIndexed { index, record ->
        sb.append("${index + 1}. TIPO: ${record.calculatorType}\n")
        sb.append("   Fecha: ${formatTimestamp(record.timestamp)}\n")
        sb.append("   Resultado: ${record.result}\n")
        sb.append("   Parámetros: ${record.inputParameters}\n\n")
    }
    sb.append("========================================\n")
    sb.append("Reporte generado de forma segura en UroCálculos.")

    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Reporte Clínico - ${pCase.title}")
        type = "text/plain"
    }
    val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir Reporte Clínico")
    context.startActivity(shareIntent)
}

// ----------------------------------------------------
// 11. CASE DETAIL VIEW (GROUPED DATA & GEMINI IA SUMMARY)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    val patientCase by viewModel.selectedCase.collectAsStateWithLifecycle()
    val records by viewModel.recordsForSelectedCase.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patientCase?.title ?: "Detalle del Caso") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.selectCase(null)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        patientCase?.let { pCase ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header overview Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(pCase.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Registrado: ${formatTimestamp(pCase.timestamp)}", style = MaterialTheme.typography.bodySmall)
                            Text("Cálculos asociados: ${records.size}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick Action: Add Calculation to Case
                Button(
                    onClick = {
                        navController.navigate("calculadoras") {
                            popUpTo("case_detail") { inclusive = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_calculation_from_case_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("➕ Realizar Cálculo para este Caso", fontWeight = FontWeight.Bold)
                }

                // Export Clinical Report
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("export_report_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Exportar Copia Digital del Caso",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Genera y comparte un reporte detallado con toda la información",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                shareCaseReport(context, pCase, records)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Exportar", fontSize = 13.sp)
                        }
                    }
                }

                // Linked Calculations List
                Text(
                    text = "Cálculos Médicos Guardados en este Caso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (records.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay cálculos guardados para este caso clínico. Haz nuevos cálculos (IMC, Hidratación, IPSS, etc.) y guárdalos seleccionando este caso.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    records.forEach { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = record.calculatorType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = formatTimestamp(record.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = record.result,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Parámetros: ${record.inputParameters}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando caso clínico...")
            }
        }
    }
}

// ----------------------------------------------------
// 12. PSA SCREEN (PSA & PSAD)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsaScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var psaTotalStr by remember { mutableStateOf("") }
    var psaLibreStr by remember { mutableStateOf("") }
    
    // Volume direct input or calculation
    var inputModeByVolume by remember { mutableStateOf(true) } // true = Vol directo, false = Ultrasound dimensions
    var volProstataStr by remember { mutableStateOf("") }
    var largoStr by remember { mutableStateOf("") }
    var anchoStr by remember { mutableStateOf("") }
    var altoStr by remember { mutableStateOf("") }

    var showSaveDialog by remember { mutableStateOf(false) }

    val psaTotal = psaTotalStr.toFloatOrNull() ?: 0f
    val psaLibre = psaLibreStr.toFloatOrNull() ?: 0f

    val volProstata = if (inputModeByVolume) {
        volProstataStr.toFloatOrNull() ?: 0f
    } else {
        val largo = largoStr.toFloatOrNull() ?: 0f
        val ancho = anchoStr.toFloatOrNull() ?: 0f
        val alto = altoStr.toFloatOrNull() ?: 0f
        if (largo > 0f && ancho > 0f && alto > 0f) {
            largo * ancho * alto * 0.52f
        } else {
            0f
        }
    }

    // Densidad de PSA (PSAD)
    val psad = if (psaTotal > 0f && volProstata > 0f) psaTotal / volProstata else 0f

    // Relación Libre/Total
    val relacionPsa = if (psaTotal > 0f && psaLibre > 0f) (psaLibre / psaTotal) * 100f else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PSA y Densidad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Evaluación del Antígeno Prostático Específico (PSA), cálculo de la densidad de PSA (PSAD) y estimación del cociente Libre/Total para el despistaje de cáncer de próstata vs. hiperplasia benigna (HBP).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Antígeno Prostático Específico (PSA)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = psaTotalStr,
                        onValueChange = { psaTotalStr = it },
                        label = { Text("PSA Total (ng/mL)") },
                        placeholder = { Text("Ej: 6.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("psa_total_input"),
                        trailingIcon = { Icon(Icons.Default.Science, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = psaLibreStr,
                        onValueChange = { psaLibreStr = it },
                        label = { Text("PSA Libre (ng/mL) [Opcional]") },
                        placeholder = { Text("Ej: 0.78") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("psa_libre_input")
                    )
                }
            }

            // Volume Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Volumen de la Próstata", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    TabRow(
                        selectedTabIndex = if (inputModeByVolume) 0 else 1,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (inputModeByVolume) 0 else 1]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(
                            selected = inputModeByVolume,
                            onClick = { inputModeByVolume = true },
                            text = { Text("Volumen Directo", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = !inputModeByVolume,
                            onClick = { inputModeByVolume = false },
                            text = { Text("Cálculo por Ecografía", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (inputModeByVolume) {
                        OutlinedTextField(
                            value = volProstataStr,
                            onValueChange = { volProstataStr = it },
                            label = { Text("Volumen Prostático (cc / mL / cm³)") },
                            placeholder = { Text("Ej: 45") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("psa_volume_input"),
                            trailingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                        )
                    } else {
                        Text(
                            text = "Fórmula elipsoide prostática: Largo (cm) × Ancho (cm) × Alto (cm) × 0.52",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = largoStr,
                                onValueChange = { largoStr = it },
                                label = { Text("Largo (cm)") },
                                placeholder = { Text("3.5") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = anchoStr,
                                onValueChange = { anchoStr = it },
                                label = { Text("Ancho (cm)") },
                                placeholder = { Text("4.0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = altoStr,
                                onValueChange = { altoStr = it },
                                label = { Text("Alto (cm)") },
                                placeholder = { Text("3.0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (volProstata > 0f) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Volumen Estimado Calculado:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text("${String.format("%.1f", volProstata)} cc (mL)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Results Card
            if (psaTotal > 0f && volProstata > 0f) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "ESTRATIFICACIÓN CLÍNICA Y RESULTADOS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Densidad de PSA (PSAD):")
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.3f", psad)} ng/mL/cc",
                                fontWeight = FontWeight.Bold,
                                color = if (psad >= 0.15f) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                            )
                        }

                        // Clinical Interpretation of PSAD
                        val (psadLabel, psadColor) = if (psad >= 0.15f) {
                            "Densidad elevada (≥ 0.15 ng/mL/cc): Mayor sospecha de neoplasia prostática. Se sugiere correlacionar con tacto rectal y considerar biopsia." to MaterialTheme.colorScheme.error
                        } else {
                            "Densidad normal (< 0.15 ng/mL/cc): Consistente habitualmente con HBP (Hiperplasia Benigna)." to Color(0xFF2E7D32)
                        }

                        Text(
                            text = psadLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = psadColor
                        )

                        if (relacionPsa > 0f) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cociente PSA Libre/Total:")
                                Text(
                                    text = "${String.format("%.1f", relacionPsa)} %",
                                    fontWeight = FontWeight.Bold,
                                    color = if (psaTotal >= 4.0f && psaTotal <= 10.0f) {
                                        if (relacionPsa < 15f) MaterialTheme.colorScheme.error else if (relacionPsa <= 25f) Color(0xFFE65100) else Color(0xFF2E7D32)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            // Interpretation of free/total PSA, especially in grey zone (4-10)
                            if (psaTotal in 4.0f..10.0f) {
                                val (relLabel, relColor) = when {
                                    relacionPsa < 15f -> "Zona Gris (4-10 ng/mL) con Cociente < 15%: Alto riesgo de malignidad. Muy sugerente de neoplasia." to MaterialTheme.colorScheme.error
                                    relacionPsa <= 25f -> "Zona Gris (4-10 ng/mL) con Cociente 15-25%: Riesgo intermedio. Correlacionar clínicamente." to Color(0xFFE65100)
                                    else -> "Zona Gris (4-10 ng/mL) con Cociente > 25%: Bajo riesgo de malignidad. Mayor sospecha de proceso benigno (HBP, prostatitis)." to Color(0xFF2E7D32)
                                }
                                Text(
                                    text = relLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = relColor
                                )
                            } else {
                                Text(
                                    text = "Nota: La relación PSA Libre/Total es de máxima utilidad diagnóstica en la 'zona gris' de PSA total (entre 4.0 y 10.0 ng/mL).",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("save_psa_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Cálculo de PSA")
                        }
                    }
                }
            }

            // Guidance info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Valores Diagnósticos de Referencia:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("• PSA Total Normal: Generalmente < 4.0 ng/mL (aunque varía por edad).", style = MaterialTheme.typography.bodySmall)
                    Text("• Densidad de PSA (PSAD): El punto de corte clásico de 0.15 ayuda a discernir si el aumento de PSA se justifica por el tamaño glandular (benigno) o por sospecha de tumor maligno.", style = MaterialTheme.typography.bodySmall)
                    Text("• Cociente Libre/Total: Un cociente bajo (< 15%) indica que la mayor parte del PSA está ligado a proteínas, lo cual se asocia estadísticamente con mayor probabilidad de malignidad prostática.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "PSAD: ${String.format(java.util.Locale.US, "%.3f", psad)} ng/mL/cc, Vol: ${volProstata.toInt()} cc" +
                        if (relacionPsa > 0f) ", Relación L/T: ${String.format("%.1f", relacionPsa)}%" else ""
                val paramsText = "PSA: $psaTotal ng/mL, Vol: ${volProstata.toInt()} cc" +
                        if (psaLibre > 0f) ", PSA L: $psaLibre ng/mL" else ""
                
                viewModel.saveCalculation(
                    calculatorType = "PSA y Densidad",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 13. CREATININA SCREEN (COCKCROFT-GAULT)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatininaScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var edadStr by remember { mutableStateOf("") }
    var pesoStr by remember { mutableStateOf("") }
    var creatininaStr by remember { mutableStateOf("") }
    var esMujer by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }

    val edad = edadStr.toFloatOrNull() ?: 0f
    val peso = pesoStr.toFloatOrNull() ?: 0f
    val creatinina = creatininaStr.toFloatOrNull() ?: 0f

    // Cockcroft-Gault formula
    val clcr = if (edad > 0f && peso > 0f && creatinina > 0f) {
        val base = ((140f - edad) * peso) / (72f * creatinina)
        if (esMujer) base * 0.85f else base
    } else {
        0f
    }

    // KDIGO staging based on ClCr
    val (kdigoStage, stageColor, stageDesc) = when {
        clcr <= 0f -> Triple("", Color.Gray, "")
        clcr >= 90f -> Triple("Grado G1", Color(0xFF2E7D32), "Función renal normal o alta.")
        clcr >= 60f -> Triple("Grado G2", Color(0xFF4CAF50), "Disminución ligera de la filtración glomerular.")
        clcr >= 45f -> Triple("Grado G3a", Color(0xFFFFA000), "Disminución ligera a moderada.")
        clcr >= 30f -> Triple("Grado G3b", Color(0xFFF57C00), "Disminución moderada a grave.")
        clcr >= 15f -> Triple("Grado G4", Color(0xFFD32F2F), "Disminución grave de la función renal.")
        else -> Triple("Grado G5", Color(0xFFC62828), "Fallo renal establecido. Precaución extrema con fármacos nefrotóxicos.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Depuración de Creatinina") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Estimación rápida de la depuración de creatinina (Fórmula de Cockcroft-Gault). Indispensable en urología para ajustar dosis de antibióticos y quimioterápicos nefrotóxicos (p. ej., Cisplatino).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Datos Biométricos del Paciente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = edadStr,
                        onValueChange = { edadStr = it },
                        label = { Text("Edad (Años)") },
                        placeholder = { Text("Ej: 68") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("creatinina_age_input"),
                        trailingIcon = { Icon(Icons.Default.Cake, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = pesoStr,
                        onValueChange = { pesoStr = it },
                        label = { Text("Peso Corporal (kg)") },
                        placeholder = { Text("Ej: 75") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("creatinina_weight_input"),
                        trailingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = creatininaStr,
                        onValueChange = { creatininaStr = it },
                        label = { Text("Creatinina Sérica (mg/dL)") },
                        placeholder = { Text("Ej: 1.2") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("creatinina_value_input"),
                        trailingIcon = { Icon(Icons.Default.Assessment, contentDescription = null) }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { esMujer = !esMujer }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = esMujer,
                            onClick = { esMujer = true }
                        )
                        Text("Femenino (Multiplica por 0.85)", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(24.dp))
                        RadioButton(
                            selected = !esMujer,
                            onClick = { esMujer = false }
                        )
                        Text("Masculino", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (clcr > 0f) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("DEPURACIÓN DE CREATININA (CG)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${clcr.toInt()} mL/min",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(stageColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$kdigoStage: $stageDesc",
                                color = stageColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("save_creatinina_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Depuración")
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "ClCr CG: ${clcr.toInt()} mL/min ($kdigoStage)"
                val paramsText = "Edad: $edadStr años, Peso: $pesoStr kg, Creatinina: $creatininaStr mg/dL, Sexo: ${if (esMujer) "Femenino" else "Masculino"}"
                
                viewModel.saveCalculation(
                    calculatorType = "Depuración de Creatinina",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// Helper model for Clavien Dindo
data class ClavienGrade(
    val grade: String,
    val title: String,
    val description: String
)

// ----------------------------------------------------
// 14. CLAVIEN-DINDO SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClavienScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var selectedGrade by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val clavienOptions = listOf(
        ClavienGrade(
            "Grado I", 
            "Sin desviación o tratamiento farmacológico avanzado", 
            "Cualquier desviación del curso posoperatorio normal sin necesidad de tratamiento farmacológico complejo o quirúrgico, endoscópico o radiológico. Se permiten antieméticos, antipiréticos, analgésicos, diuréticos y electrólitos."
        ),
        ClavienGrade(
            "Grado II", 
            "Tratamiento farmacológico avanzado", 
            "Requiere tratamiento farmacológico con medicamentos distintos de los permitidos para el Grado I. Incluye nutrición parenteral total (NPT) y transfusiones de sangre o hemoderivados."
        ),
        ClavienGrade(
            "Grado IIIa", 
            "Intervención sin anestesia general", 
            "Requiere intervención quirúrgica, endoscópica o de radiología intervencionista, pero realizada bajo anestesia local o regional, SIN necesidad de anestesia general."
        ),
        ClavienGrade(
            "Grado IIIb", 
            "Intervención con anestesia general", 
            "Requiere intervención quirúrgica, endoscópica o de radiología intervencionista, realizada obligatoriamente bajo anestesia general."
        ),
        ClavienGrade(
            "Grado IVa", 
            "Disfunción de órgano único en UCI", 
            "Complicación potencialmente mortal que requiere ingreso en Cuidados Intensivos (UCI) con disfunción o fallo de un solo órgano (por ejemplo, necesidad de diálisis por fallo renal agudo)."
        ),
        ClavienGrade(
            "Grado IVb", 
            "Disfunción multiorgánica en UCI", 
            "Complicación potencialmente mortal en UCI con disfunción o fallo de múltiples órganos simultáneamente."
        ),
        ClavienGrade(
            "Grado V", 
            "Fallecimiento del paciente", 
            "Muerte del paciente a consecuencia de las complicaciones quirúrgicas."
        )
    )

    val currentSelection = clavienOptions.find { it.grade == selectedGrade }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clasificación Clavien-Dindo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Estratificación estándar internacional para reportar y clasificar complicaciones posoperatorias basándose en la terapia requerida para tratarlas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            clavienOptions.forEach { opt ->
                val isSelected = selectedGrade == opt.grade
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGrade = opt.grade }
                        .testTag("clavien_grade_${opt.grade.replace(" ", "_")}"),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedGrade = opt.grade },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = opt.grade,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = opt.title,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = opt.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (selectedGrade.isNotEmpty() && currentSelection != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "COMPLICACIÓN REGISTRADA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${currentSelection.grade}: ${currentSelection.title}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("save_clavien_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Complicación")
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog && currentSelection != null) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Complicación: ${currentSelection.grade} (${currentSelection.title})"
                val paramsText = "Estratificación de curso quirúrgico posoperatorio"
                
                viewModel.saveCalculation(
                    calculatorType = "Clasificación Clavien-Dindo",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// Helper for Theme Options
data class ThemeOption(
    val id: String,
    val name: String,
    val color: Color,
    val description: String
)

@Composable
fun ThemeSelectionDialog(
    onDismiss: () -> Unit,
    viewModel: UroViewModel
) {
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val themes = listOf(
        ThemeOption("BLUE", "Azul Clínico", Color(0xFF0096C7), "Clásico, profesional y relajante."),
        ThemeOption("GREEN", "Verde Quirúrgico", Color(0xFF0A7E64), "Tono verde que reduce la fatiga visual."),
        ThemeOption("PURPLE", "Púrpura Oncológico", Color(0xFF6F35A5), "Color distinguido para oncología."),
        ThemeOption("DARK", "Modo Carbón", Color(0xFF2B2D31), "Interfaz oscura de alto contraste.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Tema Visual",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                themes.forEach { themeOpt ->
                    val isSelected = currentTheme == themeOpt.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTheme(themeOpt.id)
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeOpt.color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(themeOpt.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(themeOpt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setTheme(themeOpt.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}

// ----------------------------------------------------
// 12. CREDITS & CONTACT / ABOUT SCREEN
// ----------------------------------------------------
@Composable
fun AboutScreen(
    viewModel: UroViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "UroCálculos",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Clinical Decision Support Software",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "Versión 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Visual Theme Selector Card
        Text("Personalización del Tema Visual", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Seleccione el color de énfasis para la interfaz de la aplicación según su preferencia o entorno de trabajo:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()

                val themes = listOf(
                    ThemeOption("BLUE", "Azul Clínico", Color(0xFF0096C7), "Paleta clásica, profesional y relajante."),
                    ThemeOption("GREEN", "Verde Quirúrgico", Color(0xFF0A7E64), "Tono verde quirófano que reduce la fatiga visual."),
                    ThemeOption("PURPLE", "Púrpura Oncológico", Color(0xFF6F35A5), "Color distinguido para oncología y urología."),
                    ThemeOption("DARK", "Modo Carbón (Oscuro)", Color(0xFF2B2D31), "Interfaz oscura de alto contraste para guardias nocturnas.")
                )

                themes.forEach { themeOpt ->
                    val isSelected = currentTheme == themeOpt.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                FeedbackHelper.triggerTap(view, viewModel)
                                viewModel.setTheme(themeOpt.id)
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored Circle Indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(themeOpt.color)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = themeOpt.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = themeOpt.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                FeedbackHelper.triggerTap(view, viewModel)
                                viewModel.setTheme(themeOpt.id)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Retroalimentación y Efectos (Sounds & Vibration)
        Text("Efectos y Retroalimentación", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("feedback_settings_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
                val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Efectos de Sonido",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Emitir un breve sonido al pulsar botones o realizar cálculos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            viewModel.setSoundEnabled(it)
                            FeedbackHelper.triggerTap(view, viewModel)
                        },
                        modifier = Modifier.testTag("sound_feedback_switch")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Respuesta Háptica",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Vibración táctil sutil al presionar elementos interactivos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            viewModel.setHapticEnabled(it)
                            FeedbackHelper.triggerTap(view, viewModel)
                        },
                        modifier = Modifier.testTag("haptic_feedback_switch")
                    )
                }
            }
        }

        // Tamaño de Fuente (Font Size Selection)
        Text("Tamaño de la Fuente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("font_size_settings_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentScale by viewModel.fontScale.collectAsStateWithLifecycle()
                
                Text(
                    text = "Ajuste el tamaño del texto para mejorar la legibilidad. La interfaz se adaptará de forma armónica para evitar desbordamientos o desconfiguraciones estéticas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val scales = listOf(
                    Triple(0.88f, "Pequeño", "Chic"),
                    Triple(1.0f, "Normal", "Estándar"),
                    Triple(1.12f, "Mediano", "Intermedio"),
                    Triple(1.25f, "Grande", "Accesible")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scales.forEach { (scaleVal, name, desc) ->
                        val isSelected = Math.abs(currentScale - scaleVal) < 0.01f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable {
                                    FeedbackHelper.triggerTap(view, viewModel)
                                    viewModel.setFontScale(scaleVal)
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (12 * scaleVal).sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Vista Previa de Lectura",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "UroCálculos adapta la densidad visual de cada sección automáticamente. Este es un ejemplo de texto clínico para verificar la visualización.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Donations / Support Card
        Text("Apoyo y Donaciones", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("donations_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Donaciones de Forma Desinteresada",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "UroCálculos es un proyecto completamente gratuito, desarrollado con el propósito de ofrecer a los especialistas una herramienta de alta calidad y libre de publicidad en su práctica clínica diaria.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )

                Text(
                    text = "Todas las funciones avanzadas de estadísticas, análisis locales y exportación de reportes clínicos están completamente disponibles de forma libre y gratuita para todos los profesionales.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )

                Text(
                    text = "Si la aplicación le resulta de gran valor para su desempeño clínico y desea apoyar de forma voluntaria e incondicional el sostenimiento de este proyecto, su desarrollo continuo y futuras actualizaciones, puede colaborar directamente poniéndose en contacto:",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Column(verticalArrangement = SpacerWithMargin(6)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Correo: reynelbusto@gmail.com", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Teléfono/WhatsApp: +5353466832", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // About the software purpose
        Text("Propósito del Software", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "UroCálculos es una herramienta médica avanzada de soporte a la toma de decisiones clínicas específicamente diseñada para cirujanos y especialistas en urología.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                Text(
                    text = "La aplicación realiza cálculos precisos de escalas preoperatorias, estado funcional e índices de riesgo nosocomiales científicamente validados. En total respeto a los principios de privacidad médica internacional, no se recopila información personal identificable (PII) de ningún paciente. Toda la información introducida es de carácter estrictamente clínico y permanece únicamente en la memoria local y segura del dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dr. Reynel Busto Gonzalez Credits Card
        Text("Desarrollo y Créditos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dr. Reynel Busto Gonzalez",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Especialista en Urología. Creador de UroCálculos.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Contacto Médico Directo: +5353466832",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Apoya Futuros Proyectos & Donaciones:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Si deseas colaborar con el Dr. Reynel Busto Gonzalez para el financiamiento y optimización de herramientas tecnológicas para el sector salud, puedes ponerte en contacto a través de sus redes oficiales:",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = SpacerWithMargin(4)) {
                    Text("• Facebook: Reynel Busto Gonzalez", fontWeight = FontWeight.Bold)
                    Text("• Instagram: @reynelbusto", fontWeight = FontWeight.Bold)
                    Text("• YouTube: urologia fácil", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Disclaimer warning card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Advertencia",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "Advertencia de Responsabilidad Médica",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Los cálculos y pautas arrojados por esta app son herramientas auxiliares de orientación para el profesional de la salud. De ninguna manera sustituyen el criterio clínico individual, la anamnesis completa ni la valoración de un equipo médico calificado.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 15. STONE SCREEN (STONE SCORE)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoneScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var esHombre by remember { mutableStateOf(true) }
    var duracionDolor by remember { mutableStateOf(2) } // 2 = < 6h, 1 = 6-24h, 0 = > 24h
    var esNoNegro by remember { mutableStateOf(true) } // true = No-negro, false = Negro
    var nauseaVomito by remember { mutableStateOf(2) } // 2 = Vómito, 1 = Náuseas, 0 = Ninguno
    var hematuria by remember { mutableStateOf(true) } // true = Presente, false = Ausente

    var showSaveDialog by remember { mutableStateOf(false) }

    // Calculating score
    val sexPoints = if (esHombre) 2 else 0
    val durationPoints = when (duracionDolor) {
        2 -> 2
        1 -> 1
        else -> 0
    }
    val racePoints = if (esNoNegro) 3 else 0
    val nauseaPoints = when (nauseaVomito) {
        2 -> 2
        1 -> 1
        else -> 0
    }
    val hematuriaPoints = if (hematuria) 3 else 0

    val totalScore = sexPoints + durationPoints + racePoints + nauseaPoints + hematuriaPoints

    val (riskLabel, riskColor, riskProb) = when {
        totalScore <= 5 -> Triple("Bajo Riesgo", Color(0xFF2E7D32), "9.2% de probabilidad de cálculo ureteral")
        totalScore <= 9 -> Triple("Riesgo Moderado", Color(0xFFFFA000), "51.3% de probabilidad de cálculo ureteral")
        else -> Triple("Alto Riesgo", Color(0xFFD32F2F), "89.6% de probabilidad de cálculo ureteral")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escala STONE") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "La Escala STONE estratifica el riesgo en pacientes que acuden a urgencias con dolor en flanco, estimando la probabilidad de que los síntomas sean causados por un cálculo ureteral no complicado, ayudando a guiar la indicación de tomografía (UroTAC).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Sexo
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sexo Biológico", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = esHombre,
                            onClick = { esHombre = true },
                            label = { Text("Masc. (+2 pts)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !esHombre,
                            onClick = { esHombre = false },
                            label = { Text("Fem. (0 pts)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Duración del Dolor
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Duración del dolor", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = duracionDolor == 2,
                            onClick = { duracionDolor = 2 },
                            label = { Text("< 6h (+2)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = duracionDolor == 1,
                            onClick = { duracionDolor = 1 },
                            label = { Text("6-24h (+1)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = duracionDolor == 0,
                            onClick = { duracionDolor = 0 },
                            label = { Text("> 24h (0)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Origen/Raza (Origin)
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Origen étnico (Estudio original)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = esNoNegro,
                            onClick = { esNoNegro = true },
                            label = { Text("No Afro (+3 pts)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !esNoNegro,
                            onClick = { esNoNegro = false },
                            label = { Text("Afro (0 pts)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Náuseas / Vómitos
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Síntomas Vegetativos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = nauseaVomito == 2,
                            onClick = { nauseaVomito = 2 },
                            label = { Text("Vómito (+2)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = nauseaVomito == 1,
                            onClick = { nauseaVomito = 1 },
                            label = { Text("Náusea (+1)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = nauseaVomito == 0,
                            onClick = { nauseaVomito = 0 },
                            label = { Text("Ninguno (0)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Hematuria (Eritrocitos en orina)
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hematuria (Micro/Macroscópica)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = hematuria,
                            onClick = { hematuria = true },
                            label = { Text("Presente (+3 pts)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !hematuria,
                            onClick = { hematuria = false },
                            label = { Text("Ausente (0 pts)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("PUNTUACIÓN TOTAL STONE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$totalScore / 13",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = riskLabel,
                                color = riskColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = riskProb,
                                color = riskColor.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_stone_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cálculo STONE")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "STONE Score: $totalScore/13 ($riskLabel, $riskProb)"
                val paramsText = "Sexo: ${if (esHombre) "M" else "F"}, Duración: ${if (duracionDolor==2) "<6h" else if (duracionDolor==1) "6-24h" else ">24h"}, Etnicidad: ${if (esNoNegro) "No Afro" else "Afro"}, Veg: ${if (nauseaVomito==2) "Vómito" else if (nauseaVomito==1) "Náusea" else "No"}, Hematuria: ${if (hematuria) "Sí" else "No"}"
                
                viewModel.saveCalculation(
                    calculatorType = "Escala STONE",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 16. SHIM SCREEN (IIEF-5 - SEXUAL HEALTH INVENTORY FOR MEN)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShimScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var q1 by remember { mutableStateOf(5) }
    var q2 by remember { mutableStateOf(5) }
    var q3 by remember { mutableStateOf(5) }
    var q4 by remember { mutableStateOf(5) }
    var q5 by remember { mutableStateOf(5) }

    var showSaveDialog by remember { mutableStateOf(false) }

    val totalScore = q1 + q2 + q3 + q4 + q5

    val (edLabel, edColor) = when {
        totalScore >= 22 -> "Sin Disfunción Eréctil (Normal)" to Color(0xFF2E7D32)
        totalScore >= 17 -> "Disfunción Eréctil Leve" to Color(0xFF81C784)
        totalScore >= 12 -> "Disfunción Eréctil Leve a Moderada" to Color(0xFFFFA000)
        totalScore >= 8 -> "Disfunción Eréctil Moderada" to Color(0xFFF57C00)
        else -> "Disfunción Eréctil Grave" to Color(0xFFD32F2F)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escala SHIM (IIEF-5)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "El Cuestionario SHIM (Sexual Health Inventory for Men / IIEF-5) evalúa de forma rápida y reproducible la salud sexual masculina y clasifica la severidad de la disfunción eréctil en los últimos 6 meses.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Question 1
            QuestionCard(
                title = "1. Confianza",
                question = "¿Cómo califica su confianza en poder lograr y mantener una erección?",
                options = listOf(
                    "Muy baja" to 1,
                    "Baja" to 2,
                    "Moderada" to 3,
                    "Alta" to 4,
                    "Muy alta" to 5
                ),
                selectedValue = q1,
                onSelected = { q1 = it }
            )

            // Question 2
            QuestionCard(
                title = "2. Rigidez",
                question = "Cuando tuvo erecciones con estimulación sexual, ¿con qué frecuencia fueron lo suficientemente rígidas para la penetración?",
                options = listOf(
                    "Casi nunca o nunca" to 1,
                    "Pocas veces (mucho menos de la mitad)" to 2,
                    "A veces (aproximadamente la mitad)" to 3,
                    "La mayoría de las veces (más de la mitad)" to 4,
                    "Casi siempre o siempre" to 5
                ),
                selectedValue = q2,
                onSelected = { q2 = it }
            )

            // Question 3
            QuestionCard(
                title = "3. Mantenimiento",
                question = "Durante el coito, ¿con qué frecuencia pudo mantener la erección después de haber penetrado?",
                options = listOf(
                    "Casi nunca o nunca" to 1,
                    "Pocas veces (mucho menos de la mitad)" to 2,
                    "A veces (aproximadamente la mitad)" to 3,
                    "La mayoría de las veces (más de la mitad)" to 4,
                    "Casi siempre o siempre" to 5
                ),
                selectedValue = q3,
                onSelected = { q3 = it }
            )

            // Question 4
            QuestionCard(
                title = "4. Dificultad",
                question = "Durante el coito, ¿qué grado de dificultad tuvo para mantener la erección hasta finalizar el acto?",
                options = listOf(
                    "Extremadamente difícil" to 1,
                    "Muy difícil" to 2,
                    "Difícil" to 3,
                    "Ligeramente difícil" to 4,
                    "No fue difícil" to 5
                ),
                selectedValue = q4,
                onSelected = { q4 = it }
            )

            // Question 5
            QuestionCard(
                title = "5. Satisfacción",
                question = "Cuando intentó el coito, ¿con qué frecuencia fue satisfactorio para usted?",
                options = listOf(
                    "Casi nunca o nunca" to 1,
                    "Pocas veces (mucho menos de la mitad)" to 2,
                    "A veces (aproximadamente la mitad)" to 3,
                    "La mayoría de las veces (más de la mitad)" to 4,
                    "Casi siempre o siempre" to 5
                ),
                selectedValue = q5,
                onSelected = { q5 = it }
            )

            // Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ÍNDICE SHIM TOTAL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$totalScore / 25",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(edColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = edLabel,
                            color = edColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_shim_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado SHIM")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Puntaje SHIM: $totalScore/25 ($edLabel)"
                val paramsText = "Respuestas SHIM: Q1=$q1, Q2=$q2, Q3=$q3, Q4=$q4, Q5=$q5"
                
                viewModel.saveCalculation(
                    calculatorType = "Escala SHIM (IIEF-5)",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun QuestionCard(
    title: String,
    question: String,
    options: List<Pair<String, Int>>,
    selectedValue: Int,
    onSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
            Text(question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { (text, valInt) ->
                    val isSelected = selectedValue == valInt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(valInt) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelected(valInt) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$text ($valInt pts)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 17. NIH-CPSI SCREEN (CHRONIC PROSTATITIS SYMPTOM INDEX)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NihCpsiScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    // Pain questions
    var p1Perineo by remember { mutableStateOf(false) } // tip: +1 pt if yes
    var p1Testiculos by remember { mutableStateOf(false) } // tip: +1 pt if yes
    var p1Glande by remember { mutableStateOf(false) } // tip: +1 pt if yes
    var p1Pubis by remember { mutableStateOf(false) } // tip: +1 pt if yes
    
    var p2Orinar by remember { mutableStateOf(false) } // tip: +1 pt if yes
    var p2Eyacular by remember { mutableStateOf(false) } // tip: +1 pt if yes

    var p3Frecuencia by remember { mutableStateOf(0) } // 0 to 5 pts
    var p4Intensidad by remember { mutableStateOf(0) } // 0 to 10 pts

    // Urinary questions
    var u5Frecuencia by remember { mutableStateOf(0) } // 0 to 5 pts
    var u6Nicturia by remember { mutableStateOf(0) } // 0 to 5 pts

    // Quality of life
    var ql7Impacto by remember { mutableStateOf(0) } // 0 to 3 pts
    var ql8Preocupacion by remember { mutableStateOf(0) } // 0 to 3 pts
    var ql9CalidadVida by remember { mutableStateOf(0) } // 0 to 6 pts

    var showSaveDialog by remember { mutableStateOf(false) }

    // Scoring calculation
    val painScore = (if (p1Perineo) 1 else 0) + 
                    (if (p1Testiculos) 1 else 0) + 
                    (if (p1Glande) 1 else 0) + 
                    (if (p1Pubis) 1 else 0) + 
                    (if (p2Orinar) 1 else 0) + 
                    (if (p2Eyacular) 1 else 0) + 
                    p3Frecuencia + 
                    p4Intensidad // Max pain = 21

    val urinaryScore = u5Frecuencia + u6Nicturia // Max urinary = 10
    val qolScore = ql7Impacto + ql8Preocupacion + ql9CalidadVida // Max QOL = 12
    val totalScore = painScore + urinaryScore + qolScore // Max = 43

    val (severityLabel, severityColor) = when {
        totalScore <= 9 -> "Sintomatología Leve" to Color(0xFF2E7D32)
        totalScore <= 18 -> "Sintomatología Moderada" to Color(0xFFFFA000)
        else -> "Sintomatología Grave" to Color(0xFFD32F2F)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Índice NIH-CPSI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "El Índice de Síntomas de Prostatitis Crónica del National Institutes of Health (NIH-CPSI) es el cuestionario clínico estándar de oro para cuantificar los síntomas y el impacto en la calidad de vida en pacientes con sospecha o diagnóstico de prostatitis crónica o síndrome de dolor pélvico crónico (SDPC).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // DOMAIN I: PAIN
            Text("Dominio I: Dolor o Molestia (Máx. 21 pts)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. ¿Ha tenido dolor o molestia en las siguientes zonas durante la última semana?", fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p1Perineo = !p1Perineo }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p1Perineo, onCheckedChange = { p1Perineo = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perineo (entre el ano y los testículos) [+1]", modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p1Testiculos = !p1Testiculos }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p1Testiculos, onCheckedChange = { p1Testiculos = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testículos [+1]", modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p1Glande = !p1Glande }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p1Glande, onCheckedChange = { p1Glande = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Punta del pene (glande) [+1]", modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p1Pubis = !p1Pubis }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p1Pubis, onCheckedChange = { p1Pubis = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Región pubiana o suprapúbica [+1]", modifier = Modifier.weight(1f))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. ¿Ha tenido dolor o molestia al realizar estas actividades?", fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p2Orinar = !p2Orinar }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p2Orinar, onCheckedChange = { p2Orinar = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Al orinar o inmediatamente después [+1]", modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p2Eyacular = !p2Eyacular }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p2Eyacular, onCheckedChange = { p2Eyacular = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Al eyacular o después de las relaciones [+1]", modifier = Modifier.weight(1f))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Frecuencia del dolor o molestia", fontWeight = FontWeight.Bold)
                    
                    val freqOptions = listOf(
                        "Nunca" to 0,
                        "Raras veces" to 1,
                        "Algunas veces" to 2,
                        "A menudo" to 3,
                        "Casi siempre" to 4,
                        "Siempre" to 5
                    )
                    
                    freqOptions.forEach { (text, pts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { p3Frecuencia = pts }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = p3Frecuencia == pts, onClick = { p3Frecuencia = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$text ($pts pts)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("4. Intensidad promedio del dolor", fontWeight = FontWeight.Bold)
                    Text("Escala analógica visual de 0 (sin dolor) a 10 (el peor dolor imaginable)", style = MaterialTheme.typography.bodySmall)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Dolor: $p4Intensidad", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Slider(
                            value = p4Intensidad.toFloat(),
                            onValueChange = { p4Intensidad = it.toInt() },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // DOMAIN II: URINARY SYMPTOMS
            Text("Dominio II: Síntomas Urinarios (Máx. 10 pts)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("5. Sensación de vaciado incompleto", fontWeight = FontWeight.Bold)
                    Text("¿Con qué frecuencia ha tenido la sensación de no vaciar la vejiga por completo?", style = MaterialTheme.typography.bodySmall)
                    
                    val vaciadoOptions = listOf(
                        "Nunca" to 0,
                        "Menos de 1 de cada 5 veces" to 1,
                        "Menos de la mitad de las veces" to 2,
                        "La mitad de las veces" to 3,
                        "Más de la mitad de las veces" to 4,
                        "Casi siempre" to 5
                    )
                    
                    vaciadoOptions.forEach { (text, pts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { u5Frecuencia = pts }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = u5Frecuencia == pts, onClick = { u5Frecuencia = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$text ($pts pts)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("6. Poliaquiuria (Frecuencia urinaria)", fontWeight = FontWeight.Bold)
                    Text("¿Con qué frecuencia ha tenido que orinar de nuevo antes de transcurrir 2 horas?", style = MaterialTheme.typography.bodySmall)
                    
                    val freqUrinarOptions = listOf(
                        "Nunca" to 0,
                        "Menos de 1 de cada 5 veces" to 1,
                        "Menos de la mitad de las veces" to 2,
                        "La mitad de las veces" to 3,
                        "Más de la mitad de las veces" to 4,
                        "Casi siempre" to 5
                    )
                    
                    freqUrinarOptions.forEach { (text, pts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { u6Nicturia = pts }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = u6Nicturia == pts, onClick = { u6Nicturia = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$text ($pts pts)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // DOMAIN III: QUALITY OF LIFE
            Text("Dominio III: Impacto en Calidad de Vida (Máx. 12 pts)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("7. Limitación en actividades cotidianas", fontWeight = FontWeight.Bold)
                    Text("¿Cuánto han evitado sus síntomas que realice sus actividades normales?", style = MaterialTheme.typography.bodySmall)
                    
                    val normalOptions = listOf(
                        "Nada" to 0,
                        "Un poco" to 1,
                        "Moderadamente" to 2,
                        "Mucho" to 3
                    )
                    
                    normalOptions.forEach { (text, pts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ql7Impacto = pts }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = ql7Impacto == pts, onClick = { ql7Impacto = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$text ($pts pts)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("8. Preocupación por los síntomas", fontWeight = FontWeight.Bold)
                    Text("¿Cuánto ha pensado en sus síntomas durante la última semana?", style = MaterialTheme.typography.bodySmall)
                    
                    val thinkOptions = listOf(
                        "Nada" to 0,
                        "Un poco" to 1,
                        "Moderadamente" to 2,
                        "Mucho" to 3
                    )
                    
                    thinkOptions.forEach { (text, pts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ql8Preocupacion = pts }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = ql8Preocupacion == pts, onClick = { ql8Preocupacion = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$text ($pts pts)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("9. Calidad de vida percibida", fontWeight = FontWeight.Bold)
                    Text("Si tuviera que pasar el resto de su vida con los síntomas tal como los ha tenido esta última semana, ¿cómo se sentiría?", style = MaterialTheme.typography.bodySmall)
                    
                    val qolOptions = listOf(
                        "Muy bien" to 0,
                        "Bien" to 1,
                        "Satisfecho en general" to 2,
                        "Satisfecho e insatisfecho por igual" to 3,
                        "Insatisfecho en general" to 4,
                        "Mal" to 5,
                        "Terrible" to 6
                    )
                    
                    qolOptions.forEach { (text, pts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ql9CalidadVida = pts }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = ql9CalidadVida == pts, onClick = { ql9CalidadVida = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$text ($pts pts)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("PUNTUACIÓN NIH-CPSI TOTAL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$totalScore / 43",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = severityLabel,
                            color = severityColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Domain Breakdown
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dolor/Molestia (Dominio I):", style = MaterialTheme.typography.bodySmall)
                            Text("$painScore / 21", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Síntomas Urinarios (Dominio II):", style = MaterialTheme.typography.bodySmall)
                            Text("$urinaryScore / 10", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Impacto en Calidad de Vida (Dominio III):", style = MaterialTheme.typography.bodySmall)
                            Text("$qolScore / 12", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_nih_cpsi_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado NIH-CPSI")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "NIH-CPSI: $totalScore/43 ($severityLabel) [D1:$painScore, D2:$urinaryScore, D3:$qolScore]"
                val paramsText = "Pain: $painScore, Urinary: $urinaryScore, QoL: $qolScore"
                
                viewModel.saveCalculation(
                    calculatorType = "Índice NIH-CPSI",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 18. GLEASON / ISUP SCORE SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GleasonScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var primaryPattern by remember { mutableStateOf(3) }
    var secondaryPattern by remember { mutableStateOf(3) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val totalScore = primaryPattern + secondaryPattern

    // ISUP Grade Group dynamic calculation
    val isupGrade = when {
        primaryPattern == 3 && secondaryPattern == 3 -> 1
        primaryPattern == 3 && secondaryPattern == 4 -> 2
        primaryPattern == 4 && secondaryPattern == 3 -> 3
        (primaryPattern == 4 && secondaryPattern == 4) || (primaryPattern == 3 && secondaryPattern == 5) || (primaryPattern == 5 && secondaryPattern == 3) -> 4
        (primaryPattern == 5 && secondaryPattern == 5) || (primaryPattern == 4 && secondaryPattern == 5) || (primaryPattern == 5 && secondaryPattern == 4) -> 5
        else -> 1
    }

    val (riskLabel, riskColor, description, management) = when (isupGrade) {
        1 -> Quadruple(
            "Riesgo Muy Bajo / Bajo",
            Color(0xFF2E7D32),
            "Gleason 3 + 3 = 6. Células tumorales bien diferenciadas con apariencia cercana a las glándulas normales.",
            "La Vigilancia Activa (Active Surveillance) es la estrategia recomendada y estándar en la mayoría de los casos, evitando sobretratamiento."
        )
        2 -> Quadruple(
            "Riesgo Intermedio Favorable",
            Color(0xFF689F38),
            "Gleason 3 + 4 = 7. Predominio de patrón histológico 3. El pronóstico es favorable en general con bajo potencial de metástasis.",
            "Opción de Vigilancia Activa en pacientes seleccionados, o bien tratamiento curativo definitivo (Prostatectomía radical o Radioterapia externa)."
        )
        3 -> Quadruple(
            "Riesgo Intermedio Desfavorable",
            Color(0xFFFFA000),
            "Gleason 4 + 3 = 7. Predominio de patrón histológico 4. Mayor agresividad biológica, tasas de progresión y recurrencia que el grado 2.",
            "Generalmente requiere tratamiento curativo definitivo activo (Prostatectomía radical o Radioterapia con terapia de deprivación androgénica a corto plazo)."
        )
        4 -> Quadruple(
            "Riesgo Alto",
            Color(0xFFE65100),
            "Gleason 8 (ej. 4+4, 3+5, 5+3). Células tumorales mal diferenciadas, crecimiento rápido y alta probabilidad de extensión extraprostática.",
            "Requiere tratamiento agresivo definitivo multimodal (Prostatectomía con linfadenectomía extendida o Radioterapia combinada con terapia de deprivación androgénica de largo plazo)."
        )
        else -> Quadruple(
            "Riesgo Muy Alto",
            Color(0xFFD32F2F),
            "Gleason 9 o 10 (ej. 4+5, 5+4, 5+5). Células indiferenciadas o con necrosis prominente. Máxima agresividad tumoral y muy alto riesgo de metástasis.",
            "Requiere de forma inmediata terapia combinada multimodal intensiva (Cirugía/Radioterapia de rescate combinada con terapia hormonal prolongada y posible quimioterapia según guías NCCN)."
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Score de Gleason (ISUP)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introductory informative Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "El sistema de gradación de Gleason correlaciona el patrón histológico de la biopsia de próstata con la agresividad biológica del tumor. El Grupo de Grados ISUP (1-5) unifica la interpretación clínica y pronóstica.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nota: De acuerdo al consenso de la ISUP, los patrones Gleason 1 y 2 ya no se reportan en biopsias por aguja debido al riesgo de subestimación del grado y sobrediagnóstico.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // PRIMARY PATTERN SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Patrón Primario (Predominante en el tumor)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Options list
                    listOf(
                        GleasonOption(3, "Patrón Gleason 3", "Glándulas bien formadas, discretas, individuales y con lúmenes visibles."),
                        GleasonOption(4, "Patrón Gleason 4", "Glándulas fusionadas (pérdida de límites), estructuras cribiformes o glándulas mal formadas."),
                        GleasonOption(5, "Patrón Gleason 5", "Ausencia de formación de glándulas. Láminas sólidas de células tumorales, nidos, cordones o necrosis.")
                    ).forEach { option ->
                        val isSelected = primaryPattern == option.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { primaryPattern = option.value }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { primaryPattern = option.value },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = option.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // SECONDARY PATTERN SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Patrón Secundario (Segundo más común)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Options list
                    listOf(
                        GleasonOption(3, "Patrón Gleason 3", "Glándulas bien formadas, discretas, individuales y con lúmenes visibles."),
                        GleasonOption(4, "Patrón Gleason 4", "Glándulas fusionadas (pérdida de límites), estructuras cribiformes o glándulas mal formadas."),
                        GleasonOption(5, "Patrón Gleason 5", "Ausencia de formación de glándulas. Láminas sólidas de células tumorales, nidos, cordones o necrosis.")
                    ).forEach { option ->
                        val isSelected = secondaryPattern == option.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { secondaryPattern = option.value }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { secondaryPattern = option.value },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = option.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // RESULTS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("PUNTUACIÓN DE GLEASON / ISUP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    
                    Text(
                        text = "$totalScore ($primaryPattern + $secondaryPattern)",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box(
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Grupo de Grado ISUP $isupGrade",
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Categoría: $riskLabel",
                                fontWeight = FontWeight.Bold,
                                color = riskColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Enfoque Clínico Sugerido:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = management,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_gleason_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cálculo Gleason")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Gleason: $totalScore ($primaryPattern+$secondaryPattern), ISUP Grado: $isupGrade ($riskLabel)"
                val paramsText = "Patrón Primario: $primaryPattern, Patrón Secundario: $secondaryPattern"
                
                viewModel.saveCalculation(
                    calculatorType = "Score de Gleason / ISUP",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// Data helper for Gleason pattern selection
data class GleasonOption(
    val value: Int,
    val title: String,
    val desc: String
)

// Helper class for Gleason Screen properties
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// ----------------------------------------------------
// 19. R.E.N.A.L. NEPHROMETRY SCORE SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenalScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var radiusScore by remember { mutableStateOf(1) }       // R: 1, 2, 3
    var exophyticScore by remember { mutableStateOf(1) }     // E: 1, 2, 3
    var nearnessScore by remember { mutableStateOf(1) }       // N: 1, 2, 3
    var anteriorPosterior by remember { mutableStateOf("a") } // A: a, p, x
    var locationScore by remember { mutableStateOf(1) }       // L: 1, 2, 3
    var hilarSuffix by remember { mutableStateOf(false) }     // h suffix: true/false
    var showSaveDialog by remember { mutableStateOf(false) }

    val totalScore = radiusScore + exophyticScore + nearnessScore + locationScore
    val scoreSuffix = anteriorPosterior + (if (hilarSuffix) "h" else "")
    val scoreString = "$totalScore$scoreSuffix"

    // Risk classification according to clinical guidelines
    val (complexityLabel, complexityColor, complexityDesc, surgicalImplications) = when {
        totalScore in 4..6 -> Quadruple(
            "Baja Complejidad Quirúrgica",
            Color(0xFF2E7D32), // Green
            "Puntaje total de $totalScore. El tumor presenta características anatómicas favorables que minimizan la dificultad de resección.",
            "Altamente susceptible de Nefrectomía Parcial (cirugía preservadora de nefronas) de forma segura. Menor riesgo de isquemia prolongada, lesión de la vía urinaria colectora o sangrado intraoperatorio."
        )
        totalScore in 7..9 -> Quadruple(
            "Complejidad Quirúrgica Moderada",
            Color(0xFFE65100), // Orange
            "Puntaje total de $totalScore. El tumor tiene un grado moderado de penetración en el parénquima o cercanía al seno renal.",
            "La Nefrectomía Parcial es factible pero técnicamente demandante. Se asocia con mayor tiempo de clampeo vascular, riesgo moderado de fístula urinaria posoperatoria y requiere cirujanos urólogos experimentados. Considerar Nefrectomía Radical según comorbilidades del paciente."
        )
        else -> Quadruple(
            "Alta Complejidad Quirúrgica",
            Color(0xFFD32F2F), // Red
            "Puntaje total de $totalScore. Masa renal de gran tamaño, interpolar, con compromiso endofítico y gran cercanía o contacto directo con el sistema colector o los grandes vasos del hilio.",
            "Elevada dificultad técnica para cirugía preservadora. Mayor probabilidad de requerir conversión intraoperatoria a Nefrectomía Radical. Riesgo alto de complicaciones vasculares, urinarias e isquemia renal caliente prolongada. Se aconseja planificación minuciosa y abordaje por equipo urológico de alta experiencia."
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nefrometría R.E.N.A.L.", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanatory Introduction Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "El score de nefrometría R.E.N.A.L. es un sistema estandarizado que evalúa las características anatómicas de una masa renal en base a tomografía (TC) o resonancia (RM), permitiendo cuantificar la complejidad técnica de la cirugía renal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // INTERACTIVE NEPHROMETRY VISUAL REPRESENTATION CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Representación Visual Dinámica",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Layered Box for Canvas and Labels
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        // Background Grid and Kidney/Tumor Drawing
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        val outlineColor = MaterialTheme.colorScheme.outline
                        val tertiaryColor = MaterialTheme.colorScheme.tertiary

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val rx = size.width * 0.18f
                            val ry = size.height * 0.4f

                            // 1. Draw Kidney bean shape
                            val kidneyPath = Path().apply {
                                moveTo(cx, cy - ry)
                                // Left convex outer border
                                cubicTo(cx - rx * 1.8f, cy - ry, cx - rx * 1.8f, cy + ry, cx, cy + ry)
                                // Bottom right curve towards hilum
                                cubicTo(cx + rx * 0.4f, cy + ry, cx + rx * 0.5f, cy + ry * 0.6f, cx + rx * 0.4f, cy + ry * 0.3f)
                                // Hilum indentation (sinus)
                                cubicTo(cx + rx * 0.1f, cy + ry * 0.1f, cx + rx * 0.1f, cy - ry * 0.1f, cx + rx * 0.4f, cy - ry * 0.3f)
                                // Top right curve
                                cubicTo(cx + rx * 0.5f, cy - ry * 0.6f, cx + rx * 0.4f, cy - ry, cx, cy - ry)
                                close()
                            }
                            drawPath(
                                path = kidneyPath,
                                color = primaryColor.copy(alpha = 0.15f)
                            )
                            drawPath(
                                path = kidneyPath,
                                color = primaryColor.copy(alpha = 0.7f),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // 2. Draw Collecting System / Renal Sinus
                            val sinusPath = Path().apply {
                                moveTo(cx + rx * 0.4f, cy - ry * 0.25f)
                                cubicTo(cx + rx * 0.15f, cy - ry * 0.15f, cx + rx * 0.15f, cy + ry * 0.15f, cx + rx * 0.4f, cy + ry * 0.25f)
                                cubicTo(cx + rx * 0.5f, cy + ry * 0.15f, cx + rx * 0.5f, cy - ry * 0.15f, cx + rx * 0.4f, cy - ry * 0.25f)
                                close()
                            }
                            drawPath(
                                path = sinusPath,
                                color = tertiaryColor.copy(alpha = 0.25f)
                            )
                            drawPath(
                                path = sinusPath,
                                color = tertiaryColor.copy(alpha = 0.7f),
                                style = Stroke(width = 1.5f.dp.toPx())
                            )

                            // 3. Draw main hilar vessels
                            val arteryColor = Color(0xFFC62828)
                            val veinColor = Color(0xFF1565C0)
                            // Artery (red)
                            drawLine(
                                color = arteryColor,
                                start = Offset(cx + rx * 1.6f, cy - 10f),
                                end = Offset(cx + rx * 0.4f, cy - 5f),
                                strokeWidth = 3.dp.toPx()
                            )
                            // Vein (blue)
                            drawLine(
                                color = veinColor,
                                start = Offset(cx + rx * 1.6f, cy + 10f),
                                end = Offset(cx + rx * 0.4f, cy + 5f),
                                strokeWidth = 4.dp.toPx()
                            )

                            // 4. Draw dashed Polar Lines
                            val lineY1 = cy - ry * 0.33f
                            val lineY2 = cy + ry * 0.33f
                            drawLine(
                                color = outlineColor.copy(alpha = 0.4f),
                                start = Offset(cx - rx * 2.2f, lineY1),
                                end = Offset(cx + rx * 1.8f, lineY1),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                            drawLine(
                                color = outlineColor.copy(alpha = 0.4f),
                                start = Offset(cx - rx * 2.2f, lineY2),
                                end = Offset(cx + rx * 1.8f, lineY2),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            // 5. Dynamic Tumor Size (Radius)
                            val tr = when (radiusScore) {
                                1 -> 14.dp.toPx()  // Small (<=4cm)
                                2 -> 24.dp.toPx()  // Medium (4-7cm)
                                else -> 36.dp.toPx() // Large (>=7cm)
                            }

                            // Dynamic Tumor Vertical Position (L - Polar Lines Location)
                            val ty = when (locationScore) {
                                1 -> cy - ry * 0.72f // Upper pole / outside polar lines
                                2 -> cy - ry * 0.33f // Crossing polar lines
                                else -> cy            // Mid pole / interpolar
                            }

                            // Dynamic Tumor Horizontal Position (E & N - Exophytic & Nearness combined)
                            // Both E and N represent how deep the tumor is inside the parenchyma / touching sinus
                            val combinedOutwardWeight = ((3 - exophyticScore) + (3 - nearnessScore)) / 4f // 0 to 1
                            val tx = (cx + rx * 0.28f) * (1f - combinedOutwardWeight) + (cx - rx * 1.35f) * combinedOutwardWeight

                            // Draw Tumor Area
                            val tumorColor = Color(0xFFD32F2F)
                            drawCircle(
                                color = tumorColor.copy(alpha = 0.22f),
                                radius = tr,
                                center = Offset(tx, ty)
                            )
                            drawCircle(
                                color = tumorColor,
                                radius = tr,
                                center = Offset(tx, ty),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = tumorColor.copy(alpha = 0.8f),
                                radius = tr * 0.2f,
                                center = Offset(tx, ty)
                            )

                            // Draw hilar warning if contact hilar is present
                            if (hilarSuffix) {
                                drawCircle(
                                    color = Color(0xFFFFB300).copy(alpha = 0.35f),
                                    radius = 18.dp.toPx(),
                                    center = Offset(cx + rx * 0.4f, cy),
                                    style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                                )
                            }
                        }

                        // Labels layered on top of Canvas
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Polo Superior", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Zona Media (Interpolar)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Polo Inferior", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Vía Colectora / Hilio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (hilarSuffix) Color(0xFFFFB300).copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, if (hilarSuffix) Color(0xFFFFB300) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (hilarSuffix) "Sufijo h: Contacto" else "Hilio libre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hilarSuffix) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "El tumor (en rojo) se reposiciona y redimensiona dinámicamente según sus selecciones.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // SELECTORS SECTION
            Text(
                text = "Parámetros del Score R.E.N.A.L.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // R - RADIUS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "R - Radio / Diámetro del Tumor",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    listOf(
                        GleasonOption(1, "≤ 4.0 cm (+1 pt)", "Masa tumoral pequeña confinada, ideal para tumorectomía parcial."),
                        GleasonOption(2, "> 4.0 y < 7.0 cm (+2 pts)", "Masa de tamaño intermedio. Parcial es desafiante pero viable."),
                        GleasonOption(3, "≥ 7.0 cm (+3 pts)", "Masa tumoral de gran tamaño. Alto riesgo quirúrgico, frecuentemente requiere radical.")
                    ).forEach { option ->
                        val isSelected = radiusScore == option.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { radiusScore = option.value }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { radiusScore = option.value })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(option.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(option.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // E - EXOPHYTIC/ENDOPHYTIC PROPERTIES CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "E - Propiedades Exofíticas / Endofíticas",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    listOf(
                        GleasonOption(1, "≥ 50% Exofítico (+1 pt)", "La mayor parte del tumor sobresale hacia afuera del riñón."),
                        GleasonOption(2, "< 50% Exofítico (+2 pts)", "Componente mixto o mayormente sumergido en el parénquima."),
                        GleasonOption(3, "100% Endofítico (+3 pts)", "Tumor completamente incrustado dentro del riñón sin relieve externo.")
                    ).forEach { option ->
                        val isSelected = exophyticScore == option.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exophyticScore = option.value }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { exophyticScore = option.value })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(option.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(option.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // N - NEARNESS TO COLLECTING SYSTEM CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "N - Cercanía al Sistema Colector / Seno Renal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    listOf(
                        GleasonOption(1, "≥ 7.0 mm (+1 pt)", "Tumor alejado de la vía urinaria central, bajo riesgo de apertura de vía colectora."),
                        GleasonOption(2, "> 4.0 y < 7.0 mm (+2 pts)", "Cercanía moderada al seno renal o vía central."),
                        GleasonOption(3, "≤ 4.0 mm (+3 pts)", "En íntima proximidad o penetrando el seno o vía urinaria colectora.")
                    ).forEach { option ->
                        val isSelected = nearnessScore == option.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { nearnessScore = option.value }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { nearnessScore = option.value })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(option.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(option.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // A - ANTERIOR / POSTERIOR DESCRIPTOR CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "A - Localización Anterior o Posterior",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No aporta puntos numéricos al total, pero describe el abordaje quirúrgico idóneo (ej. laparoscopía transperitoneal vs retroperitoneal).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("a", "Anterior (a)", "Cara anterior"),
                            Triple("p", "Posterior (p)", "Cara posterior"),
                            Triple("x", "Indeterminado (x)", "Cara lateral/polar")
                        ).forEach { (code, label, shortDesc) ->
                            val isSelected = anteriorPosterior == code
                            FilterChip(
                                selected = isSelected,
                                onClick = { anteriorPosterior = code },
                                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // L - LOCATION RELATIVE TO POLAR LINES CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "L - Relación con las Líneas Polares",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    listOf(
                        GleasonOption(1, "Polo Superior / Inferior (+1 pt)", "Totalmente por encima de la línea polar superior o por debajo de la inferior."),
                        GleasonOption(2, "Cruza Línea Polar (+2 pts)", "El tumor cruza una de las dos líneas polares (menos del 50% interpolar)."),
                        GleasonOption(3, "Totalmente Interpolar (+3 pts)", "Totalmente entre las líneas polares (zona media) O bien > 50% de la masa es medial al borde renal.")
                    ).forEach { option ->
                        val isSelected = locationScore == option.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { locationScore = option.value }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { locationScore = option.value })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(option.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(option.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Suffix h - HILAR CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sufijo h - Relación Hiliar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hilarSuffix = !hilarSuffix }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = hilarSuffix, onCheckedChange = { hilarSuffix = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Contacto con el Hilio Renal (Sufijo h)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Añadir sufijo 'h' si el tumor toca o está inmediatamente adyacente a la arteria/veina renal principal o a la pelvis renal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // RESULTS DISPLAY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PUNTUACIÓN DE NEFROMETRÍA R.E.N.A.L.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Big Score Display
                    Text(
                        text = scoreString,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Complexity badge
                    Box(
                        modifier = Modifier
                            .background(complexityColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = complexityLabel,
                            color = complexityColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Breakdown display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "R" to radiusScore,
                            "E" to exophyticScore,
                            "N" to nearnessScore,
                            "A" to anteriorPosterior.uppercase(),
                            "L" to locationScore,
                            "h" to if (hilarSuffix) "Sí" else "No"
                        ).forEach { (param, value) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(param, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(value.toString(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(complexityColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Descripción Anatómica:",
                                fontWeight = FontWeight.Bold,
                                color = complexityColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = complexityDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Implicaciones Quirúrgicas Sugeridas:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = surgicalImplications,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("save_renal_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cálculo de Nefrometría")
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val resultText = "Nefrometría R.E.N.A.L.: $scoreString ($complexityLabel)"
                val paramsText = "R:$radiusScore, E:$exophyticScore, N:$nearnessScore, A:$anteriorPosterior, L:$locationScore, h:$hilarSuffix (Puntos total: $totalScore)"
                
                viewModel.saveCalculation(
                    calculatorType = "Nefrometría R.E.N.A.L.",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 20. TNM UROLOGICAL STAGING CALCULATOR SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TnmScreen(
    navController: NavController,
    viewModel: UroViewModel
) {
    var selectedTumorType by remember { mutableStateOf("KIDNEY") } // KIDNEY, UTUC, BLADDER, PROSTATE, TESTIS, PENIS

    // Selection states
    var tSelection by remember { mutableStateOf("T1a") }
    var nSelection by remember { mutableStateOf("N0") }
    var mSelection by remember { mutableStateOf("M0") }
    var sSelection by remember { mutableStateOf("S0") } // only for testis
    var showSaveDialog by remember { mutableStateOf(false) }

    // Synchronize selections on type change
    fun resetSelections(type: String) {
        when (type) {
            "KIDNEY" -> {
                tSelection = "T1a"
                nSelection = "N0"
                mSelection = "M0"
            }
            "UTUC" -> {
                tSelection = "Ta"
                nSelection = "N0"
                mSelection = "M0"
            }
            "BLADDER" -> {
                tSelection = "Ta"
                nSelection = "N0"
                mSelection = "M0"
            }
            "PROSTATE" -> {
                tSelection = "T1c"
                nSelection = "N0"
                mSelection = "M0"
            }
            "TESTIS" -> {
                tSelection = "pT1"
                nSelection = "N0"
                mSelection = "M0"
                sSelection = "S0"
            }
            "PENIS" -> {
                tSelection = "T1a"
                nSelection = "cN0"
                mSelection = "M0"
            }
        }
    }

    val tOptions = getTOptions(selectedTumorType)
    val nOptions = getNOptions(selectedTumorType)
    val mOptions = getMOptions(selectedTumorType)
    val sOptions = getSOptions()

    // Determine current stage and descriptions
    val (stage, stageDesc) = when (selectedTumorType) {
        "KIDNEY" -> {
            val calcStage = when {
                mSelection == "M1" -> "Estadio IV"
                tSelection == "T4" -> "Estadio IV"
                nSelection == "N1" -> "Estadio III"
                tSelection in listOf("T3a", "T3b", "T3c") -> "Estadio III"
                tSelection in listOf("T2a", "T2b") -> "Estadio II"
                else -> "Estadio I"
            }
            val desc = when (calcStage) {
                "Estadio I" -> "Tumor pequeño localizado (≤ 7 cm), sin compromiso ganglionar ni metástasis. Pronóstico favorable con resección quirúrgica (nefrectomía parcial o radical)."
                "Estadio II" -> "Tumor grande localizado (> 7 cm) confinado al riñón. Excelente tasa de supervivencia post-nefrectomía radical."
                "Estadio III" -> "Extensión regional (invasión de vena renal, grasa del seno/perirrenal o compromiso ganglionar regional N1). Requiere nefrectomía y seguimiento clínico."
                else -> "Enfermedad avanzada con invasión más allá de la fascia de Gerota (T4) o metástasis a distancia (M1). Requiere terapia sistémica moderna (inmunoterapia y/o inhibidores de tirosina quinasa)."
            }
            Pair(calcStage, desc)
        }
        "UTUC" -> {
            val calcStage = when {
                mSelection == "M1" -> "Estadio IV"
                nSelection in listOf("N1", "N2") -> "Estadio IV"
                tSelection == "T4" -> "Estadio IV"
                tSelection == "T3" -> "Estadio III"
                tSelection == "T2" -> "Estadio II"
                tSelection == "T1" -> "Estadio I"
                tSelection == "Ta" -> "Estadio 0a"
                else -> "Estadio 0is"
            }
            val desc = when (calcStage) {
                "Estadio 0a" -> "Carcinoma papilar no invasivo (Ta). Excelente pronóstico mediante resección local o tratamiento endourológico selectivo."
                "Estadio 0is" -> "Carcinoma in situ (Tis). Alto riesgo de recurrencia y progresión. Requiere vigilancia muy estrecha."
                "Estadio I" -> "Invasión de la lámina propia (T1). Pronóstico favorable. Se recomienda manejo quirúrgico radical o conservador selectivo."
                "Estadio II" -> "Invasión de la capa muscular (T2). Requiere nefroureterectomía radical estándar."
                "Estadio III" -> "Invasión de grasa peripélvica, periureteral o parénquima renal (T3). Requiere cirugía radical y quimioterapia adyuvante."
                else -> "Enfermedad localmente muy avanzada (T4), compromiso ganglionar regional (N+) o metástasis a distancia (M1). Requiere quimioterapia sistémica a base de platino."
            }
            Pair(calcStage, desc)
        }
        "BLADDER" -> {
            val calcStage = when {
                mSelection == "M1b" -> "Estadio IVb"
                mSelection == "M1a" -> "Estadio IVa"
                tSelection == "T4b" -> "Estadio IVa"
                nSelection in listOf("N2", "N3") -> "Estadio IIIb"
                nSelection == "N1" -> "Estadio IIIa"
                tSelection in listOf("T3a", "T3b", "T4a") -> "Estadio IIIa"
                tSelection in listOf("T2a", "T2b") -> "Estadio II"
                tSelection == "T1" -> "Estadio I"
                tSelection == "Ta" -> "Estadio 0a"
                else -> "Estadio 0is"
            }
            val desc = when (calcStage) {
                "Estadio 0a" -> "Tumor papilar no invasivo (Ta). Tratamiento mediante resección transuretral de vejiga (RTU-V) +/- quimioterapia intravesical única post-operatoria."
                "Estadio 0is" -> "Carcinoma in situ plano de alto grado (Tis). Alto riesgo de progresión. Se indica tratamiento con inmunoterapia intravesical (BCG)."
                "Estadio I" -> "Invasión de lámina propia (T1). Tratamiento con RTU-V completa seguido de BCG intravesical para prevenir recurrencia."
                "Estadio II" -> "Infiltración de la muscular propia (T2, cáncer músculo-invasor). Requiere quimioterapia neoadyuvante seguida de cistectomía radical."
                "Estadio IIIa" -> "Invasión perivesical/órganos adyacentes (T3-T4a) o ganglio pélvico único (N1). Requiere quimioterapia sistémica intensiva y cistectomía radical."
                "Estadio IIIb" -> "Metástasis ganglionares pélvicas múltiples (N2-N3). Requiere tratamiento sistémico prioritario (quimioterapia o inmunoterapia)."
                "Estadio IVa" -> "Extensión a pared pélvica/abdominal (T4b) o metástasis en ganglios lejanos (M1a). Requiere tratamiento sistémico multimodal."
                else -> "Metástasis visceral u ósea a distancia (M1b). Requiere quimioterapia de primera línea, inmunoterapia de mantenimiento o cuidados paliativos."
            }
            Pair(calcStage, desc)
        }
        "PROSTATE" -> {
            val calcStage = when {
                mSelection in listOf("M1a", "M1b", "M1c") -> "Estadio IVb"
                nSelection == "N1" -> "Estadio IVa"
                tSelection == "T4" -> "Estadio IVa"
                tSelection in listOf("T3a", "T3b") -> "Estadio III"
                tSelection == "T2c" -> "Estadio IIc"
                tSelection == "T2b" -> "Estadio IIb"
                tSelection == "T2a" -> "Estadio IIa"
                else -> "Estadio I"
            }
            val desc = when (calcStage) {
                "Estadio I" -> "Tumor muy localizado clínicamente inapreciable (T1a-c o T2a con bajo grado). Pronóstico excelente. Candidato ideal a Vigilancia Activa."
                "Estadio IIa" -> "Tumor localizado limitado a la mitad de un lóbulo (T2a). Excelente pronóstico con Vigilancia Activa o tratamiento definitivo local."
                "Estadio IIb" -> "Tumor que afecta más de la mitad de un lóbulo, de forma unilateral (T2b). Se evalúan comorbilidades y PSA para decidir cirugía o radioterapia."
                "Estadio IIc" -> "Afectación bilateral de la próstata (T2c). Riesgo intermedio-alto. Se aconseja prostatectomía o radioterapia radical."
                "Estadio III" -> "Extensión extracapsular o invasión de vesículas seminales (T3). Requiere tratamiento multimodal (Radioterapia combinada con Terapia de deprivación androgénica ADT)."
                "Estadio IVa" -> "Tumor fijo que invade órganos vecinos (T4) o metástasis ganglionares regionales (N1). Alto riesgo de progresión, requiere bloqueo androgénico."
                else -> "Metástasis a distancia en huesos, ganglios distantes o vísceras (M1a-c). Requiere ADT combinada con agentes hormonales de nueva generación (abiraterona, enzalutamida) o quimioterapia."
            }
            Pair(calcStage, desc)
        }
        "TESTIS" -> {
            val calcStage = when {
                mSelection == "M1b" || sSelection == "S3" -> "Estadio IIIc"
                mSelection == "M1a" || sSelection == "S2" -> "Estadio IIIb"
                sSelection == "S1" && (nSelection != "N0" || mSelection != "M0") -> "Estadio IIIa"
                nSelection in listOf("N1", "N2", "N3") -> "Estadio II"
                sSelection in listOf("S1", "S2", "S3") -> "Estadio IS"
                else -> "Estadio I"
            }
            val desc = when (calcStage) {
                "Estadio I" -> "Tumor confinado al testículo con marcadores normales post-operatorios. Tasa de curación cercana al 100% mediante orquiectomía y vigilancia estrecha."
                "Estadio IS" -> "Persistencia de marcadores tumorales elevados (S1-S3) post-orquiectomía sin metástasis macroscópica. Indica micro-metástasis ocultas. Requiere quimioterapia activa (normalmente BEP)."
                "Estadio II" -> "Presencia de ganglios retroperitoneales metastásicos (N1-N3). Requiere quimioterapia sistémica (BEP x3 o EP x4) o linfadenectomía retroperitoneal."
                "Estadio IIIa" -> "Compromiso de pulmones o ganglios distantes (M1a) o marcadores ligeramente elevados en fase ganglionar. Buen pronóstico según IGCCCG. Tratamiento con quimioterapia sistémica."
                "Estadio IIIb" -> "Metástasis viscerales pulmonares con marcadores moderadamente elevados (S2). Riesgo intermedio. Requiere quimioterapia con 3 o 4 ciclos de BEP."
                else -> "Enfermedad avanzada con metástasis visceral extrapulmonar (M1b) o marcadores séricos severamente elevados (S3). Requiere manejo quimioterápico BEP de 4 ciclos intensivo."
            }
            Pair(calcStage, desc)
        }
        "PENIS" -> {
            val calcStage = when {
                mSelection == "M1" -> "Estadio IV"
                nSelection == "cN3" -> "Estadio IV"
                tSelection == "T4" -> "Estadio IV"
                nSelection == "cN2" -> "Estadio IIIb"
                nSelection == "cN1" -> "Estadio IIIa"
                tSelection == "T3" -> "Estadio IIb"
                tSelection in listOf("T1b", "T2") -> "Estadio IIa"
                tSelection == "T1a" -> "Estadio I"
                else -> "Estadio 0"
            }
            val desc = when (calcStage) {
                "Estadio 0" -> "Carcinoma in situ (Tis) o verrugoso no invasivo (Ta). Tratamiento conservador local preservador de pene (láser, circuncisión o cremas)."
                "Estadio I" -> "Infiltración subepitelial superficial (T1a) sin invasión vascular ni alto grado. Excelente pronóstico con resección local amplia."
                "Estadio IIa" -> "Infiltración subepitelial con factores de riesgo (T1b) o invasión del cuerpo cavernoso/esponjoso (T2). Requiere cirugía local amplia y estadificación ganglionar."
                "Estadio IIb" -> "Invasión de la uretra (T3) sin ganglios. Requiere penectomía parcial o total y evaluación profiláctica ganglionar bilateral."
                "Estadio IIIa" -> "Ganglio inguinal móvil unilateral palpable (cN1). Requiere linfadenectomía inguinal bilateral radical además del manejo del primario."
                "Estadio IIIb" -> "Ganglios inguinales bilaterales o múltiples móviles (cN2). Alto riesgo de propagación pélvica. Requiere cirugía radical e inmunoterapia/quimioterapia sistémica."
                else -> "Invasión profunda adyacente (T4), masa ganglionar inguinal fija / afectación pélvica (cN3) o metástasis a distancia (M1). Requiere quimioterapia neoadyuvante sistémica."
            }
            Pair(calcStage, desc)
        }
        else -> Pair("Estadio Desconocido", "No se han seleccionado parámetros válidos.")
    }

    val displayType = when (selectedTumorType) {
        "KIDNEY" -> "Riñón (Carcinoma de Células Renales)"
        "UTUC" -> "Vías Urinarias Superiores (Pelvis Renal y Uréter)"
        "BLADDER" -> "Vejiga (Urotelial de Vejiga)"
        "PROSTATE" -> "Próstata (Adenocarcinoma)"
        "TESTIS" -> "Testículo (Células Germinales)"
        "PENIS" -> "Pene (Carcinoma Escamoso)"
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clasificación TNM", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal row for tumor type selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair("KIDNEY", "Riñón"),
                    Pair("UTUC", "Vías Sup."),
                    Pair("BLADDER", "Vejiga"),
                    Pair("PROSTATE", "Próstata"),
                    Pair("TESTIS", "Testículo"),
                    Pair("PENIS", "Pene")
                ).forEach { (typeCode, label) ->
                    val isSelected = selectedTumorType == typeCode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTumorType = typeCode
                            resetSelections(typeCode)
                        },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Estadificación para $displayType",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Seleccione la extensión tumoral (T), el grado de afectación linfática regional (N) y la presencia de metástasis (M) según la 8va edición AJCC/UICC.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. T Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "T - Tumor Primario",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        tOptions.forEach { opt ->
                            val isSelected = tSelection == opt.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tSelection = opt.code }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { tSelection = opt.code }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = opt.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = opt.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. N Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "N - Ganglios Regionales",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        nOptions.forEach { opt ->
                            val isSelected = nSelection == opt.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { nSelection = opt.code }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { nSelection = opt.code }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = opt.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = opt.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. M Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "M - Metástasis a Distancia",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        mOptions.forEach { opt ->
                            val isSelected = mSelection == opt.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mSelection = opt.code }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { mSelection = opt.code }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = opt.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = opt.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. S Selection Card (Testis only)
                if (selectedTumorType == "TESTIS") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "S - Marcadores Séricos",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            sOptions.forEach { opt ->
                                val isSelected = sSelection == opt.code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { sSelection = opt.code }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { sSelection = opt.code }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = opt.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = opt.desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. RESULTS AND SUGGESTIONS CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ESTADIFICACIÓN SINTETIZADA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        val tnmString = if (selectedTumorType == "TESTIS") {
                            "$tSelection $nSelection $mSelection $sSelection"
                        } else {
                            "$tSelection $nSelection $mSelection"
                        }

                        Text(
                            text = tnmString,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stage,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Guía Clínica e Implicación Pronóstica:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stageDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("save_tnm_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Estadificación TNM")
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveCalculationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { caseId ->
                val fullTnmString = if (selectedTumorType == "TESTIS") {
                    "$tSelection $nSelection $mSelection $sSelection"
                } else {
                    "$tSelection $nSelection $mSelection"
                }
                val resultText = "TNM $displayType: $fullTnmString ($stage)"
                val paramsText = "T:$tSelection, N:$nSelection, M:$mSelection" + if (selectedTumorType == "TESTIS") ", S:$sSelection" else ""
                
                viewModel.saveCalculation(
                    calculatorType = "Estadificación TNM",
                    inputParameters = paramsText,
                    result = resultText,
                    caseId = caseId
                )
                showSaveDialog = false
            },
            viewModel = viewModel
        )
    }
}

// Data models for TNM selection options
data class TnmOption(val code: String, val title: String, val desc: String)

fun getTOptions(type: String): List<TnmOption> {
    return when (type) {
        "KIDNEY" -> listOf(
            TnmOption("T1a", "T1a", "≤ 4 cm, limitado al riñón"),
            TnmOption("T1b", "T1b", "> 4 cm pero ≤ 7 cm, limitado al riñón"),
            TnmOption("T2a", "T2a", "> 7 cm pero ≤ 10 cm, limitado al riñón"),
            TnmOption("T2b", "T2b", "> 10 cm, limitado al riñón"),
            TnmOption("T3a", "T3a", "Invasión de vena renal, ramas segmentarias, grasa perirrenal o seno renal"),
            TnmOption("T3b", "T3b", "Invasión de vena cava por debajo del diafragma"),
            TnmOption("T3c", "T3c", "Invasión de vena cava por encima del diafragma o pared de la misma"),
            TnmOption("T4", "T4", "Invasión más allá de la fascia de Gerota o glándula adrenal directa")
        )
        "UTUC" -> listOf(
            TnmOption("Ta", "Ta", "Carcinoma papilar no invasivo"),
            TnmOption("Tis", "Tis", "Carcinoma in situ"),
            TnmOption("T1", "T1", "Invasión del tejido conectivo subepitelial"),
            TnmOption("T2", "T2", "Invasión de la muscular propia"),
            TnmOption("T3", "T3", "Invasión de grasa peripélvica, periureteral o parénquima renal"),
            TnmOption("T4", "T4", "Invasión de órganos adyacentes o grasa perirrenal profunda")
        )
        "BLADDER" -> listOf(
            TnmOption("Ta", "Ta", "Carcinoma papilar no invasivo"),
            TnmOption("Tis", "Tis", "Carcinoma in situ (plano)"),
            TnmOption("T1", "T1", "Invasión de la lámina propia"),
            TnmOption("T2a", "T2a", "Invasión del músculo detrusor superficial (mitad interna)"),
            TnmOption("T2b", "T2b", "Invasión del músculo detrusor profundo (mitad externa)"),
            TnmOption("T3a", "T3a", "Invasión microscópica del tejido perivesical"),
            TnmOption("T3b", "T3b", "Invasión macroscópica del tejido perivesical (masa extravesical)"),
            TnmOption("T4a", "T4a", "Invasión de próstata, útero o vagina"),
            TnmOption("T4b", "T4b", "Invasión de la pared pélvica o pared abdominal")
        )
        "PROSTATE" -> listOf(
            TnmOption("T1a", "T1a", "Incidental en ≤ 5% del tejido resecado"),
            TnmOption("T1b", "T1b", "Incidental en > 5% del tejido resecado"),
            TnmOption("T1c", "T1c", "Sospecha por PSA elevado, diagnosticado por biopsia por aguja"),
            TnmOption("T2a", "T2a", "Afectación de la mitad o menos de un lóbulo"),
            TnmOption("T2b", "T2b", "Afectación de más de la mitad de un lóbulo, pero unilateral"),
            TnmOption("T2c", "T2c", "Afectación de ambos lóbulos"),
            TnmOption("T3a", "T3a", "Extensión extracapsular unilateral o bilateral, o cuello vesical"),
            TnmOption("T3b", "T3b", "Invasión de vesículas seminales"),
            TnmOption("T4", "T4", "Fijo o invade órganos adyacentes como recto, pared pélvica, etc.")
        )
        "TESTIS" -> listOf(
            TnmOption("pT1", "pT1", "Limitado a testículo/epidídimo sin invasión linfovascular"),
            TnmOption("pT2", "pT2", "Limitado a testículo/epidídimo con invasión linfovascular, o invade túnica vaginal"),
            TnmOption("pT3", "pT3", "Invade el cordón espermático"),
            TnmOption("pT4", "pT4", "Invade el escroto")
        )
        "PENIS" -> listOf(
            TnmOption("Tis", "Tis", "Carcinoma in situ"),
            TnmOption("Ta", "Ta", "Carcinoma verrugoso no invasivo"),
            TnmOption("T1a", "T1a", "Invade subepitelio, sin invasión perineural/linfovascular ni alto grado"),
            TnmOption("T1b", "T1b", "Invade subepitelio con invasión perineural, linfovascular, o alto grado"),
            TnmOption("T2", "T2", "Invade cuerpo esponjoso o cavernoso"),
            TnmOption("T3", "T3", "Invade la uretra"),
            TnmOption("T4", "T4", "Invade periné, pubis, próstata")
        )
        else -> emptyList()
    }
}

fun getNOptions(type: String): List<TnmOption> {
    return when (type) {
        "KIDNEY" -> listOf(
            TnmOption("N0", "N0", "Sin metástasis ganglionares regionales"),
            TnmOption("N1", "N1", "Metástasis en ganglio(s) linfático(s) regional(es)")
        )
        "UTUC" -> listOf(
            TnmOption("N0", "N0", "Sin metástasis ganglionares regionales"),
            TnmOption("N1", "N1", "Metástasis única ≤ 2 cm en ganglios regionales"),
            TnmOption("N2", "N2", "Metástasis única > 2 cm, o ganglios regionales múltiples")
        )
        "BLADDER" -> listOf(
            TnmOption("N0", "N0", "Sin metástasis ganglionares regionales"),
            TnmOption("N1", "N1", "Metástasis única en la pelvis verdadera (obturador, hipogástrico, ilíaco externo, presacro)"),
            TnmOption("N2", "N2", "Metástasis múltiples en la pelvis verdadera"),
            TnmOption("N3", "N3", "Metástasis en ganglios ilíacos comunes")
        )
        "PROSTATE" -> listOf(
            TnmOption("N0", "N0", "Sin metástasis ganglionares regionales"),
            TnmOption("N1", "N1", "Metástasis ganglionares regionales")
        )
        "TESTIS" -> listOf(
            TnmOption("N0", "N0", "Sin metástasis ganglionares regionales"),
            TnmOption("N1", "N1", "Metástasis en ganglios regionales ≤ 2 cm, o múltiple ≤ 2 cm"),
            TnmOption("N2", "N2", "Metástasis en ganglios regionales de 2 a 5 cm"),
            TnmOption("N3", "N3", "Masa ganglionar regional > 5 cm")
        )
        "PENIS" -> listOf(
            TnmOption("cN0", "cN0", "Sin ganglios inguinales palpables o agrandados"),
            TnmOption("cN1", "cN1", "Ganglio inguinal móvil unilateral palpable"),
            TnmOption("cN2", "cN2", "Ganglios inguinales móviles múltiples o bilaterales palpables"),
            TnmOption("cN3", "cN3", "Masa inguinal fija o linfadenopatía pélvica")
        )
        else -> emptyList()
    }
}

fun getMOptions(type: String): List<TnmOption> {
    return when (type) {
        "KIDNEY" -> listOf(
            TnmOption("M0", "M0", "Sin metástasis a distancia"),
            TnmOption("M1", "M1", "Metástasis a distancia")
        )
        "UTUC" -> listOf(
            TnmOption("M0", "M0", "Sin metástasis a distancia"),
            TnmOption("M1", "M1", "Metástasis a distancia")
        )
        "BLADDER" -> listOf(
            TnmOption("M0", "M0", "Sin metástasis a distancia"),
            TnmOption("M1a", "M1a", "Metástasis en ganglios de la pelvis superior o lejanos"),
            TnmOption("M1b", "M1b", "Metástasis viscerales u óseas")
        )
        "PROSTATE" -> listOf(
            TnmOption("M0", "M0", "Sin metástasis a distancia"),
            TnmOption("M1a", "M1a", "Ganglios linfáticos lejanos (no regionales)"),
            TnmOption("M1b", "M1b", "Metástasis óseas"),
            TnmOption("M1c", "M1c", "Otras metástasis viscerales con o sin ósea")
        )
        "TESTIS" -> listOf(
            TnmOption("M0", "M0", "Sin metástasis a distancia"),
            TnmOption("M1a", "M1a", "Metástasis pulmonares o ganglionares lejanas"),
            TnmOption("M1b", "M1b", "Metástasis viscerales no pulmonares")
        )
        "PENIS" -> listOf(
            TnmOption("M0", "M0", "Sin metástasis a distancia"),
            TnmOption("M1", "M1", "Metástasis a distancia")
        )
        else -> emptyList()
    }
}

fun getSOptions(): List<TnmOption> {
    return listOf(
        TnmOption("S0", "S0", "Marcadores tumorales post-orquiectomía normales"),
        TnmOption("S1", "S1", "LDH < 1.5x N, hCG < 5000 mUI/mL, y AFP < 1000 ng/mL"),
        TnmOption("S2", "S2", "LDH 1.5-10x N, hCG 5000-50000 mUI/mL, o AFP 1000-10000 ng/mL"),
        TnmOption("S3", "S3", "LDH > 10x N, hCG > 50000 mUI/mL, o AFP > 10000 ng/mL")
    )
}

// ==========================================
// NEW CLINICAL CALCULATORS & QUESTIONNAIRES
// ==========================================

// 1. TIEMPO DE DUPLICACIÓN DE PSA (PSADT) & VELOCIDAD (PSAV)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsadtScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var psa1Str by remember { mutableStateOf("2.0") }
    var psa2Str by remember { mutableStateOf("4.5") }
    var intervalMonthsStr by remember { mutableStateOf("6.0") }

    val psa1 = psa1Str.toFloatOrNull() ?: 0f
    val psa2 = psa2Str.toFloatOrNull() ?: 0f
    val intervalMonths = intervalMonthsStr.toFloatOrNull() ?: 0f

    val isCalculable = psa1 > 0f && psa2 > 0f && intervalMonths > 0f
    val logDiff = if (isCalculable && psa2 > psa1) Math.log((psa2 / psa1).toDouble()) else 0.0
    val psadtMonths = if (logDiff > 0) (Math.log(2.0) * intervalMonths) / logDiff else 0.0
    val psavYearly = if (isCalculable && intervalMonths > 0) ((psa2 - psa1) / (intervalMonths / 12f)).toDouble() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tiempo Duplicación PSA (PSADT)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Evalúa la velocidad cinético-temporal del antígeno prostático específico (PSA) para detectar recurrencia bioquímica o progresión tumoral agresiva.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Valores de PSA e Intervalo Temporal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = psa1Str,
                        onValueChange = { psa1Str = it },
                        label = { Text("PSA Inicial (ng/mL)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("psadt_psa1_input")
                    )

                    OutlinedTextField(
                        value = psa2Str,
                        onValueChange = { psa2Str = it },
                        label = { Text("PSA Final (ng/mL)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("psadt_psa2_input")
                    )

                    OutlinedTextField(
                        value = intervalMonthsStr,
                        onValueChange = { intervalMonthsStr = it },
                        label = { Text("Intervalo entre muestras (Meses)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("psadt_interval_input")
                    )
                }
            }

            if (isCalculable) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("psadt_result_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("RESULTADOS CINÉTICOS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)

                        if (psa2 <= psa1) {
                            Text("PSA Estable o en Descenso", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Text("El valor de PSA no ha aumentado entre las dos tomas, por lo que no existe tiempo de duplicación activo.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Tiempo de Duplicación (PSADT)", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = String.format(Locale.US, "%.1f Meses", psadtMonths),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (psadtMonths < 3) MaterialTheme.colorScheme.error else if (psadtMonths < 9) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Velocidad de PSA (PSAV)", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = String.format(Locale.US, "%.2f ng/mL/año", psavYearly),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val (interpret, alertColor) = when {
                                psadtMonths < 3.0 -> "Riesgo Muy Alto / Crecimiento Agresivo: Sugerente de metástasis a distancia o recurrencia bioquímica rápida post-tratamiento radical." to MaterialTheme.colorScheme.error
                                psadtMonths < 9.0 -> "Riesgo Intermedio / Recurrencia Activa: Se aconseja estudio de extensión mediante PET-PSMA o RM." to Color(0xFFE65100)
                                psadtMonths < 12.0 -> "Riesgo Moderado: Evolución intermedia que requiere estrecho seguimiento tumoral." to MaterialTheme.colorScheme.primary
                                else -> "Crecimiento Indolente / Riesgo Bajo: Evolución lenta; vigilar tendencia clínica." to Color(0xFF2E7D32)
                            }

                            Text(
                                text = interpret,
                                style = MaterialTheme.typography.bodyMedium,
                                color = alertColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                FeedbackHelper.triggerTap(view, viewModel)
                                val recordStr = if (psa2 <= psa1) "PSADT: N/A (PSA sin aumento)" else String.format(Locale.US, "PSADT: %.1f meses | PSAV: %.2f ng/mL/año", psadtMonths, psavYearly)
                                viewModel.saveCalculation(
                                    calculatorType = "Tiempo Duplicación PSA",
                                    inputParameters = "PSA 1: $psa1, PSA 2: $psa2, Intervalo: $intervalMonths meses",
                                    result = recordStr
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Resultado")
                        }
                    }
                }
            }
        }
    }
}

// 2. VOLUMEN PROSTÁTICO Y RESIDUO POSTMICCIONAL (PVR)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProstateBladderVolumeScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var pWidth by remember { mutableStateOf("4.5") }
    var pLength by remember { mutableStateOf("3.8") }
    var pHeight by remember { mutableStateOf("4.0") }

    var preWidth by remember { mutableStateOf("8.0") }
    var preLength by remember { mutableStateOf("7.5") }
    var preHeight by remember { mutableStateOf("6.0") }

    var postWidth by remember { mutableStateOf("3.5") }
    var postLength by remember { mutableStateOf("3.0") }
    var postHeight by remember { mutableStateOf("2.5") }

    val pw = pWidth.toFloatOrNull() ?: 0f
    val pl = pLength.toFloatOrNull() ?: 0f
    val ph = pHeight.toFloatOrNull() ?: 0f
    val prostateVol = if (pw > 0 && pl > 0 && ph > 0) pw * pl * ph * 0.5236f else 0f

    val preW = preWidth.toFloatOrNull() ?: 0f
    val preL = preLength.toFloatOrNull() ?: 0f
    val preH = preHeight.toFloatOrNull() ?: 0f
    val preVol = if (preW > 0 && preL > 0 && preH > 0) preW * preL * preH * 0.5236f else 0f

    val postW = postWidth.toFloatOrNull() ?: 0f
    val postL = postLength.toFloatOrNull() ?: 0f
    val postH = postHeight.toFloatOrNull() ?: 0f
    val pvrVol = if (postW > 0 && postL > 0 && postH > 0) postW * postL * postH * 0.5236f else 0f

    val emptyingPercent = if (preVol > 0f) Math.max(0f, ((preVol - pvrVol) / preVol) * 100f) else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volumen Prostático y PVR") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cálculo volumétrico mediante fórmula elipsoide V = A × L × H × 0.5236 para cuantificar el volumen de la próstata y la retención miccional (Residuo Postmiccional - PVR).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Próstata Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Ultrasonido Prostático (cm)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pWidth,
                            onValueChange = { pWidth = it },
                            label = { Text("Ancho") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pLength,
                            onValueChange = { pLength = it },
                            label = { Text("Largo") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pHeight,
                            onValueChange = { pHeight = it },
                            label = { Text("Alto") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (prostateVol > 0) {
                        val pCategory = when {
                            prostateVol < 30 -> "Tamaño Normal (<30 cc)"
                            prostateVol < 50 -> "Crecimiento Leve (30-50 cc)"
                            prostateVol < 80 -> "Crecimiento Moderado (50-80 cc)"
                            else -> "Crecimiento Severo (>80 cc)"
                        }
                        Text(
                            text = String.format(Locale.US, "Volumen Prostático: %.1f cc (%s)", prostateVol, pCategory),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Vejiga Pre y Post
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. Vejiga Pre-Miccional (cm)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = preWidth, onValueChange = { preWidth = it }, label = { Text("Ancho") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = preLength, onValueChange = { preLength = it }, label = { Text("Largo") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = preHeight, onValueChange = { preHeight = it }, label = { Text("Alto") }, modifier = Modifier.weight(1f))
                    }

                    Text("3. Vejiga Post-Miccional / Residuo (cm)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = postWidth, onValueChange = { postWidth = it }, label = { Text("Ancho") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = postLength, onValueChange = { postLength = it }, label = { Text("Largo") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = postHeight, onValueChange = { postHeight = it }, label = { Text("Alto") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (preVol > 0 || prostateVol > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("RESUMEN DE RESULTADOS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text(String.format(Locale.US, "• Próstata: %.1f cc", prostateVol), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.US, "• Volumen Pre-Miccional: %.1f mL", preVol), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(Locale.US, "• Residuo Postmiccional (PVR): %.1f mL", pvrVol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (pvrVol > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Text(String.format(Locale.US, "• Eficiencia de Vaciado: %.1f%%", emptyingPercent), style = MaterialTheme.typography.bodyMedium)

                        val pvrEval = when {
                            pvrVol < 50 -> "PVR Normal (<50 mL): Vaciado vesical adecuado."
                            pvrVol < 100 -> "PVR Aceptable (50-100 mL): Retención mínima sin significación patológica inmediata."
                            pvrVol < 200 -> "PVR Elevado Moderado (100-200 mL): Sugiere obstrucción de salida o hipocontractilidad del detrusor."
                            else -> "PVR Retención Severa (>200 mL): Alto riesgo de infecciones, descompensación detrusor o daño renal retrógrado."
                        }
                        Text(pvrEval, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                        Button(
                            onClick = {
                                FeedbackHelper.triggerTap(view, viewModel)
                                viewModel.saveCalculation(
                                    calculatorType = "Volumen Prostático y PVR",
                                    inputParameters = "Pre: $preVol mL, Post: $pvrVol mL",
                                    result = String.format(Locale.US, "Próstata: %.1f cc | PVR: %.1f mL (%.0f%% vaciado)", prostateVol, pvrVol, emptyingPercent)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Resultado")
                        }
                    }
                }
            }
        }
    }
}

// 3. TABLAS DE PARTIN (PARTIN TABLES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartinTablesScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var psaCategory by remember { mutableStateOf(2) } // 0: <2.5, 1: 2.5-4, 2: 4.1-6, 3: 6.1-10, 4: >10
    var tStage by remember { mutableStateOf(0) } // 0: T1c, 1: T2a, 2: T2b/T2c, 3: T3a
    var isupGroup by remember { mutableStateOf(1) } // 0: ISUP1, 1: ISUP2, 2: ISUP3, 3: ISUP4, 4: ISUP5

    // Calculation approximation based on Partin Tables 2017/2021 update matrix
    val (oc, epe, svi, lni) = remember(psaCategory, tStage, isupGroup) {
        val baseEpe = (psaCategory * 5) + (tStage * 8) + (isupGroup * 10) + 12
        val baseSvi = (psaCategory * 2) + (tStage * 3) + (isupGroup * 5) + 2
        val baseLni = (psaCategory * 2) + (tStage * 3) + (isupGroup * 6) + 1

        val epeVal = Math.min(85, baseEpe)
        val sviVal = Math.min(50, baseSvi)
        val lniVal = Math.min(45, baseLni)
        val ocVal = Math.max(2, 100 - (epeVal + sviVal + lniVal))
        Quadruple(ocVal, epeVal, sviVal, lniVal)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tablas de Partin") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nomograma clínico de Johns Hopkins para predecir la probabilidad de estadio patológico tras la prostatectomía radical según el PSA, estadio clínico T y Grupo de Grado ISUP.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Selector PSA
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Antígeno Prostático (PSA)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val psaLabels = listOf("<2.5", "2.5-4.0", "4.1-6.0", "6.1-10.0", ">10.0 ng/mL")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        psaLabels.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = psaCategory == idx,
                                onClick = { psaCategory = idx },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Selector T
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Estadio T Clínico", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val tLabels = listOf("T1c", "T2a", "T2b/T2c", "T3a")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        tLabels.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = tStage == idx,
                                onClick = { tStage = idx },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Selector ISUP
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Grupo de Grado ISUP / Gleason Biopsia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val isupLabels = listOf("ISUP 1 (3+3)", "ISUP 2 (3+4)", "ISUP 3 (4+3)", "ISUP 4 (8)", "ISUP 5 (9-10)")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        isupLabels.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = isupGroup == idx,
                                onClick = { isupGroup = idx },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Resultado Partin
            Card(
                modifier = Modifier.fillMaxWidth().testTag("partin_result_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PROBABILIDADES PATOLÓGICAS ESTIMADAS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)

                    PartinBar("Confinado al Órgano (OC)", oc, Color(0xFF2E7D32))
                    PartinBar("Extensión Extraprostática (EPE)", epe, Color(0xFFE65100))
                    PartinBar("Invasión Vesículas Seminales (SVI)", svi, MaterialTheme.colorScheme.error)
                    PartinBar("Afectación Ganglionar Pélvica (LNI)", lni, Color(0xFF880E4F))

                    Button(
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            viewModel.saveCalculation(
                                calculatorType = "Tablas de Partin",
                                inputParameters = "PSA cat: $psaCategory, T: $tStage, ISUP: ${isupGroup+1}",
                                result = "Partin: OC $oc%, EPE $epe%, SVI $svi%, LNI $lni%"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado")
                    }
                }
            }
        }
    }
}

@Composable
private fun PartinBar(label: String, percent: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// 4. PUNTAJE UCSF-CAPRA (CÁNCER DE PRÓSTATA)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapraScoreScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var ageGte50 by remember { mutableStateOf(true) }
    var psaCategory by remember { mutableStateOf(1) } // 0: <=6 (0pt), 1: 6.1-10 (1pt), 2: 10.1-20 (2pt), 3: 20.1-30 (3pt), 4: >30 (4pt)
    var gleasonCategory by remember { mutableStateOf(1) } // 0: 3+3 (0pt), 1: 3+4 (1pt), 2: 4+3 (2pt), 3: 8-10 (3pt)
    var tStageCategory by remember { mutableStateOf(0) } // 0: T1c/T2a (0pt), 1: T2b/T2c/T3a (1pt)
    var percentCoresGte34 by remember { mutableStateOf(false) } // false: <34% (0pt), true: >=34% (1pt)

    val agePts = if (ageGte50) 1 else 0
    val psaPts = psaCategory
    val gleasonPts = gleasonCategory
    val tPts = tStageCategory
    val coresPts = if (percentCoresGte34) 1 else 0

    val capraScore = agePts + psaPts + gleasonPts + tPts + coresPts

    val (riskGroup, bRFS5yr, color) = when {
        capraScore <= 2 -> Triple("Riesgo Bajo (Bajo)", "~85% Libre de Recurrencia a 5 Años", Color(0xFF2E7D32))
        capraScore <= 5 -> Triple("Riesgo Intermedio", "~62% Libre de Recurrencia a 5 Años", Color(0xFFE65100))
        else -> Triple("Riesgo Alto", "~33% Libre de Recurrencia a 5 Años", MaterialTheme.colorScheme.error)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puntaje UCSF-CAPRA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cancer of the Prostate Risk Assessment Score (UCSF-CAPRA). Cuantifica el riesgo de recurrencia bioquímica post-prostatectomía o radioterapia (rango 0 a 10 puntos).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Edad del Paciente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !ageGte50, onClick = { ageGte50 = false }, label = { Text("< 50 años (0 pt)") })
                        FilterChip(selected = ageGte50, onClick = { ageGte50 = true }, label = { Text("≥ 50 años (1 pt)") })
                    }

                    Text("2. PSA al Diagnóstico (ng/mL)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val psaOpts = listOf("≤6.0 (0 pt)", "6.1-10 (1 pt)", "10.1-20 (2 pt)", "20.1-30 (3 pt)", ">30 (4 pt)")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        psaOpts.forEachIndexed { idx, label ->
                            FilterChip(selected = psaCategory == idx, onClick = { psaCategory = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Text("3. Score de Gleason (Biopsia)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val gleasonOpts = listOf("Gleason 3+3 (0 pt)", "Gleason 3+4 (1 pt)", "Gleason 4+3 (2 pt)", "Gleason 8-10 (3 pt)")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        gleasonOpts.forEachIndexed { idx, label ->
                            FilterChip(selected = gleasonCategory == idx, onClick = { gleasonCategory = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Text("4. Estadio T Clínico", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = tStageCategory == 0, onClick = { tStageCategory = 0 }, label = { Text("T1c / T2a (0 pt)") })
                        FilterChip(selected = tStageCategory == 1, onClick = { tStageCategory = 1 }, label = { Text("T2b / T2c / T3a (1 pt)") })
                    }

                    Text("5. Porcentaje de Cilindros Positivos en Biopsia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !percentCoresGte34, onClick = { percentCoresGte34 = false }, label = { Text("< 34% (0 pt)") })
                        FilterChip(selected = percentCoresGte34, onClick = { percentCoresGte34 = true }, label = { Text("≥ 34% (1 pt)") })
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("capra_result_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, color)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PUNTAJE CAPRA TOTAL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text("$capraScore / 10 Puntos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = color)
                    Text("Categoría: $riskGroup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Pronóstico: $bRFS5yr", style = MaterialTheme.typography.bodyMedium)

                    Button(
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            viewModel.saveCalculation(
                                calculatorType = "Puntaje UCSF-CAPRA",
                                inputParameters = "Edad: ${if(ageGte50) ">=50" else "<50"}, PSA: $psaCategory, Gleason: $gleasonCategory",
                                result = "CAPRA: $capraScore/10 ($riskGroup)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado CAPRA")
                    }
                }
            }
        }
    }
}

// 5. TABLAS DE RIESGO EORTC (CÁNCER DE VEJIGA NMIBC)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EortcRiskScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var numTumors by remember { mutableStateOf(0) } // 0: Unico (0), 1: 2-7 (3), 2: >=8 (6)
    var tumorSizeGte3 by remember { mutableStateOf(false) } // false: <3cm (0), true: >=3cm (3)
    var priorRecurrenceRate by remember { mutableStateOf(0) } // 0: Primario (0), 1: <=1/año (2), 2: >1/año (4)
    var tCategoryT1 by remember { mutableStateOf(false) } // false: Ta (0), true: T1 (1)
    var hasCis by remember { mutableStateOf(false) } // false: No (0), true: Si (6)
    var gradeCategory by remember { mutableStateOf(0) } // 0: G1 (0), 1: G2 (1), 2: G3 (2)

    val recScore = (if(numTumors==1) 3 else if(numTumors==2) 6 else 0) +
                   (if(tumorSizeGte3) 3 else 0) +
                   (if(priorRecurrenceRate==1) 2 else if(priorRecurrenceRate==2) 4 else 0) +
                   (if(tCategoryT1) 1 else 0) +
                   (if(hasCis) 6 else 0) +
                   gradeCategory

    val progScore = (if(numTumors==1) 3 else if(numTumors==2) 3 else 0) +
                    (if(tumorSizeGte3) 3 else 0) +
                    (if(priorRecurrenceRate==1) 2 else if(priorRecurrenceRate==2) 2 else 0) +
                    (if(tCategoryT1) 4 else 0) +
                    (if(hasCis) 6 else 0) +
                    (if(gradeCategory==1) 1 else if(gradeCategory==2) 5 else 0)

    val (recRiskGroup, rec1yr, rec5yr) = when {
        recScore == 0 -> Triple("Riesgo Bajo", "15%", "31%")
        recScore <= 4 -> Triple("Riesgo Intermedio", "24%", "46%")
        recScore <= 9 -> Triple("Riesgo Alto", "38%", "62%")
        else -> Triple("Riesgo Muy Alto", "61%", "78%")
    }

    val (progRiskGroup, prog1yr, prog5yr) = when {
        progScore == 0 -> Triple("Riesgo Bajo", "0.2%", "0.8%")
        progScore <= 6 -> Triple("Riesgo Intermedio", "1.0%", "6.0%")
        progScore <= 13 -> Triple("Riesgo Alto", "5.0%", "17.0%")
        else -> Triple("Riesgo Muy Alto", "17.0%", "45.0%")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tablas de Riesgo EORTC (Vejiga)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Estratifíca la probabilidad de Recurrencia y Progresión a 1 y 5 años en Cáncer de Vejiga No Músculo Invasivo (NMIBC) según criterios de la EORTC.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Número de Tumores", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = numTumors == 0, onClick = { numTumors = 0 }, label = { Text("Único") })
                        FilterChip(selected = numTumors == 1, onClick = { numTumors = 1 }, label = { Text("2-7 tumores") })
                        FilterChip(selected = numTumors == 2, onClick = { numTumors = 2 }, label = { Text("≥8 tumores") })
                    }

                    Text("2. Tamaño del Tumor", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = !tumorSizeGte3, onClick = { tumorSizeGte3 = false }, label = { Text("< 3 cm") })
                        FilterChip(selected = tumorSizeGte3, onClick = { tumorSizeGte3 = true }, label = { Text("≥ 3 cm") })
                    }

                    Text("3. Tasa de Recurrencia Previa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = priorRecurrenceRate == 0, onClick = { priorRecurrenceRate = 0 }, label = { Text("Primario (Sin rec previo)") }, modifier = Modifier.fillMaxWidth())
                        FilterChip(selected = priorRecurrenceRate == 1, onClick = { priorRecurrenceRate = 1 }, label = { Text("≤ 1 recurrencia / año") }, modifier = Modifier.fillMaxWidth())
                        FilterChip(selected = priorRecurrenceRate == 2, onClick = { priorRecurrenceRate = 2 }, label = { Text("> 1 recurrencia / año") }, modifier = Modifier.fillMaxWidth())
                    }

                    Text("4. Categoría T y CIS Concomitante", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = !tCategoryT1, onClick = { tCategoryT1 = false }, label = { Text("Ta") })
                        FilterChip(selected = tCategoryT1, onClick = { tCategoryT1 = true }, label = { Text("T1") })
                        FilterChip(selected = hasCis, onClick = { hasCis = !hasCis }, label = { Text(if(hasCis) "CIS (Sí)" else "CIS (No)") })
                    }

                    Text("5. Grado Tumoral OMS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = gradeCategory == 0, onClick = { gradeCategory = 0 }, label = { Text("G1 / Bajo Grado") })
                        FilterChip(selected = gradeCategory == 1, onClick = { gradeCategory = 1 }, label = { Text("G2") })
                        FilterChip(selected = gradeCategory == 2, onClick = { gradeCategory = 2 }, label = { Text("G3 / Alto Grado") })
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("eortc_result_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ESTIMACIÓN DE RIESGO EORTC", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)

                    Divider()

                    Text("Puntaje de Recurrencia: $recScore pts ($recRiskGroup)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("• Riesgo de Recurrencia a 1 año: $rec1yr | 5 años: $rec5yr", style = MaterialTheme.typography.bodyMedium)

                    Divider()

                    Text("Puntaje de Progresión: $progScore pts ($progRiskGroup)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("• Riesgo de Progresión a 1 año: $prog1yr | 5 años: $prog5yr", style = MaterialTheme.typography.bodyMedium)

                    Button(
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            viewModel.saveCalculation(
                                calculatorType = "Riesgo EORTC Vejiga",
                                inputParameters = "Rec 1yr: $rec1yr, 5yr: $rec5yr | Prog 1yr: $prog1yr, 5yr: $prog5yr",
                                result = "EORTC Rec: $recScore pts ($recRiskGroup) | Prog: $progScore pts ($progRiskGroup)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado EORTC")
                    }
                }
            }
        }
    }
}

// 6. CUESTIONARIO DE VEJIGA HIPERACTIVA (OABSS / OAB-q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OabqScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var q1 by remember { mutableStateOf(0) } // Daytime freq (0-2)
    var q2 by remember { mutableStateOf(0) } // Nighttime freq (0-3)
    var q3 by remember { mutableStateOf(0) } // Urgency (0-5)
    var q4 by remember { mutableStateOf(0) } // Urgency incontinence (0-5)

    val totalScore = q1 + q2 + q3 + q4
    val hasOabDiagnosis = q3 >= 2 && totalScore >= 3

    val (severityLabel, color) = when {
        !hasOabDiagnosis -> "Sin criterios estandarizados de Vejiga Hiperactiva" to Color(0xFF2E7D32)
        totalScore <= 5 -> "Vejiga Hiperactiva Leve (Puntaje ≤ 5)" to Color(0xFF2E7D32)
        totalScore <= 11 -> "Vejiga Hiperactiva Moderada (Puntaje 6-11)" to Color(0xFFE65100)
        else -> "Vejiga Hiperactiva Severa (Puntaje 12-15)" to MaterialTheme.colorScheme.error
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vejiga Hiperactiva (OABSS)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Overactive Bladder Symptom Score (OABSS). Cuantifica la sintomatología de la Vejiga Hiperactiva (frecuencia diurna, nocturia, urgencia e incontinencia de urgencia).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Q1
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. Frecuencia urinaria diurna habitual", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val opts1 = listOf("≤7 veces/día (0 pt)", "8-14 veces/día (1 pt)", "≥15 veces/día (2 pt)")
                    opts1.forEachIndexed { idx, label ->
                        FilterChip(selected = q1 == idx, onClick = { q1 = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Q2
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("2. Frecuencia nocturna (Nocturia)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val opts2 = listOf("0 veces (0 pt)", "1 vez (1 pt)", "2 veces (2 pt)", "≥3 veces (3 pt)")
                    opts2.forEachIndexed { idx, label ->
                        FilterChip(selected = q2 == idx, onClick = { q2 = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Q3
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("3. Urgencia urinaria (deseo repentino e imperioso de orinar)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val opts3 = listOf(
                        "Nunca / <1 vez por semana (0 pt)",
                        "1 vez a la semana o más (1 pt)",
                        "1 vez al día (2 pt)",
                        "2-4 veces al día (3 pt)",
                        "5 veces al día (4 pt)",
                        "≥5 veces al día (5 pt)"
                    )
                    opts3.forEachIndexed { idx, label ->
                        FilterChip(selected = q3 == idx, onClick = { q3 = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Q4
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("4. Incontinencia de Urgencia (pérdida súbita)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val opts4 = listOf(
                        "Nunca (0 pt)",
                        "<1 vez a la semana (1 pt)",
                        "1 vez a la semana o más (2 pt)",
                        "1 vez al día (3 pt)",
                        "2-4 veces al día (4 pt)",
                        "≥5 veces al día (5 pt)"
                    )
                    opts4.forEachIndexed { idx, label ->
                        FilterChip(selected = q4 == idx, onClick = { q4 = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Result
            Card(
                modifier = Modifier.fillMaxWidth().testTag("oabq_result_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, color)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PUNTAJE OABSS TOTAL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text("$totalScore / 15 Puntos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = color)
                    Text(severityLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    val diagnosticDetail = if (hasOabDiagnosis) {
                        "Criterio diagnóstico positivo: Urgencia (Pregunta 3) ≥2 pts y Total ≥3 pts."
                    } else {
                        "No cumple con el criterio mínimo de urgencia urinaria característico de la VH."
                    }
                    Text(diagnosticDetail, style = MaterialTheme.typography.bodyMedium)

                    Button(
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            viewModel.saveCalculation(
                                calculatorType = "Vejiga Hiperactiva (OABSS)",
                                inputParameters = "Q1:$q1, Q2:$q2, Q3:$q3, Q4:$q4",
                                result = "OABSS: $totalScore/15 ($severityLabel)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado OABSS")
                    }
                }
            }
        }
    }
}

// 7. CUESTIONARIO INTERNACIONAL DE INCONTINENCIA (ICIQ-SF)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IciqSfScreen(navController: NavController, viewModel: UroViewModel) {
    val view = LocalView.current
    var q1 by remember { mutableStateOf(0) } // Freq: 0,1,2,3,4,5
    var q2 by remember { mutableStateOf(0) } // Amount: 0, 2, 4, 6
    var q3Impact by remember { mutableStateOf(3f) } // 0-10 impact
    var typeStress by remember { mutableStateOf(true) }
    var typeUrge by remember { mutableStateOf(false) }

    val q1Pts = q1
    val q2Pts = when(q2) { 1 -> 2; 2 -> 4; 3 -> 6; else -> 0 }
    val q3Pts = Math.round(q3Impact)

    val totalScore = q1Pts + q2Pts + q3Pts

    val (severityLabel, color) = when {
        totalScore == 0 -> "Sin Incontinencia Activa" to Color(0xFF2E7D32)
        totalScore <= 5 -> "Incontinencia Leve (1-5 pts)" to Color(0xFF2E7D32)
        totalScore <= 12 -> "Incontinencia Moderada (6-12 pts)" to Color(0xFFE65100)
        totalScore <= 18 -> "Incontinencia Severa (13-18 pts)" to MaterialTheme.colorScheme.error
        else -> "Incontinencia Muy Severa (19-21 pts)" to Color(0xFF880E4F)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incontinencia (ICIQ-SF)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "International Consultation on Incontinence Questionnaire Short Form (ICIQ-SF). Evalúa la frecuencia, volumen e impacto global de la incontinencia en la calidad de vida.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Q1 Frecuencia
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. ¿Con qué frecuencia pierde orina?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val opts1 = listOf("Nunca (0 pt)", "1 vez a la semana o menos (1 pt)", "2-3 veces a la semana (2 pt)", "1 vez al día (3 pt)", "Varias veces al día (4 pt)", "Continuamente (5 pt)")
                    opts1.forEachIndexed { idx, label ->
                        FilterChip(selected = q1 == idx, onClick = { q1 = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Q2 Cantidad
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("2. Cantidad de orina que cree que se le escapa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val opts2 = listOf("Ninguna (0 pt)", "Una pequeña cantidad (2 pt)", "Una cantidad moderada (4 pt)", "Una gran cantidad (6 pt)")
                    opts2.forEachIndexed { idx, label ->
                        FilterChip(selected = q2 == idx, onClick = { q2 = idx }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Q3 Impacto
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Interferencia en su vida diaria: ${Math.round(q3Impact)} / 10", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Slider(
                        value = q3Impact,
                        onValueChange = { q3Impact = it },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0 (Nada)", style = MaterialTheme.typography.labelSmall)
                        Text("10 (Muchísimo)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Q4 Situación / Tipo
            Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("4. ¿Cuándo pierde orina? (Orientación diagnóstica)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    FilterChip(
                        selected = typeStress,
                        onClick = { typeStress = !typeStress },
                        label = { Text("Al toser, estornudar o hacer esfuerzo (Incontinencia de Esfuerzo)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = typeUrge,
                        onClick = { typeUrge = !typeUrge },
                        label = { Text("Antes de llegar al servicio con deseo urgente (Incontinencia de Urgencia)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Result
            Card(
                modifier = Modifier.fillMaxWidth().testTag("iciq_result_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, color)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PUNTAJE ICIQ-SF TOTAL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text("$totalScore / 21 Puntos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = color)
                    Text(severityLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    val typeStr = when {
                        typeStress && typeUrge -> "Patrón Mixto (Esfuerzo + Urgencia)"
                        typeStress -> "Patrón Sugerente de Incontinencia de Esfuerzo (IOE)"
                        typeUrge -> "Patrón Sugerente de Incontinencia de Urgencia (IOU)"
                        else -> "Inespecífica"
                    }
                    Text("Orientación Clínica: $typeStr", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    Button(
                        onClick = {
                            FeedbackHelper.triggerTap(view, viewModel)
                            viewModel.saveCalculation(
                                calculatorType = "Incontinencia ICIQ-SF",
                                inputParameters = "Frec: $q1Pts, Cant: $q2Pts, Impacto: $q3Pts | $typeStr",
                                result = "ICIQ-SF: $totalScore/21 ($severityLabel)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Resultado ICIQ-SF")
                    }
                }
            }
        }
    }
}


