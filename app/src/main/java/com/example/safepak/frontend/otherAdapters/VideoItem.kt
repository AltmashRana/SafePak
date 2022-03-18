package com.example.safepak.frontend.otherAdapters

import android.net.Uri
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.video_item.view.*
import java.io.File

import android.content.Intent
import androidx.core.content.FileProvider
import com.example.safepak.frontend.other.FacesActivity


class VideoItem(var file: File): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.video_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.fall_down)

        val filename = file.name.split('-')[0]
//        val title = filename[0].toString()
//        val time = filename[1].toString().substringBefore('.')

        viewHolder.itemView.videotime_text.text = file.name

        viewHolder.itemView.videotitle_text.text = "Incident"

        val uri = Uri.fromFile(file)

        Glide.with(viewHolder.itemView.context)
            .load(uri).thumbnail(0.1f)
            .into(viewHolder.itemView.video_image)

        viewHolder.itemView.video_image.setOnClickListener {
            val fileWithinMyDir = file
            fileWithinMyDir.setReadable(true, false)

            val intent = Intent()
            val intentUri = FileProvider.getUriForFile(
                viewHolder.itemView.context, viewHolder.itemView.context.applicationContext
                    .packageName.toString() + ".files.Safepak", file
            )
            intent.action = Intent.ACTION_VIEW
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(intentUri, "video/mp4")
            viewHolder.itemView.context.startActivity(intent)
        }

        viewHolder.itemView.videoextract_bt.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, FacesActivity::class.java)
            intent.putExtra("FILE_NAME", file.name)
            viewHolder.itemView.context.startActivity(intent)
        }

        viewHolder.itemView.startAnimation(animation)
    }
}