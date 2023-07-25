package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AngleArgument implements ArgumentType<AngleArgument.SingleAngle> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.angle.incomplete"));
   public static final SimpleCommandExceptionType ERROR_INVALID_ANGLE = new SimpleCommandExceptionType(Component.translatable("argument.angle.invalid"));

   public static AngleArgument angle() {
      return new AngleArgument();
   }

   public static float getAngle(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, AngleArgument.SingleAngle.class).getAngle(commandcontext.getSource());
   }

   public AngleArgument.SingleAngle parse(StringReader stringreader) throws CommandSyntaxException {
      if (!stringreader.canRead()) {
         throw ERROR_NOT_COMPLETE.createWithContext(stringreader);
      } else {
         boolean flag = WorldCoordinate.isRelative(stringreader);
         float f = stringreader.canRead() && stringreader.peek() != ' ' ? stringreader.readFloat() : 0.0F;
         if (!Float.isNaN(f) && !Float.isInfinite(f)) {
            return new AngleArgument.SingleAngle(f, flag);
         } else {
            throw ERROR_INVALID_ANGLE.createWithContext(stringreader);
         }
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static final class SingleAngle {
      private final float angle;
      private final boolean isRelative;

      SingleAngle(float f, boolean flag) {
         this.angle = f;
         this.isRelative = flag;
      }

      public float getAngle(CommandSourceStack commandsourcestack) {
         return Mth.wrapDegrees(this.isRelative ? this.angle + commandsourcestack.getRotation().y : this.angle);
      }
   }
}
