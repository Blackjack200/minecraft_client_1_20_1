package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.packs.BundleRecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Main {
   @DontObfuscate
   public static void main(String[] astring) throws IOException {
      SharedConstants.tryDetectVersion();
      OptionParser optionparser = new OptionParser();
      OptionSpec<Void> optionspec = optionparser.accepts("help", "Show the help menu").forHelp();
      OptionSpec<Void> optionspec1 = optionparser.accepts("server", "Include server generators");
      OptionSpec<Void> optionspec2 = optionparser.accepts("client", "Include client generators");
      OptionSpec<Void> optionspec3 = optionparser.accepts("dev", "Include development tools");
      OptionSpec<Void> optionspec4 = optionparser.accepts("reports", "Include data reports");
      OptionSpec<Void> optionspec5 = optionparser.accepts("validate", "Validate inputs");
      OptionSpec<Void> optionspec6 = optionparser.accepts("all", "Include all generators");
      OptionSpec<String> optionspec7 = optionparser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated");
      OptionSpec<String> optionspec8 = optionparser.accepts("input", "Input folder").withRequiredArg();
      OptionSet optionset = optionparser.parse(astring);
      if (!optionset.has(optionspec) && optionset.hasOptions()) {
         Path path = Paths.get(optionspec7.value(optionset));
         boolean flag = optionset.has(optionspec6);
         boolean flag1 = flag || optionset.has(optionspec2);
         boolean flag2 = flag || optionset.has(optionspec1);
         boolean flag3 = flag || optionset.has(optionspec3);
         boolean flag4 = flag || optionset.has(optionspec4);
         boolean flag5 = flag || optionset.has(optionspec5);
         DataGenerator datagenerator = createStandardGenerator(path, optionset.valuesOf(optionspec8).stream().map((s) -> Paths.get(s)).collect(Collectors.toList()), flag1, flag2, flag3, flag4, flag5, SharedConstants.getCurrentVersion(), true);
         datagenerator.run();
      } else {
         optionparser.printHelpOn(System.out);
      }
   }

   private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> bifunction, CompletableFuture<HolderLookup.Provider> completablefuture) {
      return (packoutput) -> bifunction.apply(packoutput, completablefuture);
   }

   public static DataGenerator createStandardGenerator(Path path, Collection<Path> collection, boolean flag, boolean flag1, boolean flag2, boolean flag3, boolean flag4, WorldVersion worldversion, boolean flag5) {
      DataGenerator datagenerator = new DataGenerator(path, worldversion, flag5);
      DataGenerator.PackGenerator datagenerator_packgenerator = datagenerator.getVanillaPack(flag || flag1);
      datagenerator_packgenerator.addProvider((packoutput3) -> (new SnbtToNbt(packoutput3, collection)).addFilter(new StructureUpdater()));
      CompletableFuture<HolderLookup.Provider> completablefuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
      DataGenerator.PackGenerator datagenerator_packgenerator1 = datagenerator.getVanillaPack(flag);
      datagenerator_packgenerator1.addProvider(ModelProvider::new);
      DataGenerator.PackGenerator datagenerator_packgenerator2 = datagenerator.getVanillaPack(flag1);
      datagenerator_packgenerator2.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(VanillaAdvancementProvider::create, completablefuture));
      datagenerator_packgenerator2.addProvider(VanillaLootTableProvider::create);
      datagenerator_packgenerator2.addProvider(VanillaRecipeProvider::new);
      TagsProvider<Block> tagsprovider = datagenerator_packgenerator2.addProvider(bindRegistries(VanillaBlockTagsProvider::new, completablefuture));
      TagsProvider<Item> tagsprovider1 = datagenerator_packgenerator2.addProvider((packoutput2) -> new VanillaItemTagsProvider(packoutput2, completablefuture, tagsprovider.contentsGetter()));
      datagenerator_packgenerator2.addProvider(bindRegistries(BannerPatternTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(BiomeTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(CatVariantTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(DamageTypeTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(EntityTypeTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(FluidTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(GameEventTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(InstrumentTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(PaintingVariantTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(PoiTypeTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(StructureTagsProvider::new, completablefuture));
      datagenerator_packgenerator2.addProvider(bindRegistries(WorldPresetTagsProvider::new, completablefuture));
      datagenerator_packgenerator2 = datagenerator.getVanillaPack(flag2);
      datagenerator_packgenerator2.addProvider((packoutput1) -> new NbtToSnbt(packoutput1, collection));
      datagenerator_packgenerator2 = datagenerator.getVanillaPack(flag3);
      datagenerator_packgenerator2.addProvider(bindRegistries(BiomeParametersDumpReport::new, completablefuture));
      datagenerator_packgenerator2.addProvider(BlockListReport::new);
      datagenerator_packgenerator2.addProvider(bindRegistries(CommandsReport::new, completablefuture));
      datagenerator_packgenerator2.addProvider(RegistryDumpReport::new);
      datagenerator_packgenerator2 = datagenerator.getBuiltinDatapack(flag1, "bundle");
      datagenerator_packgenerator2.addProvider(BundleRecipeProvider::new);
      datagenerator_packgenerator2.addProvider((packoutput) -> PackMetadataGenerator.forFeaturePack(packoutput, Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE)));
      return datagenerator;
   }
}
