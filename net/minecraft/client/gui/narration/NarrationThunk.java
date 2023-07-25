package net.minecraft.client.gui.narration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

public class NarrationThunk<T> {
   private final T contents;
   private final BiConsumer<Consumer<String>, T> converter;
   public static final NarrationThunk<?> EMPTY = new NarrationThunk<>(Unit.INSTANCE, (consumer, unit) -> {
   });

   private NarrationThunk(T object, BiConsumer<Consumer<String>, T> biconsumer) {
      this.contents = object;
      this.converter = biconsumer;
   }

   public static NarrationThunk<?> from(String s) {
      return new NarrationThunk<>(s, Consumer::accept);
   }

   public static NarrationThunk<?> from(Component component) {
      return new NarrationThunk<>(component, (consumer, component1) -> consumer.accept(component1.getString()));
   }

   public static NarrationThunk<?> from(List<Component> list) {
      return new NarrationThunk<>(list, (consumer, list2) -> list.stream().map(Component::getString).forEach(consumer));
   }

   public void getText(Consumer<String> consumer) {
      this.converter.accept(consumer, this.contents);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof NarrationThunk)) {
         return false;
      } else {
         NarrationThunk<?> narrationthunk = (NarrationThunk)object;
         return narrationthunk.converter == this.converter && narrationthunk.contents.equals(this.contents);
      }
   }

   public int hashCode() {
      int i = this.contents.hashCode();
      return 31 * i + this.converter.hashCode();
   }
}
