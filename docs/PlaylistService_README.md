# PlaylistService

## Purpose

`PlaylistService` manages user playlists and the ordered collection of songs inside each playlist.

## Main Responsibilities

### Playlist Retrieval

- Retrieve all playlists belonging to the authenticated user.
- Retrieve the songs contained in a playlist.

### Playlist Creation

When a playlist is created:

- The current authenticated user is obtained through `SecurityService`.
- The user's ID is assigned as `ownerId`.
- The playlist is saved to MongoDB.

### Playlist Update

Currently supports updating the playlist name.

### Playlist Deletion

Deletes a playlist by its ID.

### Adding Songs

Before adding a song:

- The playlist must exist.
- The song must exist.
- The service initializes the song list when necessary.
- Duplicate songs are rejected.

### Removing Songs

Removes a song ID from the playlist and saves the updated playlist.

### Reordering Songs

`moveSongToDesiredPosition()`:

1. Verifies the playlist.
2. Verifies the song.
3. Validates the requested position.
4. Finds the song's current position.
5. Removes it from the current position.
6. Inserts it at the requested position.

### Inserting a Song After Another Song

`insertNewSongAtPosition()`:

- Verifies the playlist and new song.
- Prevents duplicate songs.
- Finds the current song.
- Inserts the new song immediately after the current song.

## Main Dependencies

- `PlaylistRepository`
- `SongRepository`
- `SecurityService`

## Data Model

A playlist stores:

- Playlist ID
- Owner ID
- Playlist name
- Ordered list of song IDs

## Exceptions

The service uses:

- `ResourceNotFoundException` for missing playlists or songs.
- `InvalidInputException` for duplicate songs and invalid positions.
