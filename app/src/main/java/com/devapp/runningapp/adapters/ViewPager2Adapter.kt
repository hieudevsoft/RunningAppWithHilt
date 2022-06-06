package com.devapp.runningapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adapter(fm:FragmentManager,lifecycle: Lifecycle):FragmentStateAdapter(fm,lifecycle) {
    private var mListFragment:MutableList<Fragment> = mutableListOf()
    override fun getItemCount(): Int {
        return if(mListFragment.size<=0) return 0 else mListFragment.size
    }

    override fun createFragment(position: Int): Fragment {
        if(mListFragment.isEmpty()) return Fragment()
        return mListFragment[position]
    }

    fun getFragmentAtPosition(pos:Int) = mListFragment[pos]

    fun addFragment(fragment:Fragment) {
        if(mListFragment.contains(fragment)) return
        mListFragment.add(fragment)
    }

    override fun getItemId(position: Int): Long {
        return mListFragment[position].hashCode().toLong()
    }


}