package com.trungpt.videoplus.asyntask;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import com.trungpt.videoplus.asyntask.listener.AsyncTaskListener;
import com.trungpt.videoplus.sync.RequestDTO;
import com.trungpt.videoplus.sync.dailymotion.DailymotionConnector;
import com.trungpt.videoplus.sync.vimeo.VimeoConnector;
import com.trungpt.videoplus.sync.youtube.YoutubeConnector;
import com.trungpt.videoplus.utils.Constant;

/**
 * Created by Trung on 11/25/2015.
 */
public class AsyncTaskSearchData extends AsyncTask<Void, Void, Object>
{
    AsyncTaskListener listener;
    private Constant.HOST_NAME host_name;
    YoutubeConnector ytbConnect;
    VimeoConnector vimeoConnect;
    DailymotionConnector dailymotionConnector;
    private RequestDTO requestDTO;

    public AsyncTaskSearchData(Context context, Constant.HOST_NAME host_name, RequestDTO requestDTO)
    {
        this.host_name = host_name;
        this.requestDTO = requestDTO;
        ytbConnect = new YoutubeConnector(context);
        vimeoConnect = new VimeoConnector();
        dailymotionConnector = new DailymotionConnector();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        listener.prepare();
    }

    @Override
    protected Object doInBackground(Void... params)
    {
        Object o = null;
        switch (host_name)
        {
            case YOUTUBE:
                o = ytbConnect.search(requestDTO);
                break;
            case YOUTUBE_RELATED:
                o = ytbConnect.getRelatedVideo(requestDTO);
                break;
            case YOUTUBE_VIDEO_BY_CHANNEL_ID:
                o = ytbConnect.getVideosByUser(requestDTO);
                break;
            case YOUTUBE_VIDEO_BY_PLAYLIST_ID:
                o = ytbConnect.getVideoByPlaylistId(requestDTO);
                break;
            case VIMEO:
                o = vimeoConnect.search(requestDTO);
                break;
            case DAILYMOTION:
                o = dailymotionConnector.search(requestDTO);
        }
        return o;
    }

    @Override
    protected void onPostExecute(Object o)
    {
        super.onPostExecute(o);
        listener.complete(o);
    }


    public void setListener(AsyncTaskListener listener)
    {
        this.listener = listener;
    }

}
