package net.thunderbird.cli.l10n.terminal.component

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text

@Composable
fun Title(text: String) {
    Column {
        Text(text)
        Spacer(Modifier.height(1))
    }
}
