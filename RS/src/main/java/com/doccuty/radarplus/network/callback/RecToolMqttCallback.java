package com.doccuty.radarplus.network.callback;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RecToolMqttCallback implements MqttCallback {

	private LinkedBlockingQueue<String> messageQueue = null;
		
	public void setMessageQueue(LinkedBlockingQueue<String> messageQueue) {
		this.messageQueue = messageQueue;
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String receivedMessage = new String(message.getPayload());
		messageQueue.add(receivedMessage);
	}

}
