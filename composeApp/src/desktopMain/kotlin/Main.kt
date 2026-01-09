import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.financehub.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.awt.FileDialog
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun main() = application {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "FinanceHub - Análise Financeira",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        // Caminho padrão da pasta Notas dentro do projeto
        val defaultPath = System.getProperty("user.dir") + "/Notas"
        
        App(
            directoryPath = defaultPath,
            coroutineScope = coroutineScope,
            onSelectFiles = { selectMultipleFiles() }
        )
    }
}

/**
 * Abre diálogo nativo para selecionar múltiplos arquivos CSV/PDF
 */
fun selectMultipleFiles(): List<String> {
    val fileChooser = JFileChooser().apply {
        dialogTitle = "Selecione arquivos CSV ou PDF"
        isMultiSelectionEnabled = true
        fileSelectionMode = JFileChooser.FILES_ONLY
        
        // Filtro para CSV e PDF
        addChoosableFileFilter(
            FileNameExtensionFilter("Arquivos Financeiros (*.csv, *.pdf)", "csv", "pdf")
        )
        addChoosableFileFilter(
            FileNameExtensionFilter("Arquivos CSV (*.csv)", "csv")
        )
        addChoosableFileFilter(
            FileNameExtensionFilter("Arquivos PDF (*.pdf)", "pdf")
        )
        
        // Começar na pasta Notas se existir
        val notasPath = System.getProperty("user.dir") + "/Notas"
        val notasDir = File(notasPath)
        if (notasDir.exists() && notasDir.isDirectory) {
            currentDirectory = notasDir
        }
    }
    
    val result = fileChooser.showOpenDialog(null)
    
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFiles.map { it.absolutePath }
    } else {
        emptyList()
    }
}
