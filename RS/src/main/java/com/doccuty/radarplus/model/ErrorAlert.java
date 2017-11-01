package com.doccuty.radarplus.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAlert {

	public final static String SYSTEM_PROMPT_MODE_ERROR = "system-prompt-error";
	public final static String SYSTEM_PROMPT_MODE_INFO = "system-prompt-info";
	
	String mode;
	String message;
	
	public ErrorAlert(String mode, String message) {
		this.mode = mode;
		this.message = message;
	}
	

	public final static String PROPERTY_DELAY_DURATION = "delayDuration";

	public int analyzeErrorMessage() {
		if(this.message != null) {
			if(this.message.indexOf("Verspätung") >= 0 || this.message.indexOf("Delay") >= 0) {
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
	
	public String getMode() {
		return this.mode;
	}
	
	public void setMode(String value) {
		this.mode = value;
	}
	
	public ErrorAlert withMode(String value) {
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
	
	public ErrorAlert withMessage(String value) {
		this.setMessage(value);
		return this;
	}
}
