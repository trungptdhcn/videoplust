package com.trungpt.videoplus.event;

import com.trungpt.videoplus.ui.model.DirectLink;
import com.trungpt.videoplus.ui.model.VideoModel;

/**
 * Created by trung on 12/19/2015.
 */
public class DownloadEvent
{
    DirectLink directLink;

    public DownloadEvent(DirectLink directLink)
    {
        this.directLink = directLink;
    }

    public DirectLink getDirectLink()
    {
        return directLink;
    }

    public void setDirectLink(DirectLink directLink)
    {
        this.directLink = directLink;
    }
}
