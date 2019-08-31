package schema.sngpc;

import static java.util.Collections.singletonList;
import static javax.tools.JavaFileObject.Kind.SOURCE;

import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.*;
import org.slf4j.Logger;
import sun.misc.Unsafe;
import utils.HasLogging;

@SuppressWarnings("restriction")
public final class ControllerCompiler {

    private static final String JAVA_HOME = "java.home";
    private static final Logger LOG = HasLogging.log();

    private ControllerCompiler() {
    }

	public static List<String> compileClass(File customRwa) {
        String property = setJavaHomeProperty();
        final List<String> diagnosticMsg = new ArrayList<>();
        String className = customRwa.getName().replaceAll(".java", "");
        Class<ControllerCompiler> class1 = ControllerCompiler.class;
        String packageName = class1.getPackage().getName();
        String fullClassName = packageName + "." + className;
        try {
            if (isClassExistent(fullClassName)) {
                diagnosticMsg.add("Classe já adicionada");
                System.setProperty(JAVA_HOME, property);
                return diagnosticMsg;
            }

            final String source = Files.toString(customRwa, StandardCharsets.UTF_8);

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            final SimpleJavaFileObject simpleJavaFileObject = new SimpleJavaFileObject(customRwa.toURI(), SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }

                @Override
                public OutputStream openOutputStream() throws IOException {
                    return byteArrayOutputStream;
                }
            };

            final Locale pt = new Locale("pt");
            DiagnosticListener<JavaFileObject> diagnosticListener = diagnostic -> {
                String message = diagnostic.toString();
                diagnosticMsg.add(message);
            };

            StandardJavaFileManager standardFileManager = ToolProvider.getSystemJavaCompiler()
                .getStandardFileManager(diagnosticListener, pt, Charset.defaultCharset());
            final JavaFileManager javaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(
                standardFileManager) {

                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String className1,
                    JavaFileObject.Kind kind, FileObject sibling) throws IOException {
                    return simpleJavaFileObject;
                }
            };

            ToolProvider.getSystemJavaCompiler()
                .getTask(null, javaFileManager, diagnosticListener, null, null, singletonList(simpleJavaFileObject))
                .call();

            final byte[] bytes = byteArrayOutputStream.toByteArray();

            // use the unsafe class to load in the
            // class bytes
            final Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            final Unsafe unsafe = (Unsafe) f.get(null);
            Class<?> aClass = unsafe.defineClass(fullClassName, bytes, 0, bytes.length,
                ClassLoader.getSystemClassLoader(), class1.getProtectionDomain());
            Class.forName(fullClassName);
            aClass.newInstance();
            diagnosticMsg.add("Classe Adicionada com sucesso");
        } catch (LinkageError e) {
            LOG.error("ERROR IN " + className, e);
            diagnosticMsg.add("Classe já adicionada");
        } catch (Throwable e) {
            String localizedMessage = e.getLocalizedMessage();
            diagnosticMsg.add(localizedMessage != null ? localizedMessage : e.getMessage());
            LOG.info("ERROR IN {}", className);
            LOG.error("ERROR IN {}", e);
        }
        System.setProperty(JAVA_HOME, property);
        return diagnosticMsg;
    }

    private static boolean isClassExistent(String fullClassName) {
        try {
            Class.forName(fullClassName);
            return true;
        } catch (ClassNotFoundException e) {
            LOG.trace("CLASS NOT FOUND", e);
            return false;
        }
    }

    private static String setJavaHomeProperty() {
        String property2 = System.getProperty(JAVA_HOME);
        String property = new File(property2).getParent();
        if (property.contains("jdk")) {
            System.setProperty(JAVA_HOME, property);
        }
        return property2;
    }

}
