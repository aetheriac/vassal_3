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
package VASSAL.build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import VASSAL.configure.ValidationReport;
import VASSAL.configure.ValidityChecker;

/**
 * Abstract implementation of the Buildable interface To make a Buildable
 * component, extend this class. You'll need to implement the methods and
 * specify the Buildable attributes of this class, and the build process is
 * handled automatically.
 */
public abstract class AbstractBuildable implements Buildable, ValidityChecker {
	protected List buildComponents = new ArrayList();

	protected ValidityChecker validator; // Sub-classes can set this

	// reference to perform validity
	// checking

	/**
	 * Build this component by getting all XML attributes of the XML element and
	 * calling {@link #setAttribute} with the String value of the attribute
	 */
	public void build(org.w3c.dom.Element e) {
		if (e != null) {
			NamedNodeMap n = e.getAttributes();
			for (int i = 0; i < n.getLength(); ++i) {
				Attr att = (Attr) n.item(i);
				setAttribute(att.getName(), att.getValue());
			}
			Builder.build(e, this);
		}
	}

	/**
	 * @return a list of all attribute names for this component
	 */
	public abstract String[] getAttributeNames();

	/**
	 * Sets an attribute value for this component. The <code>key</code>
	 * parameter will be one of those listed in {@link #getAttributeNames}. If
	 * the <code>value</code> parameter is a String, it will be the value
	 * returned by {@link #getAttributeValueString} for the same
	 * <code>key</code>. If the implementing class extends
	 * {@link AbstractConfigurable}, then <code>value</code> will be an
	 * instance of the corresponding Class listed in
	 * {@link AbstractConfigurable#getAttributeTypes}
	 * 
	 * @param key
	 *            the name of the attribute. Will be one of those listed in
	 *            {@link #getAttributeNames}
	 */
	public abstract void setAttribute(String key, Object value);

	/**
	 * Return a String representation of the attribute with the given name. When
	 * initializing a module, this String value will be passed to
	 * {@link #setAttribute}.
	 * 
	 * @param key
	 *            the name of the attribute. Will be one of those listed in
	 *            {@link #getAttributeNames}
	 */
	public abstract String getAttributeValueString(String key);

	/**
	 * @return all build components that are an instance of the given class
	 */
	public Enumeration getComponents(Class target) {
		List l = new ArrayList();
		for (Iterator it = buildComponents.iterator(); it.hasNext();) {
			Object o = it.next();
			if (target.isInstance(o)) {
				l.add(o);
			}
		}
		return Collections.enumeration(l);
	}

	/**
	 * Recursively descend the build tree and return an enumeration of all
	 * components that are instances of the given class
	 * 
	 * @param target
	 * @return
	 */
	public Enumeration getAllDescendantComponents(Class target) {
		ArrayList l = new ArrayList();
		addComponents(target, l);
		return Collections.enumeration(l);
	}

	private void addComponents(Class target,
			ArrayList l) {
		if (target.isInstance(this)) {
			l.add(this);
		}
		for (Iterator it = buildComponents.iterator(); it.hasNext();) {
			Buildable b = (Buildable) it.next();
			if (target.isInstance(b)) {
				l.add(b);
			}
			else if (b instanceof AbstractBuildable) {
				((AbstractBuildable) b).addComponents(target, l);
			}
		}
	}

	public org.w3c.dom.Element getBuildElement(org.w3c.dom.Document doc) {
		Element el = doc.createElement(getClass().getName());
		String[] names = getAttributeNames();
		for (int i = 0; i < names.length; ++i) {
			String val = getAttributeValueString(names[i]);
			if (val != null) {
				el.setAttribute(names[i], val);
			}
		}
		Enumeration e = getBuildComponents();
		while (e.hasMoreElements()) {
			el.appendChild(((Buildable) e.nextElement()).getBuildElement(doc));
		}
		return el;
	}

	/**
	 * Add a Buildable object to this object
	 */
	public void add(Buildable b) {
		buildComponents.add(b);
	}

	/**
	 * Returns an enumeration of Buildable objects which are the direct children
	 * of this object in the Buildable containment hierarchy. The
	 * {@link #getBuildElement} method uses these objects to construct the XML
	 * element from which this object can be built.
	 */
	public Enumeration getBuildComponents() {
		return Collections.enumeration(buildComponents);
	}

	public void validate(Buildable target, ValidationReport report) {
		if (validator != null) {
			validator.validate(target, report);
		}
		for (Iterator it = buildComponents.iterator(); it.hasNext();) {
			Buildable child = (Buildable) it.next();
			if (child instanceof ValidityChecker) {
				((ValidityChecker) child).validate(child, report);
			}
		}
	}
}
