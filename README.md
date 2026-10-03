# 🎵 MusicPlayer Backend

A RESTful music streaming backend built with **Java and Spring Boot**. The application provides JWT authentication, role-based authorization, song and file management, playlists, likes, play history, recommendations, artist statistics, and HTTP Range-based audio streaming.

---

## 🚀 Tech Stack

- **Java 25**
- **Spring Boot 4.1.1**
- **Spring Security**
- **JWT Authentication**
- **MongoDB**
- **MongoDB GridFS**
- **Maven**
- **Jaudiotagger**
- **Lombok**
- **REST API**
- **HTTP Range-based Audio Streaming**
- **SLF4J / Spring Boot Logging**

---

## ✨ Features

### 🔐 Authentication & Authorization

- User registration and login
- JWT-based authentication
- BCrypt password hashing
- Role-based authorization
- Method-level security with `@PreAuthorize`
- Roles:
  - `ADMIN`
  - `USER`
  - `ARTIST`
- Update email and password
- Admin user management
- Ownership checks for songs and playlists

### 🎵 Song Management

Songs support:

- Song metadata creation and updates
- Audio file upload
- Cover image upload
- Song deletion
- Song search
- Genre-based browsing
- Most-played songs
- Artist statistics
- Personalized recommendations
- Play-count tracking

Supported audio formats:

- `.mp3`
- `.wav`

Supported cover image formats:

- `.jpg`
- `.jpeg`
- `.png`

Song metadata is stored in MongoDB, while audio and cover-image binary data are stored in **MongoDB GridFS**.

### ▶️ HTTP Range Audio Streaming

The backend supports partial audio responses using HTTP Range requests.

- Full audio responses
- Partial Content responses (`206 Partial Content`)
- HTTP Range handling
- `ResourceRegion` streaming
- Browser-compatible seeking
- Audio playback through a frontend client

The backend provides the stream; playback controls such as play, pause, and seeking are handled by the frontend.

### 📂 Playlists

Users can:

- Create playlists
- Rename playlists
- Delete playlists
- Add songs
- Remove songs
- Reorder songs
- Insert a song after the current song
- Retrieve playlist songs

Playlist listing supports pagination using Spring's `Pageable`.

Example:

```http
GET /api/playlist?page=0&size=20
```

### ❤️ Likes

Authenticated users can:

- Like songs
- Unlike songs
- View their liked songs

Liked songs are stored as song IDs in the user's document.

### 🕒 Play History

Authenticated users can:

- Add songs to play history
- View paginated history
- Save playback position
- Retrieve the saved position for a song

History is ordered by most recently played.

Example:

```http
GET /api/user/history?page=0&size=10
```

### 🎯 Recommendations

Recommendations are based on the genres of songs liked by the current user.

The service:

1. Reads the user's liked songs.
2. Extracts their distinct genres.
3. Finds popular songs from those genres.
4. Excludes songs already liked by the user.
5. Returns up to 10 recommendations.

### 📊 Artist Statistics

Artists can view statistics for songs they own:

- Total songs
- Total plays
- Most-played song
- Most-played song count

---

## 🗄️ MongoDB GridFS Storage

Audio and cover files are stored in MongoDB using **GridFS** instead of the local filesystem.

GridFS automatically splits large files into chunks and stores them across two MongoDB collections:

```text
MusicPlayer
├── songRepo
├── userRepo
├── playlistRepo
├── fs.files
└── fs.chunks
```

### Song file references

The `Song` document stores GridFS file IDs:

```text
Song
├── fileId       → audio file in GridFS
└── coverFileId  → cover image in GridFS
```

The actual binary data is stored by GridFS in:

- `fs.files` — file metadata
- `fs.chunks` — binary file chunks

### GridFS Service

`GridFsStorageService` is responsible for:

- Uploading files to GridFS
- Finding GridFS files by ID
- Loading files as `GridFsResource`
- Deleting files from GridFS
- Validating GridFS file IDs

The service is separated from `SongService`, keeping storage concerns independent from song business logic.

---

## 🏗️ Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── shruthan/
    │           └── musicplayer/
    │               ├── controller/
    │               ├── service/
    │               ├── repository/
    │               ├── model/
    │               ├── dto/
    │               ├── security/
    │               ├── exception/
    │               └── config/
    │
    └── resources/
        └── application.properties
```

### Service Layer

The service layer currently includes:

- `SongService`
- `GridFsStorageService`
- `PlaylistService`
- `UserService`
- `LikeService`
- `PlayHistoryService`
- `SecurityService`

---

## 🔑 API Overview

All protected endpoints require authentication unless otherwise stated.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/register` | Register a user |
| `POST` | `/api/login` | Login and receive JWT |

### Songs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/songs` | Get paginated songs |
| `GET` | `/api/songs/{id}` | Get a song |
| `POST` | `/api/songs` | Create a song |
| `PUT` | `/api/songs/{id}` | Update song metadata |
| `DELETE` | `/api/songs/{id}` | Delete a song |
| `POST` | `/api/songs/{id}/upload` | Upload audio file to GridFS |
| `POST` | `/api/songs/{id}/cover` | Upload cover image to GridFS |
| `GET` | `/api/songs/{id}/stream` | Stream audio |
| `GET` | `/api/songs/{id}/cover` | Get cover image |
| `POST` | `/api/songs/{id}/play` | Increment play count |
| `GET` | `/api/songs/search` | Search songs |
| `GET` | `/api/songs/most-played` | Get most-played songs |
| `GET` | `/api/songs/most-played/genre` | Get most-played songs by genre |
| `GET` | `/api/songs/artist/statistics` | Get artist statistics |
| `GET` | `/api/songs/recommendations` | Get recommendations |

