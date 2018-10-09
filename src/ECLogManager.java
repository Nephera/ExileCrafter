import java.io.IOException;
import java.util.logging.*;

public class ECLogManager {
	private static final Logger logger = Logger.getLogger(ECLogManager.class.getName());
	static Handler fh;
	
	ECLogManager()
	{
		try {
			fh = new FileHandler("src/logs/debug.xml", true);
			logger.addHandler(fh);
		    logger.setLevel(Level.INFO);
		} 
		catch (SecurityException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void logMessage(String message)
	{
		logger.info(message);
	}
	
	public void logItem(String itemText)
	{	
	    logger.info(itemText);
	}
	
	public void logMods(String[] rawMods, String[][] rawVals, String tag)
	{
		String logInfo = tag + "\n";
		for(int i = 0; i < rawMods.length; i++)
		{
			if(!rawMods[i].equals("None") && !rawMods[i].equals(""))
			{
				logInfo = logInfo.concat(rawMods[i] + " // ");

				for(int j = 0; j < rawVals[i].length; j++)
					logInfo = logInfo.concat(rawVals[i][j] + " ");
				
				logInfo = logInfo.concat("\n");
			}
		}
		
		logger.info(logInfo);
	}
	
	public void logOrb(int GP, int GS, int DP, int DS, int mGP, int mGS, 
			int mDP, int mDS, int pCnt, int sCnt, ItemRarity IR, Orb usedOrb, int mID)
	{
		String logInfo = "Rolling: " + usedOrb + " id:" + mID + " for itemRarity: " + IR + "\n";
		logInfo = logInfo.concat("Prefixes Rolled: " + pCnt + "\n");
		logInfo = logInfo.concat("Suffixes Rolled: " + sCnt + "\n");
		logInfo = logInfo.concat("Guaranteed Prefixes: " + GP + "\n");
		logInfo = logInfo.concat("Guaranteed Suffixes: " + GS + "\n");
		logInfo = logInfo.concat("Desired Prefixes: " + DP + "\n");
		logInfo = logInfo.concat("Desired Suffixes: " + DS + "\n");
		logInfo = logInfo.concat("matchedGPrefix: " + mGP + "\n");
		logInfo = logInfo.concat("matchedGSuffix: " + mGS + "\n");
		logInfo = logInfo.concat("matchedDPrefix: " + mDP + "\n");
		logInfo = logInfo.concat("matchedDSuffix: " + mDS + "\n");

		logger.info(logInfo);
	}
}
