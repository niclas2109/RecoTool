using System;

[Serializable]
public class Item
{
    public long id;

    public ItemAttribute domain;
    public string name;
    public string description;
    public RadARImage image;
    
    public string isTrainingItem;
    public string isProductivityItem;

    public string hasWifi;
    public string hasSockets;
    public string isOutdoor;
    public string hasOutdoorArea;

    public string toString()
    {
        return this.id + " " + this.name + " " + this.description;
    }
}
