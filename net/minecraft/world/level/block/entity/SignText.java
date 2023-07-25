package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class SignText {
   private static final Codec<Component[]> LINES_CODEC = ExtraCodecs.FLAT_COMPONENT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 4).map((list1) -> new Component[]{list1.get(0), list1.get(1), list1.get(2), list1.get(3)}), (acomponent) -> List.of(acomponent[0], acomponent[1], acomponent[2], acomponent[3]));
   public static final Codec<SignText> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(LINES_CODEC.fieldOf("messages").forGetter((signtext2) -> signtext2.messages), LINES_CODEC.optionalFieldOf("filtered_messages").forGetter(SignText::getOnlyFilteredMessages), DyeColor.CODEC.fieldOf("color").orElse(DyeColor.BLACK).forGetter((signtext1) -> signtext1.color), Codec.BOOL.fieldOf("has_glowing_text").orElse(false).forGetter((signtext) -> signtext.hasGlowingText)).apply(recordcodecbuilder_instance, SignText::load));
   public static final int LINES = 4;
   private final Component[] messages;
   private final Component[] filteredMessages;
   private final DyeColor color;
   private final boolean hasGlowingText;
   @Nullable
   private FormattedCharSequence[] renderMessages;
   private boolean renderMessagedFiltered;

   public SignText() {
      this(emptyMessages(), emptyMessages(), DyeColor.BLACK, false);
   }

   public SignText(Component[] acomponent, Component[] acomponent1, DyeColor dyecolor, boolean flag) {
      this.messages = acomponent;
      this.filteredMessages = acomponent1;
      this.color = dyecolor;
      this.hasGlowingText = flag;
   }

   private static Component[] emptyMessages() {
      return new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
   }

   private static SignText load(Component[] acomponent, Optional<Component[]> optional, DyeColor dyecolor, boolean flag) {
      Component[] acomponent1 = optional.orElseGet(SignText::emptyMessages);
      populateFilteredMessagesWithRawMessages(acomponent, acomponent1);
      return new SignText(acomponent, acomponent1, dyecolor, flag);
   }

   private static void populateFilteredMessagesWithRawMessages(Component[] acomponent, Component[] acomponent1) {
      for(int i = 0; i < 4; ++i) {
         if (acomponent1[i].equals(CommonComponents.EMPTY)) {
            acomponent1[i] = acomponent[i];
         }
      }

   }

   public boolean hasGlowingText() {
      return this.hasGlowingText;
   }

   public SignText setHasGlowingText(boolean flag) {
      return flag == this.hasGlowingText ? this : new SignText(this.messages, this.filteredMessages, this.color, flag);
   }

   public DyeColor getColor() {
      return this.color;
   }

   public SignText setColor(DyeColor dyecolor) {
      return dyecolor == this.getColor() ? this : new SignText(this.messages, this.filteredMessages, dyecolor, this.hasGlowingText);
   }

   public Component getMessage(int i, boolean flag) {
      return this.getMessages(flag)[i];
   }

   public SignText setMessage(int i, Component component) {
      return this.setMessage(i, component, component);
   }

   public SignText setMessage(int i, Component component, Component component1) {
      Component[] acomponent = Arrays.copyOf(this.messages, this.messages.length);
      Component[] acomponent1 = Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
      acomponent[i] = component;
      acomponent1[i] = component1;
      return new SignText(acomponent, acomponent1, this.color, this.hasGlowingText);
   }

   public boolean hasMessage(Player player) {
      return Arrays.stream(this.getMessages(player.isTextFilteringEnabled())).anyMatch((component) -> !component.getString().isEmpty());
   }

   public Component[] getMessages(boolean flag) {
      return flag ? this.filteredMessages : this.messages;
   }

   public FormattedCharSequence[] getRenderMessages(boolean flag, Function<Component, FormattedCharSequence> function) {
      if (this.renderMessages == null || this.renderMessagedFiltered != flag) {
         this.renderMessagedFiltered = flag;
         this.renderMessages = new FormattedCharSequence[4];

         for(int i = 0; i < 4; ++i) {
            this.renderMessages[i] = function.apply(this.getMessage(i, flag));
         }
      }

      return this.renderMessages;
   }

   private Optional<Component[]> getOnlyFilteredMessages() {
      Component[] acomponent = new Component[4];
      boolean flag = false;

      for(int i = 0; i < 4; ++i) {
         Component component = this.filteredMessages[i];
         if (!component.equals(this.messages[i])) {
            acomponent[i] = component;
            flag = true;
         } else {
            acomponent[i] = CommonComponents.EMPTY;
         }
      }

      return flag ? Optional.of(acomponent) : Optional.empty();
   }

   public boolean hasAnyClickCommands(Player player) {
      for(Component component : this.getMessages(player.isTextFilteringEnabled())) {
         Style style = component.getStyle();
         ClickEvent clickevent = style.getClickEvent();
         if (clickevent != null && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            return true;
         }
      }

      return false;
   }
}
