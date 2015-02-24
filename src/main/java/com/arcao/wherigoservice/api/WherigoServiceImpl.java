package com.arcao.wherigoservice.api;

import com.arcao.geocaching.api.impl.live_geocaching_api.parser.JsonReader;
import com.arcao.wherigoservice.api.parser.WherigoJsonResultParser;
import com.arcao.wherigoservice.api.parser.WherigoJsonResultParser.Result;
import com.google.gson.stream.MalformedJsonException;
import timber.log.Timber;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class WherigoServiceImpl implements WherigoService {
	private static final String BASE_URL = "http://wherigo-service.appspot.com/api/";

	@Override
	public String getCacheCodeFromGuid(String cacheGuid) throws WherigoServiceException {
		String cacheCode = null;

		try {
			JsonReader r = callGet(
					"getCacheCodeFromGuid?CacheGUID=" + cacheGuid +
					"&format=json"
			);

			r.beginObject();
			checkError(r);

			while (r.hasNext()) {
				String name = r.nextName();
				if ("CacheResult".equals(name)) {
					r.beginObject();
					while (r.hasNext()) {
						name = r.nextName();
						if ("CacheCode".equals(name)) {
							cacheCode = r.nextString();
						} else {
							r.skipValue();
						}
					}
					r.endObject();
				} else {
					r.skipValue();
				}
			}
			r.endObject();
			r.close();
			Timber.i("Cache code: " + cacheCode);
		} catch (IOException e) {
			Timber.e(e, e.toString());
			if (!isGsonException(e)) {
				throw new WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.getMessage(), e);
			}

			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Response is not valid JSON string: " + e.getMessage(), e);
		}

		return cacheCode;
	}

	// -------------------- Helper methods ----------------------------------------

	protected void checkError(JsonReader r) throws IOException {
		if ("Status".equals(r.nextName())) {
			Result status = WherigoJsonResultParser.parse(r);

			switch (status.getStatusCode()) {
				case WherigoServiceException.ERROR_OK:
					return;
				default:
					throw new WherigoServiceException(status.getStatusCode(), status.getStatusMessage());
			}
		} else {
			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Missing Status in a response.");
		}
	}

	protected JsonReader callGet(String function) throws WherigoServiceException {
		InputStream is;
		InputStreamReader isr;

		Timber.i("Getting " + maskParameterValues(function));

		try {
			URL url = new URL(BASE_URL + function);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// important! sometimes GC API takes too long to return response
			con.setConnectTimeout(30000);
			con.setReadTimeout(30000);

			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", "Geocaching/4.0 CFNetwork/459 Darwin/10.0.0d3");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");

			if (con.getResponseCode() >= 400) {
				is = con.getErrorStream();
			} else {
				is = con.getInputStream();
			}

			final String encoding = con.getContentEncoding();

			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				Timber.i("callGet(): GZIP OK");
				is = new GZIPInputStream(is);
			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				Timber.i("callGet(): DEFLATE OK");
				is = new InflaterInputStream(is, new Inflater(true));
			} else {
				Timber.i("callGet(): WITHOUT COMPRESSION");
			}

			if (con.getResponseCode() >= 400) {
				isr = new InputStreamReader(is, "UTF-8");

				StringBuilder sb = new StringBuilder();
				char buffer[] = new char[1024];
				int len;

				while ((len = isr.read(buffer)) != -1) {
					sb.append(buffer, 0, len);
				}

				isr.close();

				// read error response
				throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, sb.toString());
			}

			isr = new InputStreamReader(is, "UTF-8");
			return new JsonReader(isr);
		} catch (Exception e) {
			Timber.e(e, e.toString());
			throw new WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.getClass().getSimpleName(), e);
		}
	}

	protected String maskParameterValues(String function) {
		// do nothing
		//function = function.replaceAll("([Aa]ccess[Tt]oken=)([^&]+)", "$1******");
		return function;
	}

	protected boolean isGsonException(Throwable t) {
		return IOException.class.equals(t.getClass()) || t instanceof MalformedJsonException || t instanceof IllegalStateException || t instanceof NumberFormatException;
	}
}
