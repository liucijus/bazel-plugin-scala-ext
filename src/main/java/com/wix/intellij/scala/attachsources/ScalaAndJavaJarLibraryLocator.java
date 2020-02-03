package com.wix.intellij.scala.attachsources;

import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.LibraryKey;
import com.google.idea.blaze.base.projectview.ProjectViewManager;
import com.google.idea.blaze.base.projectview.ProjectViewSet;
import com.google.idea.blaze.base.sync.libraries.BlazeLibraryCollector;
import com.google.idea.blaze.java.sync.model.BlazeJarLibrary;
import com.google.idea.blaze.java.sync.model.BlazeJavaSyncData;
import com.google.idea.blaze.scala.sync.model.BlazeScalaSyncData;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import javax.annotation.Nullable;

class ScalaAndJavaJarLibraryLocator {

  private static final Logger logger = Logger.getInstance(ScalaAndJavaJarLibraryLocator.class);

  static BlazeJarLibrary findLibraryFromIntellijLibrary(
      Project project, BlazeProjectData blazeProjectData, Library library) {

    String libName = library.getName();
    if (libName == null) {
      return null;
    }
    LibraryKey libraryKey = LibraryKey.fromIntelliJLibraryName(libName);

    BlazeJarLibrary scalaSynced = findScalaSynced(project, blazeProjectData, libraryKey);

    if (scalaSynced != null) {
      return scalaSynced;
    }

    BlazeJarLibrary javaSynced = findJavaSynced(project, blazeProjectData, libraryKey);

    if (javaSynced != null) {
      return javaSynced;
    }

    BlazeJarLibrary blazeJarLibrary = fromLibraryCollector(project, blazeProjectData, libraryKey);

    if (blazeJarLibrary != null) {
      logger.warn("Library not found in sync data: " + blazeJarLibrary);
    }

    return blazeJarLibrary;
  }

  @Nullable
  private static BlazeJarLibrary fromLibraryCollector(
      Project project, BlazeProjectData blazeProjectData, LibraryKey libraryKey) {

    ProjectViewSet projectViewSet = ProjectViewManager.getInstance(project).getProjectViewSet();
    if (projectViewSet == null) {
      return null;
    }

    return (BlazeJarLibrary) BlazeLibraryCollector.getLibraries(projectViewSet, blazeProjectData)
        .stream()
        .filter(lib -> lib.key.equals(libraryKey))
        .findFirst()
        .orElse(null);
  }

  @Nullable
  private static BlazeJarLibrary findJavaSynced(
      Project project, BlazeProjectData blazeProjectData, LibraryKey libraryKey) {

    BlazeJavaSyncData syncData = blazeProjectData.getSyncState().get(BlazeJavaSyncData.class);

    if (syncData == null) {
      Messages.showErrorDialog(project, "Project isn't synced. Please resync project.", "Error");
      return null;
    }

    return syncData.getImportResult().libraries.get(libraryKey);
  }

  @Nullable
  private static BlazeJarLibrary findScalaSynced(
      Project project, BlazeProjectData blazeProjectData, LibraryKey libraryKey) {

    BlazeScalaSyncData syncData = blazeProjectData.getSyncState().get(BlazeScalaSyncData.class);

    if (syncData == null) {
      Messages.showErrorDialog(project, "Project isn't synced. Please resync project.", "Error");
      return null;
    }

    return syncData.getImportResult().libraries.get(libraryKey);
  }

}
