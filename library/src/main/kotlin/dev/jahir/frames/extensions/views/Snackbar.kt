@file:Suppress("unused")

package dev.jahir.frames.extensions.views

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.string

fun View.snackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    val snack = Snackbar.make(this, text, duration)
    try {
        snack.setAnchorView(anchorViewId)
    } catch (e: Exception) {
    }
    val textView: TextView? by snack.view.findView(R.id.snackbar_text)
    textView?.maxLines = 3
    snack.apply(config)
    if (!snack.isShownOrQueued) snack.show()
}

fun View.snackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    snackbar(context.string(text), duration, anchorViewId, config)
}

fun Activity.snackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    contentView?.snackbar(text, duration, anchorViewId, config)
}

fun Activity.snackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    snackbar(string(text), duration, anchorViewId, config)
}

fun Fragment.snackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    activity?.snackbar(text, duration, anchorViewId, config)
        ?: view?.snackbar(text, duration, anchorViewId, config)
}

fun Fragment.snackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    snackbar(context?.string(text).orEmpty(), duration, anchorViewId, config)
}

inline val Activity.contentView: View?
    get() = (findViewById(android.R.id.content) as? ViewGroup)?.getChildAt(0)