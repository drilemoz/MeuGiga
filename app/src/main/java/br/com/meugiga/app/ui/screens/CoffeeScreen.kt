package br.com.meugiga.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.meugiga.app.domain.PixPayload
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.math.BigDecimal

@Composable
fun CoffeeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var amountOption by remember { mutableStateOf("Livre") }
    var customAmount by remember { mutableStateOf("") }
    val amount: BigDecimal? = when (amountOption) {
        "R$ 5" -> BigDecimal("5.00")
        "R$ 10" -> BigDecimal("10.00")
        "R$ 10,10" -> BigDecimal("10.10")
        else -> if (customAmount.isBlank()) null else PixPayload.parseAmount(customAmount)
    }
    val invalid = amountOption == "Livre" && customAmount.isNotBlank() && amount == null
    val pix = remember(amount, invalid) { if (invalid) null else PixPayload.build(amount) }
    val qr = remember(pix) { pix?.let { createPixQr(it) } }

    InfoScreenScaffold("Me Pague um Café", onBack, Modifier) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Coffee, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text("Apoie o Meu Giga", style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 10.dp))
                }
                Text(
                    "Contribuição voluntária. Nenhuma função é desbloqueada por pagamento. " +
                        "Apoiar é opcional e não altera o uso do aplicativo.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                Text("Escolha um valor ou informe outro", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("R$ 5", "R$ 10", "R$ 10,10", "Livre").forEach { option ->
                        FilterChip(selected = amountOption == option,
                            onClick = { amountOption = option }, label = { Text(option) })
                    }
                }
                if (amountOption == "Livre") {
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it.take(12) },
                        label = { Text("Valor opcional em R$") },
                        placeholder = { Text("Em branco: escolha no banco") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = invalid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (invalid) Text("Informe um valor positivo com até duas casas decimais.",
                        color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Minha chave Pix", style = MaterialTheme.typography.titleMedium)
                        Text(PixPayload.PIX_KEY)
                        OutlinedButton(onClick = { copyText(context, "Chave Pix", PixPayload.PIX_KEY, "Chave Pix copiada!") }) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                            Text("Copiar chave", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
            if (pix != null && qr != null) {
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("QR Code Pix", style = MaterialTheme.typography.titleMedium)
                        Image(bitmap = qr.asImageBitmap(), contentDescription = "QR Code Pix estático para apoio voluntário",
                            modifier = Modifier.size(224.dp))
                        Button(onClick = { copyText(context, "Pix copia e cola", pix, "Pix copia e cola copiado!") }) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                            Text("Copiar Pix copia e cola", modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(
                            if (amount == null) "Valor livre: informe quanto deseja no banco."
                            else "Valor escolhido: R$ ${amount.toPlainString().replace('.', ',')}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            item {
                Text("Antes de confirmar, confira a chave e o nome do recebedor no aplicativo do banco. " +
                    "O Meu Giga não processa nem confirma transferências.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                OutlinedButton(onClick = { openGooglePlay(context) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Star, contentDescription = null)
                    Text("Avaliar no Google Play", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

private fun createPixQr(payload: String): Bitmap? = runCatching {
    val matrix = MultiFormatWriter().encode(
        payload, BarcodeFormat.QR_CODE, 448, 448,
        mapOf(EncodeHintType.MARGIN to 2),
    )
    Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (y in 0 until matrix.height) for (x in 0 until matrix.width)
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    }
}.getOrNull()

private fun copyText(context: Context, label: String, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun openGooglePlay(context: Context) {
    val pkg = context.packageName.removeSuffix(".debug")
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
    try {
        context.startActivity(market)
    } catch (_: Exception) {
        try { context.startActivity(fallback) }
        catch (_: Exception) {
            Toast.makeText(context, "Google Play indisponível neste aparelho.", Toast.LENGTH_SHORT).show()
        }
    }
}
