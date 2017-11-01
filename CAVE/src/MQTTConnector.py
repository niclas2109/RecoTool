'''
Created on 08.09.2017

@author: mac
'''
#!/usr/bin/env python
import viz
import paho.mqtt.client as mqtt

from thread import start_new_thread

import json

class MQTTConnector:
    
    OWN_TOPIC = 'CAVE'
    SERVER_TOPIC = 'RecoToolServer'
    
    SUBSCRIBE               = "subscribe"
    UNSUBSCRIBE             = "unsubscribe"
    SET_SIMULATED_TIME      = "setSimulatedTime"
    START_POSITION          = "startPosition"
    QUIT                    = "quit"
    UPDATE_USER_POSITION    = "updateUserPosition"
    NEW_ITEMS               = "recommendations"
    START_NAVIGATION        = "startNavigation"
    CANCEL_NAVIGATION       = "cancelNavigation"
    STOP_NAVIGATION         = "stopNavigation"
    CALCULATE_DISTANCE      = "calculateDistance"
    
    CURRENT_USER_POSITION   = "currentUserPosition"
    SWITCH_RECOMMENDER_MODE = "switchedRecommendationMode"
    
        
    client = None
    
    host = ''
    port = ''
    
    def __init__(self, host, port, mapManager):
        self.host = host
        self.port = port
        
        self.client = mqtt.Client()
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        
        self.client.on_message = self.on_message
        
        self.mapManager = mapManager
        self.mapManager.setMQTTConnector(self)
    

    def connect(self):
        self.client.connect(self.host, self.port, 60)
        self.client.loop_start()
                        
        
    def disconnect(self):
        msg = json.dumps({"action" : self.UNSUBSCRIBE, "value" : self.OWN_TOPIC})
        self.publish(self.SERVER_TOPIC, msg)
        self.client.loop_stop()
        self.client.disconnect()


    def publish(self, topic, message):
        self.client.publish(topic, message, 2)
        
        
    # Handler
        
    def on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        self.client.subscribe(self.OWN_TOPIC, 2)
        
        self.subscribeToController()
        
            
    def on_disconnect(self, client, flags, rc):
        print("Disonnected with result code " + str(rc))
    
    
    def isConnected(self):
        return self.isConnected
    
    
    def on_message(self, client, userdata, msg):

        info = json.loads(msg.payload)
        print("new mqtt message:" + info["action"])

        if info["action"] == self.NEW_ITEMS:
            self.mapManager.updateItemsCallBack(info["items"])
        elif info["action"] == self.START_NAVIGATION:

            start_new_thread(self.mapManager.startNavigation, (info["item"], ))
            
        elif info["action"] == self.CANCEL_NAVIGATION:
            self.mapManager.stopNavigation()
        elif info["action"] == self.STOP_NAVIGATION:
            self.mapManager.stopNavigation() 
        elif info["action"] == self.CALCULATE_DISTANCE:
            self.mapManager.calculateDistance(info["item"])
        elif info["action"] == self.SWITCH_RECOMMENDER_MODE:
            item = None
            
            if "item" in info:
                item = info["item"]
                    
            self.mapManager.switchRecommenderMode(info["value"], item)
        elif info["action"] == self.SET_SIMULATED_TIME:
            self.mapManager.setSimulatedTime(info["value"])
        elif info["action"] == self.START_POSITION:
            self.mapManager.setStartPosition(info["value"])
        elif info["action"] == self.QUIT:
            self.disconnect()
            viz.quit()
        elif info["action"] == self.SUBSCRIBE:
            self.subscribeToController()

            
    def subscribeToController(self):
        s = json.dumps({"action" : self.SUBSCRIBE, "value" : self.OWN_TOPIC})
        self.client.publish(self.SERVER_TOPIC, s, 2)