package mobi.omegacentauri.xupdown;

import android.content.SharedPreferences;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements /* IXposedHookZygoteInit, */ IXposedHookLoadPackage {
//	@Override
//	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
//		XposedBridge.log("ARP start "+Apps.class.getPackage().getName());
//		XSharedPreferences prefs = new XSharedPreferences(Apps.class.getPackage().getName(), Apps.PREFS);
//		if (prefs == null)
//			return;
//		prefs.makeWorldReadable();
//		XposedBridge.log("ARP ready");
//		if (prefs.getBoolean(Apps.PREF_DISABLE_HOLO, false))
//			XResources.setSystemWideReplacement(
//					"android", "drawable", "background_holo_dark", new XResources.DrawableLoader() {
//						@Override
//						public Drawable newDrawable(XResources res, int id) throws Throwable {
//							return new ColorDrawable(Color.BLACK);
//						}
//					});
//	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Apps.class.getPackage().getName(), Apps.PREFS);

		if (null != prefs.getString(Apps.PREF_APPS+lpparam.packageName, null)) {
			XposedBridge.log("ARP XUpDown hooking: " + lpparam.packageName);

			findAndHookMethod("android.view.KeyEvent", lpparam.classLoader,
					"getKeyCode", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int result = (Integer)param.getResult();

					if (result == KeyEvent.KEYCODE_VOLUME_UP)
						param.setResult(KeyEvent.KEYCODE_PAGE_UP);
					else if (result == KeyEvent.KEYCODE_VOLUME_DOWN)
						param.setResult(KeyEvent.KEYCODE_PAGE_DOWN);
				}
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				}
			});
		}
	}
	
}
