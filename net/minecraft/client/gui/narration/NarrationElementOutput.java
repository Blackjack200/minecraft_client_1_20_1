package net.minecraft.client.gui.narration;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;

public interface NarrationElementOutput {
   default void add(NarratedElementType narratedelementtype, Component component) {
      this.add(narratedelementtype, NarrationThunk.from(component.getString()));
   }

   default void add(NarratedElementType narratedelementtype, String s) {
      this.add(narratedelementtype, NarrationThunk.from(s));
   }

   default void add(NarratedElementType narratedelementtype, Component... acomponent) {
      this.add(narratedelementtype, NarrationThunk.from(ImmutableList.copyOf(acomponent)));
   }

   void add(NarratedElementType narratedelementtype, NarrationThunk<?> narrationthunk);

   NarrationElementOutput nest();
}
