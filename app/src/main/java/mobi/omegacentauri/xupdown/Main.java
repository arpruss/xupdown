package mobi.omegacentauri.xupdown;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Main extends Activity {
	Resources res;
	SharedPreferences prefs;
	static final String PREFS = "preferences";
//	public static final String PREF_ENTER = "enter";
	public static final String PREF_LEFT_RIGHT = "leftRight";
	private CheckBox leftRight;


	public static void saveIcon(Context c, String packageName) {
		deleteIcon(c, packageName);

		try {
			PackageManager pm = c.getPackageManager();
			ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
			Resources res = pm.getResourcesForApplication(app);
			Drawable icon = res.getDrawable(pm.getPackageInfo(packageName, 0).applicationInfo.icon);
//			Drawable icon = pm.getPackageInfo(packageName, 0).applicationInfo.loadIcon(c.getPackageManager());
			if (icon instanceof BitmapDrawable) {
				Bitmap bmp = ((BitmapDrawable)icon).getBitmap();
				File iconFile = getIconFile(c, packageName);
				FileOutputStream out = new FileOutputStream(iconFile);
				bmp.compress(CompressFormat.PNG, 100, out);
				out.close();
			}
		} catch (Exception e) {
			deleteIcon(c, packageName);
		}		
	}
	
	public static File getIconFile(Context c, String packageName) {
		return new File(c.getCacheDir(), 
				Uri.encode(packageName)+".png");
	}
	
	public static void deleteIcon(Context c, String packageName) {
		if (getIconFile(c, packageName).delete()) {
			Log.v("FastLaunch", "successful delete of "+packageName+" icon");
		}
	}

	private void message(String title, String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {} });
		alertDialog.show();

	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(Main.PREFS, Context.MODE_WORLD_READABLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.apps);
        
        res = getResources();

        leftRight = (CheckBox)findViewById(R.id.leftright);
        leftRight.setChecked(prefs.getBoolean(PREF_LEFT_RIGHT, false));

		leftRight.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(PREF_LEFT_RIGHT, isChecked).apply();
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		switch(item.getItemId()) {
//		case R.id.enter:
//			boolean opt = ! prefs.getBoolean(PREF_ENTER, false);
//			prefs.edit().putBoolean(PREF_ENTER, opt).commit();
//			invalidateOptionsMenu();
//			return true;
		case R.id.leftright:
			boolean opt = ! prefs.getBoolean(PREF_LEFT_RIGHT, false);
			prefs.edit().putBoolean(PREF_LEFT_RIGHT, opt).commit();
			invalidateOptionsMenu();
			return true;
		default:
			return false;
		}
	}

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch(item.getItemId()) {
//    	case R.id.clear:
//    		clear();
//    		return true;
//    	case R.id.options:
//    		startActivity(new Intent(this, Options.class));
//    		return true;
//    	default:
//    		return false;
//    	}
//    }
//
//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//	    return true;
//	}

	@Override
    public void onResume() {
    	super.onResume();
    }

}

