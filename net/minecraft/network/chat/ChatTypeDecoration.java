package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public record ChatTypeDecoration(String translationKey, List<ChatTypeDecoration.Parameter> parameters, Style style) {
   public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey), ChatTypeDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters), Style.FORMATTING_CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(ChatTypeDecoration::style)).apply(recordcodecbuilder_instance, ChatTypeDecoration::new));

   public static ChatTypeDecoration withSender(String s) {
      return new ChatTypeDecoration(s, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
   }

   public static ChatTypeDecoration incomingDirectMessage(String s) {
      Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
      return new ChatTypeDecoration(s, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), style);
   }

   public static ChatTypeDecoration outgoingDirectMessage(String s) {
      Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
      return new ChatTypeDecoration(s, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.CONTENT), style);
   }

   public static ChatTypeDecoration teamMessage(String s) {
      return new ChatTypeDecoration(s, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
   }

   public Component decorate(Component component, ChatType.Bound chattype_bound) {
      Object[] aobject = this.resolveParameters(component, chattype_bound);
      return Component.translatable(this.translationKey, aobject).withStyle(this.style);
   }

   private Component[] resolveParameters(Component component, ChatType.Bound chattype_bound) {
      Component[] acomponent = new Component[this.parameters.size()];

      for(int i = 0; i < acomponent.length; ++i) {
         ChatTypeDecoration.Parameter chattypedecoration_parameter = this.parameters.get(i);
         acomponent[i] = chattypedecoration_parameter.select(component, chattype_bound);
      }

      return acomponent;
   }

   public static enum Parameter implements StringRepresentable {
      SENDER("sender", (component, chattype_bound) -> chattype_bound.name()),
      TARGET("target", (component, chattype_bound) -> chattype_bound.targetName()),
      CONTENT("content", (component, chattype_bound) -> component);

      public static final Codec<ChatTypeDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatTypeDecoration.Parameter::values);
      private final String name;
      private final ChatTypeDecoration.Parameter.Selector selector;

      private Parameter(String s, ChatTypeDecoration.Parameter.Selector chattypedecoration_parameter_selector) {
         this.name = s;
         this.selector = chattypedecoration_parameter_selector;
      }

      public Component select(Component component, ChatType.Bound chattype_bound) {
         Component component1 = this.selector.select(component, chattype_bound);
         return Objects.requireNonNullElse(component1, CommonComponents.EMPTY);
      }

      public String getSerializedName() {
         return this.name;
      }

      public interface Selector {
         @Nullable
         Component select(Component component, ChatType.Bound chattype_bound);
      }
   }
}
