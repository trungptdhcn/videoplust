package com.trungpt.videoplus.ui.activity;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.ui.fragment.YoutubeRelatedFragment;
import com.trungpt.videoplus.ui.model.VideoModel;
import com.trungpt.videoplus.utils.Constant;

public class YoutubeVideoDetailActivity extends YouTubeFailureRecoveryActivity
{
//    @Bind(R.id.tvName)
//    TextView tvName;
//    @Bind(R.id.cbExpand)
//    CheckBox cbExpand;
//    @Bind(R.id.tvViews)
//    TextView tvViews;
//    @Bind(R.id.tvDescription)
//    TextView tvDescription;
//    @Bind(R.id.rlUser)
//    RelativeLayout rlUser;
//    @Bind(R.id.tvLikes)
//    TextView tvLikes;
//    @Bind(R.id.tvDisLikes)
//    TextView tvDisLikes;
//    @Bind(R.id.ivDownload)
//    ImageView ivDownload;
//    @Bind(R.id.layout_user_information_tvUserName)
//    TextView tvUserName;
//    @Bind(R.id.layout_user_information_tvSub)
//    TextView tvUserSubscrible;
//    @Bind(R.id.layout_user_information_ivAvatar)
//    ImageView ivUserAvatar;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.layout_video_related)
    FrameLayout layoutVideoRelated;
    VideoModel video;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_detail);
        ButterKnife.bind(this);
        Intent i = getIntent();
        video = i.getParcelableExtra("video");
        if (video != null)
        {
            YouTubePlayerFragment youTubePlayerFragment =
                    (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
            youTubePlayerFragment.initialize(Constant.KEY_YOUTUBE, this);

            YoutubeRelatedFragment relatedFragment = new YoutubeRelatedFragment();
            Bundle args = new Bundle();
            args.putParcelable("video", video);
            relatedFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.layout_video_related, relatedFragment);
            transaction.commit();
        }

    }
    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider()
    {
        return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        if (!wasRestored && video != null)
        {
            youTubePlayer.cueVideo(video.getId());
        }
    }
}
