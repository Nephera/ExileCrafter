import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class ECItemManager {
	/**
	 * Helper function that adds an ItemType to an already allocated array of ItemTypes
	 * @param itemList is the old list of ItemTypes this method will append to
	 * @param xmlName is the xml-friendly name we're initializing the new ItemType with
	 * @param displayName is the user-friendly name we're initializing the new ItemType with
	 * @return a newly allocated array with a the new ItemType appended to the end
	 */
	public static ItemType[] addItem(ItemType[] itemList, String displayName, String xmlName)
	{
		ItemType[] ret = new ItemType[itemList.length + 1];
		
		for(int i = 0; i < itemList.length; i++)
			ret[i] = itemList[i];
		
		ret[ret.length - 1] = new ItemType(xmlName, displayName);
		
		return ret;
	}
	
	/**
	 * Helper function for initialization of ItemComboBox,
	 * queries AffixData.xml for all of the different items
	 * @return a list of ItemTypes
	 */
	public static ItemType[] getItemList()
	{
		ItemType[] ret = new ItemType[0];
		
		try{
    	File inputFile = new File("src/data/AffixData.xml");
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse("src/data/AffixData.xml");
    	doc.getDocumentElement().normalize();
    	
    	NodeList docLayerNodes = doc.getChildNodes();
    	for(int i = 0; i < docLayerNodes.getLength(); i++)
    	{    		
    		NodeList itemLayerNodes = docLayerNodes.item(i).getChildNodes();
    		for(int j = 0; j < itemLayerNodes.getLength(); j++)
    		{
    			if(itemLayerNodes.item(j).getNodeName() != "#text")
    			{
    				NamedNodeMap NNM = itemLayerNodes.item(j).getAttributes();
    				String displayedText = NNM.getNamedItem("text").getNodeValue();
    				
    				ret = ECItemManager.addItem(ret, itemLayerNodes.item(j).getNodeName(), displayedText);
    			}
    		}
    	}
    	
		}catch(Exception e1){e1.printStackTrace();}
    	
		return ret;
	}
}
