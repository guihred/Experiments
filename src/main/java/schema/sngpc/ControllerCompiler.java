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
import sun.misc.Unsafe;

public class ControllerCompiler {

    public static List<String> compileClass(File customRwa) {
        final List<String> diagnosticMsg = new ArrayList<>();
        try {
            Class<ControllerCompiler> class1 = ControllerCompiler.class;
            String packageName = class1.getPackage().getName();
            final String source = Files.toString(customRwa, StandardCharsets.UTF_8);

            String className = customRwa.getName().replaceAll(".java", "");
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            final SimpleJavaFileObject simpleJavaFileObject = new SimpleJavaFileObject(customRwa.toURI(),
                SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source.toString();
                }

                @Override
                public OutputStream openOutputStream() throws IOException {
                    return byteArrayOutputStream;
                }
            };

            String property = new File(System.getProperty("java.home")).getParent();
            System.setProperty("java.home", property);

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
            Class<?> aClass = null;
            final Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            final Unsafe unsafe = (Unsafe) f.get(null);
//            aClass = unsafe.defineAnonymousClass(ControllerCompiler.class, bytes, null);
            String fullClassName = packageName + "." + className;
            aClass = unsafe.defineClass(fullClassName, bytes, 0, bytes.length,
                ClassLoader.getSystemClassLoader(),
                class1.getProtectionDomain());
            Class.forName(fullClassName);
            aClass.newInstance();
            diagnosticMsg.add("Classe Adicionada com sucesso");
        } catch (Throwable e) {
            if (e instanceof LinkageError) {
                diagnosticMsg.add("Classe já adicionada");
            } else {
                diagnosticMsg.add(e.getLocalizedMessage());
            }
        }
        return diagnosticMsg;
    }

}
