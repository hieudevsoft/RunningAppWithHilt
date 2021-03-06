package com.devapp.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.OnBoardingIntroduceAdapter
import com.devapp.runningapp.databinding.FragmentSplashBinding
import com.devapp.runningapp.model.OnBoardingIntroduceItem
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.VoidCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper
    private var _binding : FragmentSplashBinding? =null
    private val binding get() = _binding!!
    private var introduceList:List<OnBoardingIntroduceItem> = listOf()
    private var hasInitializedRootView = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        introduceList = listOf(
            OnBoardingIntroduceItem(
                getString(R.string.onboard_introduce_title_1),
                getString(R.string.onboard_introduce_content_1), "img_onboarding_1"
            ),
            OnBoardingIntroduceItem(
                getString(R.string.onboard_introduce_title_2),
                getString(R.string.onboard_introduce_content_2), "img_onboarding_2"
            ),
            OnBoardingIntroduceItem(
                getString(R.string.onboard_introduce_title_3),
                getString(R.string.onboard_introduce_content_3), "img_onboarding_3"
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null)
                _binding = FragmentSplashBinding.inflate(inflater,container,false)
            else{ (binding.root.parent as ViewGroup).removeView(binding.root) }
        }catch (e:Exception){

        }
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitializedRootView) return
        hasInitializedRootView = true
        binding.apply {
            rvIntroduce.adapter = OnBoardingIntroduceAdapter(requireContext(), introduceList)
            viewIndicator.setViewPager(rvIntroduce)
            rvIntroduce.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    if (position == 2) btnNext.text = requireContext().getString(R.string.login)
                    else btnNext.text = requireContext().getString(R.string.next)
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })
            setupAutoPager()

            btnIgnore.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        sharedPreferences.isShowOnBoarding = false
                        if (timer != null) {
                            timer!!.cancel()
                            timer = null
                        }
                        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                    }
                }, 0.96f)
            }

            btnNext.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        if (currentPage == 2) {
                            sharedPreferences.isShowOnBoarding = false
                            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                        } else binding.rvIntroduce.setCurrentItem(++currentPage, true)
                    }
                }, 0.96f)
            }
        }
    }
    private var timer: CountDownTimer? = null
    private var currentPage = 0

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAutoPager() {
        timer = object : CountDownTimer(2000, 1500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                currentPage = binding.rvIntroduce.currentItem
                currentPage++
                if (currentPage == 3) currentPage = 0
                try {
                    binding.rvIntroduce.setCurrentItem(currentPage, true)
                } catch (e: OutOfMemoryError) {
                    Log.e("error", "OutOfMemoryError")
                } catch (e: IllegalStateException) {
                    Log.e("error", "IllegalStateException")
                } catch (e: IllegalArgumentException) {
                    Log.e("error", "IllegalArgumentException")
                }
                timer!!.start()
            }
        }.start()

        binding.rvIntroduce.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> timer?.cancel()
                MotionEvent.ACTION_UP -> timer?.start()
            }
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}