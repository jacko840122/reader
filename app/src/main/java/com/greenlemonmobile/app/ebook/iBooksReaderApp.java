package com.greenlemonmobile.app.ebook;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.EmergencyHandler;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.SettingsManager;
import org.emdev.utils.android.VMRuntimeHack;

import com.github.reader.app.model.manager.DBManager;
import com.github.reader.utils.AppUtils;
import com.github.reader.utils.Constants;
import com.github.reader.utils.SharedPreferencesUtil;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Application;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.widget.TabHost;

public class iBooksReaderApp extends Application {

	public static TabHost onlyTabHost=null;
    /**
     * {@inheritDoc}
     *
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        AppUtils.init(this);
        initData();
        DBManager.getInstance().init(this);
        SharedPreferencesUtil.init(this, this.getPackageName() + "_preference", Context.MODE_PRIVATE);

        EBookDroidApp.init(this);

        EmergencyHandler.init(this);
        LogContext.init(this);
        SettingsManager.init(this);
        CacheManager.init(this);

        VMRuntimeHack.preallocateHeap(AppSettings.current().heapPreallocate);
        
		DisplayImageOptions option = new DisplayImageOptions.Builder().delayBeforeLoading(50)
		.showImageForEmptyUri(R.drawable.no_cover).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		//.showImageOnLoading(R.drawable.refresh)
		.showImageOnFail(R.drawable.no_cover).cacheInMemory(true) // default
		.cacheOnDisk(true).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder( this.getApplicationContext())
															.diskCacheSize(500 * 1024 * 1024)
															.diskCacheFileCount(1000)
															.memoryCache(new LruMemoryCache(30 * 1024 * 1024))
															.memoryCacheSize(30 * 1024 * 1024).threadPoolSize(3)
															.threadPriority(Thread.NORM_PRIORITY - 2)
															.defaultDisplayImageOptions(option).build();
		
		ImageLoader.getInstance().init(config);
        
        //Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance(this));
    }

    public void initData() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager
                .getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length <= 0) {
            Constants.isDoubleScreen = false;
        } else {
            Constants.isDoubleScreen = true;
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Application#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        BitmapManager.clear("on Low Memory: ");
    }
}
