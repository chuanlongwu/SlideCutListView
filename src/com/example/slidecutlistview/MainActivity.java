package com.example.slidecutlistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.slidecutlistview.SlideCutListView.IXListViewListener;
import com.example.slidecutlistview.SlideCutListView.RemoveDirection;
import com.example.slidecutlistview.SlideCutListView.RemoveListener;

public class MainActivity extends Activity implements RemoveListener{
	private SlideCutListView slideCutListView ;
	private ArrayAdapter<String> adapter;
	private List<String> dataSourceList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		
		slideCutListView = (SlideCutListView) findViewById(R.id.slideCutListView);
		slideCutListView.setRemoveListener(this);
		slideCutListView.setXListViewListener(new IXListViewListener() {
			@Override
			public void onRefresh() {
				slideCutListView.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.e("slide", "onRefresh");
						slideCutListView.stopRefresh();
					}
				}, 1500);
			}
			@Override
			public void onLoadMore() {
				slideCutListView.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						slideCutListView.stopLoadMore();
					}
				}, 1500);
			}
		});
		slideCutListView.setPullRefreshEnable(true);
		for(int i=0; i<20; i++){
			dataSourceList.add("滑动删除" + i); 
		}
		
		adapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item, dataSourceList);
		slideCutListView.setAdapter(adapter);
		slideCutListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.e("slide", position+"---"+dataSourceList.size());
			}
		});
	}

	
	//滑动删除之后的回调方法
	@Override
	public void removeItem(RemoveDirection direction, int position) {
		adapter.remove(adapter.getItem(position-1));
		
		switch (direction) {
		case RIGHT:
			Toast.makeText(this, "向右删除  "+ (position-1), Toast.LENGTH_SHORT).show();
			break;
		case LEFT:
			Toast.makeText(this, "向左删除  "+ (position-1), Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		
	}	


}
