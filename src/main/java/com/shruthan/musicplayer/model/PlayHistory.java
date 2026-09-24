package com.shruthan.musicplayer.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("PlayHistoryRepo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayHistory {

    @Id
    private String id;

    private String userId;

    private String songId;
    
    private long position;

    private Date playedAt;
}