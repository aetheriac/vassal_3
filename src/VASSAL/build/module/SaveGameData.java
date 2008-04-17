/*
 * $Id: SaveGameData.java 3423 2008-04-13 21:51:32Z swampwallaby $
 *
 * Copyright (c) 2008 by Brent Easton
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

import javax.xml.parsers.DocumentBuilder;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
  public static final String VERSION_ATTR = "version";
  public static final String MODULE_NODE = "module";
  public static final String NAME_ENTRY = "name";
  public static final String VERSION_ENTRY = "version";
  public static final String COMMENTS_NODE = "comments";

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
    Document xmldoc = null;
    Element e = null;
    Node n = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      DOMImplementation impl = builder.getDOMImplementation();

      xmldoc = impl.createDocument(null, ROOT_NODE, null);
      Element root = xmldoc.getDocumentElement();
      root.setAttribute(VERSION_ATTR, SAVE_VERSION);
      Element module = xmldoc.createElementNS(null, MODULE_NODE);
      root.appendChild(module);
      e = xmldoc.createElementNS(null, NAME_ENTRY);
      module.appendChild(e);
      n = xmldoc.createTextNode(moduleName);
      e.appendChild(n);
      e = xmldoc.createElementNS(null, VERSION_ENTRY);
      module.appendChild(e);
      n = xmldoc.createTextNode(moduleVersion);
      e.appendChild(n);

      Element comments = xmldoc.createElementNS(null, COMMENTS_NODE);
      root.appendChild(comments);
      n = xmldoc.createTextNode(getComments());
      comments.appendChild(n);
    }
    catch (ParserConfigurationException ex) {
      throw new IOException(ex.getMessage());
    }

    final BridgeStream out = new BridgeStream();
    final Source source = new DOMSource(xmldoc);
    final Result result = new StreamResult(out);
    Transformer xformer;
    try {
      xformer = TransformerFactory.newInstance().newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
          "2");
      xformer.transform(source, result);
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
    return;
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
    comments = "";
    moduleName = "";
    moduleVersion = "";
    ZipFile zip = null;
    InputStream is = null;
    setValid(true);

    /*
     * This may be an old-style saved game with no metadata. Check that it is a
     * valid Zip file and has a 'savedgame' entry.
     */
    try {
      zip = new ZipFile(file);
      @SuppressWarnings("unused")
      // Just checking the savedgame exists
      final ZipEntry save = zip.getEntry(GameState.SAVEFILE_ZIP_ENTRY);
    }
    catch (ZipException e) {
      setValid(false);
    }
    catch (IOException e) {
      setValid(false);
    }
    finally {
      if (!isValid() && zip != null) {
        try {
          zip.close();
        }
        catch (Exception e) {

        }
      }
    }

    if (!isValid()) {
      return;
    }

    /*
     * Try and parse the metadata. Failure is not catastrophic, we can treat it
     * like an old-style save with no metadata
     */
    try {
      final ZipEntry data = zip.getEntry(GameState.SAVEFILE_METADATA_ENTRY);
      if (data != null) {
        is = zip.getInputStream(data);
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
            .newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document metadata = docBuilder.parse(is);
        metadata.getDocumentElement().normalize();

        NodeList rootList = metadata.getElementsByTagName(ROOT_NODE);
        if (rootList.getLength() == 0) {
          return;
        }
        String metaDataVersion = ((Element) rootList.item(0)).getAttribute(VERSION_ATTR);
        if (!SAVE_VERSION.equals(metaDataVersion)) {
          // Later versions may require to do conversion
        }
        
        NodeList nodes = metadata.getElementsByTagName(MODULE_NODE);
        Node node = nodes.item(0);
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
          Element moduleElement = (Element) node;
          NodeList nl = null;

          NodeList entryList = moduleElement.getElementsByTagName(NAME_ENTRY);
          Element entry = (Element) entryList.item(0);
          if (entry != null) {
            nl = entry.getChildNodes();
            if (nl.getLength() > 0) {
              moduleName = ((Node) nl.item(0)).getNodeValue();
            }
          }

          entryList = moduleElement.getElementsByTagName(VERSION_ENTRY);
          entry = (Element) entryList.item(0);
          if (entry != null) {
            nl = entry.getChildNodes();
            if (nl.getLength() > 0) {
              moduleVersion = ((Node) nl.item(0)).getNodeValue();
            }
          }
        }

        nodes = metadata.getElementsByTagName(COMMENTS_NODE);
        node = nodes.item(0);
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
          Element commentElement = (Element) node;
          NodeList nl = commentElement.getChildNodes();
          if (nl.getLength() > 0) {
            comments = ((Node) nl.item(0)).getNodeValue();
          }
        }
      }
    }
    catch (IOException e) {

    }
    catch (ParserConfigurationException e) {

    }
    catch (SAXException e) {

    }

    finally {
      try {
        if (zip != null) {
          zip.close();
        }
      }
      catch (Exception e) {

      }
      try {
        if (is != null) {
          is.close();
        }
      }
      catch (Exception e) {

      }
    }
  }
}
