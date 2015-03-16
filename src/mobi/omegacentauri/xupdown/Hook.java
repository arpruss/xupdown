package mobi.omegacentauri.xupdown;

import android.content.SharedPreferences;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.inputmethodservice.InputMethodService;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookLoadPackage {
	static InputMethodService ims = null;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Apps.class.getPackage().getName(), Apps.PREFS);

		//		final boolean enterMap = prefs.getBoolean(Apps.PREF_ENTER, false);
		final boolean leftRight = prefs.getBoolean(Apps.PREF_LEFT_RIGHT, false);
		final boolean upDown = null != prefs.getString(Apps.PREF_APPS+lpparam.packageName, null);

		if (leftRight || upDown) {
			if (leftRight) {
				findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "onShowInputRequested", 
						int.class, boolean.class,
						new XC_MethodHook() {
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						ims = (InputMethodService) param.thisObject;
					}
				});
				findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "onKeyDown", 
						int.class, KeyEvent.class,
						new XC_MethodHook() {
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						InputMethodService inputMethod = (InputMethodService)param.thisObject;
						
						if (!leftRight || !inputMethod.isInputViewShown())
							return;

						int code = (Integer)param.args[0];

						if (code == KeyEvent.KEYCODE_VOLUME_UP) {
							inputMethod.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
							param.setResult(true);
						}
						else if (code == KeyEvent.KEYCODE_VOLUME_DOWN) {
							inputMethod.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
							param.setResult(true);
						}
					}
				});
				findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "onKeyUp", int.class, KeyEvent.class,
						new XC_MethodHook() {
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						int code = (Integer)param.args[0];
						InputMethodService inputMethod = (InputMethodService)param.thisObject;
						if (!leftRight || !inputMethod.isInputViewShown())
							return;

						if (code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) {
							param.setResult(true);
						}
					}
				});
			}
			else {
				ims = null;
			}
			

			if (upDown)
				findAndHookMethod("android.view.KeyEvent", lpparam.classLoader,
						"getKeyCode", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						int result = (Integer)param.getResult();
						
						if (result != KeyEvent.KEYCODE_VOLUME_UP && result != KeyEvent.KEYCODE_VOLUME_DOWN)
							return;
						
						try {
							if (ims != null && ims.isInputViewShown())
								return;
						}
						catch (Exception exc) {
						}
						
//						KeyEvent e = (KeyEvent)param.thisObject;
	
						if (result == KeyEvent.KEYCODE_VOLUME_UP) {
							param.setResult(KeyEvent.KEYCODE_PAGE_UP);
						}
						else if (result == KeyEvent.KEYCODE_VOLUME_DOWN) {
							param.setResult(KeyEvent.KEYCODE_PAGE_DOWN);
						}
//						else if (enterMap && result == KeyEvent.KEYCODE_ENTER) {
//							InputDevice dev = e.getDevice();
//							if (dev != null) {
//								String name = dev.getName();
//								if (name != null && name.contains("Shutter")) {
//									param.setResult(KeyEvent.KEYCODE_PAGE_DOWN);
//								}
//							}
//						}
					}
				});
		}

	}

}
