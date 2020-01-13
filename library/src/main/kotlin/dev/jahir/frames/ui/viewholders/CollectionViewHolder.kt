package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.palette.graphics.Palette
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.bestTextColor
import dev.jahir.frames.extensions.context
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.loadFramesPic
import dev.jahir.frames.extensions.withAlpha

class CollectionViewHolder(view: View) : PaletteGeneratorViewHolder(view) {

    private val image: AppCompatImageView? by view.findView(R.id.wallpaper_image)
    private val title: TextView? by view.findView(R.id.collection_title)
    private val count: TextView? by view.findView(R.id.collection_count)
    private val detailsBackground: View? by view.findView(R.id.collection_details_background)

    internal val shouldBeFilled: Boolean by lazy {
        try {
            context.resources.getBoolean(R.bool.enable_filled_collection_preview)
        } catch (e: Exception) {
            false
        }
    }

    fun bind(
        collection: Collection,
        onClick: ((collection: Collection) -> Unit)? = null
    ) {
        title?.text = collection.name
        count?.text = collection.count.toString()
        itemView.setOnClickListener { onClick?.invoke(collection) }
        collection.cover?.let {
            image?.loadFramesPic(
                it.url,
                it.thumbnail,
                context.getString(R.string.collections_placeholder),
                doWithPalette = if (shouldColorTiles) generatePalette else null
            )
        }
    }

    override fun doWithBestSwatch(swatch: Palette.Swatch) {
        detailsBackground?.setBackgroundColor(
            swatch.rgb.withAlpha(
                if (shouldBeFilled) FILLED_COLORED_TILES_ALPHA
                else WallpaperViewHolder.COLORED_TILES_ALPHA
            )
        )
        title?.setTextColor(swatch.bestTextColor)
        count?.setTextColor(swatch.bestTextColor)
    }

    companion object {
        private const val FILLED_COLORED_TILES_ALPHA =
            WallpaperViewHolder.COLORED_TILES_ALPHA - .15F
    }
}