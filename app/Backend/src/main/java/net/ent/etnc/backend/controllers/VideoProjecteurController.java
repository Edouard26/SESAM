package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.VideoProjecteurDto;
import net.ent.etnc.backend.dtos.assemblers.VideoProjecteurAssembler;
import net.ent.etnc.backend.services.VideoProjecteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST pour la gestion des vidéos projecteurs ({@code /api/v1/videoProjectors/}).
 */
@RestController
@RequestMapping("/api/v1/videoProjectors/")
public class VideoProjecteurController {

    private final VideoProjecteurService videoProjecteurService;
    private final VideoProjecteurAssembler assembler;

    @Autowired
    public VideoProjecteurController(VideoProjecteurService videoProjecteurService, VideoProjecteurAssembler assembler) {
        this.videoProjecteurService = videoProjecteurService;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<VideoProjecteurDto>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(videoProjecteurService.findAll(pageable).map(assembler::toDto));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<VideoProjecteurDto> getById(@PathVariable Long id) {
        return videoProjecteurService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toDto(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<VideoProjecteurDto> post(@RequestBody VideoProjecteurDto videoProjecteurDto) {
        return ResponseEntity.ok(assembler.toDto(videoProjecteurService.create(assembler.toEntity(videoProjecteurDto))));
    }

    @PutMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<VideoProjecteurDto> put(@PathVariable Long id, @RequestBody VideoProjecteurDto videoProjecteurDto) {
        if (!videoProjecteurService.existsById(id)) return ResponseEntity.notFound().build();
        videoProjecteurDto.setId(id);

        return ResponseEntity.ok(assembler.toDto(videoProjecteurService.update(assembler.toEntity(videoProjecteurDto))));
    }

    @DeleteMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!videoProjecteurService.existsById(id)) return ResponseEntity.notFound().build();

        videoProjecteurService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}