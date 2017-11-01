using System;
using System.Collections.Generic;

[Serializable]
public class NetworkMessage
{
    public string action;
    public string value;
    public SystemAlert systemAlert;
    public Item item;
    public List<Item> items;
}