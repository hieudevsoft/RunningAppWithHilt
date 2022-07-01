package com.devapp.runningapp.model

import android.content.Context
import androidx.annotation.DrawableRes
import com.devapp.runningapp.R
import com.devapp.runningapp.util.Constant

class Award {
    companion object{
        enum class AwardType{
            TIME,
            AVG_SPEED,
            DISTANCE,
            CALORIES
        }

        private fun getIconByTypeAndIndex(context:Context,type:AwardType,index:Int):Int?{
            when(type){
                AwardType.TIME->{
                    when(index){
                        0-> return R.drawable.ic_startup
                        1-> return R.drawable.ic_momentum
                        2-> return R.drawable.ic_hard
                        3-> return R.drawable.ic_time_color
                        4-> return R.drawable.ic_doctor
                    }
                }

                AwardType.AVG_SPEED->{
                    when(index){
                        0-> return R.drawable.ic_warmup_speed
                        1-> return R.drawable.ic_accleartion
                        2-> return R.drawable.ic_usain_bolt
                        3-> return R.drawable.ic_ghost_rider
                        4-> return R.drawable.ic_the_flash
                    }
                }

                AwardType.DISTANCE->{
                    when(index){
                        0-> return R.drawable.ic_warmup_distance
                        1-> return R.drawable.ic_enduring
                        2-> return R.drawable.ic_distance
                        3-> return R.drawable.ic_lord_distance
                        4-> return R.drawable.ic_hulk
                    }
                }

                AwardType.CALORIES->{
                    when(index){
                        0-> return R.drawable.ic_warm_up_calories
                        1-> return R.drawable.ic_calories_fire
                        2-> return R.drawable.ic_calorieser
                        3-> return R.drawable.ic_the_rock
                        4-> return R.drawable.ic_saitama
                    }
                }

            }
            return null
        }

        fun getProcess(maxValue:Int, value: Float): Float {
            if(value>=maxValue) return 100f
            return ((value.toFloat()/maxValue.toFloat())*100)
        }


        data class AwardResult(
            val type:AwardType,
            val icons:HashMap<Int,Int> = hashMapOf(),
            var awards:HashMap<Int,Int> = hashMapOf(),
        )

        data class AwardEntry(
            val type:AwardType,
            val data:HashMap<Int,Int>
        )
    }
}