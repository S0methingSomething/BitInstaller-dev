package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal data class SaveFieldEditDraft(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val field: SaveEditableField,
)
