package com.example.pw4_tv_13_ushakov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kotlin.math.pow
import com.example.pw4_tv_13_ushakov.ui.theme.PW4_TV13_UshakovTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// Enums and Data Classes
enum class CableType(val description: String) {
    BARE_COPPER("Неізольовані проводи та шини: мідні"),
    BARE_ALUMINUM("Неізольовані проводи та шини: алюмінієві"),
    PAPER_RUBBER_COPPER("Кабелі з паперовою і проводи з гумовою та полівінілхлоридною ізоляцією з жилами: мідними"),
    PAPER_RUBBER_ALUMINUM("Кабелі з паперовою і проводи з гумовою та полівінілхлоридною ізоляцією з жилами: алюмінієвими"),
    RUBBER_PLASTIC_COPPER("Кабелі з гумовою та пластмасовою ізоляцією з жилами: мідними"),
    RUBBER_PLASTIC_ALUMINUM("Кабелі з гумовою та пластмасовою ізоляцією з жилами: алюмінієвими")
}

data class EconomicCurrentDensity(
    val upTo3000: Double,
    val upTo5000: Double,
    val above5000: Double
)

val economicDensityMap = mapOf(
    CableType.BARE_COPPER to EconomicCurrentDensity(2.5, 2.1, 1.8),
    CableType.BARE_ALUMINUM to EconomicCurrentDensity(1.3, 1.1, 1.0),
    CableType.PAPER_RUBBER_COPPER to EconomicCurrentDensity(3.0, 2.5, 2.0),
    CableType.PAPER_RUBBER_ALUMINUM to EconomicCurrentDensity(1.6, 1.4, 1.2),
    CableType.RUBBER_PLASTIC_COPPER to EconomicCurrentDensity(3.5, 3.1, 2.7),
    CableType.RUBBER_PLASTIC_ALUMINUM to EconomicCurrentDensity(1.9, 1.7, 1.6)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Електричні розрахунки") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Вибір кабелю") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("КЗ на ГПП") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("(Режими  ПС") }
                )
            }

            when (selectedTab) {
                0 -> CableSelectionScreen()
                1 -> ShortCircuitCalculationScreen()
                2 -> SubstationModesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CableSelectionScreen() {
    var transformerPower by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("") }
    var loadFactor by remember { mutableStateOf("") }
    var selectedCableType by remember { mutableStateOf<CableType?>(null) }
    var annualUsageHours by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CableResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Параметри розрахунку",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = transformerPower,
                    onValueChange = { transformerPower = it },
                    label = { Text("Потужність трансформатора (кВА)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = voltage,
                    onValueChange = { voltage = it },
                    label = { Text("Напруга (кВ)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = loadFactor,
                    onValueChange = { loadFactor = it },
                    label = { Text("Коефіцієнт завантаження") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = annualUsageHours,
                    onValueChange = { annualUsageHours = it },
                    label = { Text("Час використання максимуму (год/рік)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Тип кабелю",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CableType.entries.forEach { cableType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .selectable(
                                selected = selectedCableType == cableType,
                                onClick = { selectedCableType = cableType }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCableType == cableType,
                            onClick = { selectedCableType = cableType }
                        )
                        Text(
                            text = cableType.description,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                selectedCableType?.let { cableType ->
                    result = calculateCableParameters(
                        transformerPower.toDoubleOrNull() ?: 0.0,
                        voltage.toDoubleOrNull() ?: 0.0,
                        loadFactor.toDoubleOrNull() ?: 0.0,
                        annualUsageHours.toDoubleOrNull() ?: 0.0,
                        cableType
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedCableType != null &&
                    transformerPower.isNotEmpty() &&
                    voltage.isNotEmpty() &&
                    loadFactor.isNotEmpty() &&
                    annualUsageHours.isNotEmpty()
        ) {
            Text("Розрахувати")
        }

        result?.let { cableResult ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Результати розрахунку",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Розрахунковий струм: ${cableResult.current.format(2)} А")
                    Text("Економічна щільність струму: ${cableResult.economicDensity.format(2)} А/мм²")
                    Text("Рекомендований переріз: ${cableResult.crossSection.format(2)} мм²")
                    Text("Обраний тип кабелю: ${cableResult.cableType.description}")
                }
            }
        }
    }
}
data class CableResult(
    val current: Double,
    val economicDensity: Double,
    val crossSection: Double,
    val cableType: CableType
)

fun calculateCableParameters(
    transformerPower: Double,
    voltage: Double,
    loadFactor: Double,
    annualUsageHours: Double,
    cableType: CableType
): CableResult {
    val current = (transformerPower * 1000) / (sqrt(3.0) * voltage * 1000)

    val calculatedCurrent = current * loadFactor

    val economicDensity = when {
        annualUsageHours <= 3000 -> economicDensityMap[cableType]?.upTo3000
        annualUsageHours <= 5000 -> economicDensityMap[cableType]?.upTo5000
        else -> economicDensityMap[cableType]?.above5000
    } ?: 1.0

    val crossSection = calculatedCurrent / economicDensity

    return CableResult(
        current = calculatedCurrent,
        economicDensity = economicDensity,
        crossSection = crossSection,
        cableType = cableType
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortCircuitCalculationScreen() {
    var systemPower by remember { mutableStateOf("") }
    var systemVoltage by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ShortCircuitResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Параметри системи",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = systemPower,
                    onValueChange = { systemPower = it },
                    label = { Text("потужність системи(МВА)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = systemVoltage,
                    onValueChange = { systemVoltage = it },
                    label = { Text("Напруга системи(кВ)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                result = calculateShortCircuit(
                    systemPower.toDoubleOrNull() ?: 0.0,
                    systemVoltage.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = systemPower.isNotEmpty() && systemVoltage.isNotEmpty()
        ) {
            Text("Розрахувати")
        }

        result?.let { scResult ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Результати розрахунку",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Стурм КЗ: ${scResult.shortCircuitCurrent.format(2)} кА")
                    Text("Ударний ток: ${scResult.peakCurrent.format(2)} кА")
                    Text("Потужність КЗ: ${scResult.shortCircuitPower.format(2)} МВА")
                }
            }
        }
    }
}

data class ShortCircuitResult(
    val shortCircuitCurrent: Double,
    val peakCurrent: Double,
    val shortCircuitPower: Double
)

fun calculateShortCircuit(
    systemPower: Double,
    systemVoltage: Double
): ShortCircuitResult {
    val baseImpedance = (systemVoltage * systemVoltage) / systemPower
    val shortCircuitCurrent = (systemPower * 1000) / (sqrt(3.0) * systemVoltage)
    val peakCurrent = shortCircuitCurrent * sqrt(2.0) * 1.8 // Ку = 1.8 для сети 10 кВ
    val shortCircuitPower = sqrt(3.0) * systemVoltage * shortCircuitCurrent

    return ShortCircuitResult(
        shortCircuitCurrent = shortCircuitCurrent / 1000, // перевод в кА
        peakCurrent = peakCurrent / 1000, // перевод в кА
        shortCircuitPower = shortCircuitPower / 1000000 // перевод в МВА
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstationModesScreen() {
    var selectedMode by remember { mutableStateOf(0) }
    var systemPower by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SubstationModeResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Режими роботи",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TabRow(selectedTabIndex = selectedMode) {
                    Tab(
                        selected = selectedMode == 0,
                        onClick = { selectedMode = 0 },
                        text = { Text("Нормальний") }
                    )
                    Tab(
                        selected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        text = { Text("Мінімальний") }
                    )
                    Tab(
                        selected = selectedMode == 2,
                        onClick = { selectedMode = 2 },
                        text = { Text("Аварійний") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = systemPower,
                    onValueChange = { systemPower = it },
                    label = { Text("Потужність системи (МВА)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                result = calculateSubstationMode(
                    systemPower.toDoubleOrNull() ?: 0.0,
                    selectedMode
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = systemPower.isNotEmpty()
        ) {
            Text("Розрахувати")
        }

        result?.let { modeResult ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Результати розрахунку",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Режим: ${modeResult.modeName}")
                    Text("Струм КЗ: ${modeResult.shortCircuitCurrent.format(2)} кА")
                    Text("Потужність КЗ: ${modeResult.shortCircuitPower.format(2)} МВА")
                }
            }
        }
    }
}
data class SubstationModeResult(
    val modeName: String,
    val shortCircuitCurrent: Double,
    val shortCircuitPower: Double
)

fun calculateSubstationMode(
    systemPower: Double,
    mode: Int
): SubstationModeResult {
    val (modeName, powerFactor) = when (mode) {
        0 -> Pair("Нормальний", 1.0)
        1 -> Pair("Мінімальний", 0.6)
        2 -> Pair("Аварійний", 1.2)
        else -> Pair("Невідомий", 1.0)
    }

    val actualPower = systemPower * powerFactor
    val shortCircuitCurrent = (actualPower * 1000) / (sqrt(3.0) * 10.0) // 10 кВ

    return SubstationModeResult(
        modeName = modeName,
        shortCircuitCurrent = shortCircuitCurrent / 1000, // перевод в кА
        shortCircuitPower = actualPower
    )
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

