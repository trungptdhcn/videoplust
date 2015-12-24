package com.trungpt.videoplus.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.asyntask.AsyncTaskMostPopular;
import com.trungpt.videoplus.asyntask.AsyncTaskSearchData;
import com.trungpt.videoplus.asyntask.listener.AsyncTaskListener;
import com.trungpt.videoplus.base.StringUtils;
import com.trungpt.videoplus.sync.RestfulService;
import com.trungpt.videoplus.sync.vimeo.direct.VimeoDirectDTO;
import com.trungpt.videoplus.sync.vimeo.direct.VimeoDirectProgressiveDTO;
import com.trungpt.videoplus.sync.vimeo.direct.VimeoDirectStreamURLDTO;
import com.trungpt.videoplus.sync.vimeo.request.VimeoRequestDTO;
import com.trungpt.videoplus.ui.customview.player.*;
import com.trungpt.videoplus.ui.fragment.VideoRelatedFragment;
import com.trungpt.videoplus.ui.listener.FullScreenListener;
import com.trungpt.videoplus.ui.listener.NextPreviousListener;
import com.trungpt.videoplus.ui.model.*;
import com.trungpt.videoplus.utils.Constant;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayListDetailActivity extends FragmentActivity implements SurfaceHolder.Callback
        , FullScreenListener, NextPreviousListener, DemoPlayer.Listener, AsyncTaskListener
{

    @Bind(R.id.progressPreparing)
    ProgressBar progressPreparing;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.layout_video_related)
    FrameLayout layoutVideoRelated;
    @Bind(R.id.controller)
    FrameLayout frController;
    @Bind(R.id.video_view)
    SurfaceView videoSurface;
    @Bind(R.id.videoSurfaceContainer)
    RelativeLayout root;

    PlayListModel playListModel;
    private DemoPlayer player;
    private boolean isFullScreen = false;
    private long playerPosition;
    private boolean playerNeedsPrepare;
    //    Uri urlVideo;
