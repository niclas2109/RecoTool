using System;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.VR.WSA;

#if !UNITY_EDITOR
using uPLibrary.Networking.M2Mqtt;
using uPLibrary.Networking.M2Mqtt.Messages;
#endif

public class MQTTConnector : MonoBehaviour
{

    private String base64Image;

    // MQTT Network settings
    public const String MQTT_BROKER_ADDRESS = "192.168.12.6";//"141.51.50.84";
    public const String MQTT_BROKER_PORT = "1883";

    public const String MQTT_OWN_TOPIC = "DataGlasses";
    public const String MQTT_SERVER_TOPIC = "RecoToolServer";

    // Number of frames a char of systemalert messages is displayed
    private const int FRAMES_PER_CHAR_PER_WORD = 15;

    // recommendation modes
    public const String RECOMMENDER_ABIDANCE_MODE = "abidanceMode";
    public const String RECOMMENDER_EFFICIENCY_MODE = "efficiencyMode";

    #if !UNITY_EDITOR
    private MqttClient client;
    #endif

    // Flags controlling visibility of panels (for usage see Update())
    private bool navigationEnabled;
    private bool calculatingRoute;
    private int selectedItem;
    private bool detailView;
    private bool efficiencyModeEnabled;
    private bool quitApp;
    private bool userIsMoving;
    private bool hideAll;

    private List<Item> items;

    // loading panel (used when navigation is calculated)
    public GameObject loadingPanel;
    public RawImage loading;

    // Parent panel containing all recommendation panels
    public GameObject recommendationPanel;

    public GameObject recommendationPanel1;
    private Image recommendationPanel1Background;
    public RawImage domainIcon1;
    public RawImage itemImage1;
    public Text title1;
    public GameObject body1;
    public Text description1;
    public GameObject rating1;
    public Text LikeItem1;
    public Text DislikeItem1;


    public GameObject recommendationPanel2;
    private Image recommendationPanel2Background;
    public RawImage domainIcon2;
    public RawImage itemImage2;
    public Text title2;
    public GameObject body2;
    public Text description2;
    public GameObject rating2;
    public Text LikeItem2;
    public Text DislikeItem2;

    public GameObject recommendationPanel3;
    private Image recommendationPanel3Background;
    public RawImage domainIcon3;
    public RawImage itemImage3;
    public Text title3;
    public GameObject body3;
    public Text description3;
    public GameObject rating3;
    public Text LikeItem3;
    public Text DislikeItem3;

    // default backgroundcolor for both recommendation panel
    // and system alert panel
    private Color originalColor;

    // highight backgroundcolor for selected item panel
    private Color highlightColor;

    // backgroundcolor for warning messages
    private Color errorColor;

    // backgroundcolor for investigator messages
    private Color efficiencyColor;

    // progress bar
    public GameObject progressBarWrapper;
    public GameObject progressBar;
    private RectTransform progressBarRect;
    private Vector2 progressBarOrgSize;
    private Vector2 progressBarCurrentSize;

    // system alert
    public GameObject systemAlertPanel;
    public RawImage systemAlertIcon;
    public Text systemMessage;
    private Image systemAlertBackgroundColor;

    private List<SystemAlert> systemAlerts;
    
    // Duration in frames an error message is displayed
    private double systemAlertDisplayCounter;


    private float degree;
    private int idxOfFirstItemInList;
    
    Texture2D errorTxtr;
    Texture2D infoTxtr;

