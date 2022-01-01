package mobi.omegacentauri.xupdown;

import android.content.Context;
import android.view.KeyEvent;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookLoadPackage {
	static InputMethodService ims = null;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);
		Context systemContext = (Context) XposedHelpers.callMethod( XposedHelpers.callStaticMethod( XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext" );

		boolean lr = prefs.getBoolean(Main.PREF_LEFT_RIGHT, false);
		if (lr) {
			InputMethodManager imm = (InputMethodManager) systemContext.getSystemService(INPUT_METHOD_SERVICE);
			List<InputMethodInfo> inputMethods = imm.getEnabledInputMethodList();
			lr = false;
			for (InputMethodInfo inf : inputMethods) {
				if (lpparam.packageName.equals(inf.getPackageName())) {
					lr = true;
					break;
				}
			}
		}

		final boolean leftRight = lr;
		final boolean upDown = true;

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
