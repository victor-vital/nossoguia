package com.nossoguiadecompras.presentation.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/* =========================
   Rotas
   ========================= */
object Routes {
    const val HOME = "home"
    const val INSTRUCOES = "instrucoes"
    const val PERFIL = "perfil"
    const val SORTEIO = "sorteio"
    const val SUPERMERCADOS = "supermercados"
    const val VER_ANUNCIOS = "verAnuncios"
    const val ANUNCIAR_GRATIS = "anunciarGratis"
    const val PREMIOS_SORTEIO = "premiosSorteio"
    const val CAMERAS = "cameras"
    const val JOGOS = "jogos"
    const val VAGAS = "vagas"
    const val CACA_PRODUTOS = "cacaProdutos"
    const val CACA_SERVICOS = "cacaServicos"
    const val DISK_REMEDIO = "diskRemedio"
    const val SEU_GAS_DISTRIBUIDORAS = "seuGasDistribuidoras"
    const val CARRINHO = "carrinho"
    const val CADASTRO_LOJA = "cadastroLoja"

    fun cartRouteFor(loja: String?, peso: String?): String {
        val lojaQ = loja?.let { "loja=${Uri.encode(it)}" }
        val pesoQ = peso?.let { "peso=${Uri.encode(it)}" }
        val q = listOfNotNull(lojaQ, pesoQ).joinToString("&")
        return if (q.isEmpty()) CARRINHO else "$CARRINHO?$q"
    }
}

/* =========================
   Modelos
   ========================= */
data class Anuncio(val anunciante: String, val titulo: String, val descricao: String)

data class DistribuidoraGas(
    val nome: String,
    val slogan: String,
    val distancia: String,
    val tempoEntrega: String,
    val avaliacaoPercentual: Int,
    val isRapida: Boolean
)

data class CartItem(
    val titulo: String,
    val unidade: String,          // "5 kg" | "8 kg" | "10 kg" | "13 kg"
    val precoRetirada: Double,
    val precoEntrega: Double,
    val quantidade: Int = 1
)

/* =========================
   “Backend” de preços (helpers)
   ========================= */
private val MARKET_PRICES = mapOf(
    "5 kg" to 83.00,
    "8 kg" to 110.00,
    "10 kg" to 125.00,
    "13 kg" to 145.00
)

private fun retiradaPriceFor(peso: String): Double =
    MARKET_PRICES[peso] ?: MARKET_PRICES.getValue("13 kg")

private fun entregaPriceFor(peso: String, retirada: Double): Double =
    retirada * when (peso) {
        "5 kg" -> 1.07
        "8 kg" -> 1.08
        "10 kg" -> 1.09
        else -> 1.10 // 13 kg
    }

private enum class OrderMode { RETIRADA, ENTREGA }
private enum class PaymentMethod { PIX, DINHEIRO, DEBITO, CREDITO }

private fun calcSubtotal(items: List<CartItem>, mode: OrderMode): Double {
    val isEntrega = mode == OrderMode.ENTREGA
    return items.sumOf { it.quantidade * (if (isEntrega) it.precoEntrega else it.precoRetirada) }
}

private fun calcAdjustment(subtotal: Double, method: PaymentMethod): Double {
    if (subtotal <= 0.0) return 0.0
    return when (method) {
        PaymentMethod.PIX -> -0.50
        PaymentMethod.DINHEIRO -> 0.0
        PaymentMethod.DEBITO -> subtotal * 0.02
        PaymentMethod.CREDITO -> subtotal * 0.05
    }
}

/* =========================
   ESQUEMA DE CORES — paleta leve
   ========================= */
private val MarketLightScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),       // Verde natural — frescor e confiança
    onPrimary = Color.White,
    secondary = Color(0xFFFF9800),     // Laranja suave — apetite e atenção
    onSecondary = Color.Black,
    background = Color(0xFFFFF8E1),    // Bege claro — acolhedor/neutral
    onBackground = Color(0xFF212121),  // Texto principal — legível e forte
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFE0F2F1),// Fundo de cards mais leves
    onSurfaceVariant = Color(0xFF212121),
    outline = Color(0xFFE0E0E0)        // Bordas sutis
)

/* =========================
   NavHost
   ========================= */
