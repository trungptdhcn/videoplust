package com.trungpt.videoplus.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.asyntask.AsyncTaskSearchData;
import com.trungpt.videoplus.asyntask.listener.AsyncTaskListener;
import com.trungpt.videoplus.sync.youtube.request.YoutubeRequestDTO;
import com.trungpt.videoplus.ui.adapter.VideoPlayListAdapter;
import com.trungpt.videoplus.ui.model.Item;
import com.trungpt.videoplus.ui.model.PageModel;
import com.trungpt.videoplus.ui.model.PlayListModel;
import com.trungpt.videoplus.utils.Constant;

import java.util.List;

public class PlayListYoutubeDetailActivity extends YouTubeFailureRecoveryActivity implements AsyncTaskListener
{
    PlayListModel playList;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    //    @Bind(R.id.layout_list_frameList)
//    FrameLayout frameList;
    @Bind(R.id.activity_play_list_detail_rlContainer)
    RelativeLayout rlContainer;
    @Bind(R.id.activity_play_list_detail_tvTitleOfPlayList)
    TextView tvTitleOfPlayList;
    @Bind(R.id.activity_play_list_detail_tvVideoCountOfPlayList)
    TextView tvVideoCountOfPlayList;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.activity_play_list_detail_cbExpand)
    CheckBox cbExpand;

    VideoPlayListAdapter adapter;
    String nextPage;
    YoutubeRequestDTO requestDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_detail);
        ButterKnife.bind(this);
        Intent i = getIntent();
        playList = i.getParcelableExtra("video");
        if (playList != null)
        {
            requestDTO = new YoutubeRequestDTO
                    .YoutubeRequestBuilder("")
                    .id(playList.getId())
                    .build();
            AsyncTaskSearchData asyncTask = new AsyncTaskSearchData(this, Constant.HOST_NAME.YOUTUBE_VIDEO_BY_PLAYLIST_ID, requestDTO);
            asyncTask.setListener(this);
            asyncTask.execute();
            YouTubePlayerFragment youTubePlayerFragment =
                    (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.activity_play_list_detail_youtube_fragment);
            youTubePlayerFragment.initialize(Constant.KEY_YOUTUBE, this);
        }
        if (cbExpand.isChecked())
        {
            listView.setVisibility(View.VISIBLE);
        }
        else
        {
            listView.setVisibility(View.GONE);
        }
        cbExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    listView.setVisibility(View.VISIBLE);
                }
                else
                {
                    listView.setVisibility(View.GONE);
                }
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(R.drawable.bg_selector_listview);
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider()
    {
        return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        if (!wasRestored && playList != null)
        {
            youTubePlayer.cuePlaylist(playList.getId());
        }
    }

    @Override
    public void prepare()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void complete(Object obj)
    {
        PageModel page = (PageModel) obj;
        if (page != null)
        {
            List<Item> videos = page.getItems();
            nextPage = page.getNextPage();
            if (adapter == null)
            {
                adapter = new VideoPlayListAdapter(videos, this);
                listView.setAdapter(adapter);
            }
            else
            {
                adapter.getVideos().addAll(videos);
                adapter.notifyDataSetChanged();
            }

        }
        progressBar.setVisibility(View.GONE);
    }
}