    // Use this for initialization
    void Start()
    {
        this.originalColor = recommendationPanel1.GetComponent<Image>().color;
        this.highlightColor = new Color(0.63f, 0.40f, 0.15f, 1);
        this.errorColor = new Color(0.52f, 0.1f, 0.1f, 1);
        this.efficiencyColor = new Color(0.82f, 0.38f, 0.14f, 1);

        this.resetApplication();

        this.degree = 360;
        this.systemAlertDisplayCounter = 0;
        this.systemAlertBackgroundColor = this.systemAlertPanel.GetComponent<Image>();

        this.recommendationPanel1Background = this.recommendationPanel1.GetComponent<Image>();
        this.recommendationPanel2Background = this.recommendationPanel2.GetComponent<Image>();
        this.recommendationPanel3Background = this.recommendationPanel3.GetComponent<Image>();

        this.progressBarRect = this.progressBar.GetComponent<RectTransform>();

        // default base64 string for raw image insertion
        this.base64Image = "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";
        
        this.connectToMQTTBroker();

        this.progressBarOrgSize = progressBar.GetComponent<RectTransform>().sizeDelta;
        this.progressBarCurrentSize = this.progressBarOrgSize;

        this.errorTxtr = Resources.Load("error") as Texture2D;
        this.infoTxtr = Resources.Load("info") as Texture2D;


        // prevent app from mapping surrounding
        UnityEngine.VR.WSA.WorldManager.OnPositionalLocatorStateChanged += WorldManager_OnPositionalLocatorStateChanged;
        
        SystemAlert systemAlert = new SystemAlert();
        systemAlert.mode = SystemAlert.SYSTEM_PROMPT_MODE_INFO;
        systemAlert.message = "Willkommen!";

        this.systemAlerts.Add(systemAlert);
    }

    private void WorldManager_OnPositionalLocatorStateChanged(PositionalLocatorState oldState, PositionalLocatorState newState)
    {
        if (newState == PositionalLocatorState.Active)
        {
            // Handle becoming active
        }
        else
        {
            // Handle becoming rotational only
        }
    }

    private void connectToMQTTBroker()
    {
        #if !UNITY_EDITOR

        try
        {
            // Default port is 1883
            client = new MqttClient(MQTT_BROKER_ADDRESS);

            // register to message received 
            client.MqttMsgPublishReceived += clientMqttMsgPublishReceived;
            client.MqttMsgPublished += clientMqttMsgPublished;

            string clientId = Guid.NewGuid().ToString();

            if (client.IsConnected)
                client.Disconnect();

            client.Connect(clientId);

            // subscribe to the topic with QoS 2 
            client.Subscribe(new string[] { MQTT_OWN_TOPIC }, new byte[] { MqttMsgBase.QOS_LEVEL_EXACTLY_ONCE });

            registerOnServer();

            Debug.Log("Connected to MQTT broker");
        }
        catch (Exception e)
        {
            Debug.LogError(e.Message);

            SystemAlert systemAlert = new SystemAlert();
            systemAlert.mode = SystemAlert.SYSTEM_PROMPT_MODE_ERROR;
            systemAlert.message = "MQTT Broker kann nicht erreicht werden!";
            
            this.systemAlerts.Add(systemAlert);
        }


        #endif
    }

    // register on server
    private void registerOnServer()
    {
        #if !UNITY_EDITOR

        if (!client.IsConnected)
            return;

        NetworkMessage nM = new NetworkMessage();
        nM.action = "subscribe";
        nM.value = MQTT_OWN_TOPIC;

        this.sendMessage(MQTT_SERVER_TOPIC, JsonUtility.ToJson(nM));
        #endif
    }

    // unregister from server
    private void unregisterFromServer()
    {
        #if !UNITY_EDITOR

        if (!client.IsConnected)
            return;

        NetworkMessage nM = new NetworkMessage();
        nM.action = "unsubscribe";
        nM.value = MQTT_OWN_TOPIC;

        this.sendMessage(MQTT_SERVER_TOPIC, JsonUtility.ToJson(nM));
        #endif
    }

