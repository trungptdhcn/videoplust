package com.trungpt.videoplus.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnItemClick;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.asyntask.AsyncTaskMostPopular;
import com.trungpt.videoplus.asyntask.listener.AsyncTaskListener;
import com.trungpt.videoplus.base.BaseFragment;
import com.trungpt.videoplus.base.StringUtils;
import com.trungpt.videoplus.sync.youtube.request.YoutubeRequestDTO;
import com.trungpt.videoplus.ui.activity.YoutubeSearchActivity;
import com.trungpt.videoplus.ui.activity.YoutubeVideoDetailActivity;
import com.trungpt.videoplus.ui.adapter.CommonAdapter;
import com.trungpt.videoplus.ui.adapter.CountryAdapter;
import com.trungpt.videoplus.ui.listener.EndlessScrollListener;
import com.trungpt.videoplus.ui.model.Item;
import com.trungpt.videoplus.ui.model.PageModel;
import com.trungpt.videoplus.utils.Constant;
import com.trungpt.videoplus.utils.ResourceUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by trung on 12/11/2015.
 */
public class YoutubeFragment extends BaseFragment implements AsyncTaskListener, View.OnClickListener
{

    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.menuOption)
    FloatingActionMenu menuOption;
    @Bind(R.id.fab_up)
    com.melnykov.fab.FloatingActionButton fabUp;
    @Bind(R.id.fab_search)
    com.github.clans.fab.FloatingActionButton fabSearch;
    @Bind(R.id.fab_filter)
    com.github.clans.fab.FloatingActionButton fabFilter;
    CountryAdapter countryAdapter;
    CommonAdapter adapter;
    String nextPage;
    YoutubeRequestDTO requestDTO;
    private int mPreviousVisibleItem;

    @Override
    public int getLayout()
    {
        return R.layout.youtube_fragment;
    }

    @Override
    public void setDataToView(Bundle savedInstanceState)
    {
        fabUp.hide(false);
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String regionCode = tm.getNetworkCountryIso();
        countryAdapter = new CountryAdapter(getActivity(), android.R.layout.simple_list_item_1, ResourceUtils.getCountries());
        requestDTO = new YoutubeRequestDTO
                .YoutubeRequestBuilder("")
                .regionCode(regionCode)
                .build();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        TextView header = (TextView) inflater.inflate(R.layout.layout_most_popular, listView, false);
        listView.addHeaderView(header, null, false);
        AsyncTaskMostPopular asyncTask = new AsyncTaskMostPopular(getActivity(), Constant.HOST_NAME.YOUTUBE, requestDTO);
        asyncTask.setListener(YoutubeFragment.this);
        asyncTask.execute();
        EndlessScrollListener endlessScrollListener = new EndlessScrollListener()
        {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount)
            {
                if (StringUtils.isNotEmpty(nextPage))
                {
                    requestDTO.setPageToken(nextPage);
                    AsyncTaskMostPopular asyncTask = new AsyncTaskMostPopular(getActivity(), Constant.HOST_NAME.YOUTUBE, requestDTO);
                    asyncTask.setListener(YoutubeFragment.this);
                    asyncTask.execute();
                    return true;
                }
                else
                {
                    return false;
                }

            }
        };
//        listView.setOnScrollListener(endlessScrollListener);
        menuOption.setOnMenuButtonClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (menuOption.isOpened())
                {
                }
                menuOption.toggle(true);
            }
        });
        menuOption.setClosedOnTouchOutside(true);
        fabSearch.setOnClickListener(this);
        fabFilter.setOnClickListener(this);
        fabUp.attachToListView(listView, null, endlessScrollListener);
        fabUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                listView.smoothScrollToPosition(0);
            }
        });
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
                adapter = new CommonAdapter(videos, getActivity());
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

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fab_filter:
                final AlertDialog.Builder countryDialog = new AlertDialog.Builder(YoutubeFragment.this.getActivity());
                countryDialog.setSingleChoiceItems(countryAdapter, 0, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int n)
                    {
                        adapter = null;
                        requestDTO = new YoutubeRequestDTO
                                .YoutubeRequestBuilder("")
                                .regionCode(countryAdapter.getItem(n).getKey())
                                .build();
                        AsyncTaskMostPopular asyncTask = new AsyncTaskMostPopular(getActivity(), Constant.HOST_NAME.YOUTUBE, requestDTO);
                        asyncTask.setListener(YoutubeFragment.this);
                        asyncTask.execute();
                        dialog.dismiss();
                    }

                });
                countryDialog.setNegativeButton("Cancel", null);
                countryDialog.setTitle("Chose countries");
                countryDialog.show();
                break;
//            case R.id.fab_up:
//                break;
            case R.id.fab_search:
                Intent intent = new Intent(getActivity(), YoutubeSearchActivity.class);
                intent.putExtra("host_name",Constant.HOST_NAME.YOUTUBE);
                startActivity(intent);
                break;
        }
    }

    @OnItemClick(R.id.list_view)
    public void itemClick(int position)
    {
        Intent intent = new Intent(getActivity(), YoutubeVideoDetailActivity.class);
        intent.putExtra("video", (Parcelable) adapter.getItem(position - 1));
        startActivity(intent);
    }
}
