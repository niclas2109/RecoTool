#Requirements

You need to configure

	VICPromptListener.MQTT_BROKER_IP
	VICPromptListener.MQTT_BROKER_PORT
	
to reach you MQTT broker.
Only JSON messages accepted with following syntax:

{
	"action" : "systemPrompt",
	"systemPrompt" : {
		"id" : int,
		"mode" : String,
		"message" : String
	}
}