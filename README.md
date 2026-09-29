# 🎵 MusicPlayer Backend

A RESTful music streaming backend built with **Java and Spring Boot**.  
The application provides user authentication, song management, playlists, likes, play history, audio streaming, recommendations, and artist statistics.

---

## 🚀 Tech Stack

- **Java 25**
- **Spring Boot**
- **Spring Security**
- **JWT Authentication**
- **MongoDB**
- **Maven**
- **Jaudiotagger**
- **Lombok**
- **REST API**
- **HTTP Range-based Audio Streaming**

---

## ✨ Features

### 🔐 Authentication & Authorization

- User registration and login
- JWT-based authentication
- Password encryption using BCrypt
- Role-based authorization
- Roles: `ADMIN`, `USER`, `ARTIST`
- Update email and password
- Admin user management

### 🎵 Song Management

Artists and admins can:

- Upload songs
- Update song metadata
- Delete songs
- Upload cover images
- View song details
- Search songs
- Browse songs by genre
- View most-played songs
- View artist statistics

Supported audio formats:

- `.mp3`
- `.wav`

Audio files are stored on the server filesystem while song metadata is stored in MongoDB.

### ▶️ Audio Streaming

The backend supports HTTP Range-based audio streaming.

- Full audio streaming
- Partial content responses (`206`)
- HTTP Range requests
- Browser-compatible seeking
- Pause/resume support through range requests

The frontend player controls playback while the backend provides the audio stream.

### 📂 Playlists

Users can:

- Create, rename, and delete playlists
- Add and remove songs
- Reorder songs
- Insert songs at a specific position
- Play the next song in a playlist

### ❤️ Likes

Users can:

- Like songs
- Unlike songs
- View liked songs

### 🕒 Play History

The application maintains user listening history, including:

- User
- Song
- Playback position
- Last played time

### 🎯 Recommendations

Recommendations are based on the genres of songs liked by the current user.

The backend finds popular songs from those genres, excludes already-liked songs, and returns up to 10 recommendations.

### 📊 Artist Statistics

Artists can view statistics related to their songs, including total songs, total plays, and song-level statistics.

---

## 🏗️ Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── musicplayer/
    │           ├── controller/
    │           ├── service/
    │           ├── repository/
    │           ├── model/
    │           ├── dto/
    │           ├── security/
    │           ├── exception/
    │           └── config/
    │
    └── resources/
        └── application.properties

songs/
└── Audio files

covers/
└── Cover images
```

> Package names may vary depending on the project configuration.

---

## 🔑 API Overview

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/register` | Register a user |
| `POST` | `/api/login` | Login and receive JWT |

### Songs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/songs` | Get songs |
| `POST` | `/api/song` | Create song |
| `GET` | `/api/song/{songId}` | Get song |
| `PUT` | `/api/song/{songId}` | Update song |
| `DELETE` | `/api/song/{songId}` | Delete song |
| `POST` | `/api/song/upload/{songId}` | Upload audio |
| `GET` | `/api/song/{songId}/audio` | Stream audio |

### Playlists

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/playlists` | Get user's playlists |
| `POST` | `/api/playlist` | Create playlist |
| `PUT` | `/api/playlist/{playlistId}` | Update playlist |
| `DELETE` | `/api/playlist/{playlistId}` | Delete playlist |
| `POST` | `/api/playlist/{playlistId}/song/{songId}` | Add song |
| `DELETE` | `/api/playlist/{playlistId}/song/{songId}` | Remove song |

### User

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/user/me` | Get current user |
| `PUT` | `/api/user/email` | Update email |
| `PUT` | `/api/user/password` | Update password |

---

## ⚙️ Configuration

Set the following environment variable:

```text
JWT_SECRET=<your-base64-encoded-secret>
```

Example application properties:

```properties
spring.application.name=musicplayer
spring.mongodb.uri=mongodb://localhost:27017/MusicPlayer
jwt.secret=${JWT_SECRET}
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

Make sure MongoDB is running before starting the application.

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

Set the environment variable:

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

After logging in, the server returns a JWT.

Include the token in authenticated requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

Protected endpoints require a valid JWT.

---

## 👥 Roles

### ADMIN

- Manage users
- Manage songs
- Perform administrative operations

### ARTIST

- Upload songs
- Update owned songs
- Delete owned songs
- Upload cover images
- View statistics

### USER

- Browse songs
- Stream songs
- Create playlists
- Like songs
- View play history
- Receive recommendations

---

## 💾 Data Storage

### MongoDB

Stores:

- Users
- Song metadata
- Playlists
- Play history

### File System

Stores:

```text
songs/
covers/
```

The corresponding file paths are stored in MongoDB.

---

## 🧪 Testing

The APIs can be tested using:

- Postman
- Browser
- Frontend application

For protected endpoints, include the JWT in the `Authorization` header.

---

## 🛡️ Security

The backend uses:

- Spring Security
- JWT authentication
- BCrypt password hashing
- Role-based authorization
- Method-level security
- Stateless authentication
- Ownership checks for songs and playlists

---

## 🔮 Future Improvements

- Cloud storage for audio files
- Advanced recommendation algorithms
- Album management
- Artist profiles
- Recently played songs
- Improved search
- Playlist sharing
- Production deployment
- Refresh tokens
- Detailed analytics

---

## 👨‍💻 Author

**Shruthan T M**

Java Backend Developer | Spring Boot | MongoDB | Spring Security

---

## 📄 License

This project is developed for learning and project purposes.
