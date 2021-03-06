package com.arcao.geocaching.api;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.downloader.OkHttpClientJsonDownloader;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching.api.impl.live_geocaching_api.downloader.JsonDownloader;
import com.arcao.geocaching4locus.BuildConfig;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class GeocachingApiFactory {
	private static GeocachingApiConfiguration apiConfiguration;
	private static OkHttpClient client;
	private static JsonDownloader jsonDownloader;

	public static GeocachingApi create() {
		createApiConfiguration();
		createOkHttpClient();
		createJsonDownloader();

		return LiveGeocachingApi.Builder.liveGeocachingApi().withConfiguration(apiConfiguration).withDownloader(jsonDownloader).build();
	}

	public static JsonDownloader createJsonDownloader() {
		if (jsonDownloader == null) {
			createOkHttpClient();
			jsonDownloader = new OkHttpClientJsonDownloader(client);
		}
		return jsonDownloader;
	}

	private static void createOkHttpClient() {
		if (client == null) {
			createApiConfiguration();
			client = new OkHttpClient.Builder()
					.connectTimeout(apiConfiguration.getConnectTimeout(), TimeUnit.MILLISECONDS)
					.readTimeout(apiConfiguration.getReadTimeout(), TimeUnit.MILLISECONDS)
					.build();
		}
	}

	private static void createApiConfiguration() {
		if (apiConfiguration == null) {
			apiConfiguration = new DefaultProductionGeocachingApiConfiguration();
			if (BuildConfig.GEOCACHING_API_STAGING)
				apiConfiguration = new DefaultStagingGeocachingApiConfiguration();
		}
	}
}
