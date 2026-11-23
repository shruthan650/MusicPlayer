# LikeService

## Purpose

`LikeService` manages the songs liked by the currently authenticated user.

Liked songs are stored as song IDs inside the user's `likedSongIds` collection.

## Main Responsibilities

### Like a Song

`likeSong()`:

1. Gets the current user.
2. Verifies that the song exists.
3. Initializes `likedSongIds` if necessary.
4. Adds the song ID.
5. Saves the user.

### Unlike a Song

`unlikeSong()`:

1. Gets the current user.
2. Verifies that the song exists.
3. Removes the song ID from the liked collection.
4. Saves the user.

### Get Liked Songs

`getLikedSongs()`:

- Gets the current user.
- Reads their liked song IDs.
- Fetches the corresponding songs.
- Ignores song IDs whose song document is no longer present.

## Main Dependencies

- `SecurityService`
- `SongRepository`
- `UserRepository`

## Data Storage

Likes are not stored in a separate collection.

They are stored in the `User` document:

```text
likedSongIds = [songId1, songId2, ...]
```

## Exception Handling

`ResourceNotFoundException` is thrown when a requested song does not exist.