//    int videoType;
    PlaylistControllerView controller;
    public static final int TIME_OUT_CONTROLLER = 3000;
    private boolean enableBackgroundAudio;
    List<DirectLink> directLinks = new ArrayList<>();
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vimeo_video_detail);
        ButterKnife.bind(this);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        Intent i = getIntent();
        playListModel = i.getParcelableExtra("video");
        controller = new PlaylistControllerView(this);
        controller.setAnchorView(frController);
        root.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    toggleControlsVisibility();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    view.performClick();
                }
                return true;
            }
        });
        setFullScreen();
    }

    public void setFullScreen()
    {
        if (isFullScreen())
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoSurface.getLayoutParams();
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) frController.getLayoutParams();
            params.width = width;
            params.height = height;// -80 for android controls
            params.setMargins(0, 0, 0, 0);
            params2.width = width;
            params2.height = height;// -80 for android controls
            params2.setMargins(0, 0, 0, 0);
            controller.updateFullScreen();
            isFullScreen = false;
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoSurface.getLayoutParams();
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) frController.getLayoutParams();
            params.width = width;
            params.height = (int) getResources().getDimension(R.dimen.size_of_video);
            params.setMargins(0, 0, 0, 0);
            params2.width = width;
            params2.height = (int) getResources().getDimension(R.dimen.size_of_video);
            params2.setMargins(0, 0, 0, 0);
            controller.updateFullScreen();
            isFullScreen = true;
        }
    }

    private void toggleControlsVisibility()
    {
        if (controller != null)
        {
            if (controller.isShowing())
            {
                controller.hide();
            }
            else
            {
                controller.show(TIME_OUT_CONTROLLER);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (player != null)
        {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (player != null)
        {
            player.blockingClearSurface();
        }
    }

    @Override
    public void toggleFullScreen()
    {
        setFullScreen();
    }

    @Override
    public boolean isFullScreen()
    {
        return isFullScreen;
    }

    @Override
    public void onError(Exception e)
    {
        if (e instanceof UnsupportedDrmException)
        {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        controller.show(TIME_OUT_CONTROLLER);
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio)
    {

    }

    @Override
    public void onNewIntent(Intent intent)
    {
        releasePlayer();
        playerPosition = 0;
        setIntent(intent);
    }

    private void releasePlayer()
    {
        if (player != null)
        {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Intent i = getIntent();
        playListModel = i.getParcelableExtra("video");
        if (playListModel != null)
        {
            VimeoRequestDTO requestDTO = new VimeoRequestDTO
                    .VimeoRequestBuilder("")
                    .id(playListModel.getId())
                    .build();
            AsyncTaskMostPopular asyntask = new AsyncTaskMostPopular(this, Constant.HOST_NAME.VIMEO_DIRECT_LINK, requestDTO);
            asyntask.setListener(this);
            asyntask.execute();
        }
    }

    private void preparePlayer(boolean playWhenReady)
    {
        if (player == null)
        {
            player = new DemoPlayer(getRendererBuilder(Uri.parse(directLinks.get(currentPosition).getUri()), directLinks.get(currentPosition).getTypePlay()));
            player.addListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            CustomPlayListControl playerControl = player.getPlayListControl();
            playerControl.setFullScreenListener(this);
            playerControl.setNextPreviousListener(this);
            controller.setMediaPlayer(playerControl);
            controller.setEnabled(true);
        }
        if (playerNeedsPrepare)
        {
            player.prepare();
            playerNeedsPrepare = false;
        }
        controller.show();
        player.setSurface(videoSurface.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState)
    {
        if (playbackState == ExoPlayer.STATE_ENDED)
        {
            controller.show(TIME_OUT_CONTROLLER);
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState)
        {
            case ExoPlayer.STATE_BUFFERING:
                progressPreparing.setVisibility(View.VISIBLE);
                controller.show(5000);
                break;
            case ExoPlayer.STATE_ENDED:
                progressPreparing.setVisibility(View.GONE);
                controller.show(500000);
                break;
            case ExoPlayer.STATE_IDLE:
                progressPreparing.setVisibility(View.GONE);
                controller.show(500000);
                break;
            case ExoPlayer.STATE_PREPARING:
                progressPreparing.setVisibility(View.VISIBLE);
                controller.show(8000);
                break;
            case ExoPlayer.STATE_READY:
                progressPreparing.setVisibility(View.GONE);
                controller.show(3000);
                break;
            default:
                break;
        }
    }

    private DemoPlayer.RendererBuilder getRendererBuilder(Uri uri, int type)
    {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        switch (type)
        {
            case 1:
                return new SmoothStreamingRendererBuilder(this, userAgent, uri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
//            case 2:
//                return new DashRendererBuilder(this, userAgent, uri.toString(),
//                        new WidevineTestMediaDrmCallback(contentId));
            case 3:
                return new HlsRendererBuilder(this, userAgent, uri.toString());
            case 4:
                return new ExtractorRendererBuilder(this, userAgent, uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (!enableBackgroundAudio)
        {
            releasePlayer();
        }
        else
        {
            player.setBackgrounded(true);
        }
    }

    @Override
    public boolean isNext()
    {
        return currentPosition == directLinks.size() - 1 ? false : true;
    }

    @Override
    public boolean isPrevious()
    {

        return currentPosition == 0 ? false : true;
    }

    @Override
    public void next()
    {
        currentPosition++;
        releasePlayer();
        playerPosition=0;
        preparePlayer(false);
    }

    @Override
    public void prev()
    {
        currentPosition--;
        releasePlayer();
        playerPosition=0;
        preparePlayer(false);
    }

    @Override
    public void prepare()
    {

    }

    @Override
    public void complete(Object obj)
    {
        directLinks = (List<DirectLink>) obj;
        if (player == null)
        {
            preparePlayer(false);
        }
        else
        {
            player.setBackgrounded(false);
        }

    }
}