@Composable
fun AppNav() {
    val nav = rememberNavController()
    val popBack: () -> Unit = { nav.popBackStack() }

    MaterialTheme(colorScheme = MarketLightScheme) {
        NavHost(navController = nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(onNavigate = { nav.navigate(it) }, onBack = popBack)
            }
            composable(Routes.SUPERMERCADOS) {
                SupermercadosScreen(onNavigate = { nav.navigate(it) }, onBack = popBack)
            }
            composable(Routes.VER_ANUNCIOS) {
                VerAnunciosScreen(onNavigate = { nav.navigate(it) }, onBack = popBack)
            }
            composable(Routes.SEU_GAS_DISTRIBUIDORAS) {
                SeuGasDistribuidorasScreen(onNavigate = { nav.navigate(it) }, onBack = popBack)
            }
            composable(
                route = "${Routes.CARRINHO}?loja={loja}&peso={peso}",
                arguments = listOf(
                    navArgument("loja") { type = NavType.StringType; nullable = true },
                    navArgument("peso") { type = NavType.StringType; nullable = true },
                )
            ) { entry ->
                CartScreen(
                    initialStore = entry.arguments?.getString("loja"),
                    initialPeso = entry.arguments?.getString("peso"),
                    onNavigate = { nav.navigate(it) },
                    onBack = popBack
                )
            }

            composable(Routes.CADASTRO_LOJA) {
                CadastroLojaScreen(onBack = popBack)
            }

            composable(Routes.INSTRUCOES) { PlaceholderScreen(title = "INSTRUÇÕES") }
            composable(Routes.PERFIL) { PlaceholderScreen(title = "PERFIL") }
            composable(Routes.SORTEIO) { PlaceholderScreen(title = "SORTEIO") }
            composable(Routes.ANUNCIAR_GRATIS) { PlaceholderScreen(title = "ANUNCIAR GRÁTIS") }
            composable(Routes.PREMIOS_SORTEIO) { PlaceholderScreen(title = "PRÊMIOS DO SORTEIO") }
            composable(Routes.CAMERAS) { PlaceholderScreen(title = "CÂMERAS") }
            composable(Routes.JOGOS) { PlaceholderScreen(title = "JOGOS") }
            composable(Routes.VAGAS) { PlaceholderScreen(title = "VAGAS") }
            composable(Routes.CACA_PRODUTOS) { PlaceholderScreen(title = "CAÇA-PRODUTOS") }
            composable(Routes.CACA_SERVICOS) { PlaceholderScreen(title = "CAÇA-SERVIÇOS") }
            composable(Routes.DISK_REMEDIO) { PlaceholderScreen(title = "DISK-REMÉDIO") }
        }
    }
}

