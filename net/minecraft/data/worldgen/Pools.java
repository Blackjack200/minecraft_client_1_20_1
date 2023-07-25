package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
   public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

   public static ResourceKey<StructureTemplatePool> createKey(String s) {
      return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(s));
   }

   public static void register(BootstapContext<StructureTemplatePool> bootstapcontext, String s, StructureTemplatePool structuretemplatepool) {
      bootstapcontext.register(createKey(s), structuretemplatepool);
   }

   public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapcontext) {
      HolderGetter<StructureTemplatePool> holdergetter = bootstapcontext.lookup(Registries.TEMPLATE_POOL);
      Holder<StructureTemplatePool> holder = holdergetter.getOrThrow(EMPTY);
      bootstapcontext.register(EMPTY, new StructureTemplatePool(holder, ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
      BastionPieces.bootstrap(bootstapcontext);
      PillagerOutpostPools.bootstrap(bootstapcontext);
      VillagePools.bootstrap(bootstapcontext);
      AncientCityStructurePieces.bootstrap(bootstapcontext);
      TrailRuinsStructurePools.bootstrap(bootstapcontext);
   }
}
