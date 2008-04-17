/*
 * $Id: SaveGameData.java 3423 2008-04-13 21:51:32Z swampwallaby $
 *
 * Copyright (c) 2008 by Brent Easton and Joel Uckelman
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import VASSAL.build.GameModule;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.BridgeStream;

/**
 * 
 * Class representing the metadata for a Save Game/Log File
 * 
 * @author Brent Easton
 * @since 3.1.0
 *
 */
public class SaveGameData {

  public static final String ZIP_ENTRY_NAME = "metadata";
  public static final String SAVE_VERSION = "1.0";
  public static final String ROOT_NODE = "data";
  public static final String MODULE_NODE = "module";
  public static final String NAME_ENTRY = "name";
  public static final String VERSION_ENTRY = "version";
  public static final String COMMENTS_NODE = "comments";

  public static final String NAME_ATTR = "name";
  public static final String VERSION_ATTR = "version";

  public static final String ROOT_ELEMENT = "data";
  public static final String MODULE_ELEMENT = "module";
  public static final String COMMENTS_ELEMENT = "comments";

  protected boolean valid = false;
  protected String moduleName;
  protected String moduleVersion;
  protected String comments;

  public SaveGameData() {
    moduleName = GameModule.getGameModule().getGameName();
    moduleVersion = GameModule.getGameModule().getGameVersion();
  }
  
  public SaveGameData(String comments) {
    this();
    this.comments = comments;
  }

  public SaveGameData(File file) {
    read(file);
  }

  public String getModuleName() {
    return moduleName;
  }

  public String getModuleVersion() {
    return moduleVersion;
  }
  
  public String getComments() {
    return comments;
  }

  /*
   * valid indicates whether or not this represents the metadata for a valid
   * save file. valid = true means the save file was a valid Zip archive
   * containing an entry named 'savedgame'. If the metadata entry is missing or
   * corrupt, valid will still be true, but the metadata will be set to blanks.
   * 
   */
  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean b) {
    valid = b;
  }

  /**
   * Write Save Game metadata to the specified Archive
   * @param archive Save game Archive
   * @throws IOException If anything goes wrong
   */
  public void save(ArchiveWriter archive) throws IOException {
    Document doc = null;
    Element e = null;
    Node n = null;
    try {
      doc = DocumentBuilderFactory.newInstance()
                                  .newDocumentBuilder()
                                  .newDocument();
      
      final Element root = doc.createElement(ROOT_ELEMENT);
      root.setAttribute(VERSION_ATTR, SAVE_VERSION);
      doc.appendChild(root);

      final Element module = doc.createElement(MODULE_ELEMENT);
      module.setAttribute(NAME_ATTR, moduleName);
      module.setAttribute(VERSION_ATTR, moduleVersion);
      root.appendChild(module);

      final Element comments = doc.createElement(COMMENTS_ELEMENT);
      comments.appendChild(doc.createTextNode(getComments()));
      root.appendChild(comments);
    }
    catch (ParserConfigurationException ex) {
      throw new IOException(ex.getMessage());
    }

    final BridgeStream out = new BridgeStream();
    try {
      final Transformer xformer =
        TransformerFactory.newInstance().newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
          "2");
      xformer.transform(new DOMSource(doc), new StreamResult(out));
    }
    catch (TransformerConfigurationException ex) {
      throw new IOException(ex.getMessage());
    }
    catch (TransformerFactoryConfigurationError ex) {
      throw new IOException(ex.getMessage());
    }
    catch (TransformerException ex) {
      throw new IOException(ex.getMessage());
    }

    archive.addFile(ZIP_ENTRY_NAME, out.toInputStream());
  }

  /**
   * Read and validate a Saved Game/Log file.
   *  - Check it is a Zip file
   *  - Check it has a Zip Entry named savedgame
   *  - If it has a metadata file, read and parse it.
   *  - 
   * @param file Saved Game File
   */
  public void read(File file) {
    comments = moduleName = moduleVersion = "";
    setValid(true);

    ZipFile zip = null;
    InputStream is = null;
    try {
      try {
        zip = new ZipFile(file);

        // This may be an old-style saved game with no metadata. Check that
        // it is a valid zip file and has a 'savedgame' entry.
        if (zip.getEntry(GameState.SAVEFILE_ZIP_ENTRY) == null) {
          throw new IOException("Not a valid saved game."); 
        }  
      }
      catch (ZipException e) {
        // print no stack trace, this is likely not a zip file
        setValid(false);
        return;
      }
      catch (IOException e) {
        e.printStackTrace();
        setValid(false);
        return;
      }

      // Try to parse the metadata. Failure is not catastrophic, we can
      // treat it like an old-style save with no metadata.
      try {
        final ZipEntry data = zip.getEntry(GameState.SAVEFILE_METADATA_ENTRY);
        if (data == null) return;

        final XMLReader parser = XMLReaderFactory.createXMLReader();

        // set up the handler
        final XMLHandler handler = new XMLHandler();
        parser.setContentHandler(handler);
        parser.setDTDHandler(handler);
        parser.setEntityResolver(handler);
        parser.setErrorHandler(handler);

        // parse! parse!
        is = zip.getInputStream(data);
        parser.parse(new InputSource(is));
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      catch (SAXException e) {
        e.printStackTrace();
      }
    }
    finally {
      if (zip != null) {
        try {
          zip.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class XMLHandler extends DefaultHandler {
    final StringBuilder accumulator = new StringBuilder();

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes attrs) {
      // clear the content accumulator
      accumulator.setLength(0);

      // handle element attributes we care about
      if (MODULE_ELEMENT.equals(qName)) {
        moduleName = getAttr(attrs, NAME_ATTR);
        moduleVersion = getAttr(attrs, VERSION_ATTR);
      }
/*
      else if (VASSAL_ELEMENT.equals(localName)) {
        vassalVersion = attrs.getName(VERSION_ATTR);
      }
*/
    }

    private String getAttr(Attributes attrs, String qName) {
      final String value = attrs.getValue(qName);
      return value == null ? "" : value;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      // handle all of the elements which have CDATA here
      if (COMMENTS_ELEMENT.equals(qName)) {
        comments = accumulator.toString().trim();
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
      accumulator.append(ch, start, length);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      e.printStackTrace();
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
      e.printStackTrace();
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      throw e;
    }
  }
}
