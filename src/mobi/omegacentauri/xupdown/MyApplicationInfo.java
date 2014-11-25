package mobi.omegacentauri.xupdown;

import java.util.Comparator;
import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class MyApplicationInfo {
	private String label;
	private int versionCode;
	private int uid;
	public String packageName;
	
	public static final Comparator<MyApplicationInfo> LabelComparator = 
		new Comparator<MyApplicationInfo>() {

		public int compare(MyApplicationInfo a, MyApplicationInfo b) {
//			Log.v("DoublePower", a.component+" "+b.component);
			return a.label.compareToIgnoreCase(b.label);
		}
	};
	
	String getKey() {
		return Locale.getDefault().toString() + "." + uid + "." + versionCode + "." + packageName;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public MyApplicationInfo(MyCache cache, PackageManager pm, ResolveInfo r) {
		packageName = r.activityInfo.packageName;
		uid = r.activityInfo.applicationInfo.uid;
		
		try {
			versionCode = (pm.getPackageInfo(packageName, 0)).versionCode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			versionCode = 0;
		}
		
		if (cache != null) {
			String cached = cache.lookup(getKey());
			if (cached != null) {
				label = cached;
				return;
			}
		}
		
		CharSequence l = r.activityInfo.loadLabel(pm); 
		if (l == null) {
			label = packageName;
		}
		else {			
			label = l.toString();
			if (label.equals("Angry Birds")) {
				if(packageName.startsWith("com.rovio.angrybirdsrio")) {
					label = label + " Rio";
				}
				else if (packageName.startsWith("com.rovio.angrybirdsseasons")) {
					label = label + " Seasons";
				}
			}
			if (cache != null)
				cache.add(getKey(), label);
		}
	}
	
	public String getLabel() {
		return label;
	}
	
	static public String getSmartLabel(Context c, String component, String label) {
		return label;
	}	
}

