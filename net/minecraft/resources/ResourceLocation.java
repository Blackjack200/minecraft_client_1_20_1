package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ResourceLocation implements Comparable<ResourceLocation> {
   public static final Codec<ResourceLocation> CODEC = Codec.STRING.comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));
   public static final char NAMESPACE_SEPARATOR = ':';
   public static final String DEFAULT_NAMESPACE = "minecraft";
   public static final String REALMS_NAMESPACE = "realms";
   private final String namespace;
   private final String path;

   protected ResourceLocation(String s, String s1, @Nullable ResourceLocation.Dummy resourcelocation_dummy) {
      this.namespace = s;
      this.path = s1;
   }

   public ResourceLocation(String s, String s1) {
      this(assertValidNamespace(s, s1), assertValidPath(s, s1), (ResourceLocation.Dummy)null);
   }

   private ResourceLocation(String[] astring) {
      this(astring[0], astring[1]);
   }

   public ResourceLocation(String s) {
      this(decompose(s, ':'));
   }

   public static ResourceLocation of(String s, char c0) {
      return new ResourceLocation(decompose(s, c0));
   }

   @Nullable
   public static ResourceLocation tryParse(String s) {
      try {
         return new ResourceLocation(s);
      } catch (ResourceLocationException var2) {
         return null;
      }
   }

   @Nullable
   public static ResourceLocation tryBuild(String s, String s1) {
      try {
         return new ResourceLocation(s, s1);
      } catch (ResourceLocationException var3) {
         return null;
      }
   }

   protected static String[] decompose(String s, char c0) {
      String[] astring = new String[]{"minecraft", s};
      int i = s.indexOf(c0);
      if (i >= 0) {
         astring[1] = s.substring(i + 1);
         if (i >= 1) {
            astring[0] = s.substring(0, i);
         }
      }

      return astring;
   }

   public static DataResult<ResourceLocation> read(String s) {
      try {
         return DataResult.success(new ResourceLocation(s));
      } catch (ResourceLocationException var2) {
         return DataResult.error(() -> "Not a valid resource location: " + s + " " + var2.getMessage());
      }
   }

   public String getPath() {
      return this.path;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public ResourceLocation withPath(String s) {
      return new ResourceLocation(this.namespace, assertValidPath(this.namespace, s), (ResourceLocation.Dummy)null);
   }

   public ResourceLocation withPath(UnaryOperator<String> unaryoperator) {
      return this.withPath(unaryoperator.apply(this.path));
   }

   public ResourceLocation withPrefix(String s) {
      return this.withPath(s + this.path);
   }

   public ResourceLocation withSuffix(String s) {
      return this.withPath(this.path + s);
   }

   public String toString() {
      return this.namespace + ":" + this.path;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof ResourceLocation)) {
         return false;
      } else {
         ResourceLocation resourcelocation = (ResourceLocation)object;
         return this.namespace.equals(resourcelocation.namespace) && this.path.equals(resourcelocation.path);
      }
   }

   public int hashCode() {
      return 31 * this.namespace.hashCode() + this.path.hashCode();
   }

   public int compareTo(ResourceLocation resourcelocation) {
      int i = this.path.compareTo(resourcelocation.path);
      if (i == 0) {
         i = this.namespace.compareTo(resourcelocation.namespace);
      }

      return i;
   }

   public String toDebugFileName() {
      return this.toString().replace('/', '_').replace(':', '_');
   }

   public String toLanguageKey() {
      return this.namespace + "." + this.path;
   }

   public String toShortLanguageKey() {
      return this.namespace.equals("minecraft") ? this.path : this.toLanguageKey();
   }

   public String toLanguageKey(String s) {
      return s + "." + this.toLanguageKey();
   }

   public String toLanguageKey(String s, String s1) {
      return s + "." + this.toLanguageKey() + "." + s1;
   }

   public static ResourceLocation read(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      while(stringreader.canRead() && isAllowedInResourceLocation(stringreader.peek())) {
         stringreader.skip();
      }

      String s = stringreader.getString().substring(i, stringreader.getCursor());

      try {
         return new ResourceLocation(s);
      } catch (ResourceLocationException var4) {
         stringreader.setCursor(i);
         throw ERROR_INVALID.createWithContext(stringreader);
      }
   }

   public static boolean isAllowedInResourceLocation(char c0) {
      return c0 >= '0' && c0 <= '9' || c0 >= 'a' && c0 <= 'z' || c0 == '_' || c0 == ':' || c0 == '/' || c0 == '.' || c0 == '-';
   }

   private static boolean isValidPath(String s) {
      for(int i = 0; i < s.length(); ++i) {
         if (!validPathChar(s.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   private static boolean isValidNamespace(String s) {
      for(int i = 0; i < s.length(); ++i) {
         if (!validNamespaceChar(s.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   private static String assertValidNamespace(String s, String s1) {
      if (!isValidNamespace(s)) {
         throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + s + ":" + s1);
      } else {
         return s;
      }
   }

   public static boolean validPathChar(char c0) {
      return c0 == '_' || c0 == '-' || c0 >= 'a' && c0 <= 'z' || c0 >= '0' && c0 <= '9' || c0 == '/' || c0 == '.';
   }

   private static boolean validNamespaceChar(char c0) {
      return c0 == '_' || c0 == '-' || c0 >= 'a' && c0 <= 'z' || c0 >= '0' && c0 <= '9' || c0 == '.';
   }

   public static boolean isValidResourceLocation(String s) {
      String[] astring = decompose(s, ':');
      return isValidNamespace(StringUtils.isEmpty(astring[0]) ? "minecraft" : astring[0]) && isValidPath(astring[1]);
   }

   private static String assertValidPath(String s, String s1) {
      if (!isValidPath(s1)) {
         throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + s + ":" + s1);
      } else {
         return s1;
      }
   }

   protected interface Dummy {
   }

   public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
      public ResourceLocation deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         return new ResourceLocation(GsonHelper.convertToString(jsonelement, "location"));
      }

      public JsonElement serialize(ResourceLocation resourcelocation, Type type, JsonSerializationContext jsonserializationcontext) {
         return new JsonPrimitive(resourcelocation.toString());
      }
   }
}
