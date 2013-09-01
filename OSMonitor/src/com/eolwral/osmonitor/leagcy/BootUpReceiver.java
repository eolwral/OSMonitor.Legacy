package com.eolwral.osmonitor.leagcy;

import com.eolwral.osmonitor.leagcy.preferences.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver{

	JNIInterface JNILibrary = JNIInterface.getInstance();
	
	@Override
	public void onReceive(Context context, Intent intent) {

		// get cpu info
		JNILibrary.doTaskStart(JNILibrary.doTaskMisc);
    	JNILibrary.doDataRefresh();
    	JNILibrary.doTaskStop();

		// load settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		
        if(settings.getBoolean(Preferences.PREF_AUTOSTART, false) && 
        		settings.getBoolean(Preferences.PREF_STATUSBAR, false))
        {
        	context.startService(new Intent(context, OSMonitorService.class));
        }
        
        if(settings.getBoolean(Preferences.PREF_AUTOSETCPU, false))
        {
        	String CPUGov = settings.getString(Preferences.PREF_SETCPUGOV, "");
        	String SetCPUCmd = "";
        	
        	if(!CPUGov.equals(""))
        	{
        		for(int CPUNum = 0; CPUNum < JNILibrary.GetProcessorNum(); CPUNum++)
        		{
        			SetCPUCmd += "echo "+CPUGov+
        						 " > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_governor"+"\n";
        		}
        	}
        	
        	String CPUFreq[] = settings.getString(Preferences.PREF_SETCPURANGE, ";").split(";");
        	if(CPUFreq.length == 2)
        	{
        		for(int CPUNum = 0; CPUNum < JNILibrary.GetProcessorNum(); CPUNum++)
        		{
        			SetCPUCmd += "echo "+CPUFreq[0]+
        						 " > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_min_freq"+"\n";

        			SetCPUCmd += "echo "+CPUFreq[1]+
        						 " > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_max_freq"+"\n";
        		}		
        	}
    		CommonUtil.execCommand(SetCPUCmd);
        }
	}
}
