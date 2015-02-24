package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.support.annotation.StringRes;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.data.provider.Provider;
import com.arcao.geocaching4locus.data.provider.ProviderService;
import com.arcao.geocaching4locus.fragment.preference.FilterPreferenceFragment;
import timber.log.Timber;

public final class GeocachingLiveApiProvider implements Provider {
	static final String PROVIDER_ID = "GEOCACHING_LIVE_API";

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	@StringRes
	public int getName() {
		return 0;
	}

	@Override
	public Class<? extends PreferenceFragment> getFilterPreference() {
		return FilterPreferenceFragment.class;
	}

	@Override
	public boolean canHandleCacheCode(String cacheCode) {
		try {
			return GeocachingUtils.cacheCodeToCacheId(cacheCode) > 0;
		} catch (IllegalArgumentException e) {
			Timber.e(e, e.getMessage());
			return false;
		}
	}

	@Override
	public ProviderService createService(Context context) {
		return new GeocachingLiveApiProviderService(context);
	}
}
