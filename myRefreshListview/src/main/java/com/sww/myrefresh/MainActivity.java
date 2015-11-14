package com.sww.myrefresh;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sww.myrefresh.view.RefeshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RefeshListView mListview;
    private List<String> dataList;
    int x=0;
    int y=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListview = (RefeshListView) findViewById(R.id.refresh_listview);
        dataList = new ArrayList<String>();
        for (int i=0;i<30;i++){
            dataList.add("这是数据："+i);
        }
        final MyAdapter myAdapter=new MyAdapter();
        mListview.setAdapter(myAdapter);
        //设置listView的监听
        mListview.setOnRefreshListener(new RefeshListView.OnRefreshListener() {
            @Override
            public void PullDownRefresh() {
                Toast.makeText(MainActivity.this, "开始下拉刷新了", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //会在3秒后执行
                        dataList.add(0,"我是下拉刷新出来的数据。。。"+x++);
                        myAdapter.notifyDataSetChanged();;
                        mListview.OnRefreshFinish();
                    }
                },3000);
            }


            @Override
            public void onLoadingMore() {
                Toast.makeText(MainActivity.this, "开始加载更多了", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dataList.add("我是加载更多的数据"+y++);
                        mListview.OnRefreshFinish();
                    }
                }, 5000);

            }
        });

    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
           TextView tv= new TextView(MainActivity.this);
            tv.setText(dataList.get(position));
            tv.setTextColor(Color.BLUE);
            tv.setTextSize(18);
            tv.setPadding(5,5,0,5);
            return tv;
        }
    }
}
