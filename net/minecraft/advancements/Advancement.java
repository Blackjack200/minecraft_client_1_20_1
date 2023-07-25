package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
   @Nullable
   private final Advancement parent;
   @Nullable
   private final DisplayInfo display;
   private final AdvancementRewards rewards;
   private final ResourceLocation id;
   private final Map<String, Criterion> criteria;
   private final String[][] requirements;
   private final Set<Advancement> children = Sets.newLinkedHashSet();
   private final Component chatComponent;
   private final boolean sendsTelemetryEvent;

   public Advancement(ResourceLocation resourcelocation, @Nullable Advancement advancement, @Nullable DisplayInfo displayinfo, AdvancementRewards advancementrewards, Map<String, Criterion> map, String[][] astring, boolean flag) {
      this.id = resourcelocation;
      this.display = displayinfo;
      this.criteria = ImmutableMap.copyOf(map);
      this.parent = advancement;
      this.rewards = advancementrewards;
      this.requirements = astring;
      this.sendsTelemetryEvent = flag;
      if (advancement != null) {
         advancement.addChild(this);
      }

      if (displayinfo == null) {
         this.chatComponent = Component.literal(resourcelocation.toString());
      } else {
         Component component = displayinfo.getTitle();
         ChatFormatting chatformatting = displayinfo.getFrame().getChatColor();
         Component component1 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatformatting)).append("\n").append(displayinfo.getDescription());
         Component component2 = component.copy().withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1)));
         this.chatComponent = ComponentUtils.wrapInSquareBrackets(component2).withStyle(chatformatting);
      }

   }

   public Advancement.Builder deconstruct() {
      return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements, this.sendsTelemetryEvent);
   }

   @Nullable
   public Advancement getParent() {
      return this.parent;
   }

   public Advancement getRoot() {
      return getRoot(this);
   }

   public static Advancement getRoot(Advancement advancement) {
      Advancement advancement1 = advancement;

      while(true) {
         Advancement advancement2 = advancement1.getParent();
         if (advancement2 == null) {
            return advancement1;
         }

         advancement1 = advancement2;
      }
   }

   @Nullable
   public DisplayInfo getDisplay() {
      return this.display;
   }

   public boolean sendsTelemetryEvent() {
      return this.sendsTelemetryEvent;
   }

   public AdvancementRewards getRewards() {
      return this.rewards;
   }

   public String toString() {
      return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + ", sendsTelemetryEvent=" + this.sendsTelemetryEvent + "}";
   }

   public Iterable<Advancement> getChildren() {
      return this.children;
   }

   public Map<String, Criterion> getCriteria() {
      return this.criteria;
   }

   public int getMaxCriteraRequired() {
      return this.requirements.length;
   }

   public void addChild(Advancement advancement) {
      this.children.add(advancement);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Advancement)) {
         return false;
      } else {
         Advancement advancement = (Advancement)object;
         return this.id.equals(advancement.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public String[][] getRequirements() {
      return this.requirements;
   }

   public Component getChatComponent() {
      return this.chatComponent;
   }

   public static class Builder {
      @Nullable
      private ResourceLocation parentId;
      @Nullable
      private Advancement parent;
      @Nullable
      private DisplayInfo display;
      private AdvancementRewards rewards = AdvancementRewards.EMPTY;
      private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
      @Nullable
      private String[][] requirements;
      private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;
      private final boolean sendsTelemetryEvent;

      Builder(@Nullable ResourceLocation resourcelocation, @Nullable DisplayInfo displayinfo, AdvancementRewards advancementrewards, Map<String, Criterion> map, String[][] astring, boolean flag) {
         this.parentId = resourcelocation;
         this.display = displayinfo;
         this.rewards = advancementrewards;
         this.criteria = map;
         this.requirements = astring;
         this.sendsTelemetryEvent = flag;
      }

      private Builder(boolean flag) {
         this.sendsTelemetryEvent = flag;
      }

      public static Advancement.Builder advancement() {
         return new Advancement.Builder(true);
      }

      public static Advancement.Builder recipeAdvancement() {
         return new Advancement.Builder(false);
      }

      public Advancement.Builder parent(Advancement advancement) {
         this.parent = advancement;
         return this;
      }

      public Advancement.Builder parent(ResourceLocation resourcelocation) {
         this.parentId = resourcelocation;
         return this;
      }

      public Advancement.Builder display(ItemStack itemstack, Component component, Component component1, @Nullable ResourceLocation resourcelocation, FrameType frametype, boolean flag, boolean flag1, boolean flag2) {
         return this.display(new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, flag1, flag2));
      }

      public Advancement.Builder display(ItemLike itemlike, Component component, Component component1, @Nullable ResourceLocation resourcelocation, FrameType frametype, boolean flag, boolean flag1, boolean flag2) {
         return this.display(new DisplayInfo(new ItemStack(itemlike.asItem()), component, component1, resourcelocation, frametype, flag, flag1, flag2));
      }

      public Advancement.Builder display(DisplayInfo displayinfo) {
         this.display = displayinfo;
         return this;
      }

      public Advancement.Builder rewards(AdvancementRewards.Builder advancementrewards_builder) {
         return this.rewards(advancementrewards_builder.build());
      }

      public Advancement.Builder rewards(AdvancementRewards advancementrewards) {
         this.rewards = advancementrewards;
         return this;
      }

      public Advancement.Builder addCriterion(String s, CriterionTriggerInstance criteriontriggerinstance) {
         return this.addCriterion(s, new Criterion(criteriontriggerinstance));
      }

      public Advancement.Builder addCriterion(String s, Criterion criterion) {
         if (this.criteria.containsKey(s)) {
            throw new IllegalArgumentException("Duplicate criterion " + s);
         } else {
            this.criteria.put(s, criterion);
            return this;
         }
      }

      public Advancement.Builder requirements(RequirementsStrategy requirementsstrategy) {
         this.requirementsStrategy = requirementsstrategy;
         return this;
      }

      public Advancement.Builder requirements(String[][] astring) {
         this.requirements = astring;
         return this;
      }

      public boolean canBuild(Function<ResourceLocation, Advancement> function) {
         if (this.parentId == null) {
            return true;
         } else {
            if (this.parent == null) {
               this.parent = function.apply(this.parentId);
            }

            return this.parent != null;
         }
      }

      public Advancement build(ResourceLocation resourcelocation) {
         if (!this.canBuild((resourcelocation1) -> null)) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }

            return new Advancement(resourcelocation, this.parent, this.display, this.rewards, this.criteria, this.requirements, this.sendsTelemetryEvent);
         }
      }

      public Advancement save(Consumer<Advancement> consumer, String s) {
         Advancement advancement = this.build(new ResourceLocation(s));
         consumer.accept(advancement);
         return advancement;
      }

      public JsonObject serializeToJson() {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
         }

         JsonObject jsonobject = new JsonObject();
         if (this.parent != null) {
            jsonobject.addProperty("parent", this.parent.getId().toString());
         } else if (this.parentId != null) {
            jsonobject.addProperty("parent", this.parentId.toString());
         }

         if (this.display != null) {
            jsonobject.add("display", this.display.serializeToJson());
         }

         jsonobject.add("rewards", this.rewards.serializeToJson());
         JsonObject jsonobject1 = new JsonObject();

         for(Map.Entry<String, Criterion> map_entry : this.criteria.entrySet()) {
            jsonobject1.add(map_entry.getKey(), map_entry.getValue().serializeToJson());
         }

         jsonobject.add("criteria", jsonobject1);
         JsonArray jsonarray = new JsonArray();

         for(String[] astring : this.requirements) {
            JsonArray jsonarray1 = new JsonArray();

            for(String s : astring) {
               jsonarray1.add(s);
            }

            jsonarray.add(jsonarray1);
         }

         jsonobject.add("requirements", jsonarray);
         jsonobject.addProperty("sends_telemetry_event", this.sendsTelemetryEvent);
         return jsonobject;
      }

      public void serializeToNetwork(FriendlyByteBuf friendlybytebuf) {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
         }

         friendlybytebuf.writeNullable(this.parentId, FriendlyByteBuf::writeResourceLocation);
         friendlybytebuf.writeNullable(this.display, (friendlybytebuf1, displayinfo) -> displayinfo.serializeToNetwork(friendlybytebuf1));
         Criterion.serializeToNetwork(this.criteria, friendlybytebuf);
         friendlybytebuf.writeVarInt(this.requirements.length);

         for(String[] astring : this.requirements) {
            friendlybytebuf.writeVarInt(astring.length);

            for(String s : astring) {
               friendlybytebuf.writeUtf(s);
            }
         }

         friendlybytebuf.writeBoolean(this.sendsTelemetryEvent);
      }

      public String toString() {
         return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + ", sends_telemetry_event=" + this.sendsTelemetryEvent + "}";
      }

      public static Advancement.Builder fromJson(JsonObject jsonobject, DeserializationContext deserializationcontext) {
         ResourceLocation resourcelocation = jsonobject.has("parent") ? new ResourceLocation(GsonHelper.getAsString(jsonobject, "parent")) : null;
         DisplayInfo displayinfo = jsonobject.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(jsonobject, "display")) : null;
         AdvancementRewards advancementrewards = jsonobject.has("rewards") ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(jsonobject, "rewards")) : AdvancementRewards.EMPTY;
         Map<String, Criterion> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(jsonobject, "criteria"), deserializationcontext);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "requirements", new JsonArray());
            String[][] astring = new String[jsonarray.size()][];

            for(int i = 0; i < jsonarray.size(); ++i) {
               JsonArray jsonarray1 = GsonHelper.convertToJsonArray(jsonarray.get(i), "requirements[" + i + "]");
               astring[i] = new String[jsonarray1.size()];

               for(int j = 0; j < jsonarray1.size(); ++j) {
                  astring[i][j] = GsonHelper.convertToString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (astring.length == 0) {
               astring = new String[map.size()][];
               int k = 0;

               for(String s : map.keySet()) {
                  astring[k++] = new String[]{s};
               }
            }

            for(String[] astring1 : astring) {
               if (astring1.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               for(String s1 : astring1) {
                  if (!map.containsKey(s1)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + s1 + "'");
                  }
               }
            }

            for(String s2 : map.keySet()) {
               boolean flag = false;

               for(String[] astring2 : astring) {
                  if (ArrayUtils.contains(astring2, s2)) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  throw new JsonSyntaxException("Criterion '" + s2 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
               }
            }

            boolean flag1 = GsonHelper.getAsBoolean(jsonobject, "sends_telemetry_event", false);
            return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring, flag1);
         }
      }

      public static Advancement.Builder fromNetwork(FriendlyByteBuf friendlybytebuf) {
         ResourceLocation resourcelocation = friendlybytebuf.readNullable(FriendlyByteBuf::readResourceLocation);
         DisplayInfo displayinfo = friendlybytebuf.readNullable(DisplayInfo::fromNetwork);
         Map<String, Criterion> map = Criterion.criteriaFromNetwork(friendlybytebuf);
         String[][] astring = new String[friendlybytebuf.readVarInt()][];

         for(int i = 0; i < astring.length; ++i) {
            astring[i] = new String[friendlybytebuf.readVarInt()];

            for(int j = 0; j < astring[i].length; ++j) {
               astring[i][j] = friendlybytebuf.readUtf();
            }
         }

         boolean flag = friendlybytebuf.readBoolean();
         return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring, flag);
      }

      public Map<String, Criterion> getCriteria() {
         return this.criteria;
      }
   }
}
