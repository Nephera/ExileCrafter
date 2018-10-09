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
public class ComboBoxRenderer extends JLabel implements ListCellRenderer{
	private static final long serialVersionUID = 1L;

	public ComboBoxRenderer()
	{
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object obj, 
			int index, boolean isSelected, boolean cellHasFocus){
		if(obj != null && ((ItemType)obj).displayName != null)
		setText(((ItemType)obj).displayName);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
		return this;
	}
}