    private void disconnectFromMQTTBroker()
    {

    #if !UNITY_EDITOR
        try
        {
            if (client.IsConnected) {

                // unregister from broker
                NetworkMessage nM = new NetworkMessage();
                nM.action = "unsubscribe";
                nM.value = MQTT_OWN_TOPIC;

                this.sendMessage(MQTT_SERVER_TOPIC, JsonUtility.ToJson(nM));
        
                client.Disconnect();
                Debug.Log("Disconnected from broker!");

                SystemAlert systemAlert = new SystemAlert();
                systemAlert.mode = SystemAlert.SYSTEM_PROMPT_MODE_INFO;
                systemAlert.message = "Verbindung zum Broker beendet!";
            
                this.systemAlerts.Add(systemAlert);
            }
        }
        catch (Exception e)
        {
            Debug.LogError(e.Message);

            SystemAlert systemAlert = new SystemAlert();
            systemAlert.mode = SystemAlert.SYSTEM_PROMPT_MODE_ERROR;
            systemAlert.message = "Broker kann nicht erreicht werden!";
            this.systemAlerts.Add(systemAlert);
        }

    #endif
    }
    

    // Update is called once per frame
    void Update()
    {

        if (this.quitApp)
        {
            if (this.systemAlertDisplayCounter == 0) {
                Application.Quit();
            } else if (this.systemAlerts.Count > 0 && this.systemAlertDisplayCounter < this.systemAlerts[0].message.Length / 2)
            {
                SystemAlert systemAlert = new SystemAlert();
                systemAlert.mode = SystemAlert.SYSTEM_PROMPT_MODE_INFO;
                systemAlert.message = "Auf Wiedersehen!";
                this.systemAlerts.Add(systemAlert);
            }
        }

        if (this.items.Count == 0 || this.systemAlerts.Count > 0)
        {
            this.recommendationPanel.SetActive(false);
            this.loadingPanel.SetActive(false);

            
            if (this.systemAlerts.Count > 0 && this.systemAlertDisplayCounter <= 0)
            {
                this.systemAlertDisplayCounter = this.systemAlerts[0].message.Length * FRAMES_PER_CHAR_PER_WORD;

                char[] delimiters = new char[] { ' ', '-', '\r', '\n' };
                int numofWords = this.systemAlerts[0].message.Split(delimiters, StringSplitOptions.RemoveEmptyEntries).Length;

                if (numofWords > 1)
                    this.systemAlertDisplayCounter = this.systemAlertDisplayCounter - this.systemAlerts[0].message.Split(delimiters, StringSplitOptions.RemoveEmptyEntries).Length * FRAMES_PER_CHAR_PER_WORD * 3;

                if (this.systemAlertDisplayCounter < 7 * FRAMES_PER_CHAR_PER_WORD)
                    this.systemAlertDisplayCounter = 7 * FRAMES_PER_CHAR_PER_WORD;
            }

            if (this.systemAlerts.Count > 0)
            {
                SystemAlert currentAlert = this.systemAlerts[0];

                if (String.Equals(currentAlert.mode, SystemAlert.SYSTEM_PROMPT_MODE_ERROR))
                {
                    this.systemAlertBackgroundColor.color = this.errorColor;
                    this.systemAlertIcon.texture = this.errorTxtr;
                }
                else if (String.Equals(currentAlert.mode, SystemAlert.SYSTEM_PROMPT_MODE_EFFICIENCY))
                {
                    this.systemAlertBackgroundColor.color = this.efficiencyColor;
                    this.systemAlertIcon.texture = this.infoTxtr;
                }
                else
                {
                    this.systemAlertBackgroundColor.color = this.originalColor;
                    this.systemAlertIcon.texture = this.infoTxtr;
                }

                this.systemMessage.text = currentAlert.message;
                this.systemAlertPanel.SetActive(true);

                this.systemAlertDisplayCounter = this.systemAlertDisplayCounter - 1;

                if(this.systemAlertDisplayCounter <= 0)
                    this.systemAlerts.RemoveAt(0);

                return;
            }
        }

        if (this.systemAlertDisplayCounter <= 0)
        {
            this.systemAlertPanel.SetActive(false);
        }


        if (this.progressBarCurrentSize.x <= 0 || this.navigationEnabled || this.hideAll)
        {

            #if !UNITY_EDITOR
            // Inform controller
            if(this.recommendationPanel.activeSelf && this.client.IsConnected)
            {
                NetworkMessage nM = new NetworkMessage();
                nM.action = "hideAll";
                nM.value = "false";

                this.sendMessage(MQTT_SERVER_TOPIC, JsonUtility.ToJson(nM));
            }
            #endif

            this.recommendationPanel.SetActive(false);
            this.loadingPanel.SetActive(false);
            return;
        }

        if (this.calculatingRoute)
        {
            if (degree == 0)
                degree = 359;
            else
                degree--;

            recommendationPanel.SetActive(false);
            loadingPanel.SetActive(true);
            loading.rectTransform.rotation = Quaternion.AngleAxis(Mathf.Lerp(0f, degree, 50), Vector3.forward);
            return;
        } else
        {
            recommendationPanel.SetActive(true);
            loadingPanel.SetActive(false);
        }


        // Update progress bar
        if (this.progressBarCurrentSize.x > 0 && !this.detailView && this.selectedItem == 0 && this.items.Count > 0)
        {
            this.progressBarCurrentSize.x = this.progressBarCurrentSize.x - 0.7f;
            this.progressBarWrapper.SetActive(true);
        }
        else
        {
            this.progressBarWrapper.SetActive(false);
        }

        this.progressBarRect.sizeDelta = this.progressBarCurrentSize;


        // Insert item names
        if (this.recommendationPanel.activeSelf && this.items.Count > this.idxOfFirstItemInList)
        {
            recommendationPanel1.SetActive(true);
            title1.text = items[this.idxOfFirstItemInList].name;
            description1.text = items[this.idxOfFirstItemInList].description;

            this.recommendationPanel1Background.color = (this.selectedItem == this.idxOfFirstItemInList + 1) ? highlightColor : originalColor;
            body1.SetActive(this.selectedItem == this.idxOfFirstItemInList + 1 && detailView);
            setImage(domainIcon1, items[this.idxOfFirstItemInList].domain.image);
        }
        else
        {
            recommendationPanel1.SetActive(false);
            body1.SetActive(false);
        }

        if (this.recommendationPanel.activeSelf && this.items.Count > this.idxOfFirstItemInList + 1)
        {
            recommendationPanel2.SetActive(true);
            title2.text = items[this.idxOfFirstItemInList + 1].name;
            description2.text = items[this.idxOfFirstItemInList + 1].description;

            this.recommendationPanel2Background.color = (this.selectedItem == this.idxOfFirstItemInList + 2) ? highlightColor : originalColor;
            body2.SetActive(this.selectedItem == this.idxOfFirstItemInList + 2 && detailView);

            setImage(domainIcon2, items[this.idxOfFirstItemInList + 1].domain.image);
        }
        else
        {
            recommendationPanel2.SetActive(false);
            body2.SetActive(false);
        }


        if (this.recommendationPanel.activeSelf && this.items.Count > this.idxOfFirstItemInList + 2)
        {
            recommendationPanel3.SetActive(true);
            title3.text = items[this.idxOfFirstItemInList + 2].name;
            description3.text = items[this.idxOfFirstItemInList + 2].description;

            this.recommendationPanel3Background.color = (this.selectedItem == this.idxOfFirstItemInList + 3) ? highlightColor : originalColor;
            body3.SetActive(this.selectedItem == this.idxOfFirstItemInList + 3 && detailView);

            setImage(domainIcon3, items[this.idxOfFirstItemInList + 2].domain.image);
        }
        else
        {
            recommendationPanel3.SetActive(false);
            body3.SetActive(false);
        }


        // updates in detail view

        if (body1.activeSelf)
        {
            setImage(itemImage1, items[this.idxOfFirstItemInList].image);
            this.rating1.SetActive(!this.userIsMoving);
        } else if (body2.activeSelf)
        {
            setImage(itemImage2, items[this.idxOfFirstItemInList + 1].image);
            this.rating2.SetActive(!this.userIsMoving);
        } else if (body3.activeSelf)
        {
            setImage(itemImage3, items[this.idxOfFirstItemInList + 2].image);
            this.rating2.SetActive(!this.userIsMoving);
        }
    }

