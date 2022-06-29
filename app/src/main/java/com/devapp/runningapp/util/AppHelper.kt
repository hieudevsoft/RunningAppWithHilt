package com.devapp.runningapp.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.devapp.runningapp.R
import com.google.android.material.textview.MaterialTextView
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.muddz.styleabletoast.StyleableToast
import java.io.ByteArrayOutputStream
import java.util.ArrayList


object AppHelper {
    fun Context.convertDpToPixel(dp: Float): Float {
        return dp * (this.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun Any.toJson(): String = Gson().toJson(this)

    fun <T> String.fromJson(clazz: Class<*>): T {
        return Gson().fromJson(this, clazz) as T
    }

    fun <T> String.fromJson(): List<T> {
        return Gson().fromJson(this, object: TypeToken<List<T>>(){}.type) as List<T>
    }

    fun goStore(activity: Activity) {
        var appPackageName = ""
        try {
            appPackageName = activity.packageName // getPackageName() from Context or Activity object
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            return
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        goUrl(activity, "https://play.google.com/store/apps/details?id=$appPackageName")
    }

    fun goUrl(activity: Activity, url: String) {
        var webpage = Uri.parse(url)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            webpage = Uri.parse("http://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(activity.packageManager) != null) activity.startActivity(intent)
    }

    fun encodeToBase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun decodeBase64(input: String?): Bitmap? {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    fun bitmapToBytes(image:Bitmap):ByteArray{
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, baos)
        return baos.toByteArray()
    }

    fun Context.showToastNotConnectInternet() = StyleableToast.makeText(this,this.getString(R.string.no_connect),R.style.toast_internet).show()
    fun Context.showStyleableToast(msg:String,isSuccess:Boolean) = StyleableToast.makeText(this,msg,if(isSuccess) R.style.toast_success else R.style.toast_internet).show()
    fun Fragment.showToastNotConnectInternet() = StyleableToast.makeText(requireContext(),requireContext().getString(R.string.no_connect),R.style.toast_internet).show()
    fun Fragment.showStyleableToast(msg:String,isSuccess:Boolean) = StyleableToast.makeText(requireContext(),msg,if(isSuccess) R.style.toast_success else R.style.toast_internet).show()

    fun Context.getColorContextCompat(color:Int) = ContextCompat.getColor(this,color)
    fun Activity.getColorContextCompat(color:Int) = ContextCompat.getColor(this,color)
    fun Fragment.getColorContextCompat(color:Int) = ContextCompat.getColor(this.requireContext(),color)

    fun Context.showSystemToast(msg:String) = Toast.makeText(this,msg,Toast.LENGTH_SHORT)
    fun Context.showErrorToast(msg:String) = StyleableToast.makeText(this,msg, R.style.toast_error).show()
    fun Context.showSuccessToast(msg:String) = StyleableToast.makeText(this,msg, R.style.toast_success).show()

    fun Fragment.findOnClickListener(vararg views: View, event:(View.OnClickListener)){
        views.forEach {
            it.setOnClickListener(event)
        }
    }
    fun Fragment.findOnClickWithScaleListener(vararg views:View,event:(View)->Unit){
        views.forEach {
            it.setOnClickListener {
                AnimationHelper.scaleAnimation(it,object:VoidCallback{
                    override fun execute() {
                        event.invoke(it)
                    }

                },0.96f)
            }
        }
    }

    fun Activity.findOnClickListener(vararg views:View,event:(View.OnClickListener)){
        views.forEach {
            it.setOnClickListener(event)
        }
    }
    fun Activity.findOnClickWithScaleListener(vararg views:View,event:(View)->Unit){
        views.forEach {
            it.setOnClickListener {
                AnimationHelper.scaleAnimation(it,object:VoidCallback{
                    override fun execute() {
                        event.invoke(it)
                    }
                },0.96f)
            }
        }
    }

    fun View.setOnClickWithScaleListener(event: () -> Unit){
        this.setOnClickListener {
            AnimationHelper.scaleAnimation(it,object:VoidCallback{
                override fun execute() {
                    event.invoke()
                }
            },0.96f)
        }
    }

    fun addDotPrice(price: Long): String {
        if (price < 1000) return "$price"

        var priceCached = price
        val priceSplit = ArrayList<Int>()
        while (priceCached > 0) {
            val number = priceCached % 10
            priceSplit.add(number.toInt())
            priceCached /= 10
        }

        var textPrice = ""
        for (i in 0 until priceSplit.size) {
            if (i > 0 && i % 3 == 0) textPrice = ".$textPrice"
            textPrice = "${priceSplit[i]}$textPrice"
        }
        return textPrice
    }

    fun showDialogSkipAds(
        activity: Activity?,
        booleanCallback: BooleanCallback,
    ) {
        if (activity == null) return
        val builder = AlertDialog.Builder(activity, R.style.bottom_top_dialog)
        val mView = activity.layoutInflater.inflate(R.layout.dialog_skip_ads, null)

        val btnContinue = mView.findViewById<CardView>(R.id.btn_continue)
        val btnUpgrade = mView.findViewById<CardView>(R.id.btn_upgrade)

        builder.setView(mView)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnContinue.setOnClickListener {
            booleanCallback.execute(false)
            dialog.dismiss() }
        btnUpgrade.setOnClickListener {
            booleanCallback.execute(true)
            dialog.dismiss()
        }
        dialog.setOnCancelListener {
            booleanCallback.execute(false)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showDialogTransfer(
        activity: Activity?,
        voidCallback: VoidCallback,
    ) {
        if (activity == null) return
        val builder = AlertDialog.Builder(activity, R.style.bottom_top_dialog)
        val mView = activity.layoutInflater.inflate(R.layout.dialog_transfer, null)

        val btnUpgrade = mView.findViewById<CardView>(R.id.btn_upgrade)

        builder.setView(mView)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnUpgrade.setOnClickListener {
            voidCallback.execute()
            dialog.dismiss()
        }
        dialog.setOnCancelListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showDialogPayment(
        activity: Activity?,
        isSuccess:Boolean
    ) {
        if (activity == null) return
        val builder = AlertDialog.Builder(activity, R.style.bottom_top_dialog)
        val mView = activity.layoutInflater.inflate(R.layout.dialog_payment_result, null)

        val btnClose = mView.findViewById<CardView>(R.id.btn_close)
        val tvTitle = mView.findViewById<MaterialTextView>(R.id.tv_title)
        val tvContent = mView.findViewById<MaterialTextView>(R.id.tv_content)
        val imgHeader = mView.findViewById<AppCompatImageView>(R.id.iv_payment)
        if(!isSuccess){
            tvTitle.text = "Upgrade failure"
            tvTitle.setTextColor(ContextCompat.getColor(activity,R.color.colorRed_5))
            tvContent.text = activity.getString(R.string.payment_fail)
            imgHeader.setImageResource(R.drawable.img_payment_fail)
            btnClose.setCardBackgroundColor(ContextCompat.getColor(activity,R.color.colorRed_5))
        }


        builder.setView(mView)
        val dialog = builder.create().also { it.setCancelable(false) }
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setOnCancelListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}