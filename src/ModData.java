
public class ModData {
	String modRawText;
	String modValue1;
	String modValue2;
	boolean rolledMod;
	
	ModData(String text, String val1, String val2, boolean isRolled)
	{
		modRawText = text;
		modValue1 = val1;
		modValue2 = val2;
		rolledMod = isRolled;
	}
}
