package eu.amaxilatis.ardoserial;

import eu.amaxilatis.ardoserial.graphics.ArduinoStatusImage;
import eu.amaxilatis.ardoserial.graphics.PortOutputViewerFrame;
import eu.amaxilatis.ardoserial.util.SerialPortList;
import jssc.SerialPort;
import org.apache.log4j.BasicConfigurator;

import javax.swing.*;
import java.awt.*;

/**
 * A JApplet class.
 * Provides user interface to connecto to an arduino using a usb connection.
 */
public class MyApplet extends JApplet {
    /**
     * Logger.
     */
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(MyApplet.class);

    /**
     * a connection handler.
     */
    private Thread serialPortThread;
    private final String[] rates = new String[12];
    private String[] detectedPorts;
    private String[] ports;


    @Override
    public final void destroy() {
        LOGGER.info("MyApplet called Destroy");
        ConnectionManager.getInstance().disconnect();
    }

    /**
     * default constructor.
     *
     * @throws HeadlessException an exception.
     */
    public MyApplet() {
        BasicConfigurator.configure();
        initBaudRates();
        ports = SerialPortList.getPortNames();
    }

    /**
     * Initialize the baudrates array.
     */
    private void initBaudRates() {

        rates[0] = new String(String.valueOf(SerialPort.BAUDRATE_110));
        rates[1] = new String(String.valueOf(SerialPort.BAUDRATE_300));
        rates[2] = new String(String.valueOf(SerialPort.BAUDRATE_600));
        rates[3] = new String(String.valueOf(SerialPort.BAUDRATE_1200));
        rates[4] = new String(String.valueOf(SerialPort.BAUDRATE_4800));
        rates[5] = new String(String.valueOf(SerialPort.BAUDRATE_9600));
        rates[6] = new String(String.valueOf(SerialPort.BAUDRATE_14400));
        rates[7] = new String(String.valueOf(SerialPort.BAUDRATE_19200));
        rates[8] = new String(String.valueOf(SerialPort.BAUDRATE_38400));
        rates[9] = new String(String.valueOf(SerialPort.BAUDRATE_115200));
        rates[10] = new String(String.valueOf(SerialPort.BAUDRATE_128000));
        rates[11] = new String(String.valueOf(SerialPort.BAUDRATE_256000));
    }

    public String getRates() {
        return rates.toString();
    }

    public String getFireRates() {
        final StringBuilder rateString = new StringBuilder();
        for (int i = 0; i < rates.length; i++) {
            rateString.append(",").append(rates[i]);

        }
        return (rateString.toString()).substring(1);
    }

    @Override
    public final void init() {
        LOGGER.info("MyApplet called Init");

        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
            LOGGER.error("createGUI didn't successfully complete");
        }
    }

    /**
     * Build the default user interface.
     */
    private void createGUI() {
        LOGGER.info("MyApplet called CreateGUI");

        this.setBackground(Color.white);

        ArduinoStatusImage.setDisconnected();

        LOGGER.info("booting up");
    }

    /**
     * Called from javascript.
     *
     * @return a comma separated list of all available usb ports.
     */
    public String getFire2() {
        final StringBuilder protsAvail = new StringBuilder();
        LOGGER.info(ports);
        for (int i = 0; i < ports.length; i++) {
            protsAvail.append(",");
            protsAvail.append(ports[i]);

        }
        return (protsAvail.toString()).substring(1);
    }

    /**
     * Override connect function to be used by javascript.
     *
     * @param port the index of the port to connect to.
     * @param rate the rate to use when connecting.
     */
    public void overrideConnect(final int port, final int rate) {
        ConnectionManager.getInstance().setjTextArea(new PortOutputViewerFrame());

        ConnectionManager.getInstance().setPort(ports[port], rates[rate]);
        ConnectionManager.getInstance().connect();
    }
}
