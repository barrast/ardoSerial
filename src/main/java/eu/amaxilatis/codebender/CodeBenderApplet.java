package eu.amaxilatis.codebender;

import eu.amaxilatis.codebender.actions.FlashPrivilegedAction;
import eu.amaxilatis.codebender.graphics.PortOutputViewerFrame;
import eu.amaxilatis.codebender.util.ConnectionManager;
import eu.amaxilatis.codebender.util.SerialPortList;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * A JApplet class that provides methods to interface with an Arduino over Usb.
 */
public class CodeBenderApplet extends JApplet {

    private final transient String[] rates = new String[12];
    private transient String[] ports;

    public static final int FLASH_OK = 0;
    public static final int AVRDUDE_ERROR = 2;
    public static final int CONF_ERROR = 3;
    public static final int HEX_ERROR = 4;
    public static final int PROCESS_ERROR = 5;
    public static final int INTERUPTED_ERROR = 6;
    public static final int PORT_ERROR = 7;
    public static final int LIBUSB_ERROR = 8;
    public static final int BIN_ERROR = 9;
    public static String errorMessage;
    private static transient Properties properties;

    /**
     * Default Applet constructor.
     * No arguments, only loads the version and property info from the jar file.
     */
    public CodeBenderApplet() {
        properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/props/version.properties"));
            System.out.println("Version:" + getVersion());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void destroy() {
        System.out.println("CodeBenderApplet called Destroy");
        ConnectionManager.getInstance().disconnect();
    }

    @Override
    public final void init() {
        System.out.println("CodeBenderApplet called Init");

    }

    /**
     * Returns the Baudrates available for interfacing with Arduino.
     *
     * @return comma seperated list of the supported Baudrates.
     */
    @Deprecated
    public final String getRates() {
        return rates.toString(); //NOPMD
    }

    /**
     * Returns the Baudrates available for interfacing with Arduino.
     *
     * @return comma seperated list of the supported Baudrates.
     */
    @Deprecated
    public final String getFireRates() {
        return getSupportedBaudrates();
    }

    /**
     * Returns the Baudrates available for interfacing with Arduino.
     *
     * @return comma seperated list of the supported Baudrates.
     */
    public final String getSupportedBaudrates() {
        return ConnectionManager.getInstance().getBaudrates();
    }

    /**
     * Designed to be called from javascript.
     * Probes all usb connections for devices and returns the Arduino ports.
     *
     * @return a comma separated list of all available usb ports.
     */
    @Deprecated
    public final String getFire2() {
        return probeUsb();
    }

    /**
     * Designed to be called from javascript.
     * Probes all usb connections for devices and returns the Arduino ports.
     *
     * @return a comma separated list of all available usb ports.
     */
    public final String probeUsb() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            ports = SerialPortList.getInstance().getPortNames();
//                            System.out.println(ports);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        final StringBuilder protsAvail = new StringBuilder();
//        System.out.println(ports);
        for (int i = 0; i < ports.length; i++) {
            protsAvail.append(",");
            protsAvail.append(ports[i]);

        }

        return (protsAvail.toString()).substring(1);

    }

    /**
     * Designed to be called from javascript.
     * Connects to the USB port specified to communicate over a serial connection.
     *
     * @param port the index of the port to connect to. Provided by the @see probeUsb
     */
    public final void overrideConnect(final int port) {
        ConnectionManager.getInstance().setjTextArea(new PortOutputViewerFrame(this, ports[port]));
    }


    /**
     * Designed to be called from javascript.
     * Uses avrdude to flash the Arduino connected to the specified port with the file provided.
     *
     * @param port     the index of the port to use.
     * @param filename the contents of the file to flash to the Arduino.
     * @param baudrate the baudrate to use for flashing.
     * @return 0 if succesfull and a greater than zero error code else.
     */
    public final int flash(final int port, final String filename, final String baudrate) {
        System.out.println("flash");
        final FlashPrivilegedAction action = new FlashPrivilegedAction(ports[port], filename, baudrate);
        final int response = (Integer) AccessController.doPrivileged(action);
        System.out.println("Returing value : " + response);
        return response;
    }

    /**
     * Reads the version and build number from the manifest file.
     *
     * @return a string containing the version number.
     */
    public final String getVersion() {
        return new StringBuilder()
                .append((String) properties.get("version"))
                .append("b").append((String) properties.get("build")).toString();
    }

    /**
     * Returns the latest error message.
     *
     * @return
     */
    public static String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Designed to be called from javascript.
     * Uses avrdude to flash the Arduino connected to the specified port with the file provided.
     *
     * @param port        the index of the port to use.
     * @param filename    the contents of the file to flash to the Arduino.
     * @param maximumSize the maximum size for flashing
     * @param protocol    the protocol for flashing
     * @param speed       the baudrate to use for flashing.
     * @param board       the mcu to be converted internally
     * @return 0 if succesfull and a greater than zero error code else.
     */
    public final int uploadUSB(final int port, final String filename, final String maximumSize, final String protocol, final String speed, final String board) {
        System.out.println("flash");
        final FlashPrivilegedAction action = new FlashPrivilegedAction(ports[port], filename, maximumSize, protocol, speed, board);
        final int response = (Integer) AccessController.doPrivileged(action);
        System.out.println("Returing value : " + response);
        return response;
    }
}


