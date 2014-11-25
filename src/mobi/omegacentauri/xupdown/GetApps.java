package mobi.omegacentauri.xupdown;

import java.util.ArrayList;
import java.util.Map;

import android.widget.CheckBox;
import java.util.List;


import mobi.omegacentauri.xupdown.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class GetApps extends AsyncTask<Void, Integer, List<MyApplicationInfo>> {
	final PackageManager pm;
	final Context	 context;
	final ListView listView;
	final SharedPreferences pref;
	public final static String cachePath = "app_labels"; 
	ProgressDialog progress;
	
	GetApps(Context c, ListView lv, SharedPreferences pref) {
		context = c;
		this.pref = pref;
		pm = context.getPackageManager();
		listView = lv;
	}

	private boolean profilable(ApplicationInfo a) {
		return true;
	}

	@Override
	protected List<MyApplicationInfo> doInBackground(Void... c) {
		Log.v("getting", "installed");
		
		Intent launchIntent = new Intent(Intent.ACTION_MAIN);
		launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		
		List<ResolveInfo> list = 
			pm.queryIntentActivities(launchIntent, 0);
		
		List<MyApplicationInfo> myList = new ArrayList<MyApplicationInfo>();
		
		MyCache cache = new MyCache(MyCache.genFilename(context, cachePath));
		
		for (int i = 0 ; i < list.size() ; i++) {
			publishProgress(i, list.size());
			MyApplicationInfo myAppInfo;
			myAppInfo = new MyApplicationInfo(
					cache, pm, list.get(i));
			myList.add(myAppInfo);
		}
		cache.commit();
		
		publishProgress(list.size(), list.size());
		
		return myList;
	}
	
	@Override
	protected void onPreExecute() {
//		listView.setVisibility(View.GONE);
		progress = new ProgressDialog(context);
		progress.setCancelable(false);
		progress.setMessage("Getting applications...");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setIndeterminate(true);
		progress.show();
	}
	
	protected void onProgressUpdate(Integer... p) {
		progress.setIndeterminate(false);
		progress.setMax(p[1]);
		progress.setProgress(p[0]);
	}
	
	@Override
	protected void onPostExecute(final List<MyApplicationInfo> appInfo) {
		
		ArrayAdapter<MyApplicationInfo> appInfoAdapter = 
			new ArrayAdapter<MyApplicationInfo>(context, 
					R.layout.oneline, 
					appInfo) {

			public View getView(int position, View convertView, ViewGroup parent) {
				View v;				
				
				if (convertView == null) {
	                v = View.inflate(context, R.layout.oneline, null);
	            }
				else {
					v = convertView;
				}

				final MyApplicationInfo a = appInfo.get(position); 
				CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox);
				cb.setText(a.getLabel());
				cb.setOnCheckedChangeListener(null);
				cb.setChecked(
						null != (pref.getString(Apps.PREF_APPS+a.getPackageName(), null)));
				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked)
							((Apps)context).activateApp(a);
						else
							((Apps)context).deactivateApp(a);
					}
				});
				return v;
			}				
		};
		
		appInfoAdapter.sort(MyApplicationInfo.LabelComparator);
		listView.setAdapter(appInfoAdapter);
		
		Map<String,?> map = pref.getAll();
		
		for (MyApplicationInfo app:appInfo) {
			if (map.containsKey(Apps.PREF_APPS + app.getPackageName())) {
				map.remove(Apps.PREF_APPS + app.getPackageName());
			}
		}
		
		if (map.size()>0) {
			SharedPreferences.Editor ed = pref.edit();
			for (String s: map.keySet()) {
				ed.remove(s);
			}
			ed.commit();
		}
		
		try {
			progress.dismiss();
		}
		catch (Exception e) {
		}
//		listView.setVisibility(View.VISIBLE);
	}
}
