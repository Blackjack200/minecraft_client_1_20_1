package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
   public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapcontext) {
      PlainVillagePools.bootstrap(bootstapcontext);
      SnowyVillagePools.bootstrap(bootstapcontext);
      SavannaVillagePools.bootstrap(bootstapcontext);
      DesertVillagePools.bootstrap(bootstapcontext);
      TaigaVillagePools.bootstrap(bootstapcontext);
   }
}