Song listing and search support Spring pagination.

Examples:

```http
GET /api/songs?page=0&size=20
GET /api/songs/search?query=rock&page=0&size=20
```

### Playlists

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/playlist` | Get paginated user playlists |
| `GET` | `/api/playlist/{playlistId}` | Get songs in a playlist |
| `POST` | `/api/playlist` | Create playlist |
| `PUT` | `/api/playlist/{playlistId}` | Update playlist |
| `DELETE` | `/api/playlist/{playlistId}` | Delete playlist |
| `POST` | `/api/playlist/{playlistId}/song/{songId}` | Add song |
| `DELETE` | `/api/playlist/{playlistId}/song/{songId}` | Remove song |
| `PUT` | `/api/playlist/{playlistId}/song/{songId}/{position}` | Move song |
| `POST` | `/api/playlist/{playlistId}/song/{songId}/next` | Insert song after current song |

### Likes

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/like/{songId}` | Like a song |
| `DELETE` | `/api/like/{songId}` | Unlike a song |
| `GET` | `/api/liked` | Get liked songs |

### Play History

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/user/history` | Get paginated play history |
| `POST` | `/api/user/history/{songId}` | Add/update play history |
| `PUT` | `/api/user/history/{songId}` | Update playback position |
| `GET` | `/api/user/history/{songId}` | Get saved playback position |

### Users

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/user/me` | Get current user |
| `PUT` | `/api/user/email` | Update email |
| `PUT` | `/api/user/password` | Update password |
| `GET` | `/api/user/admin/users` | Get all users |
| `DELETE` | `/api/user/admin/users/{userId}` | Delete a user |

---

## ⚙️ Configuration

Set the JWT secret as an environment variable:

```text
JWT_SECRET=<your-base64-encoded-secret>
```

Example:

```properties
spring.application.name=musicplayer
spring.mongodb.uri=mongodb://localhost:27017/MusicPlayer
jwt.secret=${JWT_SECRET}

spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

Make sure MongoDB is running before starting the application.

GridFS collections are created and managed by MongoDB through Spring Data MongoDB.

---

## ▶️ Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/shruthan650/MusicPlayer.git
cd MusicPlayer
```

### 2. Configure MongoDB

Make sure MongoDB is running locally.

Default database:

```text
MusicPlayer
```

### 3. Configure JWT Secret

Set:

```text
JWT_SECRET
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Or run the main Spring Boot application class from your IDE.

---

## 🔒 Authentication

After successful login, the server returns a JWT.

Include the token in authenticated requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

The application uses stateless JWT authentication.

---

## 👥 Roles

### ADMIN

- Administrative user management
- Access to administrative operations
- Access to protected song and playlist operations

### ARTIST

- Manage owned songs
- Upload audio files
- Upload cover images
- View artist statistics
- Create and manage playlists

### USER

- Access authenticated music features
- Create and manage playlists
- Like songs
- View play history
- Receive recommendations

Resource ownership is enforced for song and playlist operations through method-level security.

---

## 🛡️ Security

The backend uses:

- Spring Security
- JWT authentication
- BCrypt password hashing
- Stateless authentication
- Method-level authorization
- Role-based access control
- Song ownership checks
- Playlist ownership checks
- File extension validation
- GridFS file ID validation
- Global exception handling

---

## 📝 Exception Handling & Logging

The application uses a global exception handler for common application errors:

- `ResourceNotFoundException` → `404 NOT FOUND`
- `InvalidInputException` → `400 BAD REQUEST`

Application logging uses SLF4J/Spring Boot logging with different levels:

- **INFO** — important business operations
- **DEBUG** — normal application flow and read operations
- **WARN** — expected failures and invalid requests
- **ERROR** — unexpected file-processing failures and other server errors

Sensitive information such as passwords and JWT tokens should not be logged.

---

## 💾 Data Storage

### MongoDB

MongoDB stores:

- Users
- Song metadata
- Playlists
- Play history

### MongoDB GridFS

GridFS stores:

- Audio files
- Cover images

GridFS uses:

```text
fs.files
fs.chunks
```

for file metadata and binary chunks respectively.

The `Song` document stores references to these files using:

```text
fileId
coverFileId
```

No local `storage/songs` or `storage/covers` directories are required for the current file-storage implementation.

---

## 🧪 Testing

The REST APIs can be tested using:

- Postman
- Browser
- Frontend application

For protected endpoints, include the JWT in the `Authorization` header.

For file uploads, use `multipart/form-data` with the form field:

```text
file
```

For audio streaming, the backend supports HTTP Range requests such as:

```http
Range: bytes=0-1048575
```

---

## 🔮 Future Improvements

- Advanced recommendation algorithms
- Album management
- Artist profiles
- Recently played songs
- Improved search
- Playlist sharing
- Refresh tokens
- Production deployment
- Detailed analytics
- Better streaming/play-count tracking for repeated HTTP Range requests
- Cloud/object storage for production-scale media

---

## 👨‍💻 Author

**Shruthan T M**

Java Backend Developer | Spring Boot | MongoDB | Spring Security

---

## 📄 License

This project is developed for learning and project purposes.
