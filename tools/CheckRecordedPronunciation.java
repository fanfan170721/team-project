import com.wordtrail.data.DictionaryPronunciationClient;
import com.wordtrail.entity.PronunciationClip;
import java.io.*;
import java.nio.file.*;
import javax.sound.sampled.*;
import javazoom.jl.decoder.*;

/** Decodes bytes only. Never opens an audio device. Run with the packaged JAR on the classpath. */
class CheckRecordedPronunciation {
    public static void main(String[] args) throws Exception {
        byte[] bytes;
        if(args.length==2 && args[0].equals("--word")) {
            PronunciationClip clip=new DictionaryPronunciationClient().fetch(args[1],"en-US");
            bytes=clip.audio();
            System.out.println("Recording: "+args[1]+" | "+clip.accent()+" | "+clip.source()+" | "+clip.license());
        } else {
            bytes=Files.readAllBytes(Path.of(args[0]));
        }
        int samples=0;
        if(bytes[0]=='O') {
            try(AudioInputStream encoded=AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
                AudioFormat source=encoded.getFormat();
                AudioFormat pcm=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,source.getSampleRate(),16,
                        source.getChannels(),source.getChannels()*2,source.getSampleRate(),false);
                try(AudioInputStream decoded=AudioSystem.getAudioInputStream(pcm,encoded)) {
                    samples=decoded.readNBytes(4*1024*1024).length;
                }
            }
        } else {
            Bitstream stream=new Bitstream(new ByteArrayInputStream(bytes));
            Decoder decoder=new Decoder(); Header frame;
            while((frame=stream.readFrame())!=null) {
                samples+=((SampleBuffer)decoder.decodeFrame(frame,stream)).getBufferLength();
                stream.closeFrame();
            }
            stream.close();
        }
        if(samples<100) { throw new IllegalStateException("Recording did not decode."); }
        System.out.println("PASS: decoded "+samples+" samples/bytes silently; no audio device was opened.");
    }
}
