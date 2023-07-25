package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationPattern {
   public static final Codec<ResourceLocationPattern> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter((resourcelocationpattern1) -> resourcelocationpattern1.namespacePattern), ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter((resourcelocationpattern) -> resourcelocationpattern.pathPattern)).apply(recordcodecbuilder_instance, ResourceLocationPattern::new));
   private final Optional<Pattern> namespacePattern;
   private final Predicate<String> namespacePredicate;
   private final Optional<Pattern> pathPattern;
   private final Predicate<String> pathPredicate;
   private final Predicate<ResourceLocation> locationPredicate;

   private ResourceLocationPattern(Optional<Pattern> optional, Optional<Pattern> optional1) {
      this.namespacePattern = optional;
      this.namespacePredicate = optional.map(Pattern::asPredicate).orElse((s1) -> true);
      this.pathPattern = optional1;
      this.pathPredicate = optional1.map(Pattern::asPredicate).orElse((s) -> true);
      this.locationPredicate = (resourcelocation) -> this.namespacePredicate.test(resourcelocation.getNamespace()) && this.pathPredicate.test(resourcelocation.getPath());
   }

   public Predicate<String> namespacePredicate() {
      return this.namespacePredicate;
   }

   public Predicate<String> pathPredicate() {
      return this.pathPredicate;
   }

   public Predicate<ResourceLocation> locationPredicate() {
      return this.locationPredicate;
   }
}
