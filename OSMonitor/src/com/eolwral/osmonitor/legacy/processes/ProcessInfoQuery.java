package com.eolwral.osmonitor.legacy.processes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Debug;

import com.eolwral.osmonitor.legacy.*;


public class ProcessInfoQuery extends Thread
{
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	
	private static ProcessInfoQuery singletone = null;
	private static PackageManager AppInfo = null;
	private static Resources  ResInfo = null;
	private static ActivityManager ActInfo = null;
	
	public static ProcessInfoQuery getInstance(Context context)
	{
		if(singletone == null)
		{
			singletone = new ProcessInfoQuery();
            AppInfo = context.getPackageManager();
            ResInfo = context.getResources();
            ActInfo = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            singletone.start();
		}
		
		return singletone;
	}
	
	class ProcessInstance
	{
		public String Name;
		public Drawable Icon;
		public String Package;
	}
	
    private final HashMap<String, Boolean> CacheExpaned = new HashMap<String, Boolean>();
    private final HashMap<String, Boolean> CacheSelected = new HashMap<String, Boolean>();
	private final HashMap<String, ProcessInstance> ProcessCache = new HashMap<String, ProcessInstance>();
    
	public void doCacheInfo(int position)
	{
		int ProcessID = JNILibrary.GetProcessPID(position);
		ProcessInstance CacheInstance = ProcessCache.get(JNILibrary.GetProcessName(ProcessID));
		if(CacheInstance != null)
			return;
		
		try {
			QueryQueueLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		QueryQueue.add(new WaitCache(JNILibrary.GetProcessName(ProcessID),
				JNILibrary.GetProcessOwner(ProcessID), JNILibrary.GetProcessUID(ProcessID)));
		QueryQueueLock.release();
		
		CacheInstance = new ProcessInstance();
		CacheInstance.Name = JNILibrary.GetProcessName(ProcessID);
		ProcessCache.put(JNILibrary.GetProcessName(ProcessID), CacheInstance);
		
		return;
	}

	private class WaitCache
	{
		private final String ItemName;
		private final String ItemOwner;
		private final int ItemUID;
		public WaitCache(String Name, String Owner, int UID)
		{
			ItemName = Name;
			ItemOwner = Owner;
			ItemUID = UID;
		}
		
		public String getName()
		{
			return ItemName;
		}

		public String getOwner()
		{
			return ItemOwner;
		}
		
		public int getUID()
		{
			return ItemUID;
		}
	}
    private static LinkedList<WaitCache> QueryQueue = new LinkedList<WaitCache>();
	private final Semaphore QueryQueueLock = new Semaphore(1, true);
    
	
	@Override 
	public void run()
	{
 
		while(true)
		{
			if(!getCacheInfo())
			{
				try {
					sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean getCacheInfo()
	{
		if(QueryQueue.isEmpty())
			return false;
		
		try {
			QueryQueueLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		WaitCache SearchObj = QueryQueue.remove();

		QueryQueueLock.release();
		
		PackageInfo appPackageInfo = null;
		String PackageName = null;
		if(SearchObj.getName().contains(":"))
			PackageName = SearchObj.getName().substring(0,
								SearchObj.getName().indexOf(":"));
		else
			PackageName = SearchObj.getName();
		
		// for system user
		if(SearchObj.getOwner().contains("system") && 
						SearchObj.getName().contains("system") &&
							!SearchObj.getName().contains("."))
			PackageName = "android";
		
		try {  
			appPackageInfo = AppInfo.getPackageInfo(PackageName, 0);
		} catch (NameNotFoundException e) {}
		
		if(appPackageInfo == null && SearchObj.getUID() >0)
		{
			String[] subPackageName = AppInfo.getPackagesForUid(SearchObj.getUID());
				
			if(subPackageName != null)
			{
				for(int PackagePtr = 0; PackagePtr < subPackageName.length; PackagePtr++)
				{
					if (subPackageName[PackagePtr] == null)
						continue;
					try {  
						appPackageInfo = AppInfo.getPackageInfo(subPackageName[PackagePtr], 0);
						PackagePtr = subPackageName.length;
					} catch (NameNotFoundException e) {}						
				}
			}
		}
		
		ProcessInstance CacheInstance = new ProcessInstance();
		
		CacheInstance.Package = PackageName;
	
		if(appPackageInfo != null)
		{  
			CacheInstance.Name = appPackageInfo.applicationInfo.loadLabel(AppInfo).toString();
			CacheInstance.Icon = resizeImage(appPackageInfo.applicationInfo.loadIcon(AppInfo));
		}
		else if(PackageName.equals("System"))
		{ 
			CacheInstance.Name = PackageName;
			CacheInstance.Icon = resizeImage(ResInfo.getDrawable(R.drawable.system));
		}
		else
			CacheInstance.Name = PackageName;
		
		ProcessCache.put(SearchObj.getName(), CacheInstance);
		
		return true;
	}
	
	public Boolean getExpaned(int pid)
	{
		Boolean Flag = CacheExpaned.get(pid+"");
		if(Flag == null)
			Flag = false;
		
		return Flag;
	}
	
	public void setExpaned(int pid, Boolean Flag)
	{
		CacheExpaned.put(pid+"", Flag);
		return;
	}
	
	public Boolean getSelected(int pid)
	{
		Boolean Flag = CacheSelected.get(pid+"");
		if(Flag == null)
			Flag = false;
		
		return Flag;
	}
	
	public void setSelected(int pid, Boolean Flag)
	{
		CacheSelected.put(pid+"", Flag);
		return;
	}
	
	public ArrayList<String> getSelected()
	{
		ArrayList<String> selectPID = new ArrayList<String>();
        Iterator<String> It = CacheSelected.keySet().iterator();
        while (It.hasNext())
        {
        	String cacheKey = (String) It.next();
        	if(CacheSelected.get(cacheKey) == true)
        		selectPID.add(cacheKey);
        }
        
        return selectPID;
	}
	
	public void clearSelected()
	{
		CacheSelected.clear();
		return;
	}

	
	public String getPackageName(int pid) 
	{
		ProcessInstance Process = ProcessCache.get(JNILibrary.GetProcessName(pid));
		if(Process != null)
			return Process.Name;
		else
			return "";
	}

	public String getPacakge(int pid)
	{
		ProcessInstance Process = ProcessCache.get(JNILibrary.GetProcessName(pid));
		if(Process != null)
			return Process.Package;
		else
			return "";
	}
	
	public String getAppInfo(int pid) {
		StringBuilder appbuf = new StringBuilder();
		appbuf.setLength(0);

		appbuf.append("\t"+ResInfo.getString(R.string.process_detail_process)+": ")
			  .append(JNILibrary.GetProcessName(pid))
	          .append("\n\t"+ResInfo.getString(R.string.process_detail_memory)+": ");

		if(JNILibrary.GetProcessRSS(pid) > 1024) {
			appbuf.append(JNILibrary.GetProcessRSS(pid)/1024)
				  .append('M');
		}
		else {
			appbuf.append(JNILibrary.GetProcessRSS(pid))
				  .append('K');
		}
		
		// get PSS
		try {
			int ProcessPID[] = new int[1];
			ProcessPID[0] = pid;

			Method getProcessInfo = ActivityManager.class.getMethod("getProcessMemoryInfo",new Class[] { int[].class });
			Debug.MemoryInfo[] pMemoryInfo = (Debug.MemoryInfo[]) getProcessInfo.invoke(ActInfo, ProcessPID);
	        
			if(pMemoryInfo != null)
	        {
	        	int PSSMemory = pMemoryInfo[0].dalvikPss + pMemoryInfo[0].nativePss + pMemoryInfo[0].otherPss;

	        	if(PSSMemory > 1024) {
	        		appbuf.append(" (")
	        			  .append(PSSMemory/1024)
	        			  .append("M)");
	        	}
	        	else {
	        		appbuf.append("(")
	        			  .append(PSSMemory)
	        			  .append("K)");
	        	}
	        }
		} catch (Exception e) {}
		 
		appbuf.append("\t  "+ResInfo.getString(R.string.process_detail_thread)+": ")
			  .append(JNILibrary.GetProcessThreads(pid))
			  .append("\t  "+ResInfo.getString(R.string.process_detail_load)+": ")
			  .append(JNILibrary.GetProcessLoad(pid))
			  .append("%\n\t"+ResInfo.getString(R.string.process_detail_stime)+": ")
			  .append(JNILibrary.GetProcessSTime(pid))
			  .append("\t  "+ResInfo.getString(R.string.process_detail_utime)+": ")
			  .append(JNILibrary.GetProcessUTime(pid))
			  .append("\t  "+ResInfo.getString(R.string.process_detail_nice)+": ")
			  .append(JNILibrary.GetProcessNice(pid))
			  .append("\n\t"+ResInfo.getString(R.string.process_detail_user)+": ")
			  .append(JNILibrary.GetProcessOwner(pid))
			  .append("\t  "+ResInfo.getString(R.string.process_detail_status)+": ");				  
		
		String Status = JNILibrary.GetProcessStatus(pid).trim();
		if(Status.compareTo("Z") == 0)
			appbuf.append(ResInfo.getString(R.string.process_status_zombie));
		else if(Status.compareTo("S") == 0)
			appbuf.append(ResInfo.getString(R.string.process_status_sleep));
		else if(Status.compareTo("R") == 0)
			appbuf.append(ResInfo.getString(R.string.process_status_running));
		else if(Status.compareTo("D") == 0)
			appbuf.append(ResInfo.getString(R.string.process_status_waitio));
		else if(Status.compareTo("T") == 0)
			appbuf.append(ResInfo.getString(R.string.process_status_stop));
		else 
			appbuf.append(ResInfo.getString(R.string.process_status_unknown));

		appbuf.append("\n\t"+ResInfo.getString(R.string.process_detail_time)+": ")
		  	  .append(JNILibrary.GetProcessTime(pid));

		return appbuf.toString();
	}
	
	public Drawable getAppIcon(int pid) 
	{
		return ProcessCache.get(JNILibrary.GetProcessName(pid)).Icon;
	}

	private Drawable resizeImage(Drawable Icon) {

		if(CommonUtil.getScreenSize() == 2)
		{
			Bitmap BitmapOrg = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888); 
			Canvas BitmapCanvas = new Canvas(BitmapOrg);
			Icon.setBounds(0, 0, 60, 60);
			Icon.draw(BitmapCanvas); 
	        return new BitmapDrawable(BitmapOrg);
		}
		else if (CommonUtil.getScreenSize() == 0)
		{
			Bitmap BitmapOrg = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888); 
			Canvas BitmapCanvas = new Canvas(BitmapOrg);
			Icon.setBounds(0, 0, 10, 10);
			Icon.draw(BitmapCanvas); 
	        return new BitmapDrawable(BitmapOrg);
		}
		else
		{
			Bitmap BitmapOrg = Bitmap.createBitmap(22, 22, Bitmap.Config.ARGB_8888); 
			Canvas BitmapCanvas = new Canvas(BitmapOrg);
			Icon.setBounds(0, 0, 22, 22);
			Icon.draw(BitmapCanvas); 
	        return new BitmapDrawable(BitmapOrg);
		}
    }
	
}
