package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestRegistry {
   private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
   private static final Set<String> TEST_CLASS_NAMES = Sets.newHashSet();
   private static final Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS = Maps.newHashMap();
   private static final Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS = Maps.newHashMap();
   private static final Collection<TestFunction> LAST_FAILED_TESTS = Sets.newHashSet();

   public static void register(Class<?> oclass) {
      Arrays.stream(oclass.getDeclaredMethods()).forEach(GameTestRegistry::register);
   }

   public static void register(Method method) {
      String s = method.getDeclaringClass().getSimpleName();
      GameTest gametest = method.getAnnotation(GameTest.class);
      if (gametest != null) {
         TEST_FUNCTIONS.add(turnMethodIntoTestFunction(method));
         TEST_CLASS_NAMES.add(s);
      }

      GameTestGenerator gametestgenerator = method.getAnnotation(GameTestGenerator.class);
      if (gametestgenerator != null) {
         TEST_FUNCTIONS.addAll(useTestGeneratorMethod(method));
         TEST_CLASS_NAMES.add(s);
      }

      registerBatchFunction(method, BeforeBatch.class, BeforeBatch::batch, BEFORE_BATCH_FUNCTIONS);
      registerBatchFunction(method, AfterBatch.class, AfterBatch::batch, AFTER_BATCH_FUNCTIONS);
   }

   private static <T extends Annotation> void registerBatchFunction(Method method, Class<T> oclass, Function<T, String> function, Map<String, Consumer<ServerLevel>> map) {
      T annotation = method.getAnnotation(oclass);
      if (annotation != null) {
         String s = function.apply(annotation);
         Consumer<ServerLevel> consumer = map.putIfAbsent(s, turnMethodIntoConsumer(method));
         if (consumer != null) {
            throw new RuntimeException("Hey, there should only be one " + oclass + " method per batch. Batch '" + s + "' has more than one!");
         }
      }

   }

   public static Collection<TestFunction> getTestFunctionsForClassName(String s) {
      return TEST_FUNCTIONS.stream().filter((testfunction) -> isTestFunctionPartOfClass(testfunction, s)).collect(Collectors.toList());
   }

   public static Collection<TestFunction> getAllTestFunctions() {
      return TEST_FUNCTIONS;
   }

   public static Collection<String> getAllTestClassNames() {
      return TEST_CLASS_NAMES;
   }

   public static boolean isTestClass(String s) {
      return TEST_CLASS_NAMES.contains(s);
   }

   @Nullable
   public static Consumer<ServerLevel> getBeforeBatchFunction(String s) {
      return BEFORE_BATCH_FUNCTIONS.get(s);
   }

   @Nullable
   public static Consumer<ServerLevel> getAfterBatchFunction(String s) {
      return AFTER_BATCH_FUNCTIONS.get(s);
   }

   public static Optional<TestFunction> findTestFunction(String s) {
      return getAllTestFunctions().stream().filter((testfunction) -> testfunction.getTestName().equalsIgnoreCase(s)).findFirst();
   }

   public static TestFunction getTestFunction(String s) {
      Optional<TestFunction> optional = findTestFunction(s);
      if (!optional.isPresent()) {
         throw new IllegalArgumentException("Can't find the test function for " + s);
      } else {
         return optional.get();
      }
   }

   private static Collection<TestFunction> useTestGeneratorMethod(Method method) {
      try {
         Object object = method.getDeclaringClass().newInstance();
         return (Collection)method.invoke(object);
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }

   private static TestFunction turnMethodIntoTestFunction(Method method) {
      GameTest gametest = method.getAnnotation(GameTest.class);
      String s = method.getDeclaringClass().getSimpleName();
      String s1 = s.toLowerCase();
      String s2 = s1 + "." + method.getName().toLowerCase();
      String s3 = gametest.template().isEmpty() ? s2 : s1 + "." + gametest.template();
      String s4 = gametest.batch();
      Rotation rotation = StructureUtils.getRotationForRotationSteps(gametest.rotationSteps());
      return new TestFunction(s4, s2, s3, rotation, gametest.timeoutTicks(), gametest.setupTicks(), gametest.required(), gametest.requiredSuccesses(), gametest.attempts(), turnMethodIntoConsumer(method));
   }

   private static Consumer<?> turnMethodIntoConsumer(Method method) {
      return (object) -> {
         try {
            Object object1 = method.getDeclaringClass().newInstance();
            method.invoke(object1, object);
         } catch (InvocationTargetException var3) {
            if (var3.getCause() instanceof RuntimeException) {
               throw (RuntimeException)var3.getCause();
            } else {
               throw new RuntimeException(var3.getCause());
            }
         } catch (ReflectiveOperationException var4) {
            throw new RuntimeException(var4);
         }
      };
   }

   private static boolean isTestFunctionPartOfClass(TestFunction testfunction, String s) {
      return testfunction.getTestName().toLowerCase().startsWith(s.toLowerCase() + ".");
   }

   public static Collection<TestFunction> getLastFailedTests() {
      return LAST_FAILED_TESTS;
   }

   public static void rememberFailedTest(TestFunction testfunction) {
      LAST_FAILED_TESTS.add(testfunction);
   }

   public static void forgetFailedTests() {
      LAST_FAILED_TESTS.clear();
   }
}
