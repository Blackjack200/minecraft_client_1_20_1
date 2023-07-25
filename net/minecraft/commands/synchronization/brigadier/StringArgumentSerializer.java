package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentTypeInfo<StringArgumentType, StringArgumentSerializer.Template> {
   public void serializeToNetwork(StringArgumentSerializer.Template stringargumentserializer_template, FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(stringargumentserializer_template.type);
   }

   public StringArgumentSerializer.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
      StringArgumentType.StringType stringargumenttype_stringtype = friendlybytebuf.readEnum(StringArgumentType.StringType.class);
      return new StringArgumentSerializer.Template(stringargumenttype_stringtype);
   }

   public void serializeToJson(StringArgumentSerializer.Template stringargumentserializer_template, JsonObject jsonobject) {
      String var10002;
      switch (stringargumentserializer_template.type) {
         case SINGLE_WORD:
            var10002 = "word";
            break;
         case QUOTABLE_PHRASE:
            var10002 = "phrase";
            break;
         case GREEDY_PHRASE:
            var10002 = "greedy";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      jsonobject.addProperty("type", var10002);
   }

   public StringArgumentSerializer.Template unpack(StringArgumentType stringargumenttype) {
      return new StringArgumentSerializer.Template(stringargumenttype.getType());
   }

   public final class Template implements ArgumentTypeInfo.Template<StringArgumentType> {
      final StringArgumentType.StringType type;

      public Template(StringArgumentType.StringType stringargumenttype_stringtype) {
         this.type = stringargumenttype_stringtype;
      }

      public StringArgumentType instantiate(CommandBuildContext commandbuildcontext) {
         StringArgumentType var10000;
         switch (this.type) {
            case SINGLE_WORD:
               var10000 = StringArgumentType.word();
               break;
            case QUOTABLE_PHRASE:
               var10000 = StringArgumentType.string();
               break;
            case GREEDY_PHRASE:
               var10000 = StringArgumentType.greedyString();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public ArgumentTypeInfo<StringArgumentType, ?> type() {
         return StringArgumentSerializer.this;
      }
   }
}
