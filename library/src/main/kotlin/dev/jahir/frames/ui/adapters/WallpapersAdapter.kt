package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.inflate
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder

class WallpapersAdapter(
    var onClick: (Wallpaper, WallpaperViewHolder) -> Unit = { _, _ -> },
    var onFavClick: (Boolean, Wallpaper) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<WallpaperViewHolder>() {

    var wallpapers: ArrayList<Wallpaper> = ArrayList()
        set(value) {
            wallpapers.clear()
            wallpapers.addAll(value)
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(wallpapers[position], onClick, onFavClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder =
        WallpaperViewHolder(parent.inflate(R.layout.item_wallpaper))

    override fun getItemCount(): Int = wallpapers.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position
}