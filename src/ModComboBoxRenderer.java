import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/*
 * This class allows us to display a user-friendly string in the combobox
 * while allowing us to associate that string with another string that is
 * xml-friendly to perform queries.
 */
@SuppressWarnings("rawtypes")
public class ModComboBoxRenderer extends JLabel implements ListCellRenderer { 
	private static final long serialVersionUID = 1L;

	public ModComboBoxRenderer()
	{
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object obj, 
			int index, boolean isSelected, boolean cellHasFocus) {
		if(obj != null)
			setText(((ItemMod)obj).boxText);
		else
			setText("null");

        if (isSelected) {
        	if(list.isEnabled()) {
        		setBackground(list.getSelectionBackground());
        		setForeground(list.getSelectionForeground());
        	}
        	else {
        		setBackground(Color.LIGHT_GRAY);
        		setForeground(Color.DARK_GRAY);
        	}
        } 
        else {
        	if(list.isEnabled()) {
        		setBackground(Color.WHITE);
        		setForeground(list.getForeground());
        	}
        	else {
        		setBackground(Color.LIGHT_GRAY);
        		setForeground(Color.DARK_GRAY);
        	}
        }
        
		return this;
	}
}
