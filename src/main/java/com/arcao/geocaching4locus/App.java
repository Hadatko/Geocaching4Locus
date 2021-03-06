package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.authentication.helper.PreferenceAuthenticatorHelper;
import com.arcao.geocaching4locus.constants.CrashlyticsConstants;
import com.arcao.geocaching4locus.util.CrashlyticsTree;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;
import java.util.UUID;
import locus.api.android.utils.LocusUtils;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

public class App extends android.app.Application {
	private AuthenticatorHelper mAuthenticatorHelper;
	private String mDeviceId;

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
		}

		// Set up Crashlytics, disabled for debug builds
		Crashlytics crashlyticsKit = new Crashlytics.Builder()
				.core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
				.build();

		// Initialize Fabric with the debug-disabled crashlytics.
		Fabric.with(this, crashlyticsKit);
		Timber.plant(new CrashlyticsTree());

		Crashlytics.setUserIdentifier(getDeviceId());

	 	mAuthenticatorHelper = new PreferenceAuthenticatorHelper(this);
		if (mAuthenticatorHelper.hasAccount()) {
			//noinspection ConstantConditions
			Crashlytics.setUserName(mAuthenticatorHelper.getAccount().name);
			Crashlytics.setBool(CrashlyticsConstants.PREMIUM_MEMBER, mAuthenticatorHelper.getRestrictions().isPremiumMember());
		}

		try {
			LocusUtils.LocusVersion lv = LocusTesting.getActiveVersion(this);
			if (lv != null) {
				Crashlytics.setString(CrashlyticsConstants.LOCUS_VERSION, lv.getVersionName());
				Crashlytics.setString(CrashlyticsConstants.LOCUS_PACKAGE, lv.getPackageName());
			}
		} catch (Throwable t) {
			Timber.e(t, t.getMessage());
		}
	}

	public static App get(Context context) {
		return (App) context.getApplicationContext();
	}

	public AuthenticatorHelper getAuthenticatorHelper() {
		return mAuthenticatorHelper;
	}

	public String getDeviceId() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (mDeviceId == null) mDeviceId = pref.getString("device_id", null);

		if (StringUtils.isEmpty(mDeviceId)) {
			mDeviceId = UUID.randomUUID().toString();
			pref.edit().putString("device_id", mDeviceId).apply();
		}

		return mDeviceId;
	}

	public String getVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Timber.e(e, e.getMessage());
			return "1.0";
		}
	}

	@SuppressWarnings("deprecation")
	public void clearGeocachingCookies() {
		// required to call
		CookieSyncManager.createInstance(this).sync();

		// setCookie acts differently when trying to expire cookies between builds of Android that are using
		// Chromium HTTP stack and those that are not. Using both of these domains to ensure it works on both.
		clearCookiesForDomain("geocaching.com");
		clearCookiesForDomain(".geocaching.com");
		clearCookiesForDomain("https://geocaching.com");
		clearCookiesForDomain("https://.geocaching.com");

		CookieSyncManager.createInstance(this).sync();
	}

	private static void clearCookiesForDomain(String domain) {
		CookieManager cookieManager = CookieManager.getInstance();
		String cookies = cookieManager.getCookie(domain);

		if (cookies == null) return;

		for (String cookie : cookies.split(";")) {
			String[] cookieParts = cookie.split("=");
			if (cookieParts.length > 0) {
				String newCookie = cookieParts[0].trim() + "=;expires=Sat, 1 Jan 2000 00:00:01 UTC;";
				cookieManager.setCookie(domain, newCookie);
			}
		}
	}
}
