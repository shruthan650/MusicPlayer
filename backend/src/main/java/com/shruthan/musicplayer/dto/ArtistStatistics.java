package com.shruthan.musicplayer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistStatistics {

    private long totalSongs;
    private long totalPlays;
    private String mostPlayedSong;
    private long mostPlayedCount;
}
