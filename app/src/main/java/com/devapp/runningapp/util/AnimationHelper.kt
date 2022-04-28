package com.devapp.runningapp.util

import android.content.Context
import android.view.View
import android.view.animation.*
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import com.devapp.runningapp.R
import kotlin.math.sin

object AnimationHelper {
    private const val duration: Long = 100
    private const val scaleDefault = 0.94f
    fun scaleAnimation(view: View?, animationListener: VoidCallback, scale: Float) {
        if (view == null) {
            animationListener.execute()
            return
        }
        ViewCompat.animate(view)
            .setDuration(duration)
            .scaleX(if (scale != 0f) scale else scaleDefault)
            .scaleY(if (scale != 0f) scale else scaleDefault)
            .setInterpolator(CycleInterpolator())
            .setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationStart(view: View) {}
                override fun onAnimationEnd(view: View) {
                    // some thing
                    animationListener.execute()
                }

                override fun onAnimationCancel(view: View) {}
            })
            .withLayer()
            .start()
    }

    fun shakeAnimation(context: Context?, view: View, animationListener: VoidCallback?) {
        AnimationUtils.loadAnimation(context, R.anim.shake).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    animationListener?.execute()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            view.startAnimation(this)
        }
    }

    fun fadeIn(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(fadeIn)
        view.startAnimation(animation)
    }

    fun fadeOut(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = DecelerateInterpolator()
        fadeOut.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(fadeOut)
        view.startAnimation(animation)
    }

    // đi từ bên phải vào
    fun rightIn(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val rightIn = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
        )
        rightIn.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(rightIn)
        view.startAnimation(animation)
    }

    // đi ra bên phải
    fun rightOut(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val rightOut = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
        )
        rightOut.duration = duration
        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(rightOut)
        view.startAnimation(animation)
    }

    // đi từ bên trái vào
    fun leftIn(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val leftIn = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
        )
        leftIn.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(leftIn)
        view.startAnimation(animation)
    }

    // đi ra bên trái
    fun leftOut(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val leftOut = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
        )
        leftOut.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(leftOut)
        view.startAnimation(animation)
    }

    // đi vào bên trên
    fun topIn(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val topIn = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f
        )
        topIn.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(topIn)
        view.startAnimation(animation)
    }

    // đi ra bên trên
    fun topOut(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val topOut = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f
        )
        topOut.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(topOut)
        view.startAnimation(animation)
    }

    // đi vào bên dưới
    fun bottomIn(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val bottomIn = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f
        )
        bottomIn.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // some thing
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(bottomIn)
        view.startAnimation(animation)
    }

    // đi ra bên dưới
    fun bottomOut(view: View?, animationListener: VoidCallback?, duration: Long) {
        if (view == null) {
            animationListener?.execute()
            return
        }

        val bottomOut = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f
        )
        bottomOut.duration = duration

        val animation = AnimationSet(false)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                animationListener?.execute()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        animation.addAnimation(bottomOut)
        view.startAnimation(animation)
    }

    private class CycleInterpolator : Interpolator {
        private val mCycles = 0.5f
        override fun getInterpolation(input: Float): Float {
            return sin(2.0f * mCycles * Math.PI * input).toFloat()
        }
    }
}