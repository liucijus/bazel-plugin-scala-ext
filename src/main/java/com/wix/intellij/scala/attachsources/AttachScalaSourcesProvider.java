package com.wix.intellij.scala.attachsources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.idea.blaze.base.ideinfo.LibraryArtifact;
import com.google.idea.blaze.base.model.BlazeLibrary;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.LibraryKey;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.google.idea.blaze.base.sync.libraries.LibraryEditor;
import com.google.idea.blaze.java.libraries.AttachedSourceJarManager;
import com.google.idea.blaze.java.sync.model.BlazeJarLibrary;
import com.google.idea.common.transactions.Transactions;
import com.intellij.codeInsight.AttachSourcesProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.psi.PsiFile;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AttachScalaSourcesProvider implements AttachSourcesProvider {

  private static void attachSources(
      Project project,
      BlazeProjectData blazeProjectData,
      Collection<BlazeLibrary> librariesToAttachSourceTo) {

    ApplicationManager.getApplication()
        .runWriteAction(
            () -> attachLibraries(project, blazeProjectData, librariesToAttachSourceTo));
  }

  private static void attachLibraries(
      Project project,
      BlazeProjectData blazeProjectData,
      Collection<BlazeLibrary> librariesToAttachSourceTo) {

    LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);

    LibraryTable.ModifiableModel libraryTableModel = libraryTable.getModifiableModel();

    for (BlazeLibrary blazeLibrary : librariesToAttachSourceTo) {
      // Make sure we don't do it twice
      if (AttachedSourceJarManager.getInstance(project)
          .hasSourceJarAttached(blazeLibrary.key)) {
        continue;
      }

      AttachedSourceJarManager.getInstance(project).setHasSourceJarAttached(blazeLibrary.key, true);

      LibraryEditor.updateLibrary(
          project,
          blazeProjectData.getArtifactLocationDecoder(),
          libraryTable,
          libraryTableModel,
          blazeLibrary);
    }

    libraryTableModel.commit();
  }

  @NotNull
  @Override
  public Collection<AttachSourcesAction> getActions(
      List<LibraryOrderEntry> orderEntries,
      final PsiFile psiFile) {

    Project project = psiFile.getProject();
    BlazeProjectData blazeProjectData = BlazeProjectDataManager.getInstance(project)
        .getBlazeProjectData();

    if (blazeProjectData == null) {
      return ImmutableList.of();
    }

    List<BlazeLibrary> librariesToAttachSourceTo = Lists.newArrayList();
    for (LibraryOrderEntry orderEntry : orderEntries) {
      Library library = orderEntry.getLibrary();
      if (library == null) {
        continue;
      }

      String name = library.getName();
      if (name == null) {
        continue;
      }

      LibraryKey libraryKey = LibraryKey.fromIntelliJLibraryName(name);

      if (AttachedSourceJarManager.getInstance(project).hasSourceJarAttached(libraryKey)) {
        continue;
      }

      BlazeJarLibrary blazeLibrary =
          ScalaAndJavaJarLibraryLocator
              .findLibraryFromIntellijLibrary(project, blazeProjectData, library);

      if (blazeLibrary == null) {
        continue;
      }

      LibraryArtifact libraryArtifact = blazeLibrary.libraryArtifact;
      if (libraryArtifact.getSourceJars().isEmpty()) {
        continue;
      }

      // Wrap BlazeJarLibrary with WixJarLibrary to include source roots detection logic
      librariesToAttachSourceTo.add(RootsAwareJarLibrary.from(blazeLibrary));
    }

    if (librariesToAttachSourceTo.isEmpty()) {
      return ImmutableList.of();
    }

    // Hack: When sources are requested and we have them, we attach them automatically in the
    // background.
    Transactions.submitTransaction(
        project,
        () -> {
          attachSources(project, blazeProjectData, librariesToAttachSourceTo);
          psiFile.clearCaches();
        });
    return ImmutableList.of();
  }
}
