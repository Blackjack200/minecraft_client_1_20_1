package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class GlobalPos {
   public static final Codec<GlobalPos> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)).apply(recordcodecbuilder_instance, GlobalPos::of));
   private final ResourceKey<Level> dimension;
   private final BlockPos pos;

   private GlobalPos(ResourceKey<Level> resourcekey, BlockPos blockpos) {
      this.dimension = resourcekey;
      this.pos = blockpos;
   }

   public static GlobalPos of(ResourceKey<Level> resourcekey, BlockPos blockpos) {
      return new GlobalPos(resourcekey, blockpos);
   }

   public ResourceKey<Level> dimension() {
      return this.dimension;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         GlobalPos globalpos = (GlobalPos)object;
         return Objects.equals(this.dimension, globalpos.dimension) && Objects.equals(this.pos, globalpos.pos);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.dimension, this.pos);
   }

   public String toString() {
      return this.dimension + " " + this.pos;
   }
}
