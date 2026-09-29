# UserService

## Purpose

`UserService` handles user registration, authentication lookup, profile operations, password management, administrative user management, and user deletion.

It also implements Spring Security's `UserDetailsService`.

## Main Responsibilities

### User Registration

Registration:

- Requires a role.
- Prevents public ADMIN registration.
- Prevents duplicate email addresses.
- Encrypts passwords using the configured `PasswordEncoder`.
- Saves the user to MongoDB.

### Authentication Lookup

`loadUserByUsername()`:

- Finds a user using their email.
- Throws `UsernameNotFoundException` when the user does not exist.
- Converts the application user into `CustomUserDetails`.

### Current User

`getCurrentUser()` returns a safe `UserResponse` containing:

- User ID
- Email
- Role

### Email Update

`updateEmail()`:

- Gets the authenticated user.
- Checks whether the new email is already registered by another user.
- Updates the email.
- Generates a new JWT.
- Returns the new token together with user information.

### Password Update

`updatePassword()`:

- Verifies the current password.
- Encrypts the new password.
- Saves the updated password.

### Admin User Management

`getAllUsers()` returns user information without exposing passwords.

### User Deletion

`deleteUser()`:

- Prevents an admin from deleting their own account.
- Deletes the user's playlists.
- If the user is an `ARTIST`, finds their songs and delegates song cleanup to `SongService`.
- Deletes the user's play history.
- Deletes the user document.

Delegating artist-song deletion to `SongService` keeps song cleanup logic in one place.

## Main Dependencies

- `UserRepository`
- `PasswordEncoder`
- `SecurityService`
- `JWTService`
- `PlaylistRepository`
- `PlayHistoryRepository`
- `SongRepository`
- `SongService`

## Roles

The service works with:

- `ADMIN`
- `USER`
- `ARTIST`

Public registration cannot create an `ADMIN` account.
