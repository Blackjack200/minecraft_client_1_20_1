package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class SelectorContents implements ComponentContents {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String pattern;
   @Nullable
   private final EntitySelector selector;
   protected final Optional<Component> separator;

   public SelectorContents(String s, Optional<Component> optional) {
      this.pattern = s;
      this.separator = optional;
      this.selector = parseSelector(s);
   }

   @Nullable
   private static EntitySelector parseSelector(String s) {
      EntitySelector entityselector = null;

      try {
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(new StringReader(s));
         entityselector = entityselectorparser.parse();
      } catch (CommandSyntaxException var3) {
         LOGGER.warn("Invalid selector component: {}: {}", s, var3.getMessage());
      }

      return entityselector;
   }

   public String getPattern() {
      return this.pattern;
   }

   @Nullable
   public EntitySelector getSelector() {
      return this.selector;
   }

   public Optional<Component> getSeparator() {
      return this.separator;
   }

   public MutableComponent resolve(@Nullable CommandSourceStack commandsourcestack, @Nullable Entity entity, int i) throws CommandSyntaxException {
      if (commandsourcestack != null && this.selector != null) {
         Optional<? extends Component> optional = ComponentUtils.updateForEntity(commandsourcestack, this.separator, entity, i);
         return ComponentUtils.formatList(this.selector.findEntities(commandsourcestack), optional, Entity::getDisplayName);
      } else {
         return Component.empty();
      }
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
      return formattedtext_styledcontentconsumer.accept(style, this.pattern);
   }

   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
      return formattedtext_contentconsumer.accept(this.pattern);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof SelectorContents) {
            SelectorContents selectorcontents = (SelectorContents)object;
            if (this.pattern.equals(selectorcontents.pattern) && this.separator.equals(selectorcontents.separator)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = this.pattern.hashCode();
      return 31 * i + this.separator.hashCode();
   }

   public String toString() {
      return "pattern{" + this.pattern + "}";
   }
}
