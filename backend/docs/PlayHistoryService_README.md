# PlayHistoryService

## Purpose

`PlayHistoryService` manages the current user's listening history and playback position.

## Main Responsibilities

### Get History

`getHistory()`:

- Gets the authenticated user.
- Uses pagination.
- Returns play history ordered by `playedAt` in descending order.

### Add to History

`addToHistory()`:

1. Gets the current user.
2. Verifies that the song exists.
3. Looks for an existing history record for the user and song.
4. Creates one if necessary.
5. Sets the user and song IDs.
6. Initializes the position to `0` for a new record.
7. Updates `playedAt`.
8. Saves the record.

This design keeps one playback-history record per user/song pair.

### Update Playback Position

`updatePosition()`:

- Verifies the song.
- Finds or creates the user's history record for that song.
- Stores the playback position.
- Updates `playedAt`.
- Saves the record.

### Get Playback Position

`getPosition()`:

- Finds the current user's record for a song.
- Returns the stored playback information.
- Throws `ResourceNotFoundException` when no history exists.

## Main Dependencies

- `SecurityService`
- `SongRepository`
- `PlayHistoryRepository`

## Stored Data

A play-history record contains:

- User ID
- Song ID
- Playback position
- Last played timestamp

## Pagination

History retrieval accepts:

- `page`
- `size`

and returns a Spring Data `Page<PlayHistory>`.