/* =========================
   Telas principais
   ========================= */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    var headerExpanded by remember { mutableStateOf(false) }
    Scaffold(
        bottomBar = {
            BottomAppBar(Modifier.height(60.dp), MaterialTheme.colorScheme.surface) {
                BottomBarContent(onNavigate = onNavigate)
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                expanded = headerExpanded,
                onExpandClick = { headerExpanded = !headerExpanded },
                onBack = onBack,
                onForward = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionBar(currentPage = "TELA INICIAL", onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(16.dp))
            AdsSection()
            Spacer(modifier = Modifier.height(16.dp))
            CategoriesSection(onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SupermercadosScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    var headerExpanded by remember { mutableStateOf(false) }
    val items = listOf(
        "VER ANÚNCIOS" to Color(0xFFE8F5E9),
        "QUER ANUNCIAR GRÁTIS?" to Color(0xFFFFF3E0),
        "PRÊMIOS DO SORTEIO" to Color(0xFFE3F2FD),
        "CÂMERAS DE CALÇADA" to Color(0xFFF1F8E9),
        "JOGOS DE APOSTAS" to Color(0xFFFFF3E0),
        "VAGAS DE EMPREGO" to Color(0xFFE0F2F1),
        "CAÇA-PRODUTOS" to Color(0xFFE0F7FA),
        "CAÇA-SERVIÇOS" to Color(0xFFFFFDE7),
        "DISK-REMÉDIO" to Color(0xFFF1F8E9)
    )
    Scaffold(
        bottomBar = {
            BottomAppBar(Modifier.height(60.dp), MaterialTheme.colorScheme.surface) {
                BottomBarContent(onNavigate = onNavigate)
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                expanded = headerExpanded,
                onExpandClick = { headerExpanded = !headerExpanded },
                onBack = onBack,
                onForward = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionBar(currentPage = "SUPERMERCADOS", onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(16.dp))
            AdsSection()
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEach { (title, color) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (title == "VER ANÚNCIOS") onNavigate(Routes.VER_ANUNCIOS) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = color)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun VerAnunciosScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    var headerExpanded by remember { mutableStateOf(false) }
    val supermercados = listOf(
        "ESCOLHER SUPERMERCADO" to 121,
        "ATTACK" to 6, "BARATÃO DA CARNE" to 10, "CARREFOUR" to 11, "COEMA" to 4,
        "DB" to 38, "NOVA ERA" to 10, "NOVO TEMPO" to 13, "RODRIGUES" to 14,
        "VITÓRIA" to 8, "ASSAÍ" to 9, "SUPERMERCADO ABC" to 15, "SEU GÁS" to 3,
        "MERCADO CENTRAL" to 8, "SUPER ECONOMIA" to 12
    )

    Scaffold(
        bottomBar = {
            BottomAppBar(Modifier.height(60.dp), MaterialTheme.colorScheme.surface) {
                BottomBarContent(onNavigate = onNavigate)
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                expanded = headerExpanded,
                onExpandClick = { headerExpanded = !headerExpanded },
                onBack = onBack,
                onForward = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionBar(currentPage = "SUPERM. - VER ANÚNCIOS", onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                supermercados.forEachIndexed { idx, (nome, contador) ->
                    SupermercadoItem(
                        nome = nome,
                        contador = contador,
                        isDestaque = idx == 0 || nome == "BARATÃO DA CARNE" || nome == "SEU GÁS",
                        onClick = { if (nome == "SEU GÁS") onNavigate(Routes.SEU_GAS_DISTRIBUIDORAS) }
                    )
                }
                repeat(5) { SupermercadoItem(nome = "", contador = 0, isDestaque = false, onClick = {}) }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/* =========================
   “SEU GÁS” (lista de distribuidoras)
   ========================= */
@Composable
fun SeuGasDistribuidorasScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    var headerExpanded by remember { mutableStateOf(false) }
    val distribuidoras = listOf(
        DistribuidoraGas("DISTRIBUIDORA MANAUS CENTRO", "Sempre à sua disposição", "1,2 km", "15-90 min", 93, false),
        DistribuidoraGas("AMAZONAS DISTRIBUIDORA", "ÁGUA E GÁS", "2,2 km", "15-30 min", 95, true),
        DistribuidoraGas("DISTRIBUIDORA NORTE", "PEDIU, PISCOU, CHEGOU.", "0,7 km", "15-30 min", 96, true)
    )

    Scaffold(
        bottomBar = {
            BottomAppBar(Modifier.height(60.dp), MaterialTheme.colorScheme.surface) {
                BottomBarContent(onNavigate = onNavigate)
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                expanded = headerExpanded,
                onExpandClick = { headerExpanded = !headerExpanded },
                onBack = onBack,
                onForward = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionBar(currentPage = "SEU GÁS - DISTRIBUIDORAS", onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(16.dp))

            distribuidoras.forEach { d ->
                DistribuidoraCard(
                    distribuidora = d,
                    onPedido = { loja, peso -> onNavigate(Routes.cartRouteFor(loja, peso)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Card(
                onClick = { /* carregar mais */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "carregar mais distribuidoras",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun DistribuidoraCard(
    distribuidora: DistribuidoraGas,
    onPedido: (loja: String, peso: String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedWeight by remember { mutableStateOf("13 kg") }

    val precoRetirada = retiradaPriceFor(selectedWeight)
    val precoEntrega = entregaPriceFor(selectedWeight, precoRetirada)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = distribuidora.nome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = distribuidora.slogan,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                    if (distribuidora.isRapida) {
                        Card(
                            modifier = Modifier.padding(top = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .12f))
                        ) {
                            Text(
                                text = "MAIS RÁPIDA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${distribuidora.distancia} • ${distribuidora.tempoEntrega}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "retirada",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "R$ %.2f".format(precoRetirada),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "entrega",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "R$ %.2f".format(precoEntrega),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { menuExpanded = true }) {
                    Text(text = "Selecionar Tam. Botija ($selectedWeight)")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    listOf("5 kg", "8 kg", "10 kg", "13 kg").forEach { peso ->
                        DropdownMenuItem(
                            text = { Text(text = peso) },
                            onClick = { selectedWeight = peso; menuExpanded = false }
                        )
                    }
                }

                Card(
                    onClick = { onPedido(distribuidora.nome, selectedWeight) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(
                        text = "pedir gás",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

/* =========================
   Carrinho (totalmente reativo)
   ========================= */
@Composable
fun CartScreen(
    initialStore: String?,
    initialPeso: String?,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var headerExpanded by remember { mutableStateOf(false) }

    val stores = listOf("DISTRIBUIDORA MANAUS CENTRO", "AMAZONAS DISTRIBUIDORA", "DISTRIBUIDORA NORTE")
    var selectedStore by remember { mutableStateOf(initialStore) }
    var storeMenu by remember { mutableStateOf(false) }

    val items = remember { mutableStateListOf<CartItem>() }

    LaunchedEffect(initialStore, initialPeso) {
        if (initialPeso != null && items.isEmpty()) {
            val r = retiradaPriceFor(initialPeso)
            val e = entregaPriceFor(initialPeso, r)
            items.add(CartItem(titulo = "Botijão de Gás", unidade = initialPeso, precoRetirada = r, precoEntrega = e, quantidade = 1))
            if (initialStore != null) selectedStore = initialStore
        }
    }

    var orderMode by remember { mutableStateOf(OrderMode.ENTREGA) }
    var payment by remember { mutableStateOf(PaymentMethod.PIX) }
    var addMenu by remember { mutableStateOf(false) }

    val subtotal = calcSubtotal(items = items, mode = orderMode)
    val adjustment = calcAdjustment(subtotal = subtotal, method = payment)
    val total = (subtotal + adjustment).coerceAtLeast(0.0)

    if (items.isEmpty() && payment != PaymentMethod.DINHEIRO) {
        payment = PaymentMethod.DINHEIRO
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(Modifier.height(60.dp), MaterialTheme.colorScheme.surface) {
                BottomBarContent(onNavigate = onNavigate)
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                expanded = headerExpanded,
                onExpandClick = { headerExpanded = !headerExpanded },
                onBack = onBack,
                onForward = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionBar(currentPage = "CARRINHO", onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(16.dp))

            /* Loja */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Loja", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { storeMenu = true }) {
                            Text(text = selectedStore ?: "Selecionar loja")
                        }
                        DropdownMenu(expanded = storeMenu, onDismissRequest = { storeMenu = false }) {
                            stores.forEach { loja ->
                                DropdownMenuItem(text = { Text(text = loja) }, onClick = {
                                    selectedStore = loja
                                    storeMenu = false
                                })
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Itens */
            if (items.isEmpty()) {
                Text(
                    text = "Seu carrinho está vazio. Adicione uma botija.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEachIndexed { index, item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { items[index] = item.copy(quantidade = item.quantidade + 1) },
                        onDecrease = { items[index] = item.copy(quantidade = (item.quantidade - 1).coerceAtLeast(1)) },
                        onRemove = { items.removeAt(index) },
                        orderMode = orderMode
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { addMenu = true }) { Text(text = "Adicionar botija") }
                DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                    listOf("5 kg", "8 kg", "10 kg", "13 kg").forEach { peso ->
                        DropdownMenuItem(
                            text = { Text(text = peso) },
                            onClick = {
                                val r = retiradaPriceFor(peso)
                                val e = entregaPriceFor(peso, r)
                                items.add(CartItem(titulo = "Botijão de Gás", unidade = peso, precoRetirada = r, precoEntrega = e, quantidade = 1))
                                addMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Modo do Pedido */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Entrega ou Retirada", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = orderMode == OrderMode.RETIRADA,
                            onClick = { orderMode = OrderMode.RETIRADA },
                            label = { Text(text = "Retirada") },
                            enabled = items.isNotEmpty()
                        )
                        FilterChip(
                            selected = orderMode == OrderMode.ENTREGA,
                            onClick = { orderMode = OrderMode.ENTREGA },
                            label = { Text(text = "Entrega") },
                            enabled = items.isNotEmpty()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Pagamento */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Forma de pagamento", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { payment = PaymentMethod.PIX },
                            label = { Text(text = "Pix (−R$ 0,50)") },
                            enabled = subtotal > 0.0,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (payment == PaymentMethod.PIX) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surface
                            )
                        )
                        AssistChip(
                            onClick = { payment = PaymentMethod.DINHEIRO },
                            label = { Text(text = "Dinheiro") },
                            enabled = subtotal > 0.0,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (payment == PaymentMethod.DINHEIRO) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { payment = PaymentMethod.DEBITO },
                            label = { Text(text = "Débito (+2%)") },
                            enabled = subtotal > 0.0,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (payment == PaymentMethod.DEBITO) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surface
                            )
                        )
                        AssistChip(
                            onClick = { payment = PaymentMethod.CREDITO },
                            label = { Text(text = "Crédito (+5%)") },
                            enabled = subtotal > 0.0,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (payment == PaymentMethod.CREDITO) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Resumo */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow(label = "Subtotal", value = subtotal)
                    val labelAdjust = when (payment) {
                        PaymentMethod.PIX -> "Desconto Pix"
                        PaymentMethod.DINHEIRO -> "Ajuste"
                        PaymentMethod.DEBITO -> "Taxa Débito (2%)"
                        PaymentMethod.CREDITO -> "Taxa Crédito (5%)"
                    }
                    SummaryRow(label = labelAdjust, value = adjustment)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SummaryRow(label = "Total", value = total, emphasize = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canCheckout = (selectedStore != null && subtotal > 0.0)
            Card(
                onClick = { /* finalizar pedido */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (canCheckout) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
            ) {
                Text(
                    text = if (canCheckout) "concluir pedido" else "selecione a loja e adicione itens",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    orderMode: OrderMode
) {
    val unitPrice = if (orderMode == OrderMode.RETIRADA) item.precoRetirada else item.precoEntrega
    val lineTotal = unitPrice * item.quantidade

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Unidade: ${item.unidade}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Preço unit.: R$ %.2f".format(unitPrice),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onDecrease, contentPadding = PaddingValues(0.dp)) {
                        Text(text = "−", fontSize = 18.sp)
                    }
                    Text(
                        text = item.quantidade.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    OutlinedButton(onClick = onIncrease, contentPadding = PaddingValues(0.dp)) {
                        Text(text = "+", fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total do item: R$ %.2f".format(lineTotal),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onRemove) { Text(text = "Remover") }
            }
        }
    }
}

/* =========================
   Componentes compartilhados
   ========================= */
@Composable
fun HeaderSection(
    expanded: Boolean,
    onExpandClick: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    val onPrimaryFaint = MaterialTheme.colorScheme.onPrimary.copy(alpha = .12f)
    val iconTint = MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(onPrimaryFaint, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = iconTint)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NOSSO GUIA DE COMPRAS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val rot by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "expandIcon")
                    IconButton(onClick = onExpandClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expandir",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.rotate(rot)
                        )
                    }
                }

                IconButton(onClick = onForward, modifier = Modifier.size(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(onPrimaryFaint, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Avançar", tint = iconTint)
                    }
                }
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HeaderChip(icon = Icons.Filled.Info, text = "123.456")
                        HeaderChip(icon = Icons.Filled.LocationOn, text = "MANAUS")
                        HeaderChip(icon = Icons.Filled.Info, text = "123.456.789")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ActionBar(currentPage: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f))) {
            Text(
                text = currentPage,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        OutlinedCard(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.clickable { onNavigate(Routes.INSTRUCOES) }
        ) {
            Text(
                text = "Instruções",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AdsSection() {
    val ads = remember {
        listOf(
            Anuncio("Supermercado ABC", "Promoção Supermercado ABC", "Descontos especiais em frutas e verduras frescas"),
            Anuncio("Seu Gás", "Seu Gás - Entrega Rápida e Segura", "Gás de cozinha e água mineral direto na sua casa. Entrega em até 30 minutos. Qualidade garantida e preços justos."),
            Anuncio("Mercado Central", "Ofertas do Mercado Central", "Carnes frescas com preços especiais"),
            Anuncio("Super Economia", "Super Economia - Semana do Cliente", "Produtos de limpeza com até 30% de desconto")
        )
    }
    var index by remember { mutableIntStateOf(0) }
    fun prev() { if (ads.isNotEmpty()) index = (index - 1 + ads.size) % ads.size }
    fun next() { if (ads.isNotEmpty()) index = (index + 1) % ads.size }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .10f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { prev() }, modifier = Modifier.size(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = .12f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.onSecondary)
                    }
                }

                Text(
                    text = ads[index].anunciante,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { next() }, modifier = Modifier.size(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = .12f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Próximo", tint = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "adsSlide"
                ) { i ->
                    val anuncio = ads[i]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = anuncio.titulo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = anuncio.descricao,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 4,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(ads.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (i == index) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                    if (i < ads.size - 1) Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoriesSection(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryCard(
            title = "SUPERMERCADOS",
            subtitle = "(anúncios grátis)",
            count = 121,
            backgroundColor = MaterialTheme.colorScheme.surface,
            textColor = MaterialTheme.colorScheme.onSurface,
            showCounter = true,
            onClick = { onNavigate(Routes.SUPERMERCADOS) }
        )
        CategoryCard(
            title = "D+ LOJAS",
            subtitle = "(anúncios grátis)",
            count = 0,
            backgroundColor = MaterialTheme.colorScheme.surface,
            textColor = MaterialTheme.colorScheme.onSurface,
            showCounter = true,
            onClick = { }
        )
        CategoryCard(
            title = "UTILIDADE PÚBLICA",
            subtitle = "(anúncios grátis)",
            count = 0,
            backgroundColor = MaterialTheme.colorScheme.surface,
            textColor = MaterialTheme.colorScheme.onSurface,
            showCounter = true,
            onClick = { }
        )
        CategoryCard(
            title = "COMÉRCIO NOSSO",
            subtitle = "cadastre sua loja aqui",
            count = 0,
            backgroundColor = MaterialTheme.colorScheme.background,
            textColor = MaterialTheme.colorScheme.onBackground,
            showCounter = false,
            hasBorder = true,
            onClick = { onNavigate(Routes.CADASTRO_LOJA) }
        )
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    count: Int,
    backgroundColor: Color,
    textColor: Color,
    showCounter: Boolean,
    hasBorder: Boolean = false,
    onClick: () -> Unit
) {
    val modifier =
        if (hasBorder) Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable { onClick() }
        else Modifier.fillMaxWidth().clickable { onClick() }

    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                if (subtitle.isNotEmpty()) {
                    Text(text = subtitle, fontSize = 14.sp, color = textColor.copy(alpha = .7f))
                }
            }
            if (showCounter) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .8f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Lojas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

/* =========================
   BARRA INFERIOR
   ========================= */
@Composable
fun BottomBarContent(onNavigate: (String) -> Unit) {
    val tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .85f)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "00:00:00", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)

        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = "Início",
            tint = tint,
            modifier = Modifier
                .size(22.dp)
                .clickable { onNavigate(Routes.HOME) }
        )

        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Perfil",
            tint = tint,
            modifier = Modifier
                .size(22.dp)
                .clickable { onNavigate(Routes.PERFIL) }
        )

        OutlinedButton(
            onClick = { onNavigate(Routes.SORTEIO) },
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(text = "SORTEIO", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "CPF",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Text(text = "CPF", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Icon(
            imageVector = Icons.Filled.Email,
            contentDescription = "Contato",
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Fechar",
            tint = Color(0xFFD32F2F),
            modifier = Modifier
                .size(20.dp)
                .clickable { /* fechar/voltar */ }
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDE7)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "TELA: $title", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

/* =========================
   NOVA TELA — Cadastro de Lojas
   ========================= */
@Composable
fun CadastroLojaScreen(onBack: () -> Unit) {
    var headerExpanded by remember { mutableStateOf(false) }

    var razaoSocial by remember { mutableStateOf("") }
    var nomeFantasia by remember { mutableStateOf("") }
    var cnpj by remember { mutableStateOf("") }
    var ieIsento by remember { mutableStateOf(true) }
    var inscricaoEstadual by remember { mutableStateOf("") }
    var cnae by remember { mutableStateOf("") }
    var segmento by remember { mutableStateOf("") }

    var telefone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }

    var cep by remember { mutableStateOf("") }
    var logradouro by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var complemento by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var uf by remember { mutableStateOf("") }

    var abreSegSex by remember { mutableStateOf("08:00") }
    var fechaSegSex by remember { mutableStateOf("18:00") }
    var abreSab by remember { mutableStateOf("08:00") }
    var fechaSab by remember { mutableStateOf("13:00") }
    var abreDom by remember { mutableStateOf("") }
    var fechaDom by remember { mutableStateOf("") }
    var fechaAlmoco by remember { mutableStateOf(false) }
    var almocoDe by remember { mutableStateOf("12:00") }
    var almocoAte by remember { mutableStateOf("13:00") }

    var temEntrega by remember { mutableStateOf(true) }
    var temRetirada by remember { mutableStateOf(true) }
    var aceitaPix by remember { mutableStateOf(true) }
    var aceitaDebito by remember { mutableStateOf(true) }
    var aceitaCredito by remember { mutableStateOf(true) }

    var instagram by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }

    var aceitaTermos by remember { mutableStateOf(false) }

    fun isCnpjOk() = cnpj.filter { it.isDigit() }.length in 14..15
    fun isEmailOk() = email.isBlank() || "@" in email
    fun isTelefoneOk(t: String) = t.filter { it.isDigit() }.length in 10..13
    fun isObrigatoriosOk(): Boolean =
        razaoSocial.isNotBlank() && nomeFantasia.isNotBlank() && isCnpjOk() &&
                (ieIsento || inscricaoEstadual.isNotBlank()) &&
                logradouro.isNotBlank() && numero.isNotBlank() && bairro.isNotBlank() && cidade.isNotBlank() && uf.length in 2..3 &&
                aceitaTermos

    val canSalvar = isObrigatoriosOk() && isEmailOk() && isTelefoneOk(telefone.ifBlank { whatsapp })

    Scaffold(
        bottomBar = {
            BottomAppBar(Modifier.height(60.dp), MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onBack) { Text("Cancelar") }
                    Card(
                        onClick = { /* TODO: persistir cadastro */ },
                        enabled = canSalvar,
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canSalvar) MaterialTheme.colorScheme.primary else Color(0xFF9E9E9E)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar cadastro", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                expanded = headerExpanded,
                onExpandClick = { headerExpanded = !headerExpanded },
                onBack = onBack,
                onForward = onBack
            )
            Spacer(Modifier.height(8.dp))
            ActionBar(currentPage = "CADASTRO DE LOJA", onNavigate = { })

            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Identificação da Empresa", icon = Icons.Filled.Info) {
                RowTwo(
                    {
                        LabeledField("Razão Social*", razaoSocial, { razaoSocial = it })
                    },
                    {
                        LabeledField("Nome Fantasia*", nomeFantasia, { nomeFantasia = it })
                    }
                )
                RowTwo(
                    {
                        LabeledField(
                            "CNPJ*",
                            cnpj,
                            { cnpj = it },
                            placeholder = "00.000.000/0001-00",
                            keyboard = KeyboardType.Number,
                            error = cnpj.isNotBlank() && !isCnpjOk(),
                            support = if (cnpj.isNotBlank() && !isCnpjOk()) "Informe um CNPJ válido (14 dígitos)." else null
                        )
                    },
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = ieIsento, onCheckedChange = { ieIsento = it })
                            Text("Isento de Inscrição Estadual", modifier = Modifier.padding(start = 4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        LabeledField(
                            "Inscrição Estadual",
                            inscricaoEstadual,
                            { inscricaoEstadual = it },
                            enabled = !ieIsento
                        )
                    }
                )
                RowTwo(
                    {
                        LabeledField("CNAE principal", cnae, { cnae = it }, placeholder = "ex.: 4711-3/02")
                    },
                    {
                        LabeledField("Segmento", segmento, { segmento = it }, placeholder = "ex.: Supermercado")
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Contatos", icon = Icons.Filled.Call) {
                RowTwo(
                    {
                        LabeledField("Telefone", telefone, { telefone = it }, placeholder = "(92) 0000-0000", keyboard = KeyboardType.Phone,
                            error = telefone.isNotBlank() && !isTelefoneOk(telefone),
                            support = if (telefone.isNotBlank() && !isTelefoneOk(telefone)) "Telefone incompleto." else null
                        )
                    },
                    {
                        LabeledField("WhatsApp", whatsapp, { whatsapp = it }, placeholder = "(92) 90000-0000", keyboard = KeyboardType.Phone)
                    }
                )
                RowTwo(
                    {
                        LabeledField("E-mail", email, { email = it }, keyboard = KeyboardType.Email,
                            error = email.isNotBlank() && !isEmailOk(),
                            support = if (email.isNotBlank() && !isEmailOk()) "E-mail inválido." else null
                        )
                    },
                    {
                        LabeledField("Site", site, { site = it }, placeholder = "ex.: www.minhaloja.com.br")
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Endereço Comercial", icon = Icons.Filled.LocationOn) {
                RowTwo(
                    {
                        LabeledField("CEP", cep, { cep = it }, keyboard = KeyboardType.Number, placeholder = "69000-000")
                    },
                    {
                        LabeledField("UF*", uf, { uf = it.uppercase() }, placeholder = "AM")
                    }
                )
                LabeledField("Logradouro*", logradouro, { logradouro = it }, placeholder = "Rua/Avenida")
                RowTwo(
                    {
                        LabeledField("Número*", numero, { numero = it }, keyboard = KeyboardType.Number)
                    },
                    {
                        LabeledField("Complemento", complemento, { complemento = it })
                    }
                )
                RowTwo(
                    {
                        LabeledField("Bairro*", bairro, { bairro = it })
                    },
                    {
                        LabeledField("Cidade*", cidade, { cidade = it })
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Horário de Funcionamento", icon = Icons.Filled.Info, description = "Defina horários claros (seg-sex, sábado e domingo). Se houver pausa de almoço, marque abaixo.") {
                RowTwo(
                    { LabeledField("Seg–Sex abre", abreSegSex, { abreSegSex = it }, placeholder = "08:00") },
                    { LabeledField("Seg–Sex fecha", fechaSegSex, { fechaSegSex = it }, placeholder = "18:00") },
                )
                RowTwo(
                    { LabeledField("Sábado abre", abreSab, { abreSab = it }, placeholder = "08:00") },
                    { LabeledField("Sábado fecha", fechaSab, { fechaSab = it }, placeholder = "13:00") },
                )
                RowTwo(
                    { LabeledField("Domingo abre", abreDom, { abreDom = it }, placeholder = "fechado") },
                    { LabeledField("Domingo fecha", fechaDom, { fechaDom = it }, placeholder = "fechado") },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = fechaAlmoco, onCheckedChange = { fechaAlmoco = it })
                    Text("Fecha para almoço", modifier = Modifier.padding(start = 8.dp))
                }
                if (fechaAlmoco) {
                    RowTwo(
                        { LabeledField("Almoço de", almocoDe, { almocoDe = it }, placeholder = "12:00") },
                        { LabeledField("Almoço até", almocoAte, { almocoAte = it }, placeholder = "13:00") }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Serviços e Pagamento") {
                Text("Oferece:", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = temEntrega, onCheckedChange = { temEntrega = it }); Text("Entrega")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = temRetirada, onCheckedChange = { temRetirada = it }); Text("Retirada")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Aceita:", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = aceitaPix, onCheckedChange = { aceitaPix = it }); Text("Pix") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = aceitaDebito, onCheckedChange = { aceitaDebito = it }); Text("Débito") }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = aceitaCredito, onCheckedChange = { aceitaCredito = it }); Text("Crédito") }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Redes sociais") {
                RowTwo(
                    { LabeledField("Instagram", instagram, { instagram = it }, placeholder = "@sualoja") },
                    { LabeledField("Facebook", facebook, { facebook = it }, placeholder = "fb.com/sualoja") },
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Termos de uso e privacidade") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = aceitaTermos, onCheckedChange = { aceitaTermos = it })
                    Text("Li e aceito os termos de uso e a política de privacidade.")
                }
                if (!aceitaTermos) {
                    Text("Aceite os termos para prosseguir.", color = Color(0xFFD32F2F), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

/* ===== Helpers visuais para a tela de cadastro ===== */

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector? = null,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    error: Boolean = false,
    support: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (!placeholder.isNullOrBlank()) Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        isError = error,
        supportingText = { if (!support.isNullOrBlank()) Text(support) }
    )
}

@Composable
private fun RowTwo(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) { left() }
        Column(modifier = Modifier.weight(1f)) { right() }
    }
}

/* =========================
   Sumário simples
   ========================= */
@Composable
private fun SummaryRow(label: String, value: Double, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            fontSize = if (emphasize) 18.sp else 16.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        val valueText = if (value >= 0) "R$ %.2f".format(value) else "− R$ %.2f".format(kotlin.math.abs(value))
        Text(
            text = valueText,
            fontSize = if (emphasize) 18.sp else 16.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/* =========================
   Lista de supermercado (reutilizada)
   ========================= */
@Composable
private fun SupermercadoItem(
    nome: String,
    contador: Int,
    isDestaque: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isDestaque && nome == "ESCOLHER SUPERMERCADO" -> MaterialTheme.colorScheme.surface
        isDestaque && nome == "BARATÃO DA CARNE" -> MaterialTheme.colorScheme.surface
        isDestaque && nome == "SEU GÁS" -> MaterialTheme.colorScheme.surface
        nome.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { if (nome.isNotEmpty()) onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nome,
                fontSize = 16.sp,
                fontWeight = if (isDestaque) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            if (nome.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .8f))
                ) {
                    Text(
                        text = contador.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
