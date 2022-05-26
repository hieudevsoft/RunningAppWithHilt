package com.devapp.runningapp.ui.dialog.bsdf

import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.devapp.runningapp.R
import com.devapp.runningapp.ui.base.BaseFullScreenBSDF
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.AppHelper
import com.devapp.runningapp.util.VoidCallback
import com.github.chrisbanes.photoview.PhotoView

class ViewPhotosBSDF : BaseFullScreenBSDF() {
    companion object {
        private var instance: ViewPhotosBSDF? = null
        fun <T> show(image: T, fm: FragmentManager) {
            if (instance?.isAdded == true) return
            val args = Bundle()
            val result = if(image is String) image else AppHelper.encodeToBase64(image as Bitmap)
            args.putString("image", result)
            instance = ViewPhotosBSDF()
            instance?.arguments = args
            instance?.show(fm, instance?.tag ?: "full_image")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bsdf_view_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgument()
        view.findViewById<AppCompatImageView>(R.id.img_close).setOnClickListener {
            AnimationHelper.scaleAnimation(it, object : VoidCallback {
                override fun execute() {
                    dismiss()
                }
            }, 0.95f)
        }
    }

    private fun getArgument() {
        val image = arguments?.getString("image", "")
        if (image.isNullOrEmpty()) return
        view?.findViewById<PhotoView>(R.id.photo_view)?.let {
            try {
                Glide.with(context ?: return).asBitmap().load(AppHelper.decodeBase64(image)).into(it)
            }catch (e:Exception){
                Glide.with(context ?: return).load(image).into(it)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        instance = null
    }
}