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
package com.intellij.ide.bookmarks;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.favoritesTreeView.AbstractFavoritesListProvider;
import com.intellij.ide.favoritesTreeView.FavoritesManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * User: Vassiliy.Kudryashov
 */
public class BookmarksFavoriteListProvider extends AbstractFavoritesListProvider<Bookmark> implements BookmarksListener {

  public BookmarksFavoriteListProvider(Project project) {
    super(project, "Bookmarks");
    project.getMessageBus().connect(project).subscribe(BookmarksListener.TOPIC, this);
    updateChildren();
  }

  @Override
  public void bookmarkAdded(@NotNull Bookmark b) {
    updateChildren();
  }

  @Override
  public void bookmarkRemoved(@NotNull Bookmark b) {
    updateChildren();
  }

  @Override
  public void bookmarkChanged(@NotNull Bookmark b) {
    updateChildren();
  }

  @Override
  public String getListName(Project project) {
    return "Bookmarks";
  }

  protected void updateChildren() {
    myChildren.clear();
    List<Bookmark> bookmarks = BookmarkManager.getInstance(myProject).getValidBookmarks();
    for (final Bookmark bookmark : bookmarks) {
      AbstractTreeNode<Bookmark> child = new AbstractTreeNode<Bookmark>(myProject, bookmark) {
        @NotNull
        @Override
        public Collection<? extends AbstractTreeNode> getChildren() {
          return Collections.emptyList();
        }

        @Override
        public boolean canNavigate() {
          return true;
        }

        @Override
        public boolean canNavigateToSource() {
          return true;
        }

        @Override
        public void navigate(boolean requestFocus) {
          bookmark.navigate();
        }

        @Override
        protected void update(PresentationData presentation) {
          presentation.setPresentableText(bookmark.toString());
          presentation.setIcon(bookmark.getIcon());
        }
      };
      child.setParent(myNode);
      myChildren.add(child);
    }
    FavoritesManager.getInstance(myProject).fireListeners(getListName(myProject));
  }

  @Nullable
  @Override
  public String getCustomName(@NotNull CommonActionsPanel.Buttons type) {
    switch (type) {
      case EDIT:
        return IdeBundle.message("action.bookmark.edit.description");
      case REMOVE:
        return IdeBundle.message("action.bookmark.delete");
      default:
        return null;
    }
  }

  @Override
  public boolean willHandle(@NotNull CommonActionsPanel.Buttons type, Project project, @NotNull Set<Object> selectedObjects) {
    switch (type) {
      case EDIT:
        if (selectedObjects.size() != 1) {
          return false;
        }
        Object toEdit = selectedObjects.iterator().next();
        return toEdit instanceof AbstractTreeNode && ((AbstractTreeNode)toEdit).getValue() instanceof Bookmark;
      case REMOVE:
        for (Object toRemove : selectedObjects) {
          if (!(toRemove instanceof AbstractTreeNode && ((AbstractTreeNode)toRemove).getValue() instanceof Bookmark)) {
            return false;
          }
        }
        return true;
      default:
        return false;
    }
  }

  @Override
  public void handle(@NotNull CommonActionsPanel.Buttons type, Project project, @NotNull Set<Object> selectedObjects) {
    switch (type) {
      case EDIT:

        if (selectedObjects.size() != 1) {
          return;
        }
        Object toEdit = selectedObjects.iterator().next();
        if (toEdit instanceof AbstractTreeNode && ((AbstractTreeNode)toEdit).getValue() instanceof Bookmark) {
          Bookmark bookmark = (Bookmark)((AbstractTreeNode)toEdit).getValue();
          if (bookmark == null) {
            return;
          }
          BookmarkManager.getInstance(project).editDescription(bookmark);
        }
        return;
      case REMOVE:
        for (Object toRemove : selectedObjects) {
          Bookmark bookmark = (Bookmark)((AbstractTreeNode)toRemove).getValue();
          BookmarkManager.getInstance(project).removeBookmark(bookmark);
        }
        return;
      default: {
      }
    }
  }

  @Override
  public void customizeRenderer(ColoredTreeCellRenderer renderer,
                                JTree tree,
                                @NotNull Object value,
                                boolean selected,
                                boolean expanded,
                                boolean leaf,
                                int row,
                                boolean hasFocus) {
    renderer.clear();
    renderer.setIcon(Bookmark.DEFAULT_ICON);
    if (value instanceof Bookmark) {
      Bookmark bookmark = (Bookmark)value;
      BookmarkItem.setupRenderer(renderer, myProject, bookmark, selected);
      if (renderer.getIcon() != null) {
        renderer.setIcon(LayeredIcon.createHorizontalIcon(bookmark.getIcon(), renderer.getIcon()));
      }
      else {
        renderer.setIcon(bookmark.getIcon());
      }
    }
    else {
      renderer.append(getListName(myProject));
    }
  }
}
