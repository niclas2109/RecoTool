package com.doccuty.radarplusvic.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * (c) by Niclas Kannengießer
 */

public class SystemPrompt {

	public final static String SYSTEM_PROMPT_MODE_SUCCESS = "system-prompt-success";
	public final static String SYSTEM_PROMPT_MODE_ERROR = "system-prompt-error";
	public final static String SYSTEM_PROMPT_MODE_INFO = "system-prompt-info";

	long id;
	private String mode;
	private String message;

	public SystemPrompt() {

	}

	public SystemPrompt(String mode, String message) {
		this.mode = mode;
		this.message = message;
	}

	public final static String PROPERTY_DELAY_DURATION = "delayDuration";

	public int analyzeSystemPromptMessage() {
		if (this.message != null) {
			if (this.message.indexOf("Verspätung") >= 0 || this.message.indexOf("Delay") >= 0) {
				int minutes = 0;
				Pattern p = Pattern.compile("-?\\d+ min");
				Matcher m = p.matcher(this.message);
				while (m.find()) {
					minutes = Integer.parseInt(m.group().replaceAll(" min", ""));
				}

				return minutes * 60;
			}
		}

		return 0;
	}

	// =======================================

	public long getID() {
		return this.id;
	}

	public void setID(long value) {
		this.id = value;
	}

	public SystemPrompt withID(long value) {
		this.setID(value);
		return this;
	}

	// =======================================

	public String getMode() {
		return this.mode;
	}

	public void setMode(String value) {
		this.mode = value;
	}

	public SystemPrompt withMode(String value) {
		this.setMode(value);
		return this;
	}

	// =======================================

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String value) {
		this.message = value;
	}

	public SystemPrompt withMessage(String value) {
		this.setMessage(value);
		return this;
	}

	// =======================================

	@Override
	public String toString() {
		return this.mode + " " + this.message;
	}
}
