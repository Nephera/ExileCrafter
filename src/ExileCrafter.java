/**
 * @author David "Nephera" Ray, 01/18/2018
 *
 * ExileCrafter is a monolithic program designed to help users of 
 * Path of Exile to craft items quickly and with less stress.  After 
 * providing a list of desired outcomes, the user can expect to press
 * a single button (not to be automated to stay compliant with TOS)
 * until either all of their currency is gone or their desired craft 
 * is available, which will be made apparent through gfx and a sound
 * 
 * Features to Implement:
 *  * Aug/Alt/Regal Craft Tab	
 *  	TODO: Develop way to allow updates
 *  	TODO: Develop way to allow user to make additions to mod pools
 *  	TODO: Develop way to allow user to make deletions to mod pools		
 *		TODO: Proper comparator sorting in ItemMod (T12 > T2)
 *		TODO: Develop way to make sure PoE is in focus to do actions/unlocking
 *		TODO: Build algorithm for 1 PREFIX
 *		TODO: Build algorithm for 1 SUFFIX
 *		TODO: Build algorithm for 2 PREFIX
 *		TODO: Build algorithm for 2 SUFFIX
 *		TODO: Build algorithm for 1 PREFIX, 1 SUFFIX
 *		TODO: Build algorithm to deal with desiredmods (as opposed to guaranteed mods)
 *		TODO: Complete calibration implementation
 *		TODO: Complete updatecosts implementation
 *		TODO: Develop way to compare a string with pool of mods in xml doc
 */

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.AWTException;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.MouseInfo;

import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JScrollPane;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import javax.swing.ScrollPaneConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import java.awt.Font;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.IOException;
import java.awt.Cursor;
 
public class ExileCrafter {

	private static ECLogManager logger;
	
	private JFrame frmNephsExileCrafter;
	
	private static JTabbedPane tabbedPane;
	
	// Start Regal Panel
	
	private JPanel RegalPanel;
	private JComboBox<ItemType> ItemComboBox;
	private JTextField ItemLevelTextField;
	private JComboBox<String> ItemArchetypeComboBox;
	
	private static JComboBox<ItemMod> Prefix1ComboBox;
	private static JComboBox<ItemMod> Prefix2ComboBox;
	private static JComboBox<ItemMod> Suffix1ComboBox;
	private static JComboBox<ItemMod> Suffix2ComboBox;
	
	private JTextField PrefixSearchTextField;
	private JScrollPane OtherPrefixScrollPane;
	private static JList<ItemMod> PrefixList;
	
	private JTextField SuffixSearchTextField;
	private JScrollPane OtherSuffixScrollPane;
	private static JList<ItemMod> SuffixList;
	
	private ItemMod[] Prefixes;
	private ItemMod[] Suffixes;
	private static boolean actionLocked = false;
	
	// End Regal Panel
	// Start Config Panel
	
	private JPanel ConfigPanel;
	
	// Arrays to store strings we want to verify our item against
	private static String[] guaranteedPrefixes;
	private static String[] desiredPrefixes;
	private static String[] guaranteedSuffixes;
	private static String[] desiredSuffixes;
	
	// List of all of our base item types, appears in a combo box.
	private DefaultComboBoxModel<ItemType> CBM;
	
	private static JTextField PerformActionTextField;
	private static JTextField UnlockActionTextField;
	private static JTextField CalibrateCurrenciesTextField;
	private static JTextField PowerTextField;

	// Track which action step we're on (should probably be its own class)
	private static int actionStep = 1;
	private static Orb nextOrb;
	
	private static ECConfig cfg;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Start Application GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					logger = new ECLogManager();
					ExileCrafter window = new ExileCrafter();
					window.frmNephsExileCrafter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 */
	public ExileCrafter(){initialize();}
	
	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		
		cfg = new ECConfig();
		
		initGlobalKeyListener();
		initAffixArrays();
		
		frmNephsExileCrafter = new JFrame();
		frmNephsExileCrafter.setFont(new Font("Arial", Font.BOLD, 14));
		frmNephsExileCrafter.setAlwaysOnTop(true);
		frmNephsExileCrafter.setTitle("Neph's Exile Crafter");
		frmNephsExileCrafter.setBounds(100, 100, 587, 734);
		frmNephsExileCrafter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmNephsExileCrafter.setIconImage(Toolkit.getDefaultToolkit().getImage("src/img/NEClogo.gif"));
	
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmNephsExileCrafter.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		// Regal Crafting Tab
		RegalPanel = new JPanel();
		tabbedPane.addTab("Regal", null, RegalPanel, null);
		RegalPanel.setLayout(null);
		
		ECActionHandler aHandle = new ECActionHandler();
		
