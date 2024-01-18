package com.AudioFusion.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class SongMerger {
    private static final Logger log = LoggerFactory.getLogger(SongMerger.class);

    public static void mergeTwoSongs(MultipartFile song1, MultipartFile song2) {
        log.info("Started mergeTwoSongs method");
        mergeSongs(song1, song2, "/tmp");
        log.info("Completed mergeTwoSongs method");
    }

    public static void mergeSongs(MultipartFile song1, MultipartFile song2, String outputDirectory) {
        log.info("Started mergeSongs method");
        try {
            // FFmpeg command to concatenate two MP3 files from input streams
            String ffmpegCommand = "ffmpeg -i - -i - -filter_complex '[0:a] [1:a] concat=n=2:v=0:a=1[out]' -map '[out]' -codec:a libmp3lame -q:a 2 "
                    + "/home/mohds/Projects/AudioFusion/src/main/resources/merged_song.mp3";

            log.info("FFmpeg command: {}", ffmpegCommand);

            // Get input streams from MultipartFiles
            InputStream song1InputStream = song1.getInputStream();
            InputStream song2InputStream = song2.getInputStream();

            // Start FFmpeg process
            Process process = Runtime.getRuntime().exec(ffmpegCommand);

            // Get process input stream
            OutputStream ffmpegStdin = process.getOutputStream();

            // Write input streams to FFmpeg process input stream
            IOUtils.copy(song1InputStream, ffmpegStdin);
            IOUtils.copy(song2InputStream, ffmpegStdin);

            // Close FFmpeg process input stream
            ffmpegStdin.close();

            // Log the process output and errors
            // ...

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("FFmpeg command executed successfully");
            } else {
                log.error("FFmpeg command failed with exit code {}", exitCode);
            }

            // Close input streams
            song1InputStream.close();
            song2InputStream.close();

            // Play the merged song or do further processing
            // ...

        } catch (IOException | InterruptedException e) {
            log.error("Exception occurred: {}", e.getMessage());
            e.printStackTrace();
            // Handle exceptions
        }
    }


}
