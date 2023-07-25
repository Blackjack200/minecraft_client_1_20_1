package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementList {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<ResourceLocation, Advancement> advancements = Maps.newHashMap();
   private final Set<Advancement> roots = Sets.newLinkedHashSet();
   private final Set<Advancement> tasks = Sets.newLinkedHashSet();
   @Nullable
   private AdvancementList.Listener listener;

   private void remove(Advancement advancement) {
      for(Advancement advancement1 : advancement.getChildren()) {
         this.remove(advancement1);
      }

      LOGGER.info("Forgot about advancement {}", (Object)advancement.getId());
      this.advancements.remove(advancement.getId());
      if (advancement.getParent() == null) {
         this.roots.remove(advancement);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementRoot(advancement);
         }
      } else {
         this.tasks.remove(advancement);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementTask(advancement);
         }
      }

   }

   public void remove(Set<ResourceLocation> set) {
      for(ResourceLocation resourcelocation : set) {
         Advancement advancement = this.advancements.get(resourcelocation);
         if (advancement == null) {
            LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)resourcelocation);
         } else {
            this.remove(advancement);
         }
      }

   }

   public void add(Map<ResourceLocation, Advancement.Builder> map) {
      Map<ResourceLocation, Advancement.Builder> map1 = Maps.newHashMap(map);

      while(!map1.isEmpty()) {
         boolean flag = false;
         Iterator<Map.Entry<ResourceLocation, Advancement.Builder>> iterator = map1.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceLocation, Advancement.Builder> map_entry = iterator.next();
            ResourceLocation resourcelocation = map_entry.getKey();
            Advancement.Builder advancement_builder = map_entry.getValue();
            if (advancement_builder.canBuild(this.advancements::get)) {
               Advancement advancement = advancement_builder.build(resourcelocation);
               this.advancements.put(resourcelocation, advancement);
               flag = true;
               iterator.remove();
               if (advancement.getParent() == null) {
                  this.roots.add(advancement);
                  if (this.listener != null) {
                     this.listener.onAddAdvancementRoot(advancement);
                  }
               } else {
                  this.tasks.add(advancement);
                  if (this.listener != null) {
                     this.listener.onAddAdvancementTask(advancement);
                  }
               }
            }
         }

         if (!flag) {
            for(Map.Entry<ResourceLocation, Advancement.Builder> map_entry1 : map1.entrySet()) {
               LOGGER.error("Couldn't load advancement {}: {}", map_entry1.getKey(), map_entry1.getValue());
            }
            break;
         }
      }

      LOGGER.info("Loaded {} advancements", (int)this.advancements.size());
   }

   public void clear() {
      this.advancements.clear();
      this.roots.clear();
      this.tasks.clear();
      if (this.listener != null) {
         this.listener.onAdvancementsCleared();
      }

   }

   public Iterable<Advancement> getRoots() {
      return this.roots;
   }

   public Collection<Advancement> getAllAdvancements() {
      return this.advancements.values();
   }

   @Nullable
   public Advancement get(ResourceLocation resourcelocation) {
      return this.advancements.get(resourcelocation);
   }

   public void setListener(@Nullable AdvancementList.Listener advancementlist_listener) {
      this.listener = advancementlist_listener;
      if (advancementlist_listener != null) {
         for(Advancement advancement : this.roots) {
            advancementlist_listener.onAddAdvancementRoot(advancement);
         }

         for(Advancement advancement1 : this.tasks) {
            advancementlist_listener.onAddAdvancementTask(advancement1);
         }
      }

   }

   public interface Listener {
      void onAddAdvancementRoot(Advancement advancement);

      void onRemoveAdvancementRoot(Advancement advancement);

      void onAddAdvancementTask(Advancement advancement);

      void onRemoveAdvancementTask(Advancement advancement);

      void onAdvancementsCleared();
   }
}
