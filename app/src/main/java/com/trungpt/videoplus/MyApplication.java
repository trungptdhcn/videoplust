package com.trungpt.videoplus;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Trung on 11/16/2015.
 */
public class MyApplication extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.deleteRealm(realmConfiguration);

    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

}
