package dev.jahir.frames.ui.activities.base

import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import dev.jahir.frames.R
import dev.jahir.frames.extensions.isUpdate
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.buildChangelogDialog

abstract class BaseChangelogDialogActivity<out P : Prefs> : BaseSearchableActivity<P>() {

    private val changelogDialog: AlertDialog? by lazy { buildChangelogDialog() }

    internal fun showChangelog(force: Boolean = false) {
        if (isUpdate || force) changelogDialog?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.changelog) showChangelog(true)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        changelogDialog?.dismiss()
        super.onDestroy()
    }
}