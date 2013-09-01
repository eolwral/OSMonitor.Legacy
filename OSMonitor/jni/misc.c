#include "misc.h"

processor_info cur_processor[8];
unsigned int omaptemp = 0;
int cur_processor_num = 0;

void misc_dump_processor()
{
	char buf[128];
	int i;

	for(i=0; i<8; i++)
	{
		sprintf(buf, CPUINFO_MAX, i);

		// get cpu max freq
		FILE *cpufile = fopen(buf, "r");
		if(!cpufile)
			cur_processor[i].cpumax =0;
		else
		{
			fscanf(cpufile, "%d", &cur_processor[i].cpumax);
			fclose(cpufile);
			cur_processor_num = i+1;
		}

		sprintf(buf, CPUINFO_MIN, i);

		// get cpu min freq
		cpufile = fopen(buf, "r");
		if(!cpufile)
			cur_processor[i].cpumin =0;
		else
		{
			fscanf(cpufile, "%d", &cur_processor[i].cpumin);
			fclose(cpufile);
		}

		sprintf(buf, CPU_SCALING_CUR, i);

		// get scaling cur freq
		cpufile = fopen(buf, "r");
		if(!cpufile)
			cur_processor[i].scalcur =0;
		else
		{
			fscanf(cpufile, "%d", &cur_processor[i].scalcur);
			fclose(cpufile);
		}

		sprintf(buf, CPU_SCALING_MAX, i);

		// get scaling max freq
		cpufile = fopen(buf, "r");
		if(!cpufile)
			cur_processor[i].scalmax =0;
		else
		{
			fscanf(cpufile, "%d", &cur_processor[i].scalmax);
			fclose(cpufile);
		}

		sprintf(buf, CPU_SCALING_MIN, i);

		// get scaling min freq
		cpufile = fopen(buf, "r");
		if(!cpufile)
			cur_processor[i].scalmin =0;
		else
		{
			fscanf(cpufile, "%d", &cur_processor[i].scalmin);
			fclose(cpufile);
		}

		sprintf(buf, CPU_SCALING_GOR, i);

		// get scaling governor
		cpufile = fopen(buf, "r");
		if(!cpufile)
			strcpy(cur_processor[i].scalgov, "");
		else
		{
			fscanf(cpufile, "%s", cur_processor[i].scalgov);
			fclose(cpufile);
		}

	}

	// OMAP3430 temperature
	FILE *cpufile = fopen(OMAP_TEMPERATURE, "r");
	if(!cpufile)
		omaptemp = 0;
	else
	{
		fscanf(cpufile, "%d", &omaptemp);
		fclose(cpufile);
	}

}

int misc_get_processor_cpumax(int num)
{
	return cur_processor[num].cpumax;
}

int misc_get_processor_cpumin(int num)
{
	return cur_processor[num].cpumin;
}

int misc_get_processor_scalcur(int num)
{
	return cur_processor[num].scalcur;
}

int misc_get_processor_scalmax(int num)
{
	return cur_processor[num].scalmax;
}

int misc_get_processor_scalmin(int num)
{
	return cur_processor[num].scalmin;
}

void misc_get_processor_scalgov(int num, char* buf)
{
	strcpy(buf, cur_processor[num].scalgov);
	return;
}

int misc_get_processor_number()
{
	return cur_processor_num;
}

int misc_get_processor_omaptemp()
{
	return omaptemp;
}

power_info cur_powerinfo;

void misc_dump_power()
{
	char buf[128];
	FILE *fp;

	memset(&cur_powerinfo, 0, sizeof(power_info));

	fp = fopen(BATTERY_STATUS_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        strncpy(cur_powerinfo.status, buf, 16);
        fclose(fp);
    }

    fp = fopen(BATTERY_HEALTH_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%s", cur_powerinfo.health);
        fclose(fp);
    }

    fp = fopen(BATTERY_CAPACITY_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%d", &cur_powerinfo.capacity);
        fclose(fp);
    }

    fp = fopen(BATTERY_VOLTAGE_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%d", &cur_powerinfo.voltage);
        fclose(fp);
    }

    fp = fopen(BATTERY_TEMPERATURE_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%d", &cur_powerinfo.temperature);
        fclose(fp);
    }

    fp = fopen(BATTERY_TECHNOLOGY_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%s", cur_powerinfo.technology);
        fclose(fp);
    }

    fp = fopen(AC_ONLINE_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%d", &cur_powerinfo.aconline);
        fclose(fp);
    }

    fp = fopen(USB_ONLINE_PATH, "r");
    if(fp != 0)
    {
    	fgets(buf, 128, fp);
        sscanf(buf, "%d", &cur_powerinfo.usbonline);
        fclose(fp);
    }

}

int misc_get_power_capacity()
{
	return cur_powerinfo.capacity;
}

int misc_get_power_voltage()
{
	return cur_powerinfo.voltage;
}

int misc_get_power_temperature()
{
	return cur_powerinfo.temperature;
}

int misc_get_power_aconline()
{
	return cur_powerinfo.aconline;
}

int misc_get_power_usbonline()
{
	return cur_powerinfo.usbonline;
}

void misc_get_power_health(char *buf)
{
	snprintf(buf, BUFFERSIZE, "%s", cur_powerinfo.health);
	return;
}

void misc_get_power_status(char *buf)
{
	snprintf(buf, BUFFERSIZE, "%s", cur_powerinfo.status);
	return;
}

void misc_get_power_technology(char *buf)
{
	snprintf(buf, BUFFERSIZE, "%s", cur_powerinfo.technology);
	return;
}

/* File System Module */
struct statfs datafs;
struct statfs systemfs;
struct statfs sdcardfs;
struct statfs cachefs;


void misc_dump_filesystem()
{
	memset(&datafs, 0, sizeof(statfs));
	memset(&systemfs, 0, sizeof(statfs));
	memset(&sdcardfs, 0, sizeof(statfs));
	memset(&cachefs, 0, sizeof(statfs));

	statfs(DATA_PATH, &datafs);
	statfs(SYSTEM_PATH, &systemfs);
	statfs(SDCARD_PATH, &sdcardfs);
	statfs(CACHE_PATH, &cachefs);
}

double misc_get_filesystem_systemtotal()
{
	if(systemfs.f_blocks * systemfs.f_bsize == 0)
		return 0;
	return ((long long)systemfs.f_blocks * (long long)systemfs.f_bsize) / 1024;
}

double misc_get_filesystem_datatotal()
{
	if(datafs.f_blocks * datafs.f_bsize == 0)
		return 0;
	return ((long long)datafs.f_blocks * (long long)datafs.f_bsize) / 1024;
}

double misc_get_filesystem_sdcardtotal()
{
	if(sdcardfs.f_blocks * sdcardfs.f_bsize == 0)
		return 0;
	return ((long long)sdcardfs.f_blocks * (long long)sdcardfs.f_bsize) / 1024;
}

double misc_get_filesystem_cachetotal()
{
	if(cachefs.f_blocks * cachefs.f_bsize == 0)
		return 0;
	return ((long long)cachefs.f_blocks * (long long)cachefs.f_bsize) / 1024;
}

double misc_get_filesystem_systemused()
{
	if(systemfs.f_blocks * systemfs.f_bfree == 0)
		return 0;
	return ((long long)(systemfs.f_blocks - (long long)systemfs.f_bfree) * systemfs.f_bsize) / 1024;
}

double misc_get_filesystem_dataused()
{
	if(datafs.f_blocks * datafs.f_bfree == 0)
		return 0;
	return ((long long)(datafs.f_blocks - (long long)datafs.f_bfree) * datafs.f_bsize) / 1024;
}

double misc_get_filesystem_sdcardused()
{
	if(sdcardfs.f_blocks * sdcardfs.f_bfree == 0)
		return 0;
	return ((long long)(sdcardfs.f_blocks - (long long)sdcardfs.f_bfree) * sdcardfs.f_bsize) / 1024;
}

double misc_get_filesystem_cacheused()
{
	if(cachefs.f_blocks * cachefs.f_bfree == 0)
		return 0;
	return ((long long)(cachefs.f_blocks - (long long)cachefs.f_bfree) * cachefs.f_bsize) / 1024;
}


double misc_get_filesystem_systemavail()
{
	if(systemfs.f_bfree * systemfs.f_bsize == 0)
		return 0;
	return ((long long)systemfs.f_bfree * (long long)systemfs.f_bsize) / 1024;
}

double misc_get_filesystem_dataavail()
{
	if(datafs.f_bfree * datafs.f_bsize == 0)
		return 0;
	return ((long long)datafs.f_bfree * (long long)datafs.f_bsize) / 1024;
}

double misc_get_filesystem_sdcardavail()
{
	if(sdcardfs.f_bfree * sdcardfs.f_bsize == 0)
		return 0;
	return ((long long)sdcardfs.f_bfree * (long long)sdcardfs.f_bsize) / 1024;
}

double misc_get_filesystem_cacheavail()
{
	if(cachefs.f_bfree * cachefs.f_bsize == 0)
		return 0;
	return ((long long)cachefs.f_bfree * (long long)cachefs.f_bsize) / 1024;
}
