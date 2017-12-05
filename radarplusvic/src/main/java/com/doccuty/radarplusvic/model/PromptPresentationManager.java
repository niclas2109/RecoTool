package com.doccuty.radarplusvic.model;

import org.jboss.logging.Logger;

public class PromptPresentationManager extends Thread {

	private static final Logger LOG = Logger.getLogger(PromptPresentationManager.class);

	private static long FRAMES_PER_CHAR_PER_WORD = (long) 1.5;

	VICPromptListener app;

	public PromptPresentationManager(VICPromptListener app) {
		this.app = app;
	}

	private long calculatePresentationDuration(String msg) {
		long duration = msg.length() * FRAMES_PER_CHAR_PER_WORD;

		int numOfWords = msg.split(" ").length;

		if (numOfWords > 1)
			duration = duration - numOfWords * FRAMES_PER_CHAR_PER_WORD * 3;

		if (duration < 7 * FRAMES_PER_CHAR_PER_WORD)
			duration = 7 * FRAMES_PER_CHAR_PER_WORD;

		return duration;
	}

	public void run() {
		SystemPrompt prompt = null;
		long timer = 100;

		while (true) {

			if (!this.app.getNewPrompts().isEmpty() && (prompt == null || timer == 0)) {
				prompt = this.app.getNewPrompts().get(0);
				this.app.withCurrentSystemPrompt(prompt).withoutNewPrompts(prompt);
				timer = calculatePresentationDuration(prompt.getMessage());
			} else if (this.app.getNewPrompts().isEmpty() && timer == 0) {
				prompt = null;
			}

			if (prompt != null && timer > 0) {
				timer--;
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