		JPanel BasicItemInfoPanel = new JPanel();
		BasicItemInfoPanel.setBorder(new TitledBorder(null, "Basic Item Information", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		BasicItemInfoPanel.setBounds(28, 13, 509, 63);
		RegalPanel.add(BasicItemInfoPanel);
		BasicItemInfoPanel.setLayout(null);
		
		JLabel ItemLabel = new JLabel("Item");
		ItemLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		ItemLabel.setBounds(22, 26, 26, 16);
		BasicItemInfoPanel.add(ItemLabel);
		
		CBM = new DefaultComboBoxModel<ItemType>(ECItemManager.getItemList());
		
		ItemComboBox = new JComboBox<ItemType>();
		ItemComboBox.setBackground(Color.WHITE);
		ItemComboBox.setRenderer(new ComboBoxRenderer());
		ItemComboBox.setMaximumRowCount(16);
		ItemComboBox.setModel(CBM);
		ItemComboBox.setBounds(61, 23, 137, 22);
		BasicItemInfoPanel.add(ItemComboBox);
		ItemComboBox.addActionListener(aHandle);
		
		JLabel ItemLevelLabel = new JLabel("iLvl");
		ItemLevelLabel.setBounds(225, 26, 26, 16);
		BasicItemInfoPanel.add(ItemLevelLabel);
		
		ItemLevelTextField = new JTextField();
		ItemLevelTextField.setBounds(255, 23, 36, 22);
		BasicItemInfoPanel.add(ItemLevelTextField);
		ItemLevelTextField.setHorizontalAlignment(SwingConstants.CENTER);
		ItemLevelTextField.setColumns(10);
		ItemLevelTextField.addActionListener(aHandle);
		
		JLabel ItemArchetypeLabel = new JLabel("Archetype");
		ItemArchetypeLabel.setBounds(316, 26, 62, 16);
		BasicItemInfoPanel.add(ItemArchetypeLabel);
		
		ItemArchetypeComboBox = new JComboBox<String>();
		ItemArchetypeComboBox.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		ItemArchetypeComboBox.setBackground(Color.WHITE);
		ItemArchetypeComboBox.setMaximumRowCount(16);
		ItemArchetypeComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Normal", "Elder", "Shaper"}));
		ItemArchetypeComboBox.setBounds(386, 23, 96, 22);
		BasicItemInfoPanel.add(ItemArchetypeComboBox);
		ItemArchetypeComboBox.addActionListener(aHandle);
		
		JPanel GuaranteedAffixesPanel = new JPanel();
		GuaranteedAffixesPanel.setBorder(new TitledBorder(null, "Guaranteed Affixes", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GuaranteedAffixesPanel.setBounds(28, 93, 509, 164);
		RegalPanel.add(GuaranteedAffixesPanel);
		GuaranteedAffixesPanel.setLayout(null);
		
		JLabel Prefix1Label = new JLabel("Prefix 1");
		Prefix1Label.setBounds(22, 18, 55, 31);
		GuaranteedAffixesPanel.add(Prefix1Label);
		
		Prefix1ComboBox = new JComboBox<ItemMod>();
		Prefix1ComboBox.setBackground(Color.WHITE);
		Prefix1ComboBox.setRenderer(new ModComboBoxRenderer());
		Prefix1ComboBox.setMaximumRowCount(16);
		
		Prefix1ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(ECModManager.getModList(CBM.getElementAt(0).xmlName,
				ItemLevelTextField.getText(), ItemArchetypeComboBox.getSelectedItem().toString(), "Prefixes")));;
		Prefix1ComboBox.setBounds(77, 22, 406, 22);
		GuaranteedAffixesPanel.add(Prefix1ComboBox);
		Prefix1ComboBox.addActionListener(aHandle);
		
		JLabel Suffix1Label = new JLabel("Suffix 1");
		Suffix1Label.setBounds(22, 92, 43, 31);
		GuaranteedAffixesPanel.add(Suffix1Label);
		
		Suffix1ComboBox = new JComboBox<ItemMod>();
		Suffix1ComboBox.setBackground(Color.WHITE);
		Suffix1ComboBox.setRenderer(new ModComboBoxRenderer());
		Suffix1ComboBox.setMaximumRowCount(16);
		Suffix1ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(ECModManager.getModList(CBM.getElementAt(0).xmlName,
				ItemLevelTextField.getText(), ItemArchetypeComboBox.getSelectedItem().toString(), "Suffixes")));;
		Suffix1ComboBox.setBounds(77, 96, 406, 22);
		GuaranteedAffixesPanel.add(Suffix1ComboBox);
		Suffix1ComboBox.addActionListener(aHandle);
		
		JLabel Prefix2Label = new JLabel("Prefix 2");
		Prefix2Label.setBounds(22, 49, 55, 31);
		GuaranteedAffixesPanel.add(Prefix2Label);
		
		Prefix2ComboBox = new JComboBox<ItemMod>();
		Prefix2ComboBox.setBackground(Color.WHITE);
		Prefix2ComboBox.setRenderer(new ModComboBoxRenderer());
		Prefix2ComboBox.setMaximumRowCount(16);
		Prefix2ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(ECModManager.getModList(CBM.getElementAt(0).xmlName,
				ItemLevelTextField.getText(), ItemArchetypeComboBox.getSelectedItem().toString(), "Prefixes")));;
		Prefix2ComboBox.setBounds(77, 53, 406, 22);
		GuaranteedAffixesPanel.add(Prefix2ComboBox);
		Prefix2ComboBox.addActionListener(aHandle);
		
		JLabel Suffix2Label = new JLabel("Suffix 2");
		Suffix2Label.setBounds(22, 124, 43, 31);
		GuaranteedAffixesPanel.add(Suffix2Label);
		
		Suffix2ComboBox = new JComboBox<ItemMod>();
		Suffix2ComboBox.setBackground(Color.WHITE);
		Suffix2ComboBox.setRenderer(new ModComboBoxRenderer());
		Suffix2ComboBox.setMaximumRowCount(16);
		Suffix2ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(ECModManager.getModList(CBM.getElementAt(0).xmlName,
				ItemLevelTextField.getText(), ItemArchetypeComboBox.getSelectedItem().toString(), "Suffixes")));;
		Suffix2ComboBox.setBounds(77, 128, 406, 22);
		GuaranteedAffixesPanel.add(Suffix2ComboBox);
		Suffix2ComboBox.addActionListener(aHandle);
		
