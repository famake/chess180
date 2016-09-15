package chess180;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ArtNetSender {

    private static final int ARTNET_PORT = 0x1936;
    private final DatagramSocket socket;

    public ArtNetSender() throws SocketException {
        socket = new DatagramSocket();
    }

    private static byte[] makePacket(int universe, byte[] data) {
        byte[] output_buffer = new byte[data.length + 18];
        byte[] header_str = "Art-Net".getBytes();
        System.arraycopy(header_str, 0, output_buffer, 0, header_str.length);
        output_buffer[7] = 0; // null terminated string "Art-Net"
        output_buffer[8] = 0x0; // Opcode 0x5000 ArtDMX
        output_buffer[9] = 0x50;

        output_buffer[10] = 0x0; // Protocol version 14
        output_buffer[11] = 14;

        output_buffer[12] = 0; // Sequence number
        output_buffer[13] = 0; // Physical

        output_buffer[14] = (byte) (universe & 0xFF);
        output_buffer[15] = (byte) (universe >> 8);

        output_buffer[16] = (byte) (data.length >> 8);
        output_buffer[17] = (byte) (data.length & 0xFF);

        System.arraycopy(data, 0, output_buffer, 18, data.length);
        return output_buffer;
    }

    public void sendData(String destinationIp, int universe, byte[] data) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(destinationIp);
        byte[] output_buffer = makePacket(universe, data);
        DatagramPacket packet = new DatagramPacket(output_buffer, output_buffer.length, inetAddress, ARTNET_PORT);
        socket.send(packet);
    }

}