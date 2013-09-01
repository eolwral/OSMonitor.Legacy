/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eolwral.osmonitor.leagcy.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.eolwral.osmonitor.leagcy.*;
import com.eolwral.osmonitor.leagcy.preferences.Preferences;

public class MiscBox extends Activity implements OnGestureListener, OnTouchListener
{
	private JNIInterface JNILibrary = JNIInterface.getInstance();;
	
	private long PreCPUFreq = 0;
	private String SensorName = "";
	private float SensorTemp = 0;
	
	private SensorEventListener SensorListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			SensorName = event.sensor.getName().replace("sensor", "");
			SensorTemp = event.values[0];
		}
	};

	private TextView PowerBox = null;
	private boolean Rooted = false;
	
	// Gesture
	private GestureDetector gestureScanner = new GestureDetector(this);;
	
	@Override
	public boolean onTouchEvent(MotionEvent me)
	{
		return gestureScanner.onTouchEvent(me);
	}
	
	@Override
	public boolean onDown(MotionEvent e)
	{
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		try {
			if (Math.abs(e1.getY() - e2.getY()) > CommonUtil.SWIPE_MAX_OFF_PATH)
				return false;
			else if (e1.getX() - e2.getX() > CommonUtil.SWIPE_MIN_DISTANCE &&
							Math.abs(velocityX) > CommonUtil.SWIPE_THRESHOLD_VELOCITY) 
				((TabActivity) this.getParent()).getTabHost().setCurrentTab(4);
			else if (e2.getX() - e1.getX() > CommonUtil.SWIPE_MIN_DISTANCE &&
							Math.abs(velocityX) > CommonUtil.SWIPE_THRESHOLD_VELOCITY) 
				((TabActivity) this.getParent()).getTabHost().setCurrentTab(2);
			else
				return false;
		} catch (Exception e) {
			// nothing
		}

		return true;
	}
	
	@Override
	public void onLongPress(MotionEvent e)
	{
		return;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		return false;
	}
	
	public boolean onTrackballMotion(MotionEvent e)
	{
		return false;
	}
	
	@Override
	public void onShowPress(MotionEvent e)
	{
		return;
	} 
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		// avoid exception - https://review.source.android.com/#/c/21318/
		try {
			event.getY();
		}
		catch (Exception e) { 
			return false;
		}

		if(gestureScanner.onTouchEvent(event))
		{
			v.onTouchEvent(event);
			return true;
		}
		else
		{
			if(v.onTouchEvent(event))
				return true;
			return false;
		}
	}
	
	private Runnable MiscRunnable = new Runnable() {
		public void run() 
		{
		
			if(JNILibrary.doDataLoad() == 1)
			{ 
				Resources ResourceManager = getApplication().getResources();

				TextView UptimeBox = (TextView) findViewById(R.id.uptimeText);
				TextView ProcessorFreqBox = (TextView) findViewById(R.id.processorFreqText);
				TextView ProcessorTempBox = (TextView) findViewById(R.id.processorTempText);
				TextView ProcessorBox = (TextView) findViewById(R.id.processorText);
				TextView DiskBox = (TextView) findViewById(R.id.diskText);
				TextView MinCPUBox = (TextView) findViewById(R.id.setCpuMin);
				TextView MaxCPUBox = (TextView) findViewById(R.id.setCpuMax);
				TextView GovCPUBox = (TextView) findViewById(R.id.setCpuGov);

				StringBuilder m_UptimeStr = new StringBuilder();
				long Uptime = android.os.SystemClock.elapsedRealtime();
				int Seconds = (int) ((Uptime / 1000) % 60);
				int Minutes = (int) ((Uptime / 1000) / 60 % 60);
				int Hours   = (int) ((Uptime / 1000)  / 3600 % 24);
				int Days    = (int) ((Uptime / 1000) / 86400);
				 
				m_UptimeStr.append(ResourceManager.getText(R.string.uptime_text));

				m_UptimeStr.append(": "+Days+" "+ResourceManager.getText(R.string.uptimeday_text));
				if(Hours < 10)
					m_UptimeStr.append(" 0"+Hours);
				else
					m_UptimeStr.append(" "+Hours);
				
				if(Minutes < 10)
					m_UptimeStr.append(":0"+Minutes);
				else
					m_UptimeStr.append(":"+Minutes);
				
				if(Seconds < 10)
					m_UptimeStr.append(":0"+Seconds);
				else
					m_UptimeStr.append(":"+Seconds);
				
				UptimeBox.setText(Html.fromHtml(m_UptimeStr.toString()));
				
				StringBuilder m_ProcessorStr = new StringBuilder();

       		   	m_ProcessorStr.append(ResourceManager.getText(R.string.processorscal_text));
    			
    			ProcessorFreqBox.setText(Html.fromHtml(m_ProcessorStr.toString()));
    			
    			m_ProcessorStr = new StringBuilder();
    			m_ProcessorStr.append("&nbsp;&nbsp;&nbsp;&nbsp;<b>"+JNILibrary.GetProcessorScalMin(0)+"</b> ");
    			MinCPUBox.setText(Html.fromHtml(m_ProcessorStr.toString()));
    			
    			m_ProcessorStr = new StringBuilder();
    			m_ProcessorStr.append(" <b>"+JNILibrary.GetProcessorScalMax(0)+"</b> ");
    			MaxCPUBox.setText(Html.fromHtml(m_ProcessorStr.toString()));
    
    			GovCPUBox.setText(Html.fromHtml(ResourceManager.getText(R.string.processorgov_text)
    		   				  	  +": <i>"+JNILibrary.GetProcessorScalGov(0)+"</i>"));
    			
		        m_ProcessorStr = new StringBuilder();
    		   	
    		   	m_ProcessorStr.append(ResourceManager.getText(R.string.processorfreq_text)+"<br />")
    		   				  .append("&nbsp;&nbsp;&nbsp;&nbsp;<b>"+JNILibrary.GetProcessorMin(0)+"</b> ~ ")
    		   				  .append("<b>"+JNILibrary.GetProcessorMax(0)+"</b><br />");

//    		   	m_ProcessorStr.append(ResourceManager.getText(R.string.processorgov_text))
  //  		   				  .append(": <i>"+JNILibrary.GetProcessorScalGov()+"</i><br />");
    		   	
    		   	if(JNILibrary.GetProcessorScalCur(0) > PreCPUFreq)
    		   	{
        		   	m_ProcessorStr.append(ResourceManager.getText(R.string.processorcur_text))
	   				  			  .append(": <font color=red>"+JNILibrary.GetProcessorScalCur(0)+"</font><br />");
    		   	}
    		   	else if (JNILibrary.GetProcessorScalCur(0) < PreCPUFreq)
    		   	{
    		   		m_ProcessorStr.append(ResourceManager.getText(R.string.processorcur_text))
    		   					  .append(": <font color=green>"+JNILibrary.GetProcessorScalCur(0)+"</font><br />");
    		   	}
    		   	else
    		   	{
        		   	m_ProcessorStr.append(ResourceManager.getText(R.string.processorcur_text))
			  			  .append(": "+JNILibrary.GetProcessorScalCur(0)+"<br />");
    		   	}
    		   	
    		   	m_ProcessorStr.append(ResourceManager.getText(R.string.processorcore_text))
    		   				  .append(": "+JNILibrary.GetProcessorNum());
    		   	
    		   	
    		   	PreCPUFreq = JNILibrary.GetProcessorScalCur(0);

    		   	if(JNILibrary.GetProcessorScalMin(0) != 0)
    		   		ProcessorBox.setText(Html.fromHtml(m_ProcessorStr.toString()));
    		   	else
    		   		ProcessorBox.setText("");
    		   	
    		   	StringBuilder m_ProcessorTempStr = new StringBuilder();
    		   	java.text.DecimalFormat TempFormat = new java.text.DecimalFormat("#.##");

    			if(JNILibrary.GetProcessorOMAPTemp() != 0)
    				m_ProcessorTempStr.append("OMAP3403 "+ResourceManager.getText(R.string.processortmp_text)+"<br />")
    		   					      .append("&nbsp;&nbsp;&nbsp;&nbsp<i>"+JNILibrary.GetProcessorOMAPTemp()+"°C")
		    		  		   	      .append(" ("+TempFormat.format(((double)JNILibrary.GetProcessorOMAPTemp()*9/5+32))+"°F)</i>");

    		    if(SensorTemp != 0)
    		    	m_ProcessorTempStr.append("<br />"+SensorName+"<br />")
    		    	 			      .append("&nbsp;&nbsp;&nbsp;&nbsp<b>"+((double)SensorTemp)+"°C")
		    		  			      .append(" ("+TempFormat.format(((double)SensorTemp*9/5+32))+"°F)</b>");
    		   		
    		    ProcessorTempBox.setText(Html.fromHtml(m_ProcessorTempStr.toString()));

    		   	StringBuilder m_DiskStr = new StringBuilder();
	
				java.text.DecimalFormat DiskFormat = new java.text.DecimalFormat(",###");
				java.text.DecimalFormat UsageFormat = new java.text.DecimalFormat("#.#");
				
				String DiskTotal = ResourceManager.getText(R.string.disk_total_text).toString();
				String DiskUsed = ResourceManager.getText(R.string.disk_used_text).toString();
				String DiskAvail = ResourceManager.getText(R.string.disk_available_text).toString();
				
				if(JNILibrary.GetSystemMemAvail() == 0)
					m_DiskStr.append("<b>/system</b>&nbsp;&nbsp;&nbsp;0% Used");
				else
					m_DiskStr.append("<b>/system</b>&nbsp;&nbsp;&nbsp;"+UsageFormat.format(JNILibrary.GetSystemMemUsed()/JNILibrary.GetSystemMemTotal()*100)+"% Used");
					
				m_DiskStr.append("<br /> "+DiskTotal+": "+DiskFormat.format(JNILibrary.GetSystemMemTotal())+"K ")
        				 .append("<br /> "+DiskUsed+": "+DiskFormat.format(JNILibrary.GetSystemMemUsed())+"K ")
        				 .append("<br /> "+DiskAvail+": "+DiskFormat.format(JNILibrary.GetSystemMemAvail())+"K ");

				if(JNILibrary.GetDataMemAvail() == 0)
					m_DiskStr.append("<br /><br /><b>/data</b>&nbsp;&nbsp;&nbsp;0% Used");
				else
					m_DiskStr.append("<br /><br /><b>/data</b>&nbsp;&nbsp;&nbsp;"+UsageFormat.format(JNILibrary.GetDataMemUsed()/JNILibrary.GetDataMemTotal()*100)+"% Used");
        		
				m_DiskStr.append("<br /> "+DiskTotal+": "+DiskFormat.format(JNILibrary.GetDataMemTotal())+"K ")
        				 .append("<br /> "+DiskUsed+": "+DiskFormat.format(JNILibrary.GetDataMemUsed())+"K ")
        				 .append("<br /> "+DiskAvail+": "+DiskFormat.format(JNILibrary.GetDataMemAvail())+"K ");
				
				if(JNILibrary.GetSDCardMemAvail() == 0)
					m_DiskStr.append("<br /><br /><b>/sdcard</b>&nbsp;&nbsp;&nbsp;0% Used");
				else
					m_DiskStr.append("<br /><br /><b>/sdcard</b>&nbsp;&nbsp;&nbsp;"+UsageFormat.format(JNILibrary.GetSDCardMemUsed()/JNILibrary.GetSDCardMemTotal()*100)+ "% Used");
        		
				m_DiskStr.append("<br /> "+DiskTotal+": "+DiskFormat.format(JNILibrary.GetSDCardMemTotal())+"K ")
	        			 .append("<br /> "+DiskUsed+": "+DiskFormat.format(JNILibrary.GetSDCardMemUsed())+"K ")
        				 .append("<br /> "+DiskAvail+": "+DiskFormat.format(JNILibrary.GetSDCardMemAvail())+"K ");        		         		 

				if(JNILibrary.GetCacheMemAvail() == 0)
					m_DiskStr.append("<br /><br /><b>/cache</b>&nbsp;&nbsp;&nbsp;0% Used");
				else
					m_DiskStr.append("<br /><br /><b>/cache</b>&nbsp;&nbsp;&nbsp;"+UsageFormat.format(JNILibrary.GetCacheMemUsed()/JNILibrary.GetCacheMemTotal()*100)+"% Used");
        		
				m_DiskStr.append("<br /> "+DiskTotal+": "+DiskFormat.format(JNILibrary.GetCacheMemTotal())+"K ")
						 .append("<br /> "+DiskUsed+": "+DiskFormat.format(JNILibrary.GetCacheMemUsed())+"K ")
						 .append("<br /> "+DiskAvail+": "+DiskFormat.format(JNILibrary.GetCacheMemAvail())+"K ");
        		
    	    	DiskBox.setText(Html.fromHtml(m_DiskStr.toString()));		

        	}
           	MiscHandler.postDelayed(this, 1000);
        }
	};   
	
	Handler MiscHandler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.misclayout);

        ((ScrollView) findViewById(R.id.miscview)).setOnTouchListener(this);

        PowerBox = (TextView) findViewById(R.id.powerText);
        
        ImageButton MinCpu = (ImageButton) findViewById(R.id.btnCpuMin);

        MinCpu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	AlertDialog.Builder SetCPUMinBox = new AlertDialog.Builder(MiscBox.this);
            	
            	String [] CPUFreqList = GetCPUFreqList();
            	
            	if(CPUFreqList == null)
            		return;
            	
            	int CurFreq = 0;
            	for(CurFreq = 0; CurFreq < CPUFreqList.length; CurFreq++)
            	{
            		if(Integer.parseInt(CPUFreqList[CurFreq]) == JNILibrary.GetProcessorScalMin(0))
            			break;
            	}
            	
                SetCPUMinBox.setSingleChoiceItems(GetCPUFreqList(), CurFreq, new DialogInterface.OnClickListener() 
                	{
                    	public void onClick(DialogInterface dialog, int item) 
                    	{
                        	if(!Rooted)
                        	{
                				dialog.dismiss();
                        		return;
                        	}

                    		String SetCPUCmd = "";
                    		String [] CPUFreqList = GetCPUFreqList();

                    		
                    		if(CPUFreqList == null)
                    		{
                    			dialog.dismiss();
                        		return;
                    		}
                			
                    		if(Integer.parseInt(CPUFreqList[item]) <= JNILibrary.GetProcessorScalMax(0) 
                    				&& Rooted)
                			{
                    			for(int CPUNum = 0; CPUNum < JNILibrary.GetProcessorNum(); CPUNum++)
                    			{
                    				SetCPUCmd = "echo "+CPUFreqList[item]+
                    							" > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_min_freq"+"\n";
                    				CommonUtil.execCommand(SetCPUCmd);
                    			}
                			}
            				dialog.dismiss();
                    	}
                	}
                );
                
                AlertDialog SetCPUMin = SetCPUMinBox.create();
                SetCPUMin.show();
            }
        });

        ImageButton MaxCpu = (ImageButton) findViewById(R.id.btnCpuMax);

        MaxCpu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AlertDialog.Builder SetCPUMaxBox = new AlertDialog.Builder(MiscBox.this);
            	
            	String [] CPUFreqList = GetCPUFreqList();
            	
            	if(CPUFreqList == null)
            		return;
            	
            	int CurFreq = 0;
            	for(CurFreq = 0; CurFreq < CPUFreqList.length; CurFreq++)
            	{
            		if(Integer.parseInt(CPUFreqList[CurFreq]) == JNILibrary.GetProcessorScalMax(0))
            			break;
            	}
            	
                SetCPUMaxBox.setSingleChoiceItems(GetCPUFreqList(), CurFreq, new DialogInterface.OnClickListener() 
                	{
                    	public void onClick(DialogInterface dialog, int item) 
                    	{
                        	if(!Rooted)
                        	{
                				dialog.dismiss();
                        		return;
                        	}

                    		String SetCPUCmd = "";
                    		String [] CPUFreqList = GetCPUFreqList();

                    		if(CPUFreqList == null)
                    		{
                    			dialog.dismiss();
                        		return;
                    		}
                			

                    		if(Integer.parseInt(CPUFreqList[item]) >= JNILibrary.GetProcessorScalMin(0)
                				&& Rooted)
                			{
                    			for(int CPUNum = 0; CPUNum < JNILibrary.GetProcessorNum(); CPUNum++)
                    			{
                    				SetCPUCmd = "echo "+CPUFreqList[item]+
                    							" > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_max_freq"+"\n";
                    				CommonUtil.execCommand(SetCPUCmd);
                    			}
                			}
            				dialog.dismiss();
                    	}
                	}
                );
                
                AlertDialog SetCPUMax = SetCPUMaxBox.create();
                SetCPUMax.show();
            }
        });

        ImageButton GovCpu = (ImageButton) findViewById(R.id.btnCpuGov);

        GovCpu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AlertDialog.Builder SetCPUMaxBox = new AlertDialog.Builder(MiscBox.this);
            	
            	String [] CPUGovList = GetCPUGovList();
            	
            	if(CPUGovList == null)
            		return;
            	
            	int CurGov = 0;
            	for(CurGov = 0; CurGov < CPUGovList.length; CurGov++)
            	{
            		if(CPUGovList[CurGov].equals(JNILibrary.GetProcessorScalGov(0)))
            			break;
            	}
            	
                SetCPUMaxBox.setSingleChoiceItems(GetCPUGovList(), CurGov, new DialogInterface.OnClickListener() 
                	{
                    	public void onClick(DialogInterface dialog, int item) 
                    	{
                        	if(!Rooted)
                        	{
                				dialog.dismiss();
                        		return;
                        	}

                    		String SetCPUCmd = "";
                    		String [] CPUGovList = GetCPUGovList();

                    		if(CPUGovList == null)
                    		{
                    			dialog.dismiss();
                        		return;
                    		}
                			

                    		if(Rooted)
                			{
                    			for(int CPUNum = 0; CPUNum < JNILibrary.GetProcessorNum(); CPUNum++)
                    			{
                    				SetCPUCmd = "echo "+CPUGovList[item]+
                    							" > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_governor"+"\n";
                    				CommonUtil.execCommand(SetCPUCmd);
                    			}
                			}
            				dialog.dismiss();
                    	}
                	}
                );
                
                AlertDialog SetCPUMax = SetCPUMaxBox.create();
                SetCPUMax.show();
            }
        });
    }

	private void restorePrefs()
    {
		// load settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        if(settings.getBoolean(Preferences.PREF_STATUSBAR, false))
        {
        	if(OSMonitorService.getInstance() == null)
        		startService(new Intent(this, OSMonitorService.class));
        	else
        		OSMonitorService.getInstance().Notify();
        }
        else
        	if(OSMonitorService.getInstance() != null)
        		OSMonitorService.getInstance().stopSelf();

        // Root
		Rooted = settings.getBoolean(Preferences.PREF_ROOTED, false);
    }
	
    public boolean onCreateOptionsMenu(Menu optionMenu) 
    {
     	super.onCreateOptionsMenu(optionMenu);
     	optionMenu.add(0, 1, 0, getResources().getString(R.string.menu_options));
       	optionMenu.add(0, 4, 0, getResources().getString(R.string.menu_help));
       	optionMenu.add(0, 5, 0, getResources().getString(R.string.menu_forceexit));
        
    	return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) 
    {
    	switch (id)
    	{
    	case 0:
    		AlertDialog.Builder HelpWindows = new AlertDialog.Builder(this);
    		HelpWindows.setTitle(R.string.app_name);
			HelpWindows.setMessage(R.string.help_info);
			HelpWindows.setPositiveButton(R.string.button_close,
			   new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int whichButton) { }
				}
			);

   	        WebView HelpView = new WebView(this);
            HelpView.loadUrl("http://wiki.android-os-monitor.googlecode.com/hg/phonehelp.html?r=b1c196ee43855882e59ad5b015b953d62c95729d");
            HelpWindows.setView(HelpView);

        	return HelpWindows.create(); 
    	}
    	
    	return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	restorePrefs();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
        case 1:
       		Intent launchPreferencesIntent = new Intent().setClass( this, Preferences.class);
       		startActivityForResult(launchPreferencesIntent, 0);
        	break;
        case 4:
        	this.showDialog(0);
        	break;

        case 5:
        	if(OSMonitorService.getInstance() != null)
        		OSMonitorService.getInstance().stopSelf();

        	CommonUtil.killSelf(this);

        	break;
        	
        }
        
        return true;
    }
    
    @Override
    public void onPause() 
    {
   		stopBatteryMonitor();

    	SensorManager SMer = (SensorManager) getSystemService(SENSOR_SERVICE);
    	Sensor TempSensor = SMer.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
    	if(SMer != null  && TempSensor != null)
    		SMer.unregisterListener(SensorListener, TempSensor);

    	MiscHandler.removeCallbacks(MiscRunnable);
    	JNILibrary.doTaskStop();
    	super.onPause();
    }

    @Override
    protected void onResume() 
    {    
    	restorePrefs();

    	SensorManager SMer = (SensorManager) getSystemService(SENSOR_SERVICE);
    	Sensor TempSensor = SMer.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
    	if(SMer != null && TempSensor != null)
    		SMer.registerListener(SensorListener, TempSensor, SensorManager.SENSOR_DELAY_UI);
    	
   		startBatteryMonitor();
    	
    	JNILibrary.doTaskStart(JNILibrary.doTaskMisc);
    	MiscHandler.post(MiscRunnable);
    	super.onResume();
    }
    
    private String [] GetCPUFreqList() 
    {
    	try {
    		byte[] RawData = new byte[256];

    		File CPUFreq = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
    		BufferedInputStream bInputStream = 
    							new BufferedInputStream(new FileInputStream(CPUFreq));

    		bInputStream.read(RawData);
    		String CPUFreqList = (new String(RawData)).trim();
    		bInputStream.close();
    		
    		String [] FreqList = CPUFreqList.split(" ");
    		
    		return FreqList;
    		
    	} catch (Exception e) {}
    	
    	return null;
    }
    
    private String [] GetCPUGovList() 
    {
    	try {
    		byte[] RawData = new byte[256];

    		File CPUGov = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
    		BufferedInputStream bInputStream = 
    							new BufferedInputStream(new FileInputStream(CPUGov));

    		bInputStream.read(RawData);
    		String CPUGovList = (new String(RawData)).trim();
    		bInputStream.close();
    		
    		String [] GovList = CPUGovList.split(" ");
    		
    		return GovList;
    		
    	} catch (Exception e) {}
    	
    	return null;
    }
    
    private void startBatteryMonitor()
    {
    	IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	registerReceiver(battReceiver, battFilter);		        		
    }
    
    private void stopBatteryMonitor()
    {
    	unregisterReceiver(battReceiver);
    }

	private BroadcastReceiver battReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) {
			
			Resources ResourceManager = getApplication().getResources();
			
			if(ResourceManager == null || PowerBox == null)
				return;
			
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			int status = intent.getIntExtra("status", -1);
			int health = intent.getIntExtra("health", -1);
			int plugged = intent.getIntExtra("plugged", -1);
			int temperature = intent.getIntExtra("temperature", -1);
			int voltage = intent.getIntExtra("voltage", -1);
			String technology = intent.getStringExtra("technology");
				
			int level = -1;  // percentage, or -1 for unknown
			if (rawlevel > 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			else 
				level = 0;

	        StringBuilder m_PowerStr = new StringBuilder();
			m_PowerStr.append(ResourceManager.getText(R.string.battery_status_text));
			switch(status) 
			{
			case BatteryManager.BATTERY_STATUS_UNKNOWN:
				m_PowerStr.append(": <b>"+ResourceManager.getText(R.string.battery_status_unknown_text)+"</b>");
				break;
			case BatteryManager.BATTERY_STATUS_CHARGING:
				m_PowerStr.append(": <b>"+ResourceManager.getText(R.string.battery_status_charging_text)+"</b>");
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
				m_PowerStr.append(": <b>"+ResourceManager.getText(R.string.battery_status_discharging_text)+"</b>");
				break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				m_PowerStr.append(": <b>"+ResourceManager.getText(R.string.battery_status_notcharging_text)+"</b>");
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				m_PowerStr.append(": <b>"+ResourceManager.getText(R.string.battery_status_full_text)+"</b>");
				break;
			}
				
			m_PowerStr.append("<br />"+ResourceManager.getText(R.string.battery_health_text));
			switch(health)
			{
			case BatteryManager.BATTERY_HEALTH_DEAD:
				m_PowerStr.append(": "+ResourceManager.getText(R.string.battery_health_dead_text));
				break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
				m_PowerStr.append(": "+ResourceManager.getText(R.string.battery_health_good_text));
				break;
			case BatteryManager.BATTERY_HEALTH_OVERHEAT:
				m_PowerStr.append(": "+ResourceManager.getText(R.string.battery_health_overheat_text));
				break;
			case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
				m_PowerStr.append(": "+ResourceManager.getText(R.string.battery_health_overvoltage_text));
				break;
			case BatteryManager.BATTERY_HEALTH_UNKNOWN:
				m_PowerStr.append(": "+ResourceManager.getText(R.string.battery_health_unknown_text));
				break;
			case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
				m_PowerStr.append(": "+ResourceManager.getText(R.string.battery_health_failure_text));
				break;
				
			}
			
			java.text.DecimalFormat TempFormat = new java.text.DecimalFormat("#.##");
			m_PowerStr.append("<br />"+ResourceManager.getText(R.string.battery_technology_text))
					  .append(": <i>"+technology+"</i>")
					  .append("<br />"+ResourceManager.getText(R.string.battery_capacity_text))
   		    		  .append(": "+level+"%")
   		    		  .append("<br />"+ResourceManager.getText(R.string.battery_voltage_text))
   		    		  .append(": <b>"+voltage+"mV</b>")        		  
   		    		  .append("<br />"+ResourceManager.getText(R.string.battery_temperature_text))
   		    		  .append(": "+((double)temperature/10)+"\u2103")
   		    		  .append(" ("+TempFormat.format(((double)temperature/10*9/5+32))+"\u2109)");
				
			if(plugged == BatteryManager.BATTERY_PLUGGED_AC)
				m_PowerStr.append("<br />"+ResourceManager.getText(R.string.battery_acpower_text))
						  .append(": <font color=\"green\">")
						  .append(ResourceManager.getText(R.string.battery_online_text)+"</font>");
       		else
				m_PowerStr.append("<br />"+ResourceManager.getText(R.string.battery_acpower_text))
				  		  .append(": <font color=\"red\">")
				  		  .append(ResourceManager.getText(R.string.battery_offline_text)+"</font>");
       
			if(plugged == BatteryManager.BATTERY_PLUGGED_USB)
				m_PowerStr.append("<br />"+ResourceManager.getText(R.string.battery_usbpower_text))
						  .append(": <font color=\"green\">")
						  .append(ResourceManager.getText(R.string.battery_online_text)+"</font>");
    	   	else
				m_PowerStr.append("<br />"+ResourceManager.getText(R.string.battery_usbpower_text))
						  .append(": <font color=\"red\">")
						  .append(ResourceManager.getText(R.string.battery_offline_text)+"</font>");
			
			PowerBox.setText(Html.fromHtml(m_PowerStr.toString()));
		}
	};
    
}
