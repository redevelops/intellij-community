package org.jetbrains.jps.incremental.artifacts;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ProjectPaths;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.java.JavaModuleBuildTargetType;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ModuleRootsIndex;
import org.jetbrains.jps.incremental.artifacts.builders.LayoutElementBuildersRegistry;
import org.jetbrains.jps.incremental.artifacts.impl.JpsArtifactUtil;
import org.jetbrains.jps.incremental.artifacts.instructions.*;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.artifact.JpsArtifact;
import org.jetbrains.jps.model.artifact.elements.JpsArtifactOutputPackagingElement;
import org.jetbrains.jps.model.artifact.elements.JpsPackagingElement;
import org.jetbrains.jps.model.java.JpsProductionModuleOutputPackagingElement;
import org.jetbrains.jps.model.java.JpsTestModuleOutputPackagingElement;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author nik
 */
public class ArtifactBuildTarget extends BuildTarget<ArtifactRootDescriptor> {
  private final JpsArtifact myArtifact;

  public ArtifactBuildTarget(@NotNull JpsArtifact artifact) {
    super(ArtifactBuildTargetType.INSTANCE);
    myArtifact = artifact;
  }

  @Override
  public String getId() {
    return myArtifact.getName();
  }

  public JpsArtifact getArtifact() {
    return myArtifact;
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies() {
    final LinkedHashSet<BuildTarget<?>> dependencies = new LinkedHashSet<BuildTarget<?>>();
    JpsArtifactUtil.processPackagingElements(myArtifact.getRootElement(), new Processor<JpsPackagingElement>() {
      @Override
      public boolean process(JpsPackagingElement element) {
        if (element instanceof JpsArtifactOutputPackagingElement) {
          JpsArtifact included = ((JpsArtifactOutputPackagingElement)element).getArtifactReference().resolve();
          if (included != null && !included.equals(myArtifact)) {
            if (!StringUtil.isEmpty(included.getOutputPath())) {
              dependencies.add(new ArtifactBuildTarget(included));
              return false;
            }
          }
        }
        else if (element instanceof JpsProductionModuleOutputPackagingElement) {
          JpsModule module = ((JpsProductionModuleOutputPackagingElement)element).getModuleReference().resolve();
          if (module != null) {
            dependencies.add(new ModuleBuildTarget(module, JavaModuleBuildTargetType.PRODUCTION));
          }
        }
        else if (element instanceof JpsTestModuleOutputPackagingElement) {
          JpsModule module = ((JpsTestModuleOutputPackagingElement)element).getModuleReference().resolve();
          if (module != null) {
            dependencies.add(new ModuleBuildTarget(module, JavaModuleBuildTargetType.TEST));
          }
        }
        return true;
      }
    });
    return dependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    return myArtifact.equals(((ArtifactBuildTarget)o).myArtifact);
  }

  @Override
  public int hashCode() {
    return myArtifact.hashCode();
  }

  @Override
  public void writeConfiguration(PrintWriter out, BuildRootIndex buildRootIndex) {
    out.println(StringUtil.notNullize(myArtifact.getOutputPath()));
    for (ArtifactRootDescriptor descriptor : buildRootIndex.getTargetRoots(this, null)) {
      descriptor.writeConfiguration(out);
    }
  }

  @NotNull
  @Override
  public List<ArtifactRootDescriptor> computeRootDescriptors(JpsModel model, ModuleRootsIndex index) {
    ArtifactInstructionsBuilderImpl builder = new ArtifactInstructionsBuilderImpl(index, this);
    ArtifactInstructionsBuilderContext context = new ArtifactInstructionsBuilderContextImpl(model, new ProjectPaths(model.getProject()));
    String outputPath = StringUtil.notNullize(myArtifact.getOutputPath());
    final CopyToDirectoryInstructionCreator instructionCreator = new CopyToDirectoryInstructionCreator(builder, outputPath);
    LayoutElementBuildersRegistry.getInstance().generateInstructions(myArtifact, instructionCreator, context);
    return builder.getDescriptors();
  }

  @Override
  public BuildRootDescriptor findRootDescriptor(String rootId,
                                                BuildRootIndex rootIndex) {
    return rootIndex.getTargetRoots(this, null).get(Integer.valueOf(rootId));
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Artifact '" + myArtifact.getName() + "'";
  }
}
