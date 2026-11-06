package sexy.poke;

import static org.objectweb.asm.Opcodes.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class ModelWrapperFactory {
    public static final String CLASS_NAME = "sexy/poke/CreeperModel";

    private static final String OBJECT = "Ljava/lang/Object;";
    private static final String GL11_CLASS = "org/lwjgl/opengl/GL11";
    private static final String TEXTURE_URL =
        "https://raw.githubusercontent.com/SenkJu/senkjuspix/f67cafc02f7f237ba32992024b512416641bc561/classycreepers/textures/tophat.png";

    private static final float DEGREES_PER_RADIAN = 180.0F / (float) Math.PI;
    private static final float ROTATION_POINT_Y = 6.0F;

    private static final Part[] PARTS = {
        new Part("brim1", 9, 0, 4, -8, -4, 1, 1, 8),
        new Part("brim2", 9, 0, -5, -8, -4, 1, 1, 8),
        new Part("brim3", 9, 0, -5, -8, 4, 10, 1, 1),
        new Part("brim4", 9, 0, -5, -8, -5, 10, 1, 1),
        new Part("hat", 10, 0, -4, -16, -4, 8, 6, 8),
        new Part("hat2", 0, 22, -4, -10, -4, 8, 2, 8),
        new Part("mono1", 0, 0, 1, -7, -5, 2, 1, 1),
        new Part("mono2", 0, 0, 1, -4, -5, 2, 1, 1),
        new Part("mono3", 0, 0, 0, -6, -5, 1, 2, 1),
        new Part("mono4", 0, 0, 3, -6, -5, 1, 2, 1),
        new Part("mono5", 0, 0, 4, -6, -4, 1, 1, 1),
        new Part("mono6", 0, 0, 4, -5, -3, 1, 2, 1),
        new Part("mono7", 0, 0, 4, -3, -2, 1, 2, 1),
        new Part("mono8", 0, 0, 4, -1, -1, 1, 1, 1),
        new Part("mono_glass", -1, 19, 1, -6, -5, 2, 2, 1)
    };

    private ModelWrapperFactory() {
    }

    public static byte[] create(CreeperModelMappings mappings) {
        ClassWriter writer = new ClassWriter(0);
        String modelRenderer = "L" + mappings.modelRenderer + ";";

        writer.visit(V1_8, ACC_PUBLIC, CLASS_NAME, null, "java/lang/Object", null);
        for (Part part : PARTS) {
            writer.visitField(ACC_PRIVATE | ACC_STATIC, part.name, modelRenderer, null, null).visitEnd();
        }
        writer.visitField(ACC_PRIVATE | ACC_STATIC, "texture", "I", null, null).visitEnd();

        writeConstructor(writer);
        writeRender(writer, mappings, modelRenderer);
        writeSetRotationAngles(writer, mappings, modelRenderer);
        writeInit(writer, mappings, modelRenderer);
        writeGetCreeperTexture(writer);
        writeLoadTextureNearest(writer);

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void writeConstructor(ClassWriter writer) {
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        method.visitInsn(RETURN);
        method.visitMaxs(1, 1);
        method.visitEnd();
    }

    private static void writeRender(ClassWriter writer, CreeperModelMappings mappings, String modelRenderer) {
        int scale = 6;
        int model = 7;
        int previousTexture = 8;

        MethodVisitor method = writer.visitMethod(
            ACC_PUBLIC | ACC_STATIC, "render", withModelParameter(mappings.renderDescriptor), null, null);
        method.visitCode();
        initIfNeeded(method, modelRenderer, model);

        pushInt(method, GL_TEXTURE_BINDING_2D);
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glGetInteger", "(I)I", false);
        method.visitVarInsn(ISTORE, previousTexture);
        bindTexture(method);

        for (Part part : PARTS) {
            method.visitFieldInsn(GETSTATIC, CLASS_NAME, part.name, modelRenderer);
            method.visitVarInsn(FLOAD, scale);
            method.visitMethodInsn(INVOKEVIRTUAL, mappings.modelRenderer, mappings.renderMethod, "(F)V", false);
        }

        pushInt(method, GL_TEXTURE_2D);
        method.visitVarInsn(ILOAD, previousTexture);
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glBindTexture", "(II)V", false);
        method.visitInsn(RETURN);
        method.visitMaxs(2, 9);
        method.visitEnd();
    }

    private static void writeSetRotationAngles(ClassWriter writer, CreeperModelMappings mappings, String modelRenderer) {
        int headYaw = 3;
        int headPitch = 4;
        int model = 7;

        MethodVisitor method = writer.visitMethod(
            ACC_PUBLIC | ACC_STATIC, "setRotationAngles", withModelParameter(mappings.rotationAnglesDescriptor), null, null);
        method.visitCode();
        initIfNeeded(method, modelRenderer, model);

        for (Part part : PARTS) {
            setRotation(method, mappings, modelRenderer, part, headYaw, mappings.rotateAngleY);
            setRotation(method, mappings, modelRenderer, part, headPitch, mappings.rotateAngleX);
        }

        method.visitInsn(RETURN);
        method.visitMaxs(3, 8);
        method.visitEnd();
    }

    private static void setRotation(
        MethodVisitor method, CreeperModelMappings mappings, String modelRenderer, Part part, int degrees, String field) {
        method.visitFieldInsn(GETSTATIC, CLASS_NAME, part.name, modelRenderer);
        method.visitVarInsn(FLOAD, degrees);
        pushFloat(method, DEGREES_PER_RADIAN);
        method.visitInsn(FDIV);
        method.visitFieldInsn(PUTFIELD, mappings.modelRenderer, field, "F");
    }

    private static void writeInit(ClassWriter writer, CreeperModelMappings mappings, String modelRenderer) {
        int model = 0;

        MethodVisitor method = writer.visitMethod(ACC_PRIVATE | ACC_STATIC, "init", "(" + OBJECT + ")V", null, null);
        method.visitCode();

        for (Part part : PARTS) {
            method.visitTypeInsn(NEW, mappings.modelRenderer);
            method.visitInsn(DUP);
            method.visitVarInsn(ALOAD, model);
            method.visitTypeInsn(CHECKCAST, mappings.modelBase);
            pushInt(method, part.textureU);
            pushInt(method, part.textureV);
            method.visitMethodInsn(
                INVOKESPECIAL, mappings.modelRenderer, "<init>", "(L" + mappings.modelBase + ";II)V", false);
            method.visitInsn(DUP);
            method.visitFieldInsn(PUTSTATIC, CLASS_NAME, part.name, modelRenderer);
            pushFloat(method, part.x);
            pushFloat(method, part.y);
            pushFloat(method, part.z);
            pushInt(method, part.width);
            pushInt(method, part.height);
            pushInt(method, part.depth);
            pushFloat(method, 0.0F);
            method.visitMethodInsn(INVOKEVIRTUAL, mappings.modelRenderer, mappings.addBoxMethod, "(FFFIIIF)V", false);

            method.visitFieldInsn(GETSTATIC, CLASS_NAME, part.name, modelRenderer);
            pushFloat(method, 0.0F);
            pushFloat(method, ROTATION_POINT_Y);
            pushFloat(method, 0.0F);
            method.visitMethodInsn(INVOKEVIRTUAL, mappings.modelRenderer, mappings.setRotationPointMethod, "(FFF)V", false);
        }

        method.visitMethodInsn(INVOKESTATIC, CLASS_NAME, "getCreeperTexture", "()I", false);
        method.visitFieldInsn(PUTSTATIC, CLASS_NAME, "texture", "I");
        method.visitInsn(RETURN);
        method.visitMaxs(8, 1);
        method.visitEnd();
    }

    private static void writeGetCreeperTexture(ClassWriter writer) {
        MethodVisitor method = writer.visitMethod(ACC_PRIVATE | ACC_STATIC, "getCreeperTexture", "()I", null, null);
        method.visitCode();

        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label handler = new Label();
        method.visitTryCatchBlock(tryStart, tryEnd, handler, "java/lang/Exception");

        method.visitLabel(tryStart);
        method.visitTypeInsn(NEW, "java/net/URL");
        method.visitInsn(DUP);
        method.visitLdcInsn(TEXTURE_URL);
        method.visitMethodInsn(INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;)V", false);
        method.visitMethodInsn(
            INVOKESTATIC, "javax/imageio/ImageIO", "read", "(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);
        method.visitMethodInsn(
            INVOKESTATIC, CLASS_NAME, "loadTextureNearest", "(Ljava/awt/image/BufferedImage;)I", false);
        method.visitLabel(tryEnd);
        method.visitInsn(IRETURN);

        method.visitLabel(handler);
        method.visitFrame(F_SAME1, 0, null, 1, new Object[] {"java/lang/Exception"});
        method.visitVarInsn(ASTORE, 0);
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
        method.visitInsn(ICONST_M1);
        method.visitInsn(IRETURN);
        method.visitMaxs(3, 1);
        method.visitEnd();
    }

    private static void writeLoadTextureNearest(ClassWriter writer) {
        String bufferedImage = "java/awt/image/BufferedImage";
        String byteBuffer = "java/nio/ByteBuffer";
        int image = 0;
        int buffer = 1;
        int y = 2;
        int x = 3;
        int rgb = 4;
        int texture = 2;

        MethodVisitor method = writer.visitMethod(
            ACC_PUBLIC | ACC_STATIC, "loadTextureNearest", "(L" + bufferedImage + ";)I", null, null);
        method.visitCode();

        method.visitVarInsn(ALOAD, image);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getWidth", "()I", false);
        method.visitVarInsn(ALOAD, image);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getHeight", "()I", false);
        method.visitInsn(IMUL);
        pushInt(method, 4);
        method.visitInsn(IMUL);
        method.visitMethodInsn(INVOKESTATIC, "org/lwjgl/BufferUtils", "createByteBuffer", "(I)L" + byteBuffer + ";", false);
        method.visitVarInsn(ASTORE, buffer);

        Label rows = new Label();
        Label rowsDone = new Label();
        Label columns = new Label();
        Label columnsDone = new Label();

        pushInt(method, 0);
        method.visitVarInsn(ISTORE, y);
        method.visitLabel(rows);
        method.visitFrame(F_APPEND, 2, new Object[] {byteBuffer, INTEGER}, 0, null);
        method.visitVarInsn(ILOAD, y);
        method.visitVarInsn(ALOAD, image);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getHeight", "()I", false);
        method.visitJumpInsn(IF_ICMPEQ, rowsDone);

        pushInt(method, 0);
        method.visitVarInsn(ISTORE, x);
        method.visitLabel(columns);
        method.visitFrame(F_APPEND, 1, new Object[] {INTEGER}, 0, null);
        method.visitVarInsn(ILOAD, x);
        method.visitVarInsn(ALOAD, image);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getWidth", "()I", false);
        method.visitJumpInsn(IF_ICMPEQ, columnsDone);

        method.visitVarInsn(ALOAD, image);
        method.visitVarInsn(ILOAD, x);
        method.visitVarInsn(ILOAD, y);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getRGB", "(II)I", false);
        method.visitVarInsn(ISTORE, rgb);
        for (int shift : new int[] {16, 8, 0, 24}) {
            method.visitVarInsn(ALOAD, buffer);
            method.visitVarInsn(ILOAD, rgb);
            if (shift != 0) {
                pushInt(method, shift);
                method.visitInsn(ISHR);
            }
            pushInt(method, 255);
            method.visitInsn(IAND);
            method.visitInsn(I2B);
            method.visitMethodInsn(INVOKEVIRTUAL, byteBuffer, "put", "(B)L" + byteBuffer + ";", false);
            method.visitInsn(POP);
        }
        method.visitIincInsn(x, 1);
        method.visitJumpInsn(GOTO, columns);

        method.visitLabel(columnsDone);
        method.visitFrame(F_CHOP, 1, null, 0, null);
        method.visitIincInsn(y, 1);
        method.visitJumpInsn(GOTO, rows);

        method.visitLabel(rowsDone);
        method.visitFrame(F_CHOP, 1, null, 0, null);
        method.visitVarInsn(ALOAD, buffer);
        method.visitMethodInsn(INVOKEVIRTUAL, byteBuffer, "flip", "()Ljava/nio/Buffer;", false);
        method.visitInsn(POP);

        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glGetError", "()I", false);
        method.visitInsn(POP);
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glGenTextures", "()I", false);
        method.visitVarInsn(ISTORE, texture);
        pushInt(method, GL_TEXTURE_2D);
        method.visitVarInsn(ILOAD, texture);
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glBindTexture", "(II)V", false);
        setTextureParameter(method, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        setTextureParameter(method, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        setTextureParameter(method, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        setTextureParameter(method, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        pushInt(method, GL_TEXTURE_2D);
        pushInt(method, 0);
        pushInt(method, GL_RGBA8);
        method.visitVarInsn(ALOAD, image);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getWidth", "()I", false);
        method.visitVarInsn(ALOAD, image);
        method.visitMethodInsn(INVOKEVIRTUAL, bufferedImage, "getHeight", "()I", false);
        pushInt(method, 0);
        pushInt(method, GL_RGBA);
        pushInt(method, GL_UNSIGNED_BYTE);
        method.visitVarInsn(ALOAD, buffer);
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glTexImage2D", "(IIIIIIIIL" + byteBuffer + ";)V", false);

        method.visitVarInsn(ILOAD, texture);
        method.visitInsn(IRETURN);
        method.visitMaxs(9, 5);
        method.visitEnd();
    }

    private static void initIfNeeded(MethodVisitor method, String modelRenderer, int model) {
        Label initialized = new Label();
        method.visitFieldInsn(GETSTATIC, CLASS_NAME, "hat", modelRenderer);
        method.visitJumpInsn(IFNONNULL, initialized);
        method.visitVarInsn(ALOAD, model);
        method.visitMethodInsn(INVOKESTATIC, CLASS_NAME, "init", "(" + OBJECT + ")V", false);
        method.visitLabel(initialized);
        method.visitFrame(F_SAME, 0, null, 0, null);
    }

    private static void bindTexture(MethodVisitor method) {
        pushInt(method, GL_TEXTURE_2D);
        method.visitFieldInsn(GETSTATIC, CLASS_NAME, "texture", "I");
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glBindTexture", "(II)V", false);
    }

    private static void setTextureParameter(MethodVisitor method, int name, int value) {
        pushInt(method, GL_TEXTURE_2D);
        pushInt(method, name);
        pushInt(method, value);
        method.visitMethodInsn(INVOKESTATIC, GL11_CLASS, "glTexParameteri", "(III)V", false);
    }

    static String withModelParameter(String descriptor) {
        return descriptor.replace(")V", OBJECT + ")V");
    }

    private static void pushInt(MethodVisitor method, int value) {
        if (value >= -1 && value <= 5) {
            method.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            method.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            method.visitIntInsn(SIPUSH, value);
        } else {
            method.visitLdcInsn(value);
        }
    }

    private static void pushFloat(MethodVisitor method, float value) {
        if (value == 0.0F || value == 1.0F || value == 2.0F) {
            method.visitInsn(FCONST_0 + (int) value);
        } else {
            method.visitLdcInsn(value);
        }
    }

    private static final class Part {
        final String name;
        final int textureU;
        final int textureV;
        final float x;
        final float y;
        final float z;
        final int width;
        final int height;
        final int depth;

        Part(String name, int textureU, int textureV, float x, float y, float z, int width, int height, int depth) {
            this.name = name;
            this.textureU = textureU;
            this.textureV = textureV;
            this.x = x;
            this.y = y;
            this.z = z;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
    }
}
