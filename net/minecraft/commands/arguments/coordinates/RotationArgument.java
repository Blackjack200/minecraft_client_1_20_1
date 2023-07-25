package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class RotationArgument implements ArgumentType<Coordinates> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~-5 ~5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.rotation.incomplete"));

   public static RotationArgument rotation() {
      return new RotationArgument();
   }

   public static Coordinates getRotation(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, Coordinates.class);
   }

   public Coordinates parse(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();
      if (!stringreader.canRead()) {
         throw ERROR_NOT_COMPLETE.createWithContext(stringreader);
      } else {
         WorldCoordinate worldcoordinate = WorldCoordinate.parseDouble(stringreader, false);
         if (stringreader.canRead() && stringreader.peek() == ' ') {
            stringreader.skip();
            WorldCoordinate worldcoordinate1 = WorldCoordinate.parseDouble(stringreader, false);
            return new WorldCoordinates(worldcoordinate1, worldcoordinate, new WorldCoordinate(true, 0.0D));
         } else {
            stringreader.setCursor(i);
            throw ERROR_NOT_COMPLETE.createWithContext(stringreader);
         }
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
