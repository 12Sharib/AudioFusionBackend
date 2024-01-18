package com.AudioFusion.util;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class VideoSegmentation {

    //    public static void divideVideo(MultipartFile file, double segmentDurationInSeconds) {
//    //    String inputFilePath = "/path/to/input/video.mp4"; // Replace with the input file path
//        String outputDirectory = "src/main/resources/"; // Replace with the output directory
//
//        try {
//            // Save the input video file
//            File tempInputFile = File.createTempFile("input_video", ".mp4");
//            file.transferTo(tempInputFile);
//
//            // Command to segment the video using FFmpeg
//            String outputFileName = "segment_%03d.mp4"; // Adjust the naming pattern as needed
//            String ffmpegCommand = "ffmpeg -i " + tempInputFile.getAbsolutePath() +
//                    " -c:v copy -c:a copy -f segment -segment_time " + segmentDurationInSeconds +
//                    " -reset_timestamps 1 -map 0 " + outputDirectory + outputFileName;
//
//            Process process = Runtime.getRuntime().exec(ffmpegCommand);
//            process.waitFor();
//
//            // Clean up temporary input file
//            tempInputFile.delete();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            // Handle exceptions
//        }
//    }
    public static ResponseEntity<List<String>> divideVideo(MultipartFile file, double segmentDurationInSeconds) {
        String outputDirectory = "src/main/resources/"; // Replace with the output directory

        try {
            // Save the input video file
            File tempInputFile = File.createTempFile("input_video", ".mp4");
            file.transferTo(tempInputFile);

            // Command to segment the video using FFmpeg
            String outputFileName = "segment_%03d.mp4"; // Adjust the naming pattern as needed
            String ffmpegCommand = "ffmpeg -i " + tempInputFile.getAbsolutePath() +
                    " -c:v copy -c:a copy -f segment -segment_time " + segmentDurationInSeconds +
                    " -reset_timestamps 1 -map 0 " + outputDirectory + outputFileName;

            Process process = Runtime.getRuntime().exec(ffmpegCommand);

            // Wait for the FFmpeg process to complete
            int exitCode = process.waitFor();

            // Clean up temporary input file
            tempInputFile.delete();

            // Collect the bytes of segmented video files
            List<String> segmentedVideoBytes = new ArrayList<>();
            if (exitCode == 0) {
                // List the files in the output directory
                File outputDir = new File(outputDirectory);
                System.out.println("Output Directory Contents: " + Arrays.toString(outputDir.listFiles()));

                int numberOfSegments = outputDir.listFiles().length; // Correctly determine the number of segments

                for (int i = 0; i <= numberOfSegments; i++) {
                    File segmentFile = new File(outputDirectory + String.format(outputFileName, i));
                    if (!segmentFile.exists()) {
                        break; // Break the loop if the file doesn't exist
                    }

                    byte[] bytes = convertFileToBytes(segmentFile);
                    String base64Encoded = Base64.getEncoder().encodeToString(bytes);

                    segmentedVideoBytes.add(base64Encoded);
                }
                // Cleanup: Delete temporary segmented video files
                cleanupTemporaryFiles(outputDirectory, outputFileName, numberOfSegments);
            } else {
                System.err.println("FFmpeg process failed with exit code: " + exitCode);
                // Provide a more user-friendly error message or log the exception
            }
            // Return the bytes in the response
            return ResponseEntity.ok(segmentedVideoBytes);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Handle exceptions
            return ResponseEntity.status(500).body(null); // Internal Server Error
        }
    }

    private static void cleanupTemporaryFiles(String outputDirectory, String outputFileName, int numberOfSegments) {
        for (int i = 0; i <= numberOfSegments; i++) {
            File segmentFile = new File(outputDirectory + String.format(outputFileName, i));
            if (segmentFile.exists()) {
                segmentFile.delete();
            }
        }
    }

    private static byte[] convertFileToBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buf = new byte[1024];
            int readNum;

            while ((readNum = fis.read(buf)) != -1) {
                bos.write(buf, 0, readNum);
            }

            return bos.toByteArray();
        }
    }

}

    // Other helper methods specific to video handling
