package com.ezreal.huanting.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ezreal.huanting.R
import com.ezreal.huanting.adapter.MusicAdapter
import com.ezreal.huanting.bean.MusicBean
import com.ezreal.huanting.bean.MusicListBean
import com.ezreal.huanting.helper.MusicDataHelper
import kotlinx.android.synthetic.main.activity_music_list.*
import android.provider.MediaStore
import cn.hotapk.fastandrutils.utils.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ezreal.huanting.adapter.RViewHolder
import com.ezreal.huanting.adapter.RecycleViewAdapter


/**
 * 歌单详情页
 * Created by wudeng on 2018/1/8.
 */

class MusicListActivity : AppCompatActivity() {

    private val TAG = MusicListActivity::class.java.name

    private val mMusicList = ArrayList<MusicBean>()
    private var mAdapter: MusicAdapter? = null
    private var mList: MusicListBean? = null
    private var mBackColor = Color.parseColor("#bfbfbf")
    private var mHeadViewHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_list)
        getMusicList()
        initActionBar()
        initHeadView()
        initMusicList()
    }

    private fun getMusicList() {
        val listId = intent.getLongExtra("ListId", -1)
        MusicDataHelper.getMusicListById(listId, object : MusicDataHelper.OnListLoadListener {
            override fun loadSuccess(list: List<MusicListBean>) {
                if (list.isEmpty()) {
                    FToastUtils.init().show("读取歌单信息失败！")
                    finish()
                } else {
                    mList = list[0]
                    mMusicList.addAll(mList?.musicList!!)
                }
            }

            override fun loadFailed(message: String) {
                FToastUtils.init().show("读取歌单信息失败！")
                finish()
            }
        })
    }

    private fun initActionBar() {
        mTvTitle.text = "歌单"
        mIvBack.setOnClickListener { finish() }
    }

    private fun initHeadView() {

        FStatusBarUtils.translucent(this)
        val list = mList?.musicList
        if (list == null || list.isEmpty()){
            return
        }
        val albumUri = list[0].albumUri
        if (albumUri.isNullOrEmpty()) {
            return
        }
        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(albumUri))
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.splash)
        }
        mBackColor = Palette.from(bitmap).generate().darkVibrantSwatch?.rgb ?:
                ContextCompat.getColor(this, R.color.color_gray)

        setHeadViewDrawable()

        mHeadViewHeight = FConvertUtils.dip2px(200f)
    }

    private fun setHeadViewDrawable() {
        val actionBarBitmap = Bitmap.createBitmap(resources.displayMetrics.widthPixels,
                FConvertUtils.dip2px(71f), Bitmap.Config.ARGB_8888)
        actionBarBitmap.eraseColor(mBackColor)//填充颜色
        val actionBarDrawable = BitmapDrawable(resources, actionBarBitmap)
        actionBarDrawable.mutate().alpha = 0
        mActionBar.background = actionBarDrawable
    }

    private fun initMusicList() {
        mRcvMusic.layoutManager = LinearLayoutManager(this)
        mRcvMusic.setPullRefreshEnabled(false)
        mRcvMusic.setLoadingMoreEnabled(false)
        mRcvMusic.isNestedScrollingEnabled = false
        mRcvMusic.setHasFixedSize(false)
        mRcvMusic.addHeaderView(createHeadView())
        mAdapter = MusicAdapter(this, mList?.listId!!,mMusicList)
        mAdapter?.setItemClickListener(object :RecycleViewAdapter.OnItemClickListener{
            override fun onItemClick(holder: RViewHolder, position: Int) {
                mAdapter?.checkPlaySong(position-2,position)
            }
        })
        mRcvMusic.adapter = mAdapter
        mScrollView.setOnMyScrollChangeListener { _, scrollY, _, _ ->
            var scroll = scrollY
            if (scrollY < 0) {
                scroll = 0
            }
            var alpha = 0F
            if (scroll in 1..mHeadViewHeight) {
                alpha = scroll * 1.0F / mHeadViewHeight

            }else if (scroll > mHeadViewHeight){
                alpha = 1F
            }

            if (scroll > mHeadViewHeight / 2){
                mTvTitle.text = mList?.listName
            }else{
                mTvTitle.text = "歌单"
            }

            val drawable = mActionBar.background ?:
                    ContextCompat.getDrawable(this,R.drawable.action_bar_bg_black)
            drawable?.mutate()?.alpha = (alpha * 255).toInt()
            mActionBar.background = drawable
        }
    }

    private fun createHeadView(): View {
        val head = LayoutInflater.from(this)
                .inflate(R.layout.layout_music_list_head, null, false)

        val headBarBitmap = Bitmap.createBitmap(resources.displayMetrics.widthPixels,
                FConvertUtils.dip2px(271f), Bitmap.Config.ARGB_8888)
        headBarBitmap.eraseColor(mBackColor)//填充颜色
        val headDrawable = BitmapDrawable(resources, headBarBitmap)
        head.background = headDrawable

        val listCover = head.findViewById<ImageView>(R.id.mIvListCover)
        val listName = head.findViewById<TextView>(R.id.mTvListName)
        val userName = head.findViewById<TextView>(R.id.mTvCreator)

        userName.text = resources.getString(R.string.app_name)
        listName.text = mList?.listName

        if (mList?.musicList?.isEmpty()!!) {
            listCover.setImageResource(R.drawable.splash)
        } else {
            Glide.with(this)
                    .load(mList?.musicList?.get(0)?.albumUri)
                    .asBitmap()
                    .error(R.drawable.splash)
                    .into(listCover)
        }
        return head
    }
}

