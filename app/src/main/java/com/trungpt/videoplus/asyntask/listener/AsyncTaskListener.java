package com.trungpt.videoplus.asyntask.listener;

/**
 * Created by Trung on 11/25/2015.
 */
public interface AsyncTaskListener
{
    public void prepare();

    public void complete(Object obj);
}
