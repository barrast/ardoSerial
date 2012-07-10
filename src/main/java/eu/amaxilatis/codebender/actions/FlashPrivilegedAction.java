package eu.amaxilatis.codebender.actions;

import eu.amaxilatis.codebender.CodeBenderApplet;
import eu.amaxilatis.codebender.command.AvrdudeLinuxCommand;
import eu.amaxilatis.codebender.command.AvrdudeWindowsCommand;
import jssc.SerialNativeInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;

/**
 * Used to copy the files needed for flashing to the hard drive and perform flashing using avrdude.
 */
public class FlashPrivilegedAction implements PrivilegedAction {
    /**
     * Port to be used.
     */
    private final transient String port;
    /**
     * The file to flash.
     */
    private final transient String file;
    /**
     * The baudRate to use.
     */
    private final transient String baudRate;
    private static final String TEMP_HEX_UNIX = "/tmp/file.hex";
    private static final String AVRDUDE_PATH_UNIX = "/tmp/avrdude";
    private String basepath;

    /**
     * Constructs a new flash action.
     *
     * @param port     the port to use.
     * @param file     the hex file to flash.
     * @param baudRate the baudrate to use during flashing.
     */
    public FlashPrivilegedAction(final String port, final String file, final String baudRate) {
        this.port = port;
        this.file = file;
        this.baudRate = baudRate;

        if ((SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX) ||
                (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X)) {
            basepath = "/tmp/";
        } else {
            basepath = System.getProperty("user.home");
        }
    }

    public Object run() {
        if (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_LINUX) {
            return flashLinux();
        } else if (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X) {
            return flashMacOSX();
        } else {
            return flashWindows();
        }
    }


    /**
     * Used to flash on Windows.
     *
     * @return The flash Status: 0 is OK , else an Error Code is returned.
     */
    private Object flashWindows() {

        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/libusb0.dll", basepath + "\\libusb0.dll");
            makeExecutable(basepath + "\\libusb0.dll");
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.LIBUSB_ERROR;
        }

        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.exe", basepath + "\\avrdude.exe");
            makeExecutable(basepath + "\\avrdude.exe");
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.AVRDUDE_ERROR;
        }

        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.conf.windows", basepath + "\\avrdude.conf");
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.CONF_ERROR;
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(basepath + "\\file.hex");
            fileWriter.write(file);
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            reportError(e);
            return CodeBenderApplet.HEX_ERROR;
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                reportError(e);
            }
        }

        final AvrdudeWindowsCommand flashCommand =
                new AvrdudeWindowsCommand(basepath, port, basepath + "\\file.hex\"", baudRate);

        System.out.println("running : " + flashCommand.toString());

        Process flashProc1 = null;
        try {
            flashProc1 = Runtime.getRuntime().exec(flashCommand.toString());
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.PROCESS_ERROR;
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        flashProc1.destroy();

        return CodeBenderApplet.FLASH_OK;
    }

    private void reportError(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : e.getStackTrace()) {
            sb.append(el.toString()).append("\n");
        }
        CodeBenderApplet.errorMessage = sb.toString();
    }

    private Object flashMacOSX() {
        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.mac", AVRDUDE_PATH_UNIX);
            makeExecutable(AVRDUDE_PATH_UNIX);
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.AVRDUDE_ERROR;
        }


        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.conf.mac", basepath + "avrdude.conf");
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.CONF_ERROR;
        }

        try {
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(TEMP_HEX_UNIX);
            fileWriter.write(file);
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            reportError(e);
            return CodeBenderApplet.HEX_ERROR;
        }

        final AvrdudeLinuxCommand flashCommand =
                new AvrdudeLinuxCommand(basepath, port, TEMP_HEX_UNIX, baudRate);

        try {
            System.out.println("running : " + flashCommand.toString());
            final Process flashProc = Runtime.getRuntime().exec(flashCommand.toString());
            try {
                flashProc.waitFor();
                System.out.println("flashed=" + flashProc.exitValue());
                return flashProc.exitValue();

            } catch (InterruptedException e) {
                e.printStackTrace();
                reportError(e);
                return CodeBenderApplet.INTERUPTED_ERROR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            reportError(e);
            return CodeBenderApplet.PROCESS_ERROR;
        }
    }


    private Object flashLinux() {

        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.linux", AVRDUDE_PATH_UNIX);
            makeExecutable(AVRDUDE_PATH_UNIX);
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.AVRDUDE_ERROR;
        }

        try {
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.conf.linux", "/tmp/avrdude.conf");
        } catch (IOException e) {
            reportError(e);
            return CodeBenderApplet.CONF_ERROR;
        }

        try {
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(TEMP_HEX_UNIX);
            fileWriter.write(file);
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            reportError(e);
            return CodeBenderApplet.HEX_ERROR;
        }

        final AvrdudeLinuxCommand flashCommand =
                new AvrdudeLinuxCommand(basepath, port, TEMP_HEX_UNIX, baudRate);

        try {
            System.out.println("running : " + flashCommand.toString());
            final Process flashProcess = Runtime.getRuntime().exec(flashCommand.toString());
            try {
                flashProcess.waitFor();
                System.out.println("flashed=" + flashProcess.exitValue());
                return flashProcess.exitValue();

            } catch (InterruptedException e) {
                e.printStackTrace();
                reportError(e);
                return CodeBenderApplet.INTERUPTED_ERROR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            reportError(e);
            return CodeBenderApplet.PROCESS_ERROR;
        }

    }

    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();
            downloadBinaryToDisk("http://codebender.cc/dudes/avrdude.linux", AVRDUDE_PATH_UNIX);
            System.out.println((System.currentTimeMillis() - start));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadBinaryToDisk(final String inputFile, final String destinationFile) throws IOException {
        System.out.println("downloading to disk " + inputFile);
        final URL url = new URL(inputFile);
        url.openConnection();
        final InputStream input = url.openStream();
        File filea = new File(destinationFile);
        FileOutputStream fo = new FileOutputStream(filea);
        int data = input.read();
        while (data != -1) {
            fo.write(data);
            data = input.read();
        }
        input.close();
        fo.close();

    }

    private void makeExecutable(final String filename) {
        final File dudeFile = new File(filename);
        dudeFile.setExecutable(true);
    }

}
