/*
 * $Id$
 *
 * Copyright (c) 2004 by Michael Blumoehr
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
package VASSAL.build.module;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.AutoConfigurer;
import VASSAL.configure.ConfigurerWindow;
import VASSAL.configure.StringArrayConfigurer;
import VASSAL.configure.VisibilityCondition;
import VASSAL.tools.LaunchButton;

/**
 * @author Michael Blumoehr
 *
 * This component places a button into the controls window toolbar.
 * Pressing the button generates random numbers or strings and displays the
 * result in the Chatter */
// TODO Expose result as a property
public class RandomTextButton extends DiceButton {
  private String[] m_faces;               // array with dice faces
  private boolean isNumeric;

  public static final String FACES = "faces";
  public static final String NUMERIC = "numeric";

  public RandomTextButton() {
    super();
    ActionListener ranAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (promptAlways) {
          promptAlways = false; // Show the usu
          // Remove label, hotkey, and prompt controls
          AutoConfigurer ac = (AutoConfigurer) getConfigurer();
          ConfigurerWindow w = new ConfigurerWindow(ac, true);
          ac.getConfigurer(NAME).getControls().setVisible(false);
          ac.getConfigurer(BUTTON_TEXT).getControls().setVisible(false);
          ac.getConfigurer(ICON).getControls().setVisible(false);
          ac.getConfigurer(HOTKEY).getControls().setVisible(false);
          ac.getConfigurer(PROMPT_ALWAYS).getControls().setVisible(false);
          ac.getConfigurer(REPORT_FORMAT).getControls().setVisible(false);
          ac.getConfigurer(REPORT_TOTAL).getControls().setVisible(false);
          ac.getConfigurer(FACES).getControls().setVisible(false);
          ac.getConfigurer(NUMERIC).getControls().setVisible(false);
          w.pack();
          w.setVisible(true);
          ac.getConfigurer(NAME).getControls().setVisible(true);
          ac.getConfigurer(BUTTON_TEXT).getControls().setVisible(true);
          ac.getConfigurer(ICON).getControls().setVisible(true);
          ac.getConfigurer(HOTKEY).getControls().setVisible(true);
          ac.getConfigurer(PROMPT_ALWAYS).getControls().setVisible(true);
          ac.getConfigurer(REPORT_FORMAT).getControls().setVisible(true);
          ac.getConfigurer(REPORT_TOTAL).getControls().setVisible(true);
          ac.getConfigurer(FACES).getControls().setVisible(true);
          ac.getConfigurer(NUMERIC).getControls().setVisible(true);
          DR();
          promptAlways = true;
        }
        else {
          DR();
        }
      }
    };
    launch = new LaunchButton(null, BUTTON_TEXT, HOTKEY, ICON, ranAction);
  }
  
  public static String getConfigureTypeName() {
    return "Random Text Button";
  }

  /**
   * Forwards the result of the roll to the {@link Chatter#send}
   * method of the {@link Chatter} of the {@link GameModule}.  Format is
   * prefix+[comma-separated roll list]+suffix */
  protected void DR() {
    StringBuffer result = new StringBuffer();
    int total = 0;
    for (int i = 0; i < nDice; ++i) {
      int roll = (int) (ran.nextFloat() * nSides + 1);

      // take the face value from user defined faces
      if (isNumeric) {
        roll = Integer.parseInt(m_faces[roll - 1]) + plus;
      }

      // no totals if text output
      if (reportTotal && isNumeric) {
        total += roll;
      }
      else {
        if (!isNumeric)
          result.append(m_faces[roll - 1]);
        else
          result.append(roll);
        if (i < nDice - 1)
          result.append(",");
      }
    }

    // totals only if no text output
    if (reportTotal && isNumeric)
      result.append(total);

    String msg = formatResult(result.toString());
    GameModule.getGameModule().getChatter().send(msg);
  }

  public VisibilityCondition getAttributeVisibility(String name) {
    if (REPORT_TOTAL.equals(name)
        || PLUS.equals(name)) {
      return new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return isNumeric;
        }
      };
    }
    else {
      return super.getAttributeVisibility(name);
    }
  }


  /**
   * The additional Attributes of a RandomTextButton are:
   *
   * <code>FACES</code> Text of the dice faces
   *                    must be integer if USE_FACES=NUMERIC

   * <code>NUMERIC</code>   If true, then face text must be an integer,
   *  and reportTotal is enabled
   */
  public String[] getAttributeNames() {
    ArrayList l = new ArrayList(Arrays.asList(super.getAttributeNames()));
    l.remove(N_SIDES);
    l.add(FACES);
    l.add(NUMERIC);
    return (String[]) l.toArray(new String[l.size()]);
  }

  public String[] getAttributeDescriptions() {
    ArrayList l = new ArrayList(Arrays.asList(super.getAttributeDescriptions()));
    ArrayList names = new ArrayList(Arrays.asList(super.getAttributeNames()));
    l.remove(names.indexOf(N_SIDES));
    l.add("Faces");
    l.add("Faces have numeric values");
    return (String[]) l.toArray(new String[l.size()]);
  }

  public Class[] getAttributeTypes() {
    ArrayList l = new ArrayList(Arrays.asList(super.getAttributeTypes()));
    ArrayList names = new ArrayList(Arrays.asList(super.getAttributeNames()));
    l.remove(names.indexOf(N_SIDES));
    l.add(String[].class);
    l.add(Boolean.class);
    return (Class[]) l.toArray(new Class[names.size()]);
  }

  public void setAttribute(String key, Object value) {
    if (NUMERIC.equals(key)) {
      isNumeric = Boolean.TRUE.equals(value) || "true".equals(value);
    }
    else if (FACES.equals(key)) {
      if (value instanceof String) {
        value = StringArrayConfigurer.stringToArray((String) value);
      }
      m_faces = (String[]) value;
      nSides = m_faces.length;
    }
    else {
      super.setAttribute(key, value);
    }
  }

  public String getAttributeValueString(String key) {
    if (NUMERIC.equals(key)) {
      return "" + isNumeric;
    }
    else if (FACES.equals(key)) {
      return StringArrayConfigurer.arrayToString(m_faces);
    }
    else {
      return super.getAttributeValueString(key);
    }
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "GameModule.htm"), "#RandomTextButton");
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }


}
