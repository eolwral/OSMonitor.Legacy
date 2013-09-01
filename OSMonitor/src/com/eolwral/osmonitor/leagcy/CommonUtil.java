package com.eolwral.osmonitor.leagcy;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;

public class CommonUtil
{
	public final static String NiceCMD = "/data/data/com.eolwral.osmonitor/nice";
	public static Random RandomGen = new Random();

	public static boolean checkExtraStore(PreferenceActivity activity)
	{
		boolean flag = false;
    	if(Integer.parseInt(Build.VERSION.SDK) >= 8)
    	{
    		// use Reflection to avoid errors (for cupcake 1.5)
    		Method MethodList[] = activity.getClass().getMethods();
    		for(int checkMethod = 0; checkMethod < MethodList.length; checkMethod++)
    		{
    			if(MethodList[checkMethod].getName().indexOf("ApplicationInfo") != -1)
    			{
    				try{
    					if((((ApplicationInfo) MethodList[checkMethod].invoke(activity , new Object[]{})).flags & 0x40000 /* ApplicationInfo.FLAG_EXTERNAL_STORAGE*/ ) != 0 )
    						flag = true;
    				}
    				catch(Exception e) {}
    			}
    		}
    	}
    	return flag;
	}
	
	public static int getSDKVersion()
	{
		return Integer.parseInt(Build.VERSION.SDK);
	}
	
	
	// Screen Size
	private static int ScreenSize = 1; /* 0 == Small, 1 == Normal, 2 == Large */

	public static void detectScreen(Activity activity)
	{
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        int lanscapeHight = 0 ; 
        if( activity.getResources().getConfiguration().orientation ==
        								Configuration.ORIENTATION_PORTRAIT)
        	lanscapeHight = metrics.heightPixels;
        else
        	lanscapeHight = metrics.widthPixels;
        	        
        if(lanscapeHight >= 800)
        	ScreenSize = 2;
        else if(lanscapeHight <= 320)
        	ScreenSize = 0;
        else 
        	ScreenSize = 1;
	}
	
	public static int getScreenSize()
	{
		return ScreenSize;
	}
	
	// Gesture Threshold
	public static final int SWIPE_MIN_DISTANCE = 120;
	public static final int SWIPE_MAX_OFF_PATH = 250;
	public static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	
	public static void execCommand(String command) {
		try {
			Process shProc = Runtime.getRuntime().exec("su");
			DataOutputStream InputCmd = new DataOutputStream(shProc.getOutputStream());

			InputCmd.writeBytes(command);

			// Close the terminal
			InputCmd.writeBytes("exit\n");
			InputCmd.flush();
			InputCmd.close();
	    	
			try {
				shProc.waitFor();
			} catch (InterruptedException e) { };
		} catch (IOException e)	{}
	}    

	private static Handler EndHelper = new Handler()
	{
		public void handleMessage(Message msg)
		{
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}; 
    
	public static void killSelf(Context target)
	{
		if(CommonUtil.getSDKVersion() <= 7)
		{
			((ActivityManager) target.getSystemService(Context.ACTIVITY_SERVICE))
	           									.restartPackage("com.eolwral.osmonitor");
	    }
		else
		{ 
			EndHelper.sendEmptyMessageDelayed(0, 500);
		}
	}
	
	public static void CheckNice(AssetManager Asset)
	{
		try {
			InputStream bNiceIn = Asset.open("nice");
			OutputStream bNiceOut = new FileOutputStream(NiceCMD);
			

			// Transfer bytes from in to out  
            byte[] bTransfer = new byte[1024];   
            int bTransferLen = 0;   
            while ((bTransferLen = bNiceIn.read(bTransfer)) > 0)   
            {   
            	bNiceOut.write(bTransfer, 0, bTransferLen);   
            }   

            bNiceIn.close();   
            bNiceOut.close();    
            
            CommonUtil.execCommand("chmod 755 "+NiceCMD+"\n");
			
		} catch (IOException e) { 
		}
	}
	
}