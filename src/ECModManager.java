import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ECModManager {
	/**
	 * Helper function to populate prefix and suffix comboboxes and lists with ItemMod objects
	 * @param item is the type of item, e.g. Belt, Ring, Amulet, etc...
	 * @param ilvl is the item level of the item
	 * @param archetype is the archetype of the item, can be Normal, Elder, or Shaper
	 * @param affixType is the affix type of the item, e.g. Prefix or Suffix
	 * @param subString is a String used for searching; if subString is supplied, the return
	 * should only include ItemMods that have the subString in their boxText.
	 * @return a list of ItemMods to populate a JComboBox or JList
	 */	
	// Returns a mod list parsed from AffixData.xml queried against item, ilvl, archetype, and affixtype inputs
	public static ItemMod[] getModList(String item, String ilvl, String archetype, String affixType, String subString){
		ItemMod[] affixList = new ItemMod[]{new ItemMod("None")};
		
	    try {
	    	File inputFile = new File("src/data/AffixData.xml");
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(inputFile);
	    	doc.getDocumentElement().normalize();

	    	//Item level
	    	NodeList itemLayerNodes = doc.getElementsByTagName(item);
	    	Node itemLayer = itemLayerNodes.item(0);
	         
	    	//Affix level
	    	NodeList itemLayerChildren = itemLayer.getChildNodes();
	    	Node affixLayer = null;
	         
	    	for(int i = 0; i < itemLayerChildren.getLength(); i++)
	    		if(itemLayerChildren.item(i).getNodeName() == affixType)
	    			affixLayer = itemLayerChildren.item(i);
	         
	    	//Archetype level
	    	NodeList affixLayerChildren = affixLayer.getChildNodes();
	    	Node archetypeLayer = null;
	    	Node normalArchetypeLayer = null;
	         
	    	for(int i = 0; i < affixLayerChildren.getLength(); i++){
	    		if(affixLayerChildren.item(i).getNodeName() == "Normal")
	    			normalArchetypeLayer = affixLayerChildren.item(i);
	
	    		else if(affixLayerChildren.item(i).getNodeName() == archetype)
	    			archetypeLayer = affixLayerChildren.item(i);
	    	}
	        
	    	//Mod level
	    	// Get base mods
	    	NodeList archetypeLayerChildrenBase = normalArchetypeLayer.getChildNodes();
	    	// For each mod type
	    	for(int i = 0; i < archetypeLayerChildrenBase.getLength(); i++){
	    		if(archetypeLayerChildrenBase.item(i).getNodeName() != "#text"){
	    			String modName = archetypeLayerChildrenBase.item(i).getAttributes().getNamedItem("modName").getNodeValue();
	        		 
	    			// For each tier within each mod
	    			NodeList modLayerChildren = archetypeLayerChildrenBase.item(i).getChildNodes();
	    			for(int j = 0; j < modLayerChildren.getLength(); j++){
	    				if(modLayerChildren.item(j).getNodeName() != "#text"){
	    					ItemMod currMod = new ItemMod(modLayerChildren.item(j).getNodeName(), // tier
	    													Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("minVal1").getNodeValue()), // min1
	    													Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("minVal2").getNodeValue()), // min2
	    													Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("maxVal1").getNodeValue()), // max1
	    													Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("maxVal2").getNodeValue()), // max2
	    													modLayerChildren.item(j).getAttributes().getNamedItem("displayText").getNodeValue(), // display
	    													modName, // name
	    													Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("iLvl").getNodeValue())); // ilvl
	    					currMod.boxText = currMod.tier + " " + currMod.modText + ": [" + currMod.displayText + "]";
	    					
	    					if(subString != "" && currMod.boxText.contains(subString))
	    						affixList = addMod(affixList, currMod);
	    					else if(subString == "")
	    						affixList = addMod(affixList, currMod);    					
	    				}
	    			} 
	    		}
	    	}
	         
	    	// Get non-base mods
	    	if(archetype != "Normal"){
	    		NodeList archetypeLayerChildren = archetypeLayer.getChildNodes();
	    		// For each mod type
	    		for(int i = 0; i < archetypeLayerChildren.getLength(); i++){
	    			if(archetypeLayerChildren.item(i).getNodeName() != "#text"){
	    				String modName = archetypeLayerChildren.item(i).getAttributes().getNamedItem("modName").getNodeValue();

	    				// For each tier within each mod
	    				NodeList modLayerChildren = archetypeLayerChildren.item(i).getChildNodes();
	    				for(int j = 0; j < modLayerChildren.getLength(); j++){
	    					if(modLayerChildren.item(j).getNodeName() != "#text"){
	    						ItemMod currMod = new ItemMod(modLayerChildren.item(j).getNodeName(), // tier
	    														Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("minVal1").getNodeValue()), // min1)
	    														Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("minVal2").getNodeValue()), // min2
	    														Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("maxVal1").getNodeValue()), // max1
	    														Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("maxVal2").getNodeValue()), // max2
	    														modLayerChildren.item(j).getAttributes().getNamedItem("displayText").getNodeValue(), // display
	    														modName, // name
	    														Integer.parseInt(modLayerChildren.item(j).getAttributes().getNamedItem("iLvl").getNodeValue())); // ilvl
	    						currMod.boxText = currMod.tier + " " + currMod.modText + ": [" + currMod.displayText + "]";
	    						
	    						if(subString != "" && currMod.boxText.contains(subString))
	    							affixList = addMod(affixList, currMod);
	    						else if(subString == "")
	    							affixList = addMod(affixList, currMod);
	    					}
	    				}
	    			}
	    		}
	    	}  
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
		// Sort affixList alphabetically by boxText
		Arrays.sort(affixList, new SortByBoxText());

		return affixList;
	}
	
	// Overload of getModList for when substring is missing
	public static ItemMod[] getModList(String item, String ilvl, String archetype, String affixType){
		return getModList(item, ilvl, archetype, affixType, "");
	}
	
	/**
	 * Helper function that adds an ItemMod to an already allocated array of ItemMods
	 * @param affixList is the old list of ItemMods this function will append to
	 * @param currMod is the ItemMod that we're going to append to the affixList
	 * @return a newly allocated array with currMod appended to the end
	 */
	public static ItemMod[] addMod(ItemMod[] affixList, ItemMod currMod){		
		// Add an index
		ItemMod[] newMods = new ItemMod[affixList.length + 1];
		
		// Copy all old mods to new mod list
		for(int i = 0; i < affixList.length; i++)
			newMods[i] = affixList[i];
		
		// Add new mod
		newMods[newMods.length - 1] = currMod;
		
		return newMods;
	}
}
