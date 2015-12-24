package com.trungpt.videoplus.download;

import com.trungpt.videoplus.ui.model.DirectLink;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trung on 12/21/2015.
 */
public class DownloadManager2
{
    private static DownloadManager2 instance = null;
    private static final int DEFAULT_NUM_CONN_PER_DOWNLOAD = 8;
    public static final String DEFAULT_OUTPUT_FOLDER = "";
    private int numConnPerDownload;
    private List<Downloader2> downloadList;

    protected DownloadManager2()
    {
        numConnPerDownload = DEFAULT_NUM_CONN_PER_DOWNLOAD;
        downloadList = new ArrayList<>();
    }

    public int getNumConnPerDownload()
    {
        return numConnPerDownload;
    }

    public void setNumConnPerDownload(int numConnPerDownload)
    {
        this.numConnPerDownload = numConnPerDownload;
    }

    public Downloader2 getDownload(int index)
    {
        return downloadList.get(index);
    }

    public void removeDownload(int index)
    {
        downloadList.remove(index);
    }

    public List<Downloader2> getDownloadList()
    {
        return downloadList;
    }

    public Downloader2 createDownload(String folder, DirectLink directLink)
    {
        Downloader2 fd = new Downloader2(folder, numConnPerDownload, directLink);
        downloadList.add(fd);
        return fd;
    }

    public static DownloadManager2 getInstance()
    {
        if (instance == null)
        {
            instance = new DownloadManager2();
        }

        return instance;
    }


}
