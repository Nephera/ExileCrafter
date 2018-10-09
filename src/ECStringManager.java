
public class ECStringManager {
	public static String[] addString(String[] stringList, String currString){		
		// Add an index
		String[] newStrings = new String[stringList.length + 1];
		
		// Copy all old mods to new mod list
		for(int i = 0; i < stringList.length; i++)
			newStrings[i] = stringList[i];
		
		// Add new mod
		newStrings[newStrings.length - 1] = currString;
		
		return newStrings;
	}
	
	static String[] parseItemMods(String itemText)
	{
		String[] mods = new String[0];
		
		if(itemText.contains("Rarity: Normal"))
			return new String[0];
		
		// If contains Elder Item or Shaper Item, cut off at preceding --------
		if(itemText.contains("Shaper Item") || itemText.contains("Elder Item"))
			itemText = itemText.substring(0, itemText.lastIndexOf("--------"));
		
		// Get String after last --------
		itemText = itemText.substring(itemText.lastIndexOf("--------") + 9, itemText.length());
		
		// Parse out individual mods
		String singleMod = null;
		String remainingMods = itemText;
		int split = remainingMods.indexOf("\n");
		
		while(split != -1)
		{
			singleMod = remainingMods.substring(0, split);
			remainingMods = remainingMods.substring(split + 1, remainingMods.length());
			
			if(split != -1)
				mods = addString(mods, singleMod);
			
			split = remainingMods.indexOf("\n");
		}
		
		return mods;
	}
	
	static String getRawModText(String inString)
	{
		return inString.replaceAll("[0-9\\-\\#]", "");
	}
	
	static String[] getRawModValues(String inString)
	{
		String[] ret = new String[0];
		inString = inString.replaceAll("[%+a-zA-Z]", "");
		inString = inString.replaceAll("\\-", " ");
		inString = inString.trim();
		ret = inString.split("\\s+");
		
		return ret;
	}
	
	public static String[] addRawText(String[] array, String rawText)
	{
		String[] ret = new String[array.length + 1];
		
		for(int i = 0; i < array.length; i++)
			ret[i] = array[i];
		
		ret[ret.length - 1] = rawText;
		
		return ret;
	}
	
	public static String[][] addRawVals(String[][] array, String[] rawVals)
	{
		String[][] ret = new String[array.length + 1][4];
		
		for(int i = 0; i < array.length; i++)
			ret[i] = array[i];
		
		ret[ret.length - 1] = rawVals;
		
		return ret;
	}
}