    // insert a base64 into rawimage
    private void setImage(RawImage img, RadARImage imageObj)
    {
        if (img == null)
            return;

        string image = "";


        if (imageObj == null || imageObj.image == null || ("").CompareTo(imageObj.image) == 0)
        {
            image = base64Image;
        } else
        {
            image = imageObj.image;
        }

        try
        {
            Texture2D tex = (Texture2D) img.texture;

            byte[] byteImg = Convert.FromBase64String(image);
            
            ((Texture2D) img.texture).LoadImage(byteImg);
        }
        catch (Exception e)
        {
            Debug.Log(e.StackTrace);
        }
    }


#if !UNITY_EDITOR

    public void sendMessage(String topic, String message)
    {
        client.Publish(MQTT_SERVER_TOPIC, Encoding.UTF8.GetBytes(message));
    }

    // MQTT message handler
    public void clientMqttMsgPublishReceived(object sender, MqttMsgPublishEventArgs e)
    {
        String msg = Encoding.UTF8.GetString(e.Message);

        NetworkMessage message = null;

        try
        {
            message = JsonUtility.FromJson<NetworkMessage>(msg);
        } catch (Exception ex)
        {
            Debug.Log(ex.ToString());
            return;
        }
        
        switch (message.action)
        {
            case "recommendations":
                this.updateRecommendations(message);
                break;
            case "selectItem":
                this.selectItem(message);
                break;
            case "unselectItem":
                this.unselectItem();
                break;
            case "showDetailView":
                this.showDetailView(message);
                break;
            case "hideDetailView":
                this.hideDetailView();
                break;
            case "startNavigation":
                this.startNavigation(message);
                break;
            case "cancelNavigation":
                this.canceledNavigation(message);
                break;
            case "calculatedRoute":
                this.calculatedRoute(message);
                break;
            case "stopNavigation":
                this.stopNavigation(message);
                break;
            case "finishedNavigation":
                this.stopNavigation(message);
                break;
            case "userIsMoving":
                this.userIsMoving = (("true").CompareTo(message.value) == 0) ? true : false;
                break;
            case "switchedRecommendationMode":
                this.switchedRecommendationMode(message);
                break;
            case "systemAlert":
                this.setSystemAlert(message);
                break;
            case "hideAll":
                this.hideAll = (("true").CompareTo(message.value) == 0) ? true : false;
                this.progressBarCurrentSize.x = (("true").CompareTo(message.value) == 0) ? 0 : this.progressBarOrgSize.x;

                break;
            case "subscribe":
                this.registerOnServer();
                break;
            case "clearSystemPromptQueue":
                this.systemAlerts.Clear();
                this.systemAlertDisplayCounter = 0;
                break;
            case "reset":
                this.resetApplication();
                break;
            case "quit":
                this.unregisterFromServer();
                this.disconnectFromMQTTBroker();
                this.quitApp = true;
                break;
            default:
                Debug.Log("command not found");
                break;
        }
    }

