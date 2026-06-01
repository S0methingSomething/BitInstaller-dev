package dev.bitinstaller.app

internal fun BitInstallerAppState.showBusyNotice(message: String) {
    noticeToken += 1
    noticeMessage = message
}

internal fun BitInstallerAppState.busyMessageForPatch(targetName: String): String =
    when {
        saveScanTargetId != null -> "Save scan is analyzing files. Wait before opening $targetName MonetizationVars."
        saveEditTargetPath != null -> "Save edit is writing a backup. Wait before opening $targetName MonetizationVars."
        loadingTargetId != null -> "Patch editor is already opening. Wait before starting another patch."
        else -> "Another file operation is running. Wait before opening $targetName MonetizationVars."
    }

internal fun BitInstallerAppState.busyMessageForSaveScan(targetName: String): String =
    when {
        loadingTargetId != null -> "Patch editor is opening. Wait before scanning $targetName saves."
        saveScanTargetId != null -> "Save scan is already analyzing files. Wait for it to finish."
        saveEditTargetPath != null -> "Save edit is writing a backup. Wait before scanning saves."
        else -> "Another file operation is running. Wait before scanning $targetName saves."
    }

internal fun BitInstallerAppState.busyMessageForSaveEdit(heroName: String): String =
    when {
        loadingTargetId != null -> "Patch editor is opening. Wait before editing $heroName."
        saveScanTargetId != null -> "Save scan is analyzing files. Wait before editing $heroName."
        saveEditTargetPath != null -> "A save edit is already writing. Wait before changing another value."
        else -> "Another file operation is running. Wait before editing $heroName."
    }
