import java.util.Comparator;

public class ItemMod {
	String tier;
	int minVal1;
	int maxVal1;
	int minVal2;
	int maxVal2;
	String displayText = "";
	String modText = "";
	String boxText = "";
	int ilvl;
	
	ItemMod(String inBoxText) {
		boxText = inBoxText;
	}
	
	ItemMod(String t, int min1, int max1, int min2, int max2, String disText, String mText, int lvl) {
		tier = t;
		minVal1 = min1;
		maxVal1 = max1;
		minVal2 = min2;
		maxVal2 = max2;
		displayText = disText;
		modText = mText;
		ilvl = lvl;
	}
}

class SortByBoxText implements Comparator<ItemMod> {
	@Override
	public int compare(ItemMod item1, ItemMod item2) {
		return item1.boxText.compareTo(item2.boxText);
	}
	
}
