package Audio;

import javax.sound.sampled.*;
import java.util.Arrays;
import Common.Configuration;
import Common.MulticastEndpoint;


public class AudioSender {

    static Configuration Config = new Configuration();
    static MulticastEndpoint multiCastEndPoint;

    /**
     * Records and Sends the voice chat
     * **/

    public static void startVoiceChatSpeak() {


        TargetDataLine line;
        //Audio format
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            return;
        }

        int numBytesRead;

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);

            byte[] data = new byte[line.getBufferSize()];
            line.start();   // start capturing
            System.out.println("Start capturing...");

            // Begin audio capture.
            line.start();
            //Configure multiCast for speak
            Config.mAddr_ = Config.mAddr_forAudio;
            multiCastEndPoint = new MulticastEndpoint(Config);
            //join the group
            multiCastEndPoint.join();
            System.out.println(line.getBufferSize());
            //while it is not disabled
            while (!Configuration.voiceChatSpeakDisabled) {
                // Read the next chunk of data from the TargetDataLine.
                numBytesRead = line.read(data, 0, data.length);
                //System.out.println(data);
                // Save this chunk of data.
                byte[] bytes = Arrays.copyOf(data, numBytesRead);
                //Send the data
                multiCastEndPoint.tx(bytes);

            }


        } catch (LineUnavailableException e) {
            System.out.println("there is problem with capturing");
        }


    }


}


