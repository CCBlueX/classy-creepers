import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        File attachLibrary = extractAttachLibrary();
        System.setProperty("java.library.path", attachLibrary.getAbsolutePath());

        VirtualMachineDescriptor minecraft = findMinecraft();
        if (minecraft == null) {
            System.err.println("No minecraft game instance found :c");
            for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
                System.out.println(descriptor.displayName());
            }
            System.exit(-1);
        }

        File agent = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        VirtualMachine.attach(minecraft).loadAgent(agent.getAbsolutePath(), "");
    }

    private static VirtualMachineDescriptor findMinecraft() {
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (descriptor.displayName().contains("--version")) {
                return descriptor;
            }
        }
        return null;
    }

    private static File extractAttachLibrary() throws IOException {
        String resource = attachLibraryResource();
        if (resource == null) {
            System.out.println("Not supported OS");
            System.exit(-1);
        }

        File library = new File(resource.substring(resource.lastIndexOf('/') + 1));
        if (!library.exists()) {
            try (InputStream stream = Main.class.getResourceAsStream(resource)) {
                Files.copy(stream, library.toPath());
            }
        }
        return library;
    }

    private static String attachLibraryResource() {
        String arch = System.getProperty("os.arch");
        String bits = arch.equals("amd64") || arch.equals("x86_64") ? "64" : "32";
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "/lib/windows/" + bits + "/attach.dll";
        }
        if (os.contains("mac")) {
            return "/lib/osx/attach.dylib";
        }
        if (os.contains("solaris") || os.contains("sunos") || os.contains("linux") || os.contains("unix")) {
            return "/lib/linux/" + bits + "/attach.so";
        }
        return null;
    }
}
