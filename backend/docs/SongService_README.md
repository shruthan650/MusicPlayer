# SongService

## Purpose

`SongService` contains the business logic for song management, audio-file handling, streaming, search, popularity tracking, artist statistics, and recommendations.

## Main Responsibilities

### Song Management

- Retrieve all songs with pagination.
- Create song metadata and associate it with the authenticated user.
- Retrieve a song by ID.
- Update song metadata.
- Delete a song and clean up related data.

### Audio Upload

`uploadSong()`:

- Accepts MP3 and WAV files.
- Validates the file extension and MIME type.
- Creates the `songs/` directory when required.
- Generates a UUID-based filename.
- Stores the audio file on the filesystem.
- Reads the audio duration using Jaudiotagger.
- Stores the file path and duration in MongoDB.
- Removes the previous audio file when replacing a song.

### Audio Streaming

The service provides:

- Full-file streaming.
- HTTP Range parsing.
- Range validation.
- `206 Partial Content` responses.
- `Content-Range` and `Accept-Ranges` headers.
- Streaming without loading the complete audio file into memory.

### Song Deletion

`deleteSongById()` also cleans related data:

- Deletes the physical audio file.
- Deletes the MongoDB song document.
- Deletes play-history records for the song.
- Removes the song from playlists.
- Removes the song from users' liked-song collections.
- Deletes the associated cover image.

### Search and Popularity

- Search by song title, artist, or album through the repository.
- Track play count.
- Retrieve most-played songs.
- Retrieve most-played songs by genre.

### Cover Images

- Accepts JPG, JPEG, and PNG images.
- Stores cover files in `covers/`.
- Replaces an existing cover image.
- Retrieves cover images as filesystem resources.

### Artist Statistics

Provides:

- Total number of songs.
- Total number of plays.
- Most-played song.
- Play count of the most-played song.

### Recommendations

Recommendations are generated from the genres of songs liked by the current user.

The service:

1. Reads the user's liked songs.
2. Finds their genres.
3. Finds popular songs from those genres.
4. Excludes already-liked songs.
5. Returns up to 10 songs.

## Main Dependencies

- `SongRepository`
- `PlaylistRepository`
- `UserRepository`
- `PlayHistoryRepository`
- `SecurityService`
- Jaudiotagger
- Spring `Resource`
- Spring `MultipartFile`

## Storage

- **MongoDB:** song metadata, ownership, play count, and file paths.
- **Filesystem:** actual audio files in `songs/` and cover images in `covers/`.
