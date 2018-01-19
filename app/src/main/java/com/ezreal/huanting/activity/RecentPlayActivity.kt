package com.ezreal.huanting.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import cn.hotapk.fastandrutils.utils.FToastUtils
import com.ezreal.huanting.R
import com.ezreal.huanting.adapter.MusicAdapter
import com.ezreal.huanting.adapter.RViewHolder
import com.ezreal.huanting.adapter.RecycleViewAdapter
import com.ezreal.huanting.bean.MusicBean
import com.ezreal.huanting.helper.MusicDataHelper
import com.ezreal.huanting.utils.Constant
import com.fondesa.recyclerviewdivider.RecyclerViewDivider
import kotlinx.android.synthetic.main.activity_recnet_play.*
import java.util.ArrayList

/**
 * 最近播放列表
 * Created by wudeng on 2018/1/3.
 */
class RecentPlayActivity : AppCompatActivity() {
    private var mSongList = ArrayList<MusicBean>()
    private var mAdapter:MusicAdapter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recnet_play)
        initView()
        initListener()
        loadSongList()
    }

    private fun initView() {
        mRvRecentPlay.layoutManager = LinearLayoutManager(this)
        mRvRecentPlay.setLoadingMoreEnabled(false)
        mRvRecentPlay.setPullRefreshEnabled(false)
        mAdapter = MusicAdapter(this,Constant.RECENT_MUSIC_LIST_ID ,mSongList)
        mAdapter?.setItemClickListener(object : RecycleViewAdapter.OnItemClickListener{
            override fun onItemClick(holder: RViewHolder, position: Int) {
                mAdapter?.checkPlaySong(position-1,position)
            }
        })
        mRvRecentPlay.adapter = mAdapter
    }

    private fun initListener() {
        mIvBack.setOnClickListener {
            this.finish()
        }

        mTvClear.setOnClickListener {
            AlertDialog.Builder(this, R.style.MyAlertDialog)
                    .setTitle("清空最近播放列表")
                    .setCancelable(true)
                    .setNegativeButton("取消", { dialog, _ -> dialog.dismiss() })
                    .setPositiveButton("清空", { dialog, _ ->
                        mSongList.clear()
                        mAdapter?.notifyDataSetChanged()
                        MusicDataHelper.clearRecentPlay()
                        dialog.dismiss()
                    })
                    .show()
        }
    }

    private fun loadSongList() {
        MusicDataHelper.loadRecentPlayFromDB(object : MusicDataHelper.OnMusicLoadListener {
            override fun loadSuccess(musicList: List<MusicBean>) {
                mSongList.addAll(musicList)
                mAdapter?.notifyDataSetChanged()
            }

            override fun loadFailed(message: String) {
                FToastUtils.init().show("加载出错：" + message)
            }
        })
    }
}