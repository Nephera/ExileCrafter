import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JTextField;

public class ECConfig {
	String actionKey;
	String unlockKey;
	String calibrateKey;
	String powerKey;
	
	CalibrationMode mode = CalibrationMode.OFF;
	boolean power = true;
	
	ItemLoc[] transmutations;
	ItemLoc[] alterations;
	ItemLoc[] scours;
	ItemLoc[] regals;
	ItemLoc[] augmentations;
	ItemLoc[] chaos;
	
	ECConfig()
	{
		File configFile = new File("src/config/config.properties");
		FileReader reader;
		try {
			reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);
			actionKey = props.getProperty("Action");
			unlockKey = props.getProperty("Unlock");
			calibrateKey = props.getProperty("Calibrate");
			powerKey = props.getProperty("Power");
		} catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void iterateMode()
	{
		switch(mode)
		{
			case OFF:
				mode = CalibrationMode.START;
				break;
				
			case START:
				break;
			
			case TRANSMUTATION:
				mode = CalibrationMode.ALTERATION;
				break;
				
			case ALTERATION:
				mode = CalibrationMode.AUGMENTATION;
				break;
			case AUGMENTATION:
				mode = CalibrationMode.REGAL;
				break;
			case REGAL:
				mode = CalibrationMode.SCOUR;
				break;
			case SCOUR:
				mode = CalibrationMode.OFF;
				break;
			case CHAOS:
				mode = CalibrationMode.OFF;
				break;
		}
	}
	public void updateConfigProperty(Object src)
	{
		String cfgProp;
		
		cfgProp = ((JTextField)src).getName();
		cfgProp = cfgProp.replaceAll("KeyTextField", "");
		System.out.println(cfgProp);

		if(cfgProp.equals("Action"))
			actionKey = ((JTextField)src).getText();
		else if(cfgProp.equals("Unlock"))
			unlockKey = ((JTextField)src).getText();
		else if(cfgProp.equals("Calibrate"))
			calibrateKey = ((JTextField)src).getText();
		else if(cfgProp.equals("Power"))
			powerKey = ((JTextField)src).getText();
		
		File configFile = new File("src/config/config.properties");
		Properties props = new Properties();
		FileReader reader;
		try {
			reader = new FileReader(configFile);
			props.load(reader);
			
			props.setProperty(cfgProp, ((JTextField)src).getText());
			
			FileWriter writer;
			writer = new FileWriter(configFile);
			props.store(writer, "Hotkey Settings");
		} catch (FileNotFoundException e1) {e1.printStackTrace();
		} catch (IOException e) {e.printStackTrace();
		}
	}
}
