//package com.God.sRecords.one.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/notes")
//@RequiredArgsConstructor
//public class NoteApiController {
//
//    private final NoteService noteService;
//
//    @GetMapping
//    public List<NoteDto> list() {
//        return noteService.findAllForCurrentUser();
//    }
//
//    @PostMapping
//    public NoteDto create(@RequestBody CreateNoteRequest req) {
//        return noteService.create(req);
//    }
//}