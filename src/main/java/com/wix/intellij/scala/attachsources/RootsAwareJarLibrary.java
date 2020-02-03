package com.wix.intellij.scala.attachsources;

import com.google.idea.blaze.base.ideinfo.ArtifactLocation;
import com.google.idea.blaze.base.model.BlazeLibrary;
import com.google.idea.blaze.base.sync.workspace.ArtifactLocationDecoder;
import com.google.idea.blaze.java.libraries.AttachedSourceJarManager;
import com.google.idea.blaze.java.libraries.JarCache;
import com.google.idea.blaze.java.sync.model.BlazeJarLibrary;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.ui.configuration.LibrarySourceRootDetectorUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RootsAwareJarLibrary extends BlazeLibrary {

    private static final Logger logger = Logger.getInstance(RootsAwareJarLibrary.class);
    private BlazeJarLibrary blazeJarLibrary;

    private RootsAwareJarLibrary(BlazeJarLibrary blazeJarLibrary) {
        super(blazeJarLibrary.key);

        this.blazeJarLibrary = blazeJarLibrary;
    }

    public static RootsAwareJarLibrary from(BlazeJarLibrary blazeJarLibrary) {
        return new RootsAwareJarLibrary(blazeJarLibrary);
    }

    @Override
    public void modifyLibraryModel(
            Project project,
            ArtifactLocationDecoder artifactLocationDecoder,
            Library.ModifiableModel libraryModel) {
        JarCache jarCache = JarCache.getInstance(project);
        File jar = jarCache.getCachedJar(artifactLocationDecoder, blazeJarLibrary);
        if (jar != null) {
            libraryModel.addRoot(pathToUrl(jar), OrderRootType.CLASSES);
        } else {
            logger.error("No local jar file found for " + blazeJarLibrary.libraryArtifact.jarForIntellijLibrary());
        }

        AttachedSourceJarManager sourceJarManager = AttachedSourceJarManager.getInstance(project);
        if (!sourceJarManager.hasSourceJarAttached(key)) {
            return;
        }
        for (ArtifactLocation srcJar : blazeJarLibrary.libraryArtifact.getSourceJars()) {
            File sourceJar = jarCache.getCachedSourceJar(artifactLocationDecoder, srcJar);

            if (sourceJar != null) {
                detectSourceRoots(sourceJar).forEach(root -> {
                    libraryModel.addRoot(root, OrderRootType.SOURCES);
                });
            }
        }
    }

    private List<VirtualFile> detectSourceRoots(File sourceJar) {
        List<VirtualFile> roots = new ArrayList<>();

        VirtualFile srcFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(sourceJar);
        if (srcFile == null) {
            return roots;
        }

        VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(srcFile);
        if (jarRoot == null) {
            return roots;
        }

        List<RootDetector> detectors = LibrarySourceRootDetectorUtil.JAVA_SOURCE_ROOT_DETECTOR.getExtensionList();

        return detect(detectors, jarRoot);
    }

    private List<VirtualFile> detect(List<RootDetector> detectors, VirtualFile jarRoot) {
        List<VirtualFile> roots = new ArrayList<>();

        for (RootDetector detector : detectors) {
            EmptyProgressIndicator dummyIndicator = new EmptyProgressIndicator();
            roots.addAll(detector.detectRoots(jarRoot, dummyIndicator));
        }

        return roots;
    }
}
