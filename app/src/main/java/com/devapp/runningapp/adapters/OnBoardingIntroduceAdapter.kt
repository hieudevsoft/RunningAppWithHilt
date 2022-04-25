package com.devapp.runningapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.devapp.runningapp.databinding.ItemOnboardingIntroduceBinding
import com.devapp.runningapp.model.OnBoardingIntroduceItem

class OnBoardingIntroduceAdapter constructor(
    private val context: Context,
    private val introduceList: List<OnBoardingIntroduceItem>
) : PagerAdapter() {

    override fun getCount(): Int {
        return introduceList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ItemOnboardingIntroduceBinding.inflate(LayoutInflater.from(context), container, false)
        container.addView(binding.root)

        if (position < count) {
            introduceList[position].let { introduceItem ->
                binding.tvTitle.text = introduceItem.title
                binding.tvDes.text = introduceItem.content
                val identifier = context.resources.getIdentifier(
                    introduceItem.image, "drawable", context.packageName
                )
                binding.ivIntroduce.setImageResource(identifier)
            }
        }

        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout?)
    }

}