// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.projectview;

import com.intellij.icons.AllIcons;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.AbstractProjectViewPSIPane;
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase;
import com.intellij.ide.projectView.impl.ProjectTreeStructure;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeUpdater;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.twitter.intellij.pants.PantsBundle;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProjectFilesViewPane extends AbstractProjectViewPSIPane {
  @NonNls public static final String ID = "ProjectFilesPane";
  public static final String SHOW_EXCLUDED_FILES_OPTION = "show-excluded-files";
  public static final String SHOW_ONLY_LOADED_FILES_OPTION = "show-only-loaded-files";
  private boolean myShowExcludedFiles = true;
  private boolean myShowOnlyLoadedFiles = true;

  public ProjectFilesViewPane(Project project) {
    super(project);
  }

  @Override
  public String getTitle() {
    return PantsBundle.message("pants.title.project.files");
  }

  @Override
  public Icon getIcon() {
    return AllIcons.General.ProjectTab;
  }

  @Override
  @NotNull
  public String getId() {
    return ID;
  }

  @Override
  public int getWeight() {
    // There are 5 project view panels with ids from 1 to 5.
    // There is an assertion that id should be unique. See ProjectViewImpl.java:441
    // Picked an id that I like. :-)
    return 239;
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    final String showExcludedOption = JDOMExternalizerUtil.readField(element, SHOW_EXCLUDED_FILES_OPTION);
    myShowExcludedFiles = showExcludedOption == null || Boolean.parseBoolean(showExcludedOption);

    final String showOnlyLoadedOption = JDOMExternalizerUtil.readField(element, SHOW_ONLY_LOADED_FILES_OPTION);
    myShowOnlyLoadedFiles = showOnlyLoadedOption == null || Boolean.parseBoolean(showOnlyLoadedOption);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizerUtil.writeField(element, SHOW_EXCLUDED_FILES_OPTION, String.valueOf(myShowExcludedFiles));
    JDOMExternalizerUtil.writeField(element, SHOW_ONLY_LOADED_FILES_OPTION, String.valueOf(myShowOnlyLoadedFiles));
  }

  @Override
  public void addToolbarActions(DefaultActionGroup actionGroup) {
    actionGroup.addAction(new ShowExcludedFilesAction()).setAsSecondary(true);
    actionGroup.addAction(new ShowOnlyLoadedFilesAction()).setAsSecondary(true);
  }

  @Override
  protected ProjectAbstractTreeStructureBase createStructure() {
    return new ProjectViewPaneTreeStructure();
  }

  @Override
  protected ProjectViewTree createTree(DefaultTreeModel treeModel) {
    return new ProjectViewTree(myProject, treeModel) {
      @Override
      public DefaultMutableTreeNode getSelectedNode() {
        return ProjectFilesViewPane.this.getSelectedNode();
      }
    };
  }

  @Override
  protected AbstractTreeUpdater createTreeUpdater(AbstractTreeBuilder treeBuilder) {
    return new AbstractTreeUpdater(treeBuilder);
  }

  @Override
  public SelectInTarget createSelectInTarget() {
    return new PantsProjectPaneSelectInTarget(myProject);
  }

  private class ProjectViewPaneTreeStructure extends ProjectTreeStructure implements PantsViewSettings {
    public ProjectViewPaneTreeStructure() {
      super(ProjectFilesViewPane.this.myProject, ID);
    }

    @Override
    protected AbstractTreeNode createRoot(final Project project, ViewSettings settings) {
      return new ProjectFilesViewProjectNode(project, settings);
    }

    @Override
    public boolean isShowExcludedFiles() {
      return myShowExcludedFiles;
    }

    @Override
    public boolean isShowOnlyLoadedFiles() {
      return myShowOnlyLoadedFiles;
    }
  }

  private final class ShowExcludedFilesAction extends ToggleAction {
    private ShowExcludedFilesAction() {
      super(
        PantsBundle.message("pants.action.show.excluded.files"),
        PantsBundle.message("pants.action.show.hide.excluded.files"),
        null
      );
    }

    @Override
    public boolean isSelected(AnActionEvent event) {
      return myShowExcludedFiles;
    }

    @Override
    public void setSelected(AnActionEvent event, boolean flag) {
      if (myShowExcludedFiles != flag) {
        myShowExcludedFiles = flag;
        updateFromRoot(true);
      }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      super.update(e);
      final Presentation presentation = e.getPresentation();
      final ProjectView projectView = ProjectView.getInstance(myProject);
      presentation.setEnabledAndVisible(projectView.getCurrentProjectViewPane() == ProjectFilesViewPane.this);
    }
  }

  private final class ShowOnlyLoadedFilesAction extends ToggleAction {
    private ShowOnlyLoadedFilesAction() {
      super(
        PantsBundle.message("pants.action.show.only.loaded.files"),
        PantsBundle.message("pants.action.show.hide.only.loaded.files"),
        null
      );
    }

    @Override
    public boolean isSelected(AnActionEvent event) {
      return myShowOnlyLoadedFiles;
    }

    @Override
    public void setSelected(AnActionEvent event, boolean flag) {
      if (myShowOnlyLoadedFiles != flag) {
        myShowOnlyLoadedFiles = flag;
        updateFromRoot(true);
      }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      super.update(e);
      final Presentation presentation = e.getPresentation();
      final ProjectView projectView = ProjectView.getInstance(myProject);
      presentation.setEnabledAndVisible(projectView.getCurrentProjectViewPane() == ProjectFilesViewPane.this);
    }
  }
}
