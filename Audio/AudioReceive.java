package Audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import Common.Configuration;
import Common.MulticastEndpoint;

public class AudioReceive {
    static Configuration Config = new Configuration();
    static MulticastEndpoint multiCastEndPoint;

    public static void startVoiceChatListen() {
        //Configure multiCast for speak
        Config.mAddr_ = Config.mAddr_forAudio;
        Config.msgSize_ = Config.MsgSizeForAudio;
        multiCastEndPoint = new MulticastEndpoint(Config);
        multiCastEndPoint.join();

        SourceDataLine dataLine = null;
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); // format is an AudioFormat object

        //I was using Clip class in the past but i have turned to SourceDataLine for preventing audio chopping
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);

            dataLine.open();
            dataLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        //while it is not disabled
        while (!Configuration.voiceChatDisabled) {

            GetAudio(dataLine);

        }


    }
    /**
     * A method for getting the audio
     * */

    static void GetAudio(SourceDataLine dataLine){


        byte[] b = new byte[Config.msgSize_];

        if (multiCastEndPoint.rx(b) && b.length > 0) {
            //get as datagram packets
            DatagramPacket packet = new DatagramPacket(b, b.length);
            try {
                //recive as Datagram packets
                multiCastEndPoint.mSocket_.receive(packet);

            } catch (IOException e) {

            }
            //write as datagram packets
            dataLine.write(packet.getData(), 0, 44100);
            //to see it is working
            System.out.println("-> rx : " + new String(b).trim());
        }
    }


}
