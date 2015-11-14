package com.sww.myrefresh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sww.myrefresh.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sww on 2015/10/15.
 */
public class RefeshListView extends ListView implements AbsListView.OnScrollListener {

    private int downY;
    private int headerViewHeight;
    private View headerView;
    private final int DOWN_PULL = 0;//下拉刷新
    private final int RELEASE_REFRESH = 1;//释放刷新
    private final int REFRESHING = 2;//正在刷新

    private int currentState = DOWN_PULL;//头布局当前状态，默认为下拉刷新
    private RotateAnimation upAnimation;
    private RotateAnimation downAnimation;
    private ImageView ivArrow;
    private ProgressBar mProgressBar;
    private TextView tvState;
    private TextView tvLastUpdateTime;

    private boolean isLoadingMore=false;//是否正在加载更多中，默认为false
    private OnRefreshListener mOnRefreshListener;
    private View footerView;
    private int footerViewHeight;

    public RefeshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderView();
        initFooterView();
        setOnScrollListener(this);
    }



    public RefeshListView(Context context) {
        super(context);
        initHeaderView();
        initFooterView();
        setOnScrollListener(this);
    }
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.listview_footer, null);
        //设置脚布局的paddingTop为自己高度的负数
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        this.addFooterView(footerView);
    }
    /**
     * 初始化listView下拉刷新头
     */
    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.listview_header, null);
        ivArrow = (ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
        mProgressBar = (ProgressBar) headerView.findViewById(R.id.pb_listview_header);
        tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
        tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_last_update_time);
        tvLastUpdateTime.setText("最后刷新时间"+getCurrentTime());

        headerView.measure(0, 0);//让系统框架去帮我们测量头布局的宽和高
        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        this.addHeaderView(headerView);
        initAnimation();
    }

    private String getCurrentTime() {

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void initAnimation() {
        upAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);//让控件停止在动画结束的状态下

        downAnimation = new RotateAnimation(-180,-360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);//让控件停止在动画结束的状态下



    }

    /**
     * 根据当前的状态currentState来刷新头布局的状态
     */
    private void refreshHeaderViewState(){
        switch (currentState){
            case DOWN_PULL://下拉刷新
                ivArrow.startAnimation(downAnimation);
                tvState.setText("下拉刷新");
                break;
            case RELEASE_REFRESH://松开刷新
                ivArrow.startAnimation(upAnimation);
                tvState.setText("松开刷新");
                break;
            case REFRESHING://正在刷新中
                ivArrow.clearAnimation();
                ivArrow.setVisibility(INVISIBLE);
                tvState.setText("正在刷新。。。");
                break;
            default:
                break;

        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //当前的状态是否是正在刷新，如果是，直接跳出
                if (currentState == REFRESHING) {
                    break;
                }
                int moveY = (int) ev.getY();
                //间距=移动y-按下y；
                int diffY = moveY - downY;
                //头布局最新的paddingTop
                int paddingTop = -headerViewHeight + diffY;

                int firstVisiblePosition = getFirstVisiblePosition();
                if (paddingTop > -headerViewHeight && firstVisiblePosition == 0) {
                    if (paddingTop > 0 && currentState == DOWN_PULL) {//头布局完全显示，进入下拉刷新状态
                        System.out.println("松开刷新");
                        currentState = RELEASE_REFRESH;
                        refreshHeaderViewState();

                    } else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
                        System.out.println("下拉刷新");
                        currentState=DOWN_PULL;
                        refreshHeaderViewState();
                    }
                    headerView.setPadding(0, paddingTop, 0, 0);
                    return true;//自己处理用户触摸滑动的事件

                }
                break;
            case MotionEvent.ACTION_UP:
                //判断当前的状态是哪一种
                if(currentState==DOWN_PULL){//当前是在下拉刷新的状态下松开了，什么都不做，把头布局隐藏就可以
                    headerView.setPadding(0,-headerViewHeight,0,0);
                }else if(currentState==RELEASE_REFRESH){
                    //当前状态是释放状态，并且松开了，应该把头布局正常显示，进入正在刷新状态
                    headerView.setPadding(0,0,0,0);
                    currentState=REFRESHING;
                    refreshHeaderViewState();

                //调用用户的监听事件
                    if(mOnRefreshListener!=null){
                        mOnRefreshListener.PullDownRefresh();
                    }

                }

                break;
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                break;
        }

        return super.onTouchEvent(ev);
    }


    public void setOnRefreshListener(OnRefreshListener listener){
        mOnRefreshListener = listener;
    }
    /**
     * 当滚动状态改变时, 触发此方法.
     * scrollState 当前滚动的状态.
     *
     * OnScrollListener.SCROLL_STATE_IDLE; 停滞状态
     * OnScrollListener.SCROLL_STATE_TOUCH_SCROLL; 手指触摸在屏幕上滑动.
     * OnScrollListener.SCROLL_STATE_FLING; 手指快速的滑动一下.
     *
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //当前的状态是停止，并且屏幕上显示的最后一个条目的索引是listView中总条目个数-1
        if((scrollState==OnScrollListener.SCROLL_STATE_IDLE||scrollState==OnScrollListener.SCROLL_STATE_FLING)&&
                getLastVisiblePosition()==(getCount()-1)&&
                !isLoadingMore){
            System.out.println("滑动到底部，可以加载更多的数据了");
            isLoadingMore=true;
            footerView.setPadding(0,0,0,0);
            setSelection(getCount());//滑动到最底部
            if(mOnRefreshListener!=null){
                mOnRefreshListener.onLoadingMore();
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public interface OnRefreshListener{
        /**
         * 当下拉刷新时回调次方法
         */
        public  void PullDownRefresh();

        /**
         * 当加载更多时调用此方法
         */
        public void onLoadingMore();
    }

    /**
     * 刷新完毕，用户调用此方法，把对应的头布局或脚布局给隐藏掉
     */
    public void OnRefreshFinish() {
        if (isLoadingMore) {//当前属于加载更多
            //隐藏脚布局
            footerView.setPadding(0,-footerViewHeight,0,0);
            isLoadingMore=false;

        } else {
            //隐藏头布局
            headerView.setPadding(0, -headerViewHeight, 0, 0);
            currentState = DOWN_PULL;
            mProgressBar.setVisibility(View.INVISIBLE);
            ivArrow.setVisibility(View.VISIBLE);
            tvState.setText("下拉刷新");
            tvLastUpdateTime.setText("最后刷新时间：" + getCurrentTime());

        }
    }


}
