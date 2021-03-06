package com.arcao.wherigoservice.api.parser;


import com.arcao.geocaching.api.impl.live_geocaching_api.parser.JsonReader;

import java.io.IOException;

public class WherigoJsonResultParser {
	public static Result parse(JsonReader r) throws IOException {
		Result status = new Result();
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			switch (name) {
				case "Code":
					status.statusCode = r.nextInt();
					break;
				case "Text":
					status.statusMessage = r.nextString();
					break;
				default:
					r.skipValue();
					break;
			}
		}
		r.endObject();
		return status;
	}

	public static class Result {
		private int statusCode;
		private String statusMessage;

		public int getStatusCode() {
			return statusCode;
		}

		public String getStatusMessage() {
			return statusMessage;
		}
	}
}