    public void clientMqttMsgUnsubscribed(object sender, MqttMsgUnsubscribedEventArgs e)
    {
        Debug.Log("Unsubscribed ");
    }

    public void clientMqttMsgSubscribed(object sender, MqttMsgSubscribedEventArgs e)
    {
        Debug.Log("Subscribed ");
    }

    public void clientMqttMsgPublished(object sender, MqttMsgPublishedEventArgs e)
    {
        Debug.Log("Published");
    }


    // Actions initiated by MQTT-Message Tag action
    private void updateRecommendations(NetworkMessage msg)
    {
        this.items = msg.items;
        this.unselectItem();
        
        this.progressBarCurrentSize = this.progressBarOrgSize;
    }

    // unselect all items
    private void unselectItem()
    {
        this.detailView = false;
        this.selectedItem = 0;
    }

    // select a specific item
    private void selectItem(NetworkMessage msg)
    {
        for (var i = 0; i < this.items.Count; i++)
        { 
            if (msg.item.id == this.items[i].id)
            {
                this.selectedItem = ++i;
                break;
            }
        }

        if (this.selectedItem <= this.idxOfFirstItemInList || this.selectedItem > this.idxOfFirstItemInList + 3)
        {
            int d = this.selectedItem / 3;

            if (this.selectedItem % 3 == 0)
                d--;

            this.idxOfFirstItemInList = d*3;

            this.selectedItem = 0;
            this.detailView = false;
        }

        this.progressBarCurrentSize = this.progressBarOrgSize;
    }

