/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.internal.mandatory;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWiki;

/**
 * Update XWiki.XWikiRights document with all required informations.
 *
 * @version $Id$
 * @since 4.3M1
 */
@Component
@Named(XWikiRightsDocumentInitializer.CLASS_REFERENCE_STRING)
@Singleton
public class XWikiRightsDocumentInitializer extends AbstractRightsDocumentInitializer
{
    /**
     * The name of the document containing the class.
     * 
     * @since 11.7RC1
     * @since 10.11.10
     */
    public static final String CLASS_NAME = "XWikiRights";

    /**
     * The local reference of the class as a String.
     * 
     * @since 11.7RC1
     * @since 10.11.10
     */
    public static final String CLASS_REFERENCE_STRING = XWiki.SYSTEM_SPACE + '.' + CLASS_NAME;

    /**
     * The local reference of the class as a String.
     * 
     * @since 11.7RC1
     * @since 10.11.10
     */
    public static final LocalDocumentReference CLASS_REFERENCE =
        new LocalDocumentReference(XWiki.SYSTEM_SPACE, CLASS_NAME);

    /**
     * Default constructor.
     */
    public XWikiRightsDocumentInitializer()
    {
        super(CLASS_NAME);
    }
}
