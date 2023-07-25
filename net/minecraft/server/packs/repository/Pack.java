package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String id;
   private final Pack.ResourcesSupplier resources;
   private final Component title;
   private final Component description;
   private final PackCompatibility compatibility;
   private final FeatureFlagSet requestedFeatures;
   private final Pack.Position defaultPosition;
   private final boolean required;
   private final boolean fixedPosition;
   private final PackSource packSource;

   @Nullable
   public static Pack readMetaAndCreate(String s, Component component, boolean flag, Pack.ResourcesSupplier pack_resourcessupplier, PackType packtype, Pack.Position pack_position, PackSource packsource) {
      Pack.Info pack_info = readPackInfo(s, pack_resourcessupplier);
      return pack_info != null ? create(s, component, flag, pack_resourcessupplier, pack_info, packtype, pack_position, false, packsource) : null;
   }

   public static Pack create(String s, Component component, boolean flag, Pack.ResourcesSupplier pack_resourcessupplier, Pack.Info pack_info, PackType packtype, Pack.Position pack_position, boolean flag1, PackSource packsource) {
      return new Pack(s, flag, pack_resourcessupplier, component, pack_info, pack_info.compatibility(packtype), pack_position, flag1, packsource);
   }

   private Pack(String s, boolean flag, Pack.ResourcesSupplier pack_resourcessupplier, Component component, Pack.Info pack_info, PackCompatibility packcompatibility, Pack.Position pack_position, boolean flag1, PackSource packsource) {
      this.id = s;
      this.resources = pack_resourcessupplier;
      this.title = component;
      this.description = pack_info.description();
      this.compatibility = packcompatibility;
      this.requestedFeatures = pack_info.requestedFeatures();
      this.required = flag;
      this.defaultPosition = pack_position;
      this.fixedPosition = flag1;
      this.packSource = packsource;
   }

   @Nullable
   public static Pack.Info readPackInfo(String s, Pack.ResourcesSupplier pack_resourcessupplier) {
      try {
         PackResources packresources = pack_resourcessupplier.open(s);

         Object var10;
         label52: {
            Pack.Info var6;
            try {
               PackMetadataSection packmetadatasection = packresources.getMetadataSection(PackMetadataSection.TYPE);
               if (packmetadatasection == null) {
                  LOGGER.warn("Missing metadata in pack {}", (Object)s);
                  var10 = null;
                  break label52;
               }

               FeatureFlagsMetadataSection featureflagsmetadatasection = packresources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
               FeatureFlagSet featureflagset = featureflagsmetadatasection != null ? featureflagsmetadatasection.flags() : FeatureFlagSet.of();
               var6 = new Pack.Info(packmetadatasection.getDescription(), packmetadatasection.getPackFormat(), featureflagset);
            } catch (Throwable var8) {
               if (packresources != null) {
                  try {
                     packresources.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (packresources != null) {
               packresources.close();
            }

            return var6;
         }

         if (packresources != null) {
            packresources.close();
         }

         return (Pack.Info)var10;
      } catch (Exception var9) {
         LOGGER.warn("Failed to read pack metadata", (Throwable)var9);
         return null;
      }
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getDescription() {
      return this.description;
   }

   public Component getChatLink(boolean flag) {
      return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id))).withStyle((style) -> style.withColor(flag ? ChatFormatting.GREEN : ChatFormatting.RED).withInsertion(StringArgumentType.escapeIfRequired(this.id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.description))));
   }

   public PackCompatibility getCompatibility() {
      return this.compatibility;
   }

   public FeatureFlagSet getRequestedFeatures() {
      return this.requestedFeatures;
   }

   public PackResources open() {
      return this.resources.open(this.id);
   }

   public String getId() {
      return this.id;
   }

   public boolean isRequired() {
      return this.required;
   }

   public boolean isFixedPosition() {
      return this.fixedPosition;
   }

   public Pack.Position getDefaultPosition() {
      return this.defaultPosition;
   }

   public PackSource getPackSource() {
      return this.packSource;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Pack)) {
         return false;
      } else {
         Pack pack = (Pack)object;
         return this.id.equals(pack.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public static record Info(Component description, int format, FeatureFlagSet requestedFeatures) {
      public PackCompatibility compatibility(PackType packtype) {
         return PackCompatibility.forFormat(this.format, packtype);
      }
   }

   public static enum Position {
      TOP,
      BOTTOM;

      public <T> int insert(List<T> list, T object, Function<T, Pack> function, boolean flag) {
         Pack.Position pack_position = flag ? this.opposite() : this;
         if (pack_position == BOTTOM) {
            int i;
            for(i = 0; i < list.size(); ++i) {
               Pack pack = function.apply(list.get(i));
               if (!pack.isFixedPosition() || pack.getDefaultPosition() != this) {
                  break;
               }
            }

            list.add(i, object);
            return i;
         } else {
            int j;
            for(j = list.size() - 1; j >= 0; --j) {
               Pack pack1 = function.apply(list.get(j));
               if (!pack1.isFixedPosition() || pack1.getDefaultPosition() != this) {
                  break;
               }
            }

            list.add(j + 1, object);
            return j + 1;
         }
      }

      public Pack.Position opposite() {
         return this == TOP ? BOTTOM : TOP;
      }
   }

   @FunctionalInterface
   public interface ResourcesSupplier {
      PackResources open(String s);
   }
}
