package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final Properties properties;

   public Settings(Properties properties) {
      this.properties = properties;
   }

   public static Properties loadFromFile(Path path) {
      try {
         try {
            InputStream inputstream = Files.newInputStream(path);

            Properties var13;
            try {
               CharsetDecoder charsetdecoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
               Properties properties = new Properties();
               properties.load(new InputStreamReader(inputstream, charsetdecoder));
               var13 = properties;
            } catch (Throwable var8) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable var6) {
                     var8.addSuppressed(var6);
                  }
               }

               throw var8;
            }

            if (inputstream != null) {
               inputstream.close();
            }

            return var13;
         } catch (CharacterCodingException var9) {
            LOGGER.info("Failed to load properties as UTF-8 from file {}, trying ISO_8859_1", (Object)path);
            Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1);

            Properties var4;
            try {
               Properties properties1 = new Properties();
               properties1.load(reader);
               var4 = properties1;
            } catch (Throwable var7) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var5) {
                     var7.addSuppressed(var5);
                  }
               }

               throw var7;
            }

            if (reader != null) {
               reader.close();
            }

            return var4;
         }
      } catch (IOException var10) {
         LOGGER.error("Failed to load properties from file: {}", path, var10);
         return new Properties();
      }
   }

   public void store(Path path) {
      try {
         Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

         try {
            this.properties.store(writer, "Minecraft server properties");
         } catch (Throwable var6) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var7) {
         LOGGER.error("Failed to store properties to file: {}", (Object)path);
      }

   }

   private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> function) {
      return (s) -> {
         try {
            return function.apply(s);
         } catch (NumberFormatException var3) {
            return (V)null;
         }
      };
   }

   protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> intfunction, Function<String, V> function) {
      return (s) -> {
         try {
            return intfunction.apply(Integer.parseInt(s));
         } catch (NumberFormatException var4) {
            return function.apply(s);
         }
      };
   }

   @Nullable
   private String getStringRaw(String s) {
      return (String)this.properties.get(s);
   }

   @Nullable
   protected <V> V getLegacy(String s, Function<String, V> function) {
      String s1 = this.getStringRaw(s);
      if (s1 == null) {
         return (V)null;
      } else {
         this.properties.remove(s);
         return function.apply(s1);
      }
   }

   protected <V> V get(String s, Function<String, V> function, Function<V, String> function1, V object) {
      String s1 = this.getStringRaw(s);
      V object1 = MoreObjects.firstNonNull((V)(s1 != null ? function.apply(s1) : null), object);
      this.properties.put(s, function1.apply(object1));
      return object1;
   }

   protected <V> Settings<T>.MutableValue<V> getMutable(String s, Function<String, V> function, Function<V, String> function1, V object) {
      String s1 = this.getStringRaw(s);
      V object1 = MoreObjects.firstNonNull((V)(s1 != null ? function.apply(s1) : null), object);
      this.properties.put(s, function1.apply(object1));
      return new Settings.MutableValue<>(s, object1, function1);
   }

   protected <V> V get(String s, Function<String, V> function, UnaryOperator<V> unaryoperator, Function<V, String> function1, V object) {
      return this.get(s, (s1) -> {
         V object1 = function.apply(s1);
         return (V)(object1 != null ? unaryoperator.apply(object1) : null);
      }, function1, object);
   }

   protected <V> V get(String s, Function<String, V> function, V object) {
      return this.get(s, function, Objects::toString, object);
   }

   protected <V> Settings<T>.MutableValue<V> getMutable(String s, Function<String, V> function, V object) {
      return this.getMutable(s, function, Objects::toString, object);
   }

   protected String get(String s, String s1) {
      return this.get(s, Function.identity(), Function.identity(), s1);
   }

   @Nullable
   protected String getLegacyString(String s) {
      return this.getLegacy(s, Function.identity());
   }

   protected int get(String s, int i) {
      return this.get(s, wrapNumberDeserializer(Integer::parseInt), Integer.valueOf(i));
   }

   protected Settings<T>.MutableValue<Integer> getMutable(String s, int i) {
      return this.getMutable(s, wrapNumberDeserializer(Integer::parseInt), i);
   }

   protected int get(String s, UnaryOperator<Integer> unaryoperator, int i) {
      return this.get(s, wrapNumberDeserializer(Integer::parseInt), unaryoperator, Objects::toString, i);
   }

   protected long get(String s, long i) {
      return this.get(s, wrapNumberDeserializer(Long::parseLong), i);
   }

   protected boolean get(String s, boolean flag) {
      return this.get(s, Boolean::valueOf, flag);
   }

   protected Settings<T>.MutableValue<Boolean> getMutable(String s, boolean flag) {
      return this.getMutable(s, Boolean::valueOf, flag);
   }

   @Nullable
   protected Boolean getLegacyBoolean(String s) {
      return this.getLegacy(s, Boolean::valueOf);
   }

   protected Properties cloneProperties() {
      Properties properties = new Properties();
      properties.putAll(this.properties);
      return properties;
   }

   protected abstract T reload(RegistryAccess registryaccess, Properties properties);

   public class MutableValue<V> implements Supplier<V> {
      private final String key;
      private final V value;
      private final Function<V, String> serializer;

      MutableValue(String s, V object, Function<V, String> function) {
         this.key = s;
         this.value = object;
         this.serializer = function;
      }

      public V get() {
         return this.value;
      }

      public T update(RegistryAccess registryaccess, V object) {
         Properties properties = Settings.this.cloneProperties();
         properties.put(this.key, this.serializer.apply(object));
         return Settings.this.reload(registryaccess, properties);
      }
   }
}
