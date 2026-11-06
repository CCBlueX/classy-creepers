package sexy.poke;

import static org.objectweb.asm.Opcodes.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class AgentMain {
    private static final float CREEPER_LEG_SWING_SPEED = 0.6662F;
    private static final int CREEPER_MODEL_FIELD_COUNT = 7;
    private static final int ROTATE_ANGLE_X_FIELD = 7;
    private static final int ROTATE_ANGLE_Y_FIELD = 8;

    private static final String[] IGNORED_PACKAGES = {
        "java.", "sun.", "javax.", "com.sun.", "org.apache.", "org.objectweb.", "com.google.",
        "joptsimple.", "com.mojang.", "org.lwjgl", "oshi.", "io.netty"
    };

    private AgentMain() {
    }

    public static void agentmain(String arguments, Instrumentation instrumentation) {
        try {
            instrumentation.addTransformer(new CreeperModelTransformer(), true);
            for (Class<?> type : retransformableClasses(instrumentation)) {
                if (!type.getName().contains("Lambda")) {
                    instrumentation.retransformClasses(type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class CreeperModelTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(
            ClassLoader loader, String className, Class<?> redefinedClass, ProtectionDomain domain, byte[] bytes) {
            try {
                ClassNode node = readClass(bytes);
                if (isCreeperModel(node)) {
                    return patchCreeperModel(node, loader);
                }
                if (node.name.equals("java/lang/ClassLoader")) {
                    return disableCertificateCheck(node);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static boolean isCreeperModel(ClassNode node) {
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode
                    && ((LdcInsnNode) instruction).cst.equals(CREEPER_LEG_SWING_SPEED)) {
                    return node.fields.size() == CREEPER_MODEL_FIELD_COUNT;
                }
            }
        }
        return false;
    }

    private static byte[] patchCreeperModel(ClassNode node, ClassLoader loader) throws Exception {
        System.out.println("found CreeperModel " + node.name);

        Class<?> modelRenderer = Class.forName(Type.getType(node.fields.get(0).desc).getClassName());
        Field[] rendererFields = modelRenderer.getDeclaredFields();

        CreeperModelMappings mappings = new CreeperModelMappings();
        mappings.modelBase = node.superName;
        mappings.modelRenderer = Type.getInternalName(modelRenderer);
        mappings.rotateAngleX = rendererFields[ROTATE_ANGLE_X_FIELD].getName();
        mappings.rotateAngleY = rendererFields[ROTATE_ANGLE_Y_FIELD].getName();
        System.out.println("found ModelRenderer " + mappings.modelRenderer);
        System.out.println("found rotateY " + mappings.rotateAngleY);
        System.out.println("found rotateX " + mappings.rotateAngleX);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                findModelRendererSetup(method, mappings);
            } else if (!method.name.startsWith("<")) {
                if (method.desc.startsWith("(F")) {
                    System.out.println("hooked rotatepoint");
                    mappings.rotationAnglesDescriptor = method.desc;
                    callBeforeReturn(method, "setRotationAngles");
                } else {
                    System.out.println("hooked renderpoint");
                    mappings.renderDescriptor = method.desc;
                    findPartRender(method, mappings);
                    callBeforeReturn(method, "render");
                }
            }
        }

        byte[] patched = writeClass(node);
        defineClass(ModelWrapperFactory.create(mappings), loader);
        return patched;
    }

    private static void findModelRendererSetup(MethodNode constructor, CreeperModelMappings mappings) {
        for (AbstractInsnNode instruction : constructor.instructions.toArray()) {
            if (instruction.getOpcode() != INVOKEVIRTUAL) {
                continue;
            }
            String name = ((MethodInsnNode) instruction).name;
            if (mappings.addBoxMethod == null) {
                mappings.addBoxMethod = name;
            } else if (mappings.setRotationPointMethod == null) {
                mappings.setRotationPointMethod = name;
            }
        }
    }

    private static void findPartRender(MethodNode render, CreeperModelMappings mappings) {
        for (AbstractInsnNode instruction : render.instructions.toArray()) {
            if (mappings.renderMethod == null
                && instruction.getOpcode() == INVOKEVIRTUAL
                && ((MethodInsnNode) instruction).desc.equals("(F)V")) {
                mappings.renderMethod = ((MethodInsnNode) instruction).name;
            }
        }
    }

    private static void callBeforeReturn(MethodNode method, String target) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() != RETURN) {
                continue;
            }
            InsnList call = new InsnList();
            int slot = 1;
            for (Type argument : Type.getArgumentTypes(method.desc)) {
                call.add(new VarInsnNode(argument.getOpcode(ILOAD), slot));
                slot += argument.getSize();
            }
            call.add(new VarInsnNode(ALOAD, 0));
            call.add(new MethodInsnNode(
                INVOKESTATIC,
                ModelWrapperFactory.CLASS_NAME,
                target,
                ModelWrapperFactory.withModelParameter(method.desc),
                false));
            method.instructions.insertBefore(instruction, call);
        }
    }

    private static byte[] disableCertificateCheck(ClassNode node) {
        System.out.println("Modify Classloader");
        for (MethodNode method : node.methods) {
            if (method.name.equals("checkCerts")) {
                method.instructions.insert(new InsnNode(RETURN));
            }
        }
        return writeClass(node);
    }

    private static void defineClass(byte[] bytes, ClassLoader loader) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            defineClass.invoke(loader, bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?>[] retransformableClasses(Instrumentation instrumentation) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(ClassLoader.class);
        for (Class<?> type : instrumentation.getAllLoadedClasses()) {
            if (type != null
                && instrumentation.isModifiableClass(type)
                && type.getClassLoader() != null
                && !isIgnored(type.getName())) {
                classes.add(type);
            }
        }
        return classes.toArray(new Class<?>[0]);
    }

    private static boolean isIgnored(String className) {
        for (String prefix : IGNORED_PACKAGES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static ClassNode readClass(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        try {
            reader.accept(node, 0);
        } catch (RuntimeException e) {
            node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return node;
    }

    private static byte[] writeClass(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
