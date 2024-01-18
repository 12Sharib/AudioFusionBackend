package com.AudioFusion.controller;

import com.AudioFusion.util.AudioProcessor;
import com.AudioFusion.util.SongMerger;
import com.AudioFusion.util.VideoSegmentation;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@Log4j2
public class SongController {
    @PostMapping("/merge")
    public ResponseEntity<String> mergeSongs(@RequestParam("song1") final MultipartFile song1,
                                              @RequestParam("song2") final MultipartFile song2) {
        log.info("Started merge two songs controller");

        // Handle file uploads and merge songs, Call method to merge songs
        SongMerger.mergeTwoSongs(song1, song2);
        return ResponseEntity.ok("Songs uploaded and merged successfully");
    }

    @PostMapping("/divide")
    public List<byte[]> handleFileUpload(@RequestParam("file") final MultipartFile file) throws Exception {
        log.info("Inside divide mp3 song controller with file name: {}", file.getOriginalFilename());

        // Logic to divide the uploaded mp3 song into smaller segments
        return AudioProcessor.divideAudio(file, 10);
    }

    @PostMapping("/divide/video")
    public ResponseEntity<List<String>> handleVideoUpload(@RequestParam("file") MultipartFile file) {
        log.info("Inside divide video song controller: {}", file.getOriginalFilename());

        // Logic to divide the uploaded video song into smaller segments
        return VideoSegmentation.divideVideo(file, 10);
    }
}
