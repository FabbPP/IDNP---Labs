package com.example.tarjetascampus_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.tarjetascampus_compose.ui.theme.TarjetasCampus_ComposeTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarjetasCampus_ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object List : Screen()
    data class Detail(val nombre: String, val descripcion: String, val imagenRes: Int) : Screen()
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val screen = currentScreen) {
        is Screen.List -> {
            PantallaTarjetas(
                onNavigateToDetail = { nombre, desc, imgRes ->
                    currentScreen = Screen.Detail(nombre, desc, imgRes)
                }
            )
        }
        is Screen.Detail -> {
            DetalleLugarScreen(
                nombre = screen.nombre,
                descripcion = screen.descripcion,
                imagen = painterResource(id = screen.imagenRes),
                onBack = { currentScreen = Screen.List }
            )
        }
    }
}

@Composable
fun PantallaTarjetas(onNavigateToDetail: (String, String, Int) -> Unit = {_,_,_->}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Estilo 1: tarjetas con Row (imagen circular + texto)
        ListaTarjetasLugar(onNavigateToDetail)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Estilo 2: tarjetas apiladas con Box (imagen de fondo + scrim + texto)
        TarjetaCompacta(
            nombre = "Biblioteca Central",
            imagen = painterResource(id = R.drawable.biblioteca),
            onClick = { onNavigateToDetail("Biblioteca Central", "El centro principal de recursos literarios y espacios de estudio silencioso. Cuenta con más de 100,000 ejemplares y salas de lectura grupales.", R.drawable.biblioteca) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        TarjetaCompacta(
            nombre = "Comedor Universitario",
            imagen = painterResource(id = R.drawable.comedor),
            onClick = { onNavigateToDetail("Comedor Universitario", "Servicio de alimentación principal para la comunidad estudiantil. Capacidad amplia y menú variado.", R.drawable.comedor) }
        )
    }
}

@Composable
fun TarjetaLugar(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    colorFondo: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorFondo,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = descripcion,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TarjetaCompacta(
    nombre: String,
    imagen: Painter,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = imagen,
            contentDescription = nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        Text(
            text = nombre,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

@Composable
fun ListaTarjetasLugar(onNavigateToDetail: (String, String, Int) -> Unit = {_,_,_->}) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaLugar(
            nombre = "Biblioteca Central",
            descripcion = "Zona de estudio y préstamo de libros",
            imagen = painterResource(id = R.drawable.biblioteca),
            colorFondo = Color(0xFFE3F2FD),
            onClick = { onNavigateToDetail("Biblioteca Central", "Zona de estudio y préstamo de libros", R.drawable.biblioteca) }
        )
        TarjetaLugar(
            nombre = "Comedor Universitario",
            descripcion = "Servicio de alimentación para estudiantes",
            imagen = painterResource(id = R.drawable.comedor),
            colorFondo = Color(0xFFFFF3E0),
            onClick = { onNavigateToDetail("Comedor Universitario", "Servicio de alimentación para estudiantes", R.drawable.comedor) }
        )
    }
}
@Composable
fun DetalleLugarScreen(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    horario: String = "07:00 - 20:00",
    aforo: String = "Capacidad: 250 personas",
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Banner Hero a tamaño completo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        ) {
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.40f))
            )
            // Agregado un elemento decorativo: Un pill de estado en la parte superior derecha
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Abierto",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Text(
                text = nombre,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            )
        }

        // Bloque de información y descripción
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Descripción General", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = descripcion, fontSize = 14.sp, color = Color(0xFF555555))
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Horario: $horario", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = aforo, fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFC8310E),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "Volver a Edificios", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleLugarScreenPreview() {
    TarjetasCampus_ComposeTheme {
        DetalleLugarScreen(
            nombre = "Biblioteca Central",
            descripcion = "El centro principal de recursos literarios y espacios de estudio silencioso. Cuenta con más de 100,000 ejemplares y salas de lectura grupales.",
            imagen = painterResource(id = R.drawable.biblioteca),
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaTarjetasPreview() {
    TarjetasCampus_ComposeTheme {
        PantallaTarjetas()
    }
}