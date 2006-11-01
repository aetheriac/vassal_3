package VASSAL.build.module.gamepieceimage;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import org.w3c.dom.Element;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;
import VASSAL.configure.SingleChildInstance;

/*
 * $Id$
 *
 * Copyright (c) 2005 by Rodney Kinney, Brent Easton
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

/**
 * Container for definitions of Generic Color Definitions
 */
public class FontManager extends AbstractConfigurable {

  protected static FontManager instance;

  public static FontManager getFontManager() {
    return instance;
  }
    
  protected HashMap fontStyles = new HashMap();

  public static final String DIALOG = "Dialog";
  public static final String SERIF = "Serif";
  public static final String SANS_SERIF = "SanSerif";
  public static final String DIALOG_INPUT = "DialogInput";
  public static final String MONOSPACED = "Monospaced";
  
  public static final String DEFAULT = "Default";
  public static final OutlineFont DEFAULT_FONT = new OutlineFont(DIALOG, Font.PLAIN, 12, false);
  public static final FontStyle DEFAULT_STYLE = new FontStyle();

//  public static final String[] ALLOWABLE_FONTS = new String[] { DIALOG, DIALOG_INPUT, MONOSPACED, SANS_SERIF, SERIF };
  public static final String[] ALLOWABLE_FONTS = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
  
  public FontManager() {
  }
  
  public void build(Element e) {
    super.build(e);

    if (fontStyles.get(DEFAULT) == null) {
      addChild(new FontStyle(DEFAULT, DEFAULT_FONT));
    }
  }

  private void addChild(Buildable b) {
    add(b);
    b.addTo(this);
  }

  protected FontStyle getFontStyle(String name) {
    FontStyle fs = (FontStyle) fontStyles.get(name);
    return fs == null ? DEFAULT_STYLE : fs;
  }

  public String[] getAttributeDescriptions() {
    return new String[0];
  }

  public Class[] getAttributeTypes() {
    return new Class[0];
  }

  public String[] getAttributeNames() {
    return new String[0];
  }

  public String getAttributeValueString(String key) {
    return null;
  }

  public void setAttribute(String key, Object value) {
  }

  public Configurer getConfigurer() {
    return null;
  }

  public void addTo(Buildable parent) {
    validator = new SingleChildInstance(GameModule.getGameModule(), getClass());
    instance = this;
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[] { FontStyle.class };
  }

  public static String getConfigureTypeName() {
    return "Font Styles";
  }

  public void add(Buildable b) {
    super.add(b);
    if (b instanceof FontStyle) {
      FontStyle def = (FontStyle) b;
      fontStyles.put(def.getConfigureName(), def);
      def.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (Configurable.NAME_PROPERTY.equals(evt.getPropertyName())) {
            fontStyles.remove(evt.getOldValue());
            fontStyles.put(evt.getNewValue(), evt.getSource());
          }
        }
      });
    }
  }

  public void remove(Buildable b) {
    super.remove(b);
    if (b instanceof ColorSwatch) {
      fontStyles.remove(((ColorSwatch) b).getConfigureName());
    }
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("GamePieceImageDefinitions.htm","FontStyles");
  }

  public void removeFrom(Buildable parent) {
  }

  public String[] getFontNames() {
    String[] names = new String[fontStyles.size()];
    Iterator i = fontStyles.values().iterator();
    int j = 0;
    while (i.hasNext()) {
      names[j++] = ((FontStyle) i.next()).getConfigureName();
    }
    return names;
  }

}
