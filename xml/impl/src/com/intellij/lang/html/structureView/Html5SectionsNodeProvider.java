/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.html.structureView;

import com.intellij.ide.util.FileStructureNodeProvider;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.util.PropertyOwner;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class Html5SectionsNodeProvider implements FileStructureNodeProvider<Html5SectionTreeElement>, PropertyOwner {

  public static final String ACTION_ID = "HTML5_OUTLINE_MODE";
  public static final String HTML5_OUTLINE_PROVIDER_PROPERTY = "html5.sections.node.provider";

  @NotNull
  public String getName() {
    return ACTION_ID;
  }

  @NotNull
  public ActionPresentation getPresentation() {
    return new ActionPresentationData(XmlBundle.message("html5.outline.mode"), null, null);
  }

  public String getCheckBoxText() {
    return XmlBundle.message("html5.outline.mode");
  }

  public Shortcut[] getShortcut() {
    return KeymapManager.getInstance().getActiveKeymap().getShortcuts("FileStructurePopup");
  }

  @NotNull
  public String getPropertyName() {
    return HTML5_OUTLINE_PROVIDER_PROPERTY;
  }

  public Collection<Html5SectionTreeElement> provideNodes(final TreeElement node) {
    if (!(node instanceof HtmlFileTreeElement)) return Collections.emptyList();

    final XmlFile xmlFile = ((HtmlFileTreeElement)node).getElement();
    final XmlTag rootTag = xmlFile == null ? null : xmlFile.getRootTag();
    if (rootTag == null) return Collections.emptyList();

    return Html5SectionsProcessor.processAndGetRootSections(rootTag);
  }
}
