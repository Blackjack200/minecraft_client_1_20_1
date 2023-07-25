package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Collection;

public class CommonComponents {
   public static final Component EMPTY = Component.empty();
   public static final Component OPTION_ON = Component.translatable("options.on");
   public static final Component OPTION_OFF = Component.translatable("options.off");
   public static final Component GUI_DONE = Component.translatable("gui.done");
   public static final Component GUI_CANCEL = Component.translatable("gui.cancel");
   public static final Component GUI_YES = Component.translatable("gui.yes");
   public static final Component GUI_NO = Component.translatable("gui.no");
   public static final Component GUI_OK = Component.translatable("gui.ok");
   public static final Component GUI_PROCEED = Component.translatable("gui.proceed");
   public static final Component GUI_CONTINUE = Component.translatable("gui.continue");
   public static final Component GUI_BACK = Component.translatable("gui.back");
   public static final Component GUI_TO_TITLE = Component.translatable("gui.toTitle");
   public static final Component GUI_ACKNOWLEDGE = Component.translatable("gui.acknowledge");
   public static final Component GUI_OPEN_IN_BROWSER = Component.translatable("chat.link.open");
   public static final Component GUI_COPY_LINK_TO_CLIPBOARD = Component.translatable("gui.copy_link_to_clipboard");
   public static final Component CONNECT_FAILED = Component.translatable("connect.failed");
   public static final Component NEW_LINE = Component.literal("\n");
   public static final Component NARRATION_SEPARATOR = Component.literal(". ");
   public static final Component ELLIPSIS = Component.literal("...");
   public static final Component SPACE = space();

   public static MutableComponent space() {
      return Component.literal(" ");
   }

   public static MutableComponent days(long i) {
      return Component.translatable("gui.days", i);
   }

   public static MutableComponent hours(long i) {
      return Component.translatable("gui.hours", i);
   }

   public static MutableComponent minutes(long i) {
      return Component.translatable("gui.minutes", i);
   }

   public static Component optionStatus(boolean flag) {
      return flag ? OPTION_ON : OPTION_OFF;
   }

   public static MutableComponent optionStatus(Component component, boolean flag) {
      return Component.translatable(flag ? "options.on.composed" : "options.off.composed", component);
   }

   public static MutableComponent optionNameValue(Component component, Component component1) {
      return Component.translatable("options.generic_value", component, component1);
   }

   public static MutableComponent joinForNarration(Component... acomponent) {
      MutableComponent mutablecomponent = Component.empty();

      for(int i = 0; i < acomponent.length; ++i) {
         mutablecomponent.append(acomponent[i]);
         if (i != acomponent.length - 1) {
            mutablecomponent.append(NARRATION_SEPARATOR);
         }
      }

      return mutablecomponent;
   }

   public static Component joinLines(Component... acomponent) {
      return joinLines(Arrays.asList(acomponent));
   }

   public static Component joinLines(Collection<? extends Component> collection) {
      return ComponentUtils.formatList(collection, NEW_LINE);
   }
}
