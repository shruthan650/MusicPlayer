# SecurityService

## Purpose

`SecurityService` provides application-level security and ownership checks based on the authenticated user.

It uses Spring Security's `SecurityContextHolder` to identify the current user.

## Main Responsibilities

### Playlist Ownership

`isPlaylistOwner()`:

1. Finds the playlist.
2. Gets the current authentication.
3. Reads the authenticated email.
4. Finds the corresponding user.
5. Compares the user's ID with the playlist's `ownerId`.

Returns `true` only when the authenticated user owns the playlist.

### Song Ownership

`isSongOwner()` performs the same ownership check for songs.

It compares:

```text
authenticatedUser.id
```

with:

```text
song.ownerId
```

### Current User

`getCurrentUser()`:

1. Reads the current Spring Security authentication.
2. Gets the authenticated username/email.
3. Finds the corresponding user in MongoDB.
4. Returns the application `User`.

## Main Dependencies

- `PlaylistRepository`
- `SongRepository`
- `UserRepository`
- Spring Security `SecurityContextHolder`

## Where It Is Used

The service is used by other services and method-level security expressions to enforce resource ownership.

Typical flow:

```text
JWT
  ↓
Spring Security Authentication
  ↓
SecurityService
  ↓
Current User
  ↓
Ownership Check
```

## Important Note

`SecurityService` assumes that the endpoint is already authenticated before ownership methods are called.
