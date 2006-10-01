/*
 * $Id$
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import VASSAL.build.GameModule;
import VASSAL.configure.Configurer;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.StringConfigurer;

/**
 * A JButton for placing into a VASSAL component's toolbar.
 * Handles configuration of a hotkey shortcut, maintains appropriate
 * tooltip text, etc.
 */
public class LaunchButton extends JButton {
  protected String tooltipAtt;
  protected String nameAtt;
  protected String keyAtt;
  protected String iconAtt;
  protected IconConfigurer iconConfig;
  protected String toolTipText;
  protected KeyStrokeListener keyListener;
  protected Configurer nameConfig, keyConfig;

  public LaunchButton(String text, String textAttribute,
                      String hotkeyAttribute, ActionListener al) {
                      this(text,textAttribute,hotkeyAttribute,null,al);
  }

  public LaunchButton(String text, String tooltipAttribute, String textAttribute,
      String hotkeyAttribute, String iconAttribute, final ActionListener al) {
    this(text, textAttribute, hotkeyAttribute, iconAttribute, al);
    tooltipAtt = tooltipAttribute;
  }
  
  public LaunchButton(String text, String textAttribute,
                      String hotkeyAttribute, String iconAttribute, final ActionListener al) {
    super(text);
    nameAtt = textAttribute;
    keyAtt = hotkeyAttribute;
    iconAtt = iconAttribute;
    iconConfig = new IconConfigurer(iconAtt,null,null);
    setAlignmentY(0.0F);
    keyListener = new KeyStrokeListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (isEnabled() && getParent() != null && getParent().isShowing()) {
          al.actionPerformed(e);
        }
      }
    });
    GameModule.getGameModule().addKeyStrokeListener(keyListener);
    addActionListener(al);
  }

  protected void fireActionPerformed(ActionEvent event) {
    super.fireActionPerformed(event);
    GameModule.getGameModule().getChatter().getInputField().requestFocus();
  }

  public String getNameAttribute() {
    return nameAtt;
  }

  public String getHotkeyAttribute() {
    return keyAtt;
  }

  public String getIconAttribute() {
    return iconAtt;
  }
  
	public String getAttributeValueString(String key) {
    if (key.equals(nameAtt)) {
      return getText();
    }
    else if (key.equals(keyAtt)) {
      return HotKeyConfigurer.encode(keyListener.getKeyStroke());
    }
    else if (key.equals(iconAtt)) {
      return iconConfig.getValueString();
    }
    else if (key.equals(tooltipAtt)) {
      return toolTipText;
    }
    else {
      return null;
    }
  }

  public void setAttribute(String key, Object value) {
    if (key != null) {
      if (key.equals(nameAtt)) {
        setText((String) value);
        checkVisibility();
      }
      else if (key.equals(keyAtt)) {
        if (value instanceof String) {
          value = HotKeyConfigurer.decode((String) value);
        }
        keyListener.setKeyStroke((KeyStroke) value);
        setToolTipText(toolTipText);
      }
      else if (key.equals(tooltipAtt)) {
        toolTipText  = (String)value;
        setToolTipText(toolTipText);
      }
      else if (key.equals(iconAtt)) {
        if (value instanceof String) {
          iconConfig.setValue((String) value);
          setIcon(iconConfig.getIconValue());
        }
        checkVisibility();
      }
    }
  }

  public void setToolTipText(String text) {
    toolTipText = text;
    if (keyListener.getKeyStroke() != null) {
      text =
        (text == null ? "" : text + " ")
        + "[" + HotKeyConfigurer.getString(keyListener.getKeyStroke()) + "]";
    }
    super.setToolTipText(text);
  }

  public Configurer getNameConfigurer() {
    if (nameConfig == null && nameAtt != null) {
      nameConfig = new StringConfigurer(nameAtt, "Button text", getText());
    }
    return nameConfig;
  }

  public Configurer getHotkeyConfigurer() {
    if (keyConfig == null && keyAtt != null) {
      keyConfig = new HotKeyConfigurer(keyAtt, "Hotkey", keyListener.getKeyStroke());
    }
    return keyConfig;
  }

	protected void checkVisibility() {
		setVisible((getText() != null && getText().length() > 0) || getIcon() != null);
	}
}