    // show detail view of selected item
    private void showDetailView(NetworkMessage msg)
    {
        this.selectItem(msg);
        this.detailView = true;
    }

    // hide detail view of selected item
    private void hideDetailView()
    {
        this.detailView = false;
        this.progressBarCurrentSize = this.progressBarOrgSize;
    }

    // called after an item has been chosen as next next destination
    private void startNavigation(NetworkMessage value)
    {
        this.calculatingRoute = true;
        this.navigationEnabled = false;
        this.detailView = false;

        this.progressBarCurrentSize = this.progressBarOrgSize;
    }

    // called after an item has been chosen as next next destination
    private void canceledNavigation(NetworkMessage message)
    {
        this.calculatingRoute = false;
        this.navigationEnabled = false;
        this.detailView = false;
        this.setSystemAlert(message);
    }

    // called after a route was calculated
    private void calculatedRoute(NetworkMessage msg)
    {
        this.calculatingRoute = false;
        
        this.detailView = false;
        
        if (msg.item != null && msg.item.id >= 0)
        {
            this.navigationEnabled = true;
            this.idxOfFirstItemInList = 0;
        }
        else
        {
            navigationEnabled = false;
        }

        this.setSystemAlert(msg);
    }
    
    // called after navigation is done
    private void stopNavigation(NetworkMessage msg)
    {
        this.calculatingRoute = false;
        this.navigationEnabled = false;
        
        this.progressBarCurrentSize.x = 0;
        this.idxOfFirstItemInList = 0;
    }

    private void setSystemAlert(NetworkMessage msg)
    {
        // Check if message is navigation message
        if(12 <= msg.systemAlert.id && msg.systemAlert.id <= 16)
        {
            for (int i = 0; i < systemAlerts.Count; i++)
            {
                SystemAlert alert = systemAlerts[i];
                // Remove deprecated navigation messages
                if (12 <= alert.id && alert.id < 16)
                {
                    Debug.Log("removed " + alert.id);
                    systemAlerts.Remove(alert);
                }
            }
        }
        
        this.systemAlerts.Add(msg.systemAlert);
    }

    private void switchedRecommendationMode(NetworkMessage message)
    {
        if (message.value == RECOMMENDER_EFFICIENCY_MODE && !this.efficiencyModeEnabled)
        {
            this.efficiencyModeEnabled = true;
            this.calculatingRoute = true;
        }
        else if(this.efficiencyModeEnabled)
        {
            this.efficiencyModeEnabled = false;
            this.calculatingRoute = false;
            this.navigationEnabled = false;
        }

        if(message.systemAlert != null && message.systemAlert.message != null && message.systemAlert.message.Length > 1)
            this.setSystemAlert(message);
    }
#endif

    private void resetApplication()
    {
        this.selectedItem = 0;
        this.detailView = false;
        this.navigationEnabled = false;
        this.calculatingRoute = false;
        this.efficiencyModeEnabled = false;
        this.userIsMoving = false;

        this.quitApp = false;

        this.idxOfFirstItemInList = 0;

        this.systemAlerts = new List<SystemAlert>();


        this.items = new List<Item>();
    }
}
