import com.fazecast.jSerialComm.*;
import com.leapmotion.leap.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FingerTest {

    public static HashMap<Finger.Type, Double> maxValue = new HashMap<>();

    public static double curl(Finger fingey) {
        Vector far = fingey.jointPosition(Finger.Joint.JOINT_DIP);//doesn't exist on thumb
        if (fingey.type() == Finger.Type.TYPE_THUMB) {
            far = fingey.tipPosition();
        }
        Vector medium = fingey.jointPosition(Finger.Joint.JOINT_PIP);
        Vector close = fingey.jointPosition(Finger.Joint.JOINT_MCP);
        Vector middleSegmentVector = far.minus(medium);
        Vector closeSegmentVector = medium.minus(close);
        double dot = middleSegmentVector.dot(closeSegmentVector);
        double angle = 180*Math.acos(dot/(middleSegmentVector.magnitude()*closeSegmentVector.magnitude()))/Math.PI;
        // Scale range to [0, 90]
        Double max = maxValue.get(fingey.type());
        if (max == null || angle > max) {
            maxValue.put(fingey.type(), max = angle);
        }
        angle *= (90/max);
        return angle;
    }

    public static void main(String[] args) throws InterruptedException {
        // Initialize Port
        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort arduinoPort = null;
        for (SerialPort port : ports) {
            if (port.getSystemPortName().equals("COM3")) {
                arduinoPort = port;
                break;
            }
        }
        if (arduinoPort == null) {
            System.out.println("NO ARDUINO PORT!!! Try again");
            return;
        }
        // Open the port and set the parameters
        arduinoPort.openPort();
        arduinoPort.setBaudRate(9600);
        arduinoPort.setNumDataBits(8);
        arduinoPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        arduinoPort.setParity(SerialPort.NO_PARITY);
        arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 100);

        // Initialize Leap
        Controller c = new Controller();
        if (!c.isConnected()) {
            System.out.println("LEAP NOT CONNECTED!!! Try again");
//            return;
        }


        // Loop
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis()-start > -1) {// < 60_000) {
            // Rate limit
            Thread.sleep(5);
            // Get valid info
            Frame frame = c.frame();
            Hand hand = frame.hands().rightmost();
            Finger[] fingeys = new Finger[5];
            for (int i = 0; i < 5; i++) {
                fingeys[i] = hand.fingers().get(i);
            }
            if (Arrays.stream(fingeys).anyMatch(f -> !f.isValid())) {
                // invalid finger
                continue;
            }
            // Use hand data
            Double[] fingeyCurls = new Double[5];
            AtomicInteger i = new AtomicInteger();
            Arrays.stream(fingeys).map(FingerTest::curl).forEach(aDouble -> fingeyCurls[i.getAndIncrement()] = aDouble);
            // Send hand data
            DataPacket data = new DataPacket(fingeyCurls);
            sendDataToArduinoAndWaitForAck(arduinoPort, data);
        }

        // Close port
        arduinoPort.closePort();
    }

    static void sendDataToArduinoAndWaitForAck(SerialPort arduinoPort, DataPacket data) {
        // Send the data packet
        String dataPacket = data.serialize();
        System.out.println(dataPacket);
        byte[] dataBytes = dataPacket.getBytes();
        arduinoPort.writeBytes(dataBytes, dataBytes.length);
        arduinoPort.flushIOBuffers();

        // Await ack
        byte[] resp = new byte[6];
        arduinoPort.readBytes(resp, 6);
        String respString = new String(new char[]{(char)resp[0], (char)resp[1], (char)resp[2], (char)resp[3], (char)resp[4], (char)resp[5]});
        System.out.println("Ack: " + respString);
    }
}