		JPanel OtherDesiredAffixesPanel = new JPanel();
		OtherDesiredAffixesPanel.setBorder(new TitledBorder(null, "Other Desired Affixes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		OtherDesiredAffixesPanel.setBounds(28, 269, 509, 367);
		RegalPanel.add(OtherDesiredAffixesPanel);
		OtherDesiredAffixesPanel.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel OtherPrefixPanel = new JPanel();
		OtherPrefixPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Prefixes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		OtherDesiredAffixesPanel.add(OtherPrefixPanel);
		OtherPrefixPanel.setLayout(null);
		
		OtherPrefixScrollPane = new JScrollPane();
		OtherPrefixScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		OtherPrefixScrollPane.setBounds(12, 22, 475, 138);
		OtherPrefixPanel.add(OtherPrefixScrollPane);
		
		PrefixSearchTextField = new JTextField();
		PrefixSearchTextField.setToolTipText("Search...");
		OtherPrefixScrollPane.setColumnHeaderView(PrefixSearchTextField);
		PrefixSearchTextField.setColumns(10);
		PrefixSearchTextField.addActionListener(aHandle);
		
		ECListSelectionListener lslHandle = new ECListSelectionListener();
		
		PrefixList = new JList<ItemMod>();
		PrefixList.setCellRenderer(new ModComboBoxRenderer());
		PrefixList.setModel(new AbstractListModel<ItemMod>() {
			private static final long serialVersionUID = 1L;
			ItemMod[] values = new ItemMod[] {new ItemMod("None")};
			final public int getSize() {return values.length;}
			final public ItemMod getElementAt(int index) {return values[index];}
		});
		OtherPrefixScrollPane.setViewportView(PrefixList);
		PrefixList.addListSelectionListener(lslHandle);
		PrefixList.setSelectedIndex(0);
		
		JPanel OtherSuffixPanel = new JPanel();
		OtherSuffixPanel.setLayout(null);
		OtherSuffixPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Suffixes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		OtherDesiredAffixesPanel.add(OtherSuffixPanel);
		
		OtherSuffixScrollPane = new JScrollPane();
		OtherSuffixScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		OtherSuffixScrollPane.setBounds(12, 22, 475, 138);
		OtherSuffixPanel.add(OtherSuffixScrollPane);
		
		SuffixSearchTextField = new JTextField();
		SuffixSearchTextField.setToolTipText("Search...");
		OtherSuffixScrollPane.setColumnHeaderView(SuffixSearchTextField);
		SuffixSearchTextField.setColumns(10);
		SuffixSearchTextField.addActionListener(aHandle);
		
		SuffixList = new JList<ItemMod>();
		SuffixList.setCellRenderer(new ModComboBoxRenderer());
		SuffixList.setModel(new AbstractListModel<ItemMod>() {
			private static final long serialVersionUID = 1L;
			ItemMod[] values = new ItemMod[] {new ItemMod("None")};
			public int getSize() {return values.length;}
			public ItemMod getElementAt(int index) {return values[index];}
		});
		OtherSuffixScrollPane.setViewportView(SuffixList);
		SuffixList.addListSelectionListener(lslHandle);
		SuffixList.setSelectedIndex(0);
		
		// Master Crafting Tab
		JPanel MasterPanel = new JPanel();
		//tabbedPane.addTab("Master", null, MasterPanel, null);
		MasterPanel.setLayout(null);
		
		// Chaos Crafting Tab
		JPanel ChaosPanel = new JPanel();
		//tabbedPane.addTab("Chaos", null, ChaosPanel, null);
		ChaosPanel.setLayout(null);
		
		// Configuration Tab
		ConfigPanel = new JPanel();
		tabbedPane.addTab("Config", null, ConfigPanel, null);
		ConfigPanel.setLayout(null);
		
		JPanel HotkeyPanel = new JPanel();
		HotkeyPanel.setBorder(new TitledBorder(null, "Hotkeys", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		HotkeyPanel.setBounds(12, 12, 557, 142);
		ConfigPanel.add(HotkeyPanel);
		HotkeyPanel.setLayout(null);
		
		JLabel lblPower = new JLabel("Turn On/Off");
		lblPower.setBounds(12, 24, 86, 16);
		HotkeyPanel.add(lblPower);
		
		JLabel lblPerformAction = new JLabel("Perform Action");
		lblPerformAction.setBounds(12, 52, 86, 16);
		HotkeyPanel.add(lblPerformAction);
		
		JLabel lblUnlockAction = new JLabel("Unlock Action");
		lblUnlockAction.setBounds(12, 80, 78, 16);
		HotkeyPanel.add(lblUnlockAction);
		
		JLabel lblCalibrateCurrencies = new JLabel("Calibrate Currencies");
		lblCalibrateCurrencies.setBounds(12, 108, 117, 16);
		HotkeyPanel.add(lblCalibrateCurrencies);
		
		ECHotKeyFocusListener fHandle = new ECHotKeyFocusListener();
		
		PowerTextField = new JTextField();
		PowerTextField.setName("PowerKeyTextField");
		PowerTextField.setEditable(false);
		PowerTextField.setHorizontalAlignment(SwingConstants.CENTER);
		PowerTextField.setText(cfg.powerKey);
		PowerTextField.setColumns(10);
		PowerTextField.setBounds(142, 22, 114, 20);
		PowerTextField.addActionListener(aHandle);
		PowerTextField.addFocusListener(fHandle);
		HotkeyPanel.add(PowerTextField);
		
		PerformActionTextField = new JTextField();
		PerformActionTextField.setName("ActionKeyTextField");
		PerformActionTextField.setEditable(false);
		PerformActionTextField.setHorizontalAlignment(SwingConstants.CENTER);
		PerformActionTextField.setText(cfg.actionKey);
		PerformActionTextField.setBounds(142, 50, 114, 20);
		HotkeyPanel.add(PerformActionTextField);
		PerformActionTextField.setColumns(10);
		PerformActionTextField.addActionListener(aHandle);
		PerformActionTextField.addFocusListener(fHandle);

		UnlockActionTextField = new JTextField();
		UnlockActionTextField.setName("UnlockKeyTextField");
		UnlockActionTextField.setEditable(false);
		UnlockActionTextField.setHorizontalAlignment(SwingConstants.CENTER);
		UnlockActionTextField.setText(cfg.unlockKey);
		UnlockActionTextField.setBounds(142, 78, 114, 20);
		HotkeyPanel.add(UnlockActionTextField);
		UnlockActionTextField.setColumns(10);
		UnlockActionTextField.addActionListener(aHandle);
		UnlockActionTextField.addFocusListener(fHandle);

		CalibrateCurrenciesTextField = new JTextField();
		CalibrateCurrenciesTextField.setName("CalibrateKeyTextField");
		CalibrateCurrenciesTextField.setEditable(false);
		CalibrateCurrenciesTextField.setHorizontalAlignment(SwingConstants.CENTER);
		CalibrateCurrenciesTextField.setText(cfg.calibrateKey);
		CalibrateCurrenciesTextField.setBounds(142, 106, 114, 20);
		HotkeyPanel.add(CalibrateCurrenciesTextField);
		CalibrateCurrenciesTextField.setColumns(10);
		CalibrateCurrenciesTextField.addActionListener(aHandle);
		CalibrateCurrenciesTextField.addFocusListener(fHandle);
	} // end of initialize()
	
	/**
	 * A listener class designated for hotkey text fields on config panel.
	 * Listens for keyevents and replaces text inside of focused text field.
	 * Config file is also updated upon key press.
	 */
	public class ECHotKeyFocusListener implements FocusListener
	{
		@Override
		public void focusGained(FocusEvent e) {
			final Object src = e.getSource();

			((JTextField)src).selectAll();
			
			// Listen for next keypress
			KeyListener kl = new KeyListener(){
				public void keyPressed(KeyEvent ke) {
					// If press escape, cancel
					if(KeyEvent.getKeyText(ke.getKeyCode()) != "Escape")
					{
						// Replace src text with keypress text
						((JTextField)src).setText(KeyEvent.getKeyText(ke.getKeyCode()));
						
						// update config
						cfg.updateConfigProperty(src);
					}
				}
				@Override
				public void keyReleased(KeyEvent ke) {/*Do Nothing*/}

				@Override
				public void keyTyped(KeyEvent ke) {/*Do Nothing*/}
			};
					
			((JTextField)src).addKeyListener(kl);
		}

		@Override
		public void focusLost(FocusEvent e) {/*Do Nothing*/}
	}
	
	/**
	 * A listener class designated for JLists on regal panel.
	 * Updates desiredPrefixes / desiredSuffixes arrays based on
	 * selections made in PrefixList or SuffixList
	 */
	public class ECListSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			Object src = e.getSource();

			if(src==PrefixList) {
				List<ItemMod> selectedPrefixes = PrefixList.getSelectedValuesList();
				desiredPrefixes = new String[PrefixList.getSelectedIndices().length];
				for(int i = 0, j = 0; i < selectedPrefixes.size(); i++, j++)
					desiredPrefixes[j] = selectedPrefixes.get(i).displayText;
			}
			if(src==SuffixList) {
				List<ItemMod> selectedSuffixes = SuffixList.getSelectedValuesList();
				desiredSuffixes = new String[SuffixList.getSelectedIndices().length];
				for(int i = 0, j = 0; i < selectedSuffixes.size(); i++, j++)
					desiredSuffixes[j] = selectedSuffixes.get(i).displayText;
			}
		}	
	}

	/**
	 * A listener class designated for comboboxes and remaining text fields
	 * in application. Mainly used to update related fields and comboboxes
	 */
	public class ECActionHandler implements ActionListener {
		private String oldItemComboBoxValue = "";
		private String oldItemLevelTextFieldValue = "";
		private String oldItemArchetypeComboBoxValue = "";
		
		// Returns the number of guaranteed affixes selected
		public int getGAffixCount() {
			int gAffixCount = 0;
			
			// If the selection is not "None"
			if(Prefix1ComboBox.getSelectedIndex() > 0)
				gAffixCount++;
				
			if(Prefix2ComboBox.getSelectedIndex() > 0)
				gAffixCount++;
			
			if(Suffix1ComboBox.getSelectedIndex() > 0)
				gAffixCount++;
				
			if(Suffix2ComboBox.getSelectedIndex() > 0)
				gAffixCount++;
				
			return gAffixCount;
		}
		
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();

			// Regal Tab
			if(tabbedPane.getSelectedIndex() == 0) {
				if(src==ItemComboBox || src==ItemLevelTextField || src==ItemArchetypeComboBox) {	
					// If new value != old value, reset:
					if((ItemComboBox.getSelectedItem() != oldItemComboBoxValue) ||
							(ItemLevelTextField.getText() != oldItemLevelTextFieldValue) ||
							(ItemArchetypeComboBox.getSelectedItem() != oldItemArchetypeComboBoxValue)){
						ItemType IT = ((ItemType)(ItemComboBox.getSelectedItem()));
						
						Prefixes = ECModManager.getModList(IT.xmlName,  ItemLevelTextField.getText(), 
								ItemArchetypeComboBox.getSelectedItem().toString(), "Prefixes");
						Suffixes = ECModManager.getModList(IT.xmlName, ItemLevelTextField.getText(), 
								ItemArchetypeComboBox.getSelectedItem().toString(), "Suffixes");
						
						Prefix1ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(Prefixes));
						Prefix2ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(Prefixes));
						Suffix1ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(Suffixes));
						Suffix2ComboBox.setModel(new DefaultComboBoxModel<ItemMod>(Suffixes));
						
						PrefixSearchTextField.setText("");
						SuffixSearchTextField.setText("");

						PrefixList.setListData(Prefixes);
						PrefixList.setSelectedIndex(0);
						SuffixList.setListData(Suffixes);
						SuffixList.setSelectedIndex(0);
						
						oldItemComboBoxValue = ItemComboBox.getSelectedItem().toString();
						oldItemLevelTextFieldValue = ItemLevelTextField.getText();
						oldItemArchetypeComboBoxValue = ItemArchetypeComboBox.getSelectedItem().toString();
					}
				}
				else if(src==Prefix1ComboBox || src==Prefix2ComboBox ||
						src==Suffix1ComboBox || src==Suffix2ComboBox) {	
					// If 3 ComboBoxes are not "None", disable 4th
					if(getGAffixCount() == 3) {
						if(Prefix1ComboBox.getSelectedIndex() == 0)
							Prefix1ComboBox.setEnabled(false);
						if(Prefix2ComboBox.getSelectedIndex() == 0)
							Prefix2ComboBox.setEnabled(false);
						if(Suffix1ComboBox.getSelectedIndex() == 0)
							Suffix1ComboBox.setEnabled(false);
						if(Suffix2ComboBox.getSelectedIndex() == 0)
							Suffix2ComboBox.setEnabled(false);
					}
					// If < 3 ComboBoxes are not "None", enable all
					else if(getGAffixCount() < 3) {
						if(!Prefix1ComboBox.isEnabled())
							Prefix1ComboBox.setEnabled(true);
						if(!Prefix2ComboBox.isEnabled())
							Prefix2ComboBox.setEnabled(true);
						if(!Suffix1ComboBox.isEnabled())
							Suffix1ComboBox.setEnabled(true);
						if(!Suffix2ComboBox.isEnabled())
							Suffix2ComboBox.setEnabled(true);
					}
					
					// Enable/Disable PrefixList if 2 of guaranteed prefix selected
					if(Prefix1ComboBox.getSelectedIndex() != 0 && Prefix2ComboBox.getSelectedIndex() != 0)
						PrefixList.setEnabled(false);
					else if(!PrefixList.isEnabled())
						PrefixList.setEnabled(true);
					// Enable/Disable SuffixList if 2 of guaranteed suffix selected
					if(Suffix1ComboBox.getSelectedIndex() != 0 && Suffix2ComboBox.getSelectedIndex() != 0)
						SuffixList.setEnabled(false);
					else if(!SuffixList.isEnabled())
						SuffixList.setEnabled(true);
					
					if(src==Prefix1ComboBox)
						guaranteedPrefixes[0] = ((ItemMod)(Prefix1ComboBox.getSelectedItem())).displayText;
					else if(src==Prefix2ComboBox)
						guaranteedPrefixes[1] = ((ItemMod)(Prefix2ComboBox.getSelectedItem())).displayText;
					else if(src==Suffix1ComboBox)
						guaranteedSuffixes[0] = ((ItemMod)(Suffix1ComboBox.getSelectedItem())).displayText;
					else if(src==Suffix2ComboBox)
						guaranteedSuffixes[1] = ((ItemMod)(Suffix2ComboBox.getSelectedItem())).displayText;
				}
				else if(event.getSource()==PrefixSearchTextField) {
					ItemType IT = ((ItemType)(ItemComboBox.getSelectedItem()));
					Prefixes = ECModManager.getModList(IT.xmlName,  ItemLevelTextField.getText(), 
						ItemArchetypeComboBox.getSelectedItem().toString(), "Prefixes",
						PrefixSearchTextField.getText());
					
					PrefixList.setListData(Prefixes);
				}
				else if(event.getSource()==SuffixSearchTextField) {
					ItemType IT = ((ItemType)(ItemComboBox.getSelectedItem()));
					Suffixes = ECModManager.getModList(IT.xmlName, ItemLevelTextField.getText(), 
						ItemArchetypeComboBox.getSelectedItem().toString(), "Suffixes",
						SuffixSearchTextField.getText());
					
					SuffixList.setListData(Suffixes);
				}
			}
		}
	}
	
	/**
	 * Helper function to turn on listener for keys at the OS level
	 * allowing us to capture keys designated by config file while
	 * Path of Exile is in focus.
	 */
	private static void initGlobalKeyListener()
	{
		try {
			
			// Turn IDE error logger off
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
			logger.setUseParentHandlers(false);
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}
		
		GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
			@Override
			public void nativeKeyPressed(NativeKeyEvent e) {/*Do Nothing*/}

			@Override
			public void nativeKeyReleased(NativeKeyEvent e) {
				String key = NativeKeyEvent.getKeyText(e.getKeyCode());

				if(key.equals(cfg.actionKey))
					performAction();
				else if(key.equals(cfg.unlockKey))
					performUnlock();
				else if(key.equals(cfg.calibrateKey) && cfg.mode == CalibrationMode.OFF)	
					startCalibration(tabbedPane.getSelectedIndex());
				else if(key.equals(cfg.powerKey))
					performPowerSwitch();
			}

			@Override
			public void nativeKeyTyped(NativeKeyEvent e) {/*Do Nothing*/}
		});
	}
	
	/*
	 * Performs a single in-game action that complies with Path of Exile's EULA as of 2018/03/01.
	 * EULA: 
	 * 
	 * "6. Restrictions: Under no circumstances, without the prior written 
	 * approval of Grinding Gear Games, may you:
	 * ...
	 * (b) Modify or adapt (including through third parties and third party tools)
	 * the game client or its data, other than in the normal course of POE gameplay 
	 * as permitted in accordance with the Licence. 
	 * ...
	 * (c) Utilise any automated software or ‘bots’ in relation to your access or use
	 * of the Website, Materials or Services."
	 * 
	 * Actions include:
	 *	* Calibrating Locations of Currencies in Inventory
	 *	* Peeling a Single Orb off Stack
	 *	* Clicking Item to Change Mods (Orb on Cursor)
	 *	* Copying Mods to Clipboard for Parsing
	 *
	 *  TODO: Finish implementation
	 */
	static void performAction()
	{		
		if(cfg.mode != CalibrationMode.OFF)
		{
			performCalibrate();
		}
		else if(actionLocked)
		{
			// Play already locked sound
		}
		else if(!actionLocked)
		{
			// If we need to break out actions
			switch(actionStep)
			{
				case 1:
					System.out.println("COPY STEP");
					String itemText = actionCopyItemDetails();
					
					logger.logItem(itemText);
					
					ItemRarity itemRarity = getItemRarity(itemText);
					
					// Parse details into just Strings of mods
					String[] rolledMods = ECStringManager.parseItemMods(itemText);
				  
					// Compare rolled mods to wanted mods, lock if wanted
					nextOrb = compareMods(rolledMods, itemRarity);
					
					if(nextOrb == Orb.NONE)
					{
						actionLocked = true;
						ECSoundPlayer.playLockFX();
					}
					else
					{
						actionStep++;
					}
					break;
					
				case 2:
					System.out.println("PREPARE STEP");
					actionPrepareNextRoll(nextOrb);
					
					actionStep++;
					break;
					
				case 3:
					System.out.println("ROLL STEP");
					actionRoll();
					// fall through
					
				default:
					actionStep = 1;
			}
		}
	}
	
	static ItemRarity getItemRarity(String itemText)
	{
		ItemRarity ret = ItemRarity.NORMAL;
		
		if(itemText.contains("Rarity: Normal"))
			return ItemRarity.NORMAL;
		if(itemText.contains("Rarity: Magic"))
			return ItemRarity.MAGIC;
		if(itemText.contains("Rarity: Rare"))
			return ItemRarity.RARE;
		if(itemText.contains("Rarity: Unique"))
			return ItemRarity.UNIQUE;
		
		return ret;
	}
	
	/*
	 * One action in Path of Exile, simulates left click
	 * when an orb is on the cursor to reroll the mods
	 * on the item
	 */
	static void actionRoll()
	{
	}
	
	/*
	 * One action in Path of Exile, simulates right click
	 * to peel an orb off of a stack for use
	 */
	static void actionPrepareNextRoll(Orb nextOrb)
	{
	}
	
	/*
	 * One action in Path of Exile, simulates CTRL+C to
	 * copy details to clipboard which defines itemText
	 */
	static String actionCopyItemDetails()
	{
		// TODO: If not an item, return nothing
		
		String itemText = "";
		
		try{
			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_CONTROL);
			r.keyPress(KeyEvent.VK_C);
			r.keyRelease(KeyEvent.VK_CONTROL);
			r.keyRelease(KeyEvent.VK_C);
		}catch(AWTException e){e.printStackTrace();}
		
		// Give time for clipboard to be written
		try {TimeUnit.MILLISECONDS.sleep(50);} 
		catch (InterruptedException e1) {e1.printStackTrace();}
		
		// then copy clipboard to local String
		try{itemText = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);} 
	    catch (HeadlessException e) {e.printStackTrace();}
	    catch (UnsupportedFlavorException e) {e.printStackTrace();} 
	    catch (IOException e) {e.printStackTrace();}
		
		return itemText;
	}
	
	
	private static void initAffixArrays()
	{
		guaranteedPrefixes = new String[2];
		Arrays.fill(guaranteedPrefixes, "None");
		guaranteedSuffixes = new String[2];
		Arrays.fill(guaranteedSuffixes, "None");
		desiredPrefixes = new String[0];
		desiredSuffixes = new String[0];
	}
	
	static void performUnlock()
	{
		if(cfg.mode != CalibrationMode.OFF)
		{
			cfg.iterateMode();
			performCalibrate();
		}
		if(actionLocked)
		{
			actionLocked = false;

			// Play unlock sound
			ECSoundPlayer.playUnlockFX();
		}
	}
	
	static void performPowerSwitch()
	{
	}
	
	static void performCalibrate()
	{
		switch(cfg.mode)
		{
			case START:
				cfg.iterateMode();
				break;
				
			case TRANSMUTATION:
				calibrate(cfg.transmutations, "Transmutation Orbs");
				break;
				
			case AUGMENTATION:
				calibrate(cfg.augmentations, "Augmentation Orbs");
				break;
				
			case ALTERATION:
				calibrate(cfg.alterations, "Alteration Orbs");
				break;
				
			case REGAL:
				calibrate(cfg.regals, "Regal Orbs");
				break;
				
			case SCOUR:
				calibrate(cfg.scours, "Scouring Orbs");
				break;
			
			case CHAOS:
				calibrate(cfg.chaos, "Chaos Orbs");
				break;
			
			case OFF:
				
			default:
		}
	}
	
	static void startCalibration(int tab)
	{
		switch(tab)
		{
			// Regal Tab
			case 0:
				cfg.mode = CalibrationMode.TRANSMUTATION;
				performCalibrate();
				break;
				
			// Config Tab
			case 1:
				
			// Chaos Tab
			case 2:	
				cfg.mode = CalibrationMode.CHAOS;
				performCalibrate();
				break;
				
			default:
		}
	}
	
	// TODO: Finish implementation
	// currencyType not passed by reference! 
	static void calibrate(ItemLoc[] currencyType, String currencyName)
	{
		ItemLoc[] orbLocArray;
		ItemLoc newLoc;

		System.out.println("Press [" + cfg.actionKey +"] once on each of your stacks of " + currencyName + ".");
		System.out.println("Press [" + cfg.unlockKey + "] when you're finished.");
		
		String stackSizeStr = "";
		int stackSize = 0;
		int tempBeg = 0;
		int tempEnd = 0;

		// Copy item details to clipboard then a local string
		try {
			Robot r;
		
			r = new Robot();
			r.keyPress(KeyEvent.VK_CONTROL);
			r.keyPress(KeyEvent.VK_C);
			r.keyRelease(KeyEvent.VK_CONTROL);
			r.keyRelease(KeyEvent.VK_C);
		} catch (AWTException e) {e.printStackTrace();}

		try{
			stackSizeStr = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
	    } 
	    catch (HeadlessException e) {e.printStackTrace();}
	    catch (UnsupportedFlavorException e) {e.printStackTrace();} 
	    catch (IOException e) {e.printStackTrace();}
		
		// Give time for clipboard to be written
		try {TimeUnit.MILLISECONDS.sleep(50);} 
		catch (InterruptedException e1) {e1.printStackTrace();}
		
		if(stackSizeStr.contains("Rarity: Currency"))
		{
			if(stackSizeStr.contains("Stack Size: "))
			{
				tempBeg = stackSizeStr.indexOf("Stack Size: ");
				tempEnd = stackSizeStr.indexOf("/");
				stackSize = Integer.parseInt(stackSizeStr.substring(tempBeg+12, tempEnd));
				System.out.println("Stack: " + stackSize);
			}
			
			if(stackSizeStr.contains("Orb of Augmentation"))
			{
				newLoc = new ItemLoc("Orb of Augmentation",
						MouseInfo.getPointerInfo().getLocation().x,
						MouseInfo.getPointerInfo().getLocation().y,
						stackSize);
				
				orbLocArray = new ItemLoc[cfg.augmentations.length + 1];
				
				for(int i = 0; i < cfg.augmentations.length; i++)
					orbLocArray[i] = cfg.augmentations[i];

				orbLocArray[orbLocArray.length - 1] = newLoc;
				
				cfg.augmentations = orbLocArray;
			}
			if(stackSizeStr.contains("Orb of Transmutation"))
			{
				newLoc = new ItemLoc("Orb of Transmutation",
						MouseInfo.getPointerInfo().getLocation().x,
						MouseInfo.getPointerInfo().getLocation().y,
						stackSize);
				
				orbLocArray = new ItemLoc[cfg.transmutations.length + 1];

				for(int i = 0; i < cfg.transmutations.length; i++)
					orbLocArray[i] = cfg.transmutations[i];

				orbLocArray[orbLocArray.length - 1] = newLoc;
				
				cfg.transmutations = orbLocArray;
			}
			if(stackSizeStr.contains("Orb of Alteration"))
			{
				newLoc = new ItemLoc("Orb of Alteration",
						MouseInfo.getPointerInfo().getLocation().x,
						MouseInfo.getPointerInfo().getLocation().y,
						stackSize);
				
				orbLocArray = new ItemLoc[cfg.alterations.length + 1];

				for(int i = 0; i < cfg.alterations.length; i++)
					orbLocArray[i] = cfg.alterations[i];

				orbLocArray[orbLocArray.length - 1] = newLoc;
				
				cfg.alterations = orbLocArray;
			}
			if(stackSizeStr.contains("Regal Orb"))
			{
				newLoc = new ItemLoc("Regal Orb",
						MouseInfo.getPointerInfo().getLocation().x,
						MouseInfo.getPointerInfo().getLocation().y,
						stackSize);
				
				orbLocArray = new ItemLoc[cfg.regals.length + 1];

				for(int i = 0; i < cfg.regals.length; i++)
					orbLocArray[i] = cfg.regals[i];

				orbLocArray[orbLocArray.length - 1] = newLoc;
				
				cfg.regals = orbLocArray;
			}
			if(stackSizeStr.contains("Orb of Scouring"))
			{
				newLoc = new ItemLoc("Orb of Scouring",
						MouseInfo.getPointerInfo().getLocation().x,
						MouseInfo.getPointerInfo().getLocation().y,
						stackSize);
				
				orbLocArray = new ItemLoc[cfg.scours.length + 1];

				for(int i = 0; i < cfg.scours.length; i++)
					orbLocArray[i] = cfg.scours[i];

				orbLocArray[orbLocArray.length - 1] = newLoc;
				
				cfg.scours = orbLocArray;
			}
			if(stackSizeStr.contains("Chaos Orb"))
			{
				newLoc = new ItemLoc("Chaos Orb",
						MouseInfo.getPointerInfo().getLocation().x,
						MouseInfo.getPointerInfo().getLocation().y,
						stackSize);
				
				orbLocArray = new ItemLoc[cfg.chaos.length + 1];
				
				for(int i = 0; i < cfg.chaos.length; i++)
					orbLocArray[i] = cfg.chaos[i];

				orbLocArray[orbLocArray.length - 1] = newLoc;
				
				cfg.chaos = orbLocArray;
			}
		}
	}
	
	/**
	 * compareMods calculates the next orb to be used by the user according to 
	 * what mods they rolled on the item compared to what mods they wanted
	 * rolled on the item (specified by PrefixComboBox1, PrefixComboBox2,
	 * SuffixComboBox1, SuffixComboBox2, PrefixList, and SuffixList
	 * @param rolledMods is an array of strings that specify the mods rolled
	 * on a particular item
	 * @param itemRarity is the item's rolled rarity
	 * @return the next orb type to be used based on the combination of
	 * affixes that are wanted and rolled
	 */
	static Orb compareMods(String[] rolledMods, ItemRarity itemRarity)
	{
		// Get mod data on rolled mods for comparison
		String[] rawRolledText = new String[0];
		String[] rawGPrefixText = new String[0];
		String[] rawGSuffixText = new String[0];
		String[] rawDPrefixText = new String[0];
		String[] rawDSuffixText = new String[0];
		String[][] rawRolledVals = new String[0][0];
		String[][] rawGPrefixVals = new String[0][0];
		String[][] rawGSuffixVals = new String[0][0];
		String[][] rawDPrefixVals = new String[0][0];
		String[][] rawDSuffixVals = new String[0][0];
		int GPrefixCnt = 0;
		int GSuffixCnt = 0;
		int DPrefixCnt = 0;
		int DSuffixCnt = 0;
		int matchedGPrefix = 0;
		int matchedGSuffix = 0;
		int matchedDPrefix = 0;
		int matchedDSuffix = 0;
		int prefixCnt = 0;
		int suffixCnt = 0;
		
		for(String mod : rolledMods)
		{
			rawRolledText = ECStringManager.addRawText(rawRolledText, ECStringManager.getRawModText(mod));
			rawRolledVals = ECStringManager.addRawVals(rawRolledVals, ECStringManager.getRawModValues(mod));
		}
		
		logger.logMods(rawRolledText, rawRolledVals, "Item Raw Values");
		
		for(String mod : guaranteedPrefixes)
		{
			rawGPrefixText = ECStringManager.addRawText(rawGPrefixText, ECStringManager.getRawModText(mod));
			rawGPrefixVals = ECStringManager.addRawVals(rawGPrefixVals, ECStringManager.getRawModValues(mod));
		}
		
		logger.logMods(rawGPrefixText, rawGPrefixVals, "Guaranteed Prefix Raw Values");
		
		for(String mod : guaranteedSuffixes)
		{
			rawGSuffixText = ECStringManager.addRawText(rawGSuffixText, ECStringManager.getRawModText(mod));
			rawGSuffixVals = ECStringManager.addRawVals(rawGSuffixVals, ECStringManager.getRawModValues(mod));
		}
		
		logger.logMods(rawGSuffixText, rawGSuffixVals, "Guaranteed Suffix Raw Values");
		
		for(String mod : desiredPrefixes)
		{
			rawDPrefixText = ECStringManager.addRawText(rawDPrefixText, ECStringManager.getRawModText(mod));
			rawDPrefixVals = ECStringManager.addRawVals(rawDPrefixVals, ECStringManager.getRawModValues(mod));
		}
		
		logger.logMods(rawDPrefixText, rawDPrefixVals, "Desired Prefix Raw Values");
		
		for(String mod : desiredSuffixes)
		{
			rawDSuffixText = ECStringManager.addRawText(rawDSuffixText, ECStringManager.getRawModText(mod));
			rawDSuffixVals = ECStringManager.addRawVals(rawDSuffixVals, ECStringManager.getRawModValues(mod));
		}
		
		logger.logMods(rawDSuffixText, rawDSuffixVals, "Desired Suffix Raw Values");
	
		// Get Guaranteed Prefixes Count
		if(Prefix1ComboBox.getSelectedIndex() != 0)
			GPrefixCnt++;
		if(Prefix2ComboBox.getSelectedIndex() != 0)
			GPrefixCnt++;
		
		// Get Guaranteed Suffixes Count
		if(Suffix1ComboBox.getSelectedIndex() != 0)
			GSuffixCnt++;
		if(Suffix2ComboBox.getSelectedIndex() != 0)
			GSuffixCnt++;
		
		// Get Desired Prefixes Count
		if(PrefixList.getSelectedIndex() != 0)
			DPrefixCnt = PrefixList.getSelectedIndices().length;
		
		// Get Desired Suffixes Count
		if(SuffixList.getSelectedIndex() != 0)
			GPrefixCnt = SuffixList.getSelectedIndices().length;
		
		// Count Guaranteed Prefixes Hit
		for(int i = 0; i < rawGPrefixText.length; i++)
		{
			for(int j = 0; j < rawRolledText.length; j++)
			{
				if(rawRolledText[j].toUpperCase().equals(rawGPrefixText[i].toUpperCase()))
				{					
					// Get number of values we're checking (some mods have more than 1 value)
					int valcnt = rawRolledVals[j].length;		
					switch(valcnt)
					{
						case 1:
							// If rolledVal is higher than or equal to GPrefixVal, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawGPrefixVals[i][0]))
								matchedGPrefix++;
							break;
						case 2:
							// If BOTH rolledVal are higher than or equal to GPrefixVals, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawGPrefixVals[i][0]) &&
									Float.parseFloat(rawRolledVals[j][1]) >= Float.parseFloat(rawGPrefixVals[i][2]))
								matchedGPrefix++;
							break;
						default:
					}
				}
			}
		}
		
		// Count Guaranteed Suffixes Hit
		for(int i = 0; i < rawGSuffixText.length; i++)
		{
			for(int j = 0; j < rawRolledText.length; j++)
			{
				if(rawRolledText[j].toUpperCase().equals(rawGSuffixText[i].toUpperCase()))
				{					
					// Get number of values we're checking (some mods have more than 1 value)
					int valcnt = rawRolledVals[j].length;		
					switch(valcnt)
					{
						case 1:
							// If rolledVal is higher than or equal to GSuffixVal, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawGSuffixVals[i][0]))
								matchedGSuffix++;
							break;
						case 2:
							// If BOTH rolledVal are higher than or equal to GSuffixVals, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawGSuffixVals[i][0]) &&
									Float.parseFloat(rawRolledVals[j][1]) >= Float.parseFloat(rawGSuffixVals[i][2]))
								matchedGSuffix++;
							break;
						default:
					}
				}
			}
		}
		
		// Count Desired Prefixes Hit
		for(int i = 0; i < rawDPrefixText.length; i++)
		{
			for(int j = 0; j < rawRolledText.length; j++)
			{
				if(rawRolledText[j].toUpperCase().equals(rawDPrefixText[i].toUpperCase()))
				{					
					// Get number of values we're checking (some mods have more than 1 value)
					int valcnt = rawRolledVals[j].length;		
					switch(valcnt)
					{
						case 1:
							// If rolledVal is higher than or equal to DPrefixVal, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawDPrefixVals[i][0]))
								matchedDPrefix++;
							break;
						case 2:
							// If BOTH rolledVal are higher than or equal to DPrefixVals, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawDPrefixVals[i][0]) &&
									Float.parseFloat(rawRolledVals[j][1]) >= Float.parseFloat(rawDPrefixVals[i][2]))
								matchedDPrefix++;
							break;
						default:
					}
				}
			}
		}
		
		// Count Desired Suffixes Hit
		for(int i = 0; i < rawDSuffixText.length; i++)
		{
			for(int j = 0; j < rawRolledText.length; j++)
			{
				if(rawRolledText[j].toUpperCase().equals(rawDSuffixText[i].toUpperCase()))
				{					
					// Get number of values we're checking (some mods have more than 1 value)
					int valcnt = rawRolledVals[j].length;		
					switch(valcnt)
					{
						case 1:
							// If rolledVal is higher than or equal to DSuffixVal, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawDSuffixVals[i][0]))
								matchedDSuffix++;
							break;
						case 2:
							// If BOTH rolledVal are higher than or equal to DSuffixVals, we have a match
							if(Float.parseFloat(rawRolledVals[j][0]) >= Float.parseFloat(rawDSuffixVals[i][0]) &&
									Float.parseFloat(rawRolledVals[j][1]) >= Float.parseFloat(rawDSuffixVals[i][2]))
								matchedDSuffix++;
							break;
						default:
					}
				}
			}
		}
		
		// Get total prefix count
		for(String mod : rawRolledText)
		{
			boolean modFound = false;
			for(int i = 0; i < Prefix1ComboBox.getItemCount(); i++)
			{
				mod = mod.toUpperCase();
				String pmod = ECStringManager.getRawModText(Prefix1ComboBox.getItemAt(i).displayText).toUpperCase();
	
				if(pmod.equals(mod))
					modFound = true;
			}
			if(modFound == true)
				prefixCnt++;
		}
		
		// Get total suffix count
		for(String mod : rawRolledText)
		{
			boolean modFound = false;
			for(int i = 0; i < Suffix1ComboBox.getItemCount(); i++)
			{
				mod = mod.toUpperCase();
				String pmod = ECStringManager.getRawModText(Suffix1ComboBox.getItemAt(i).displayText).toUpperCase();
				
				if(pmod.equals(mod))
					modFound = true;
			}
			if(modFound == true)
				suffixCnt++;
		}
		
		return getNextOrb(GPrefixCnt, GSuffixCnt, DPrefixCnt, DSuffixCnt,
				matchedGPrefix, matchedGSuffix, matchedDPrefix, matchedDSuffix, prefixCnt, suffixCnt, itemRarity);
	}
	
	/**
	 * Helper function for compareMods(...)
	 * Is the workhorse for finding which orb to use next given prefix/suffix combination
	 * @param GP is the number of guaranteed prefixes user has selected between Prefix1ComboBox and Prefix2ComboBox
	 * @param GS is the number of guaranteed suffixes user has selected between Suffix1ComboBox and Suffix2ComboBox
	 * @param DP is the number of desired prefixes user has selected on PrefixList
	 * @param DS is the number of desired suffixes user has selected on SuffixList
	 * @param mGP is the number of guaranteed prefixes the user has matched
	 * @param mGS is the number of guaranteed suffixes the user has matched
	 * @param mDP is the number of desired prefixes the user has matched
	 * @param mDS is the number of desired suffixes the user has matched
	 * @param pCnt is the number of prefixes the user has rolled on the item (matched or unmatched)
	 * @param sCnt is the number of suffixes the user has rolled on the item (matched or unmatched)
	 * @param IR is the rarity of the item, represented as an enum
	 */
	static Orb getNextOrb(int GP, int GS, int DP, int DS, int mGP, int mGS, 
		int mDP, int mDS, int pCnt, int sCnt, ItemRarity IR) {
		Orb ret = Orb.NONE;
		int retID = 0;
		
	
		try {
			switch (IR) {
			case NORMAL:
				retID = 1;
				ret = Orb.TRANSMUTATION;
				break;

			case MAGIC:
				if (pCnt == 1 && sCnt == 0) {
					if ((GP == mGP || mDP == 1) && (GS + DS == 0)) {
						retID = 2;
						ret = Orb.NONE;
						break;
					}
					if ((GP == mGP || mDP == 1) && (GS + DS >= 1)) {
						retID = 3;
						ret = Orb.AUGMENTATION;
						break;
					}
					if (GP == 2) {
						if (mGP == 1) {
							if (GS + DS >= 1) {
								retID = 4;
								ret = Orb.AUGMENTATION;
								break;
							} else {
								retID = 5;
								ret = Orb.REGAL;
								break;
							}
						} else {
							retID = 6;
							ret = Orb.ALTERATION;
							break;
						}
					}
					if (GS == 2) {
						if (mGP == 1 || mDP == 1) {
							retID = 7;
							ret = Orb.AUGMENTATION;
							break;
						} else {
							retID = 8;
							ret = Orb.ALTERATION;
							break;
						}
					}
					retID = 9;
					ret = Orb.ALTERATION;
					break;
				}
				if (pCnt == 0 && sCnt == 1) {
					if ((GS == mGS || mDS == 1) && (GP + DP == 0)) {
						retID = 10;
						ret = Orb.NONE;
						break;
					}
					if ((GS == mGS || mDS == 1) && (GP + DP >= 1)) {
						retID = 11;
						ret = Orb.AUGMENTATION;
						break;
					}
					if (GS == 2) {
						if (mGS == 1) {
							if (GP + DP >= 1) {
								retID = 12;
								ret = Orb.AUGMENTATION;
								break;
							} else {
								retID = 13;
								ret = Orb.REGAL;
								break;
							}
						} else {
							retID = 14;
							ret = Orb.ALTERATION;
							break;
						}
					}
					if (GP == 2) {
						if (mGS == 1 || mDS == 1) {
							retID = 15;
							ret = Orb.AUGMENTATION;
							break;
						} else {
							retID = 16;
							ret = Orb.ALTERATION;
							break;
						}
					}
					retID = 17;
					ret = Orb.ALTERATION;
					break;
				}
				if (pCnt == 1 && sCnt == 1) {
					if (GP == 2) {
						if (GS == 0) {
							if (mGP == 1) {
								retID = 18;
								ret = Orb.REGAL;
								break;
							} else {
								retID = 19;
								ret = Orb.ALTERATION;
								break;
							}
						}
						if (GS == 1) {
							if (mGP == 1 && mGS == 1) {
								retID = 20;
								ret = Orb.REGAL;
								break;
							} else {
								retID = 21;
								ret = Orb.ALTERATION;
								break;
							}
						}
					}
					if (GS == 2) {
						if (GP == 0) {
							if (mGS == 1) {
								retID = 22;
								ret = Orb.REGAL;
								break;
							} else {
								retID = 23;
								ret = Orb.ALTERATION;
								break;
							}
						}
						if (GP == 1) {
							if (mGP == 1 && mGS == 1) {
								retID = 24;
								ret = Orb.REGAL;
								break;
							} else {
								retID = 25;
								ret = Orb.ALTERATION;
								break;
							}
						}
					}
					if (GP == mGP && GS == mGS) {
						if ((DP >= 1 && mDP == 0) || (DS >= 1 && mDS == 0)) {
							retID = 26;
							ret = Orb.ALTERATION;
							break;
						}
						retID = 27;
						ret = Orb.NONE;
						break;
					}
				}

			case RARE:
				if ((mGP + mGS) < (GP + GS)) {
					retID = 28;
					ret = Orb.SCOUR;
					break;
				}

			default:
				retID = 29;
				ret = Orb.NONE;
				break;
			}
			
			logger.logOrb(GP, GS, DP, DS, mGP, mGS, mDP, mDS, pCnt, sCnt, IR, ret, retID);
			
		} catch (Exception e) {e.printStackTrace();}
		
		return ret;
	}
}