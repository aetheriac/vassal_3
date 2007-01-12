package VASSAL.build.module;

import java.awt.Component;
import javax.swing.JMenuItem;

/**
 * Combines multiple buttons from the toolbar into a single button. Pushing the single button is equivalent to pushing
 * the other buttons in order.
 * 
 * @author rkinney
 * 
 */
public class MultiActionButton extends ToolbarMenu {

	public MultiActionButton() {
		super();
		setAttribute(BUTTON_TEXT, "Multi-Action");
	}
	
  public String[] getAttributeDescriptions() {
    return new String[] {"Button text", "Button Icon", "Hotkey", "Buttons"};
  }

	public void launch() {
		for (int i=0,n=menu.getComponentCount();i<n;++i) {
			Component c = menu.getComponent(i);
			if (c instanceof JMenuItem) {
				((JMenuItem)c).doClick();
			}
		}
	}
	
  public static String getConfigureTypeName() {
    return "Multi-Action Button";
  }
}
