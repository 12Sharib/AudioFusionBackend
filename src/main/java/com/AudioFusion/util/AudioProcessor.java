package com.AudioFusion.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Component
@Log4j2
public class AudioProcessor {

    public static List<byte[]> divideAudio(MultipartFile file, double segmentDurationInSeconds) throws Exception {
        log.info("Started divide audio method");
        List<byte[]> segments = new ArrayList<>();
        String songName = file.getOriginalFilename();


        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file.getInputStream());
            AudioFormat format = audioInputStream.getFormat();
            long frameSize = format.getFrameSize();
            long audioFrameLength = audioInputStream.getFrameLength();

            long segmentFrameLength = (long) (segmentDurationInSeconds * format.getSampleRate());

            int bytesPerSegment = (int) (segmentFrameLength * 1);

            byte[] buffer = new byte[bytesPerSegment];
            int bytesRead;

            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                segments.add(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.close();
            }

            audioInputStream.close();
        } catch (final Exception exception) {
            log.error("exception while dividing songs: ", exception);
        }
        String [] arr = Objects.requireNonNull(file.getOriginalFilename()).split("  ");
        return convertSegmentsToMP3(segments, arr[0]);
    }

    public static List<byte[]> convertSegmentsToMP3(final List<byte[]> segments, final String fileName) throws Exception {
        log.info("Started convert segments into mp3");
        final List<byte[]> mp3Segments = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            byte[] segmentBytes = segments.get(i);

            // Convert segment from WAV to MP3 using FFmpeg or any suitable library
             final byte[] convertedMP3Bytes = convertWAVToMP3(segmentBytes, i, fileName);

            // Add the converted MP3 segment to the list
             mp3Segments.add(convertedMP3Bytes);
        }
        return mp3Segments;
    }

    // Method to convert WAV bytes to MP3 bytes (replace this with your actual conversion logic)
    private static byte[] convertWAVToMP3(byte[] wavBytes, int i, String fileName) throws Exception {
        log.info("Started convert wav to mp3");
        final String tempWavPath = "src/main/resources" + File.separator + "temp_segment_" + i + ".wav";
        try (FileOutputStream fos = new FileOutputStream(tempWavPath)) {
            fos.write(wavBytes);
        }

        // Convert temporary WAV file to MP3 using FFmpeg command
        final String outputFilePath = "src/main/resources" + File.separator + "segment_" +  i + ".mp3";
        final String ffmpegCommand = "ffmpeg -i " + tempWavPath + " -codec:a libmp3lame -q:a 2 " + outputFilePath;

        Process process = Runtime.getRuntime().exec(ffmpegCommand);
        process.waitFor();

        // Read the content of the MP3 file into a byte array
        File file = new File(outputFilePath);

        // Read MP3 file as bytes
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStream.read(bytes);
        fileInputStream.close();

        // Convert bytes to Base64 string
        final String base64String = Base64.getEncoder().encodeToString(bytes);

        final byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        // Clean up temporary WAV file
        final File tempWavFile = new File(tempWavPath);

        if (tempWavFile.exists()) {
            tempWavFile.delete();
        }

        // Clean up decoded mp3 file
        final File mp3File = new File(outputFilePath);
        if (mp3File.exists()){
            mp3File.delete();
        }
        return decodedBytes;
    }
}

