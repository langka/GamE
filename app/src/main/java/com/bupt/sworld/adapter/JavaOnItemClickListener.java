package com.bupt.sworld.adapter;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by xusong on 2018/3/25.
 * About:坑爹的scala不能overrride java的统配泛型<?>方法，必须从Java层面做个弊
 */

public abstract class JavaOnItemClickListener implements AdapterView.OnItemClickListener{
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        onItemClick(i);
    }
    protected abstract void onItemClick(int location);

}
