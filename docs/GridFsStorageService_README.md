# GridFsStorageService

`GridFsStorageService` handles file storage in the MusicPlayer backend using **MongoDB GridFS**.

## Overview

```text
SongController
      ↓
SongService
      ↓
GridFsStorageService
      ↓
GridFsTemplate
      ↓
MongoDB GridFS
      ↓
┌─────────────┐
│  fs.files   │
│  fs.chunks  │
└─────────────┘
```

It provides a centralized layer for uploading, retrieving, and deleting audio files and cover images.

## Why GridFS?

MongoDB documents have a size limit, so large audio files should not be stored directly inside normal MongoDB documents.

GridFS stores large files as chunks:

```text
Large audio file
      ↓
    GridFS
      ↓
┌───────────────┐
│ fs.files      │ → File metadata
│ fs.chunks     │ → Binary data chunks
└───────────────┘
```

## Song Storage Model

The `Song` document stores references to GridFS files instead of binary data.

```text
Song
├── fileId
│     └──→ Audio file in GridFS
│
└── coverFileId
      └──→ Cover image in GridFS
```

Example:

```json
{
    "id": "6aa5819c50e4f8da7cdfb29c",
    "title": "Naam Chale",
    "artistName": "Artist",
    "fileId": "6ac12ff44fad95ee34863368",
    "coverFileId": "6ac12ff44fad95ee34863369"
}
```

## Main Methods

### `store()`

```java
public String store(MultipartFile file) throws IOException
```

Uploads a `MultipartFile` to GridFS and returns its generated file ID.

```text
MultipartFile
      ↓
InputStream
      ↓
GridFsTemplate.store()
      ↓
MongoDB GridFS
      ↓
fileId
```

### `findById()`

```java
public GridFSFile findById(String fileId)
```

Retrieves a GridFS file using its ID.

```text
String fileId
      ↓
ObjectId
      ↓
MongoDB Query
      ↓
GridFsTemplate.findOne()
      ↓
GridFSFile
```

Invalid IDs produce `InvalidInputException`. Missing files produce `ResourceNotFoundException`.

### `getResource()`

```java
public GridFsResource getResource(String fileId)
```

Converts a `GridFSFile` into a Spring `GridFsResource`.

```text
fileId
   ↓
findById()
   ↓
GridFSFile
   ↓
GridFsTemplate.getResource()
   ↓
GridFsResource
```

This is used for audio streaming and cover-image retrieval.

### `delete()`

```java
public void delete(String fileId)
```

Deletes a file from GridFS.

```text
fileId
   ↓
getQuery()
   ↓
GridFsTemplate.delete()
   ↓
File removed
```

### `getQuery()`

```java
private Query getQuery(String fileId)
```

Converts the String file ID into a MongoDB `ObjectId` and creates the query used by `findById()` and `delete()`.

## Integration with SongService

### Audio Upload

```text
POST /api/songs/{songId}/upload
              ↓
       SongController
              ↓
        SongService
              ↓
GridFsStorageService.store()
              ↓
           GridFS
              ↓
          fileId
              ↓
        Song.fileId
```

### Cover Upload

```text
POST /api/songs/{songId}/cover
              ↓
       SongController
              ↓
        SongService
              ↓
GridFsStorageService.store()
              ↓
           GridFS
              ↓
       coverFileId
              ↓
     Song.coverFileId
```

### Audio Retrieval

```text
GET /api/songs/{songId}/stream
              ↓
       SongController
              ↓
        SongService
              ↓
getSongResource(fileId)
              ↓
GridFsStorageService
              ↓
       GridFsResource
              ↓
      ResourceRegion
              ↓
HTTP 206 Partial Content
```

### Cover Retrieval

```text
GET /api/songs/{songId}/cover
              ↓
       SongController
              ↓
        SongService
              ↓
getResource(coverFileId)
              ↓
       GridFsResource
              ↓
       HTTP Response
```

## File Deletion

When a song is deleted:

```text
Delete Song
    ↓
Delete audio using fileId
    ↓
Delete cover using coverFileId
    ↓
Delete Song document
```

When replacing a file:

```text
Old GridFS file
      ↓
Upload new file
      ↓
Save new fileId
      ↓
Delete old GridFS file
```

## MongoDB GridFS Structure

```text
MusicPlayer
│
├── songRepo
├── userRepo
├── playlistRepo
├── PlayHistory
│
├── fs.files
└── fs.chunks
```

`fs.files` contains file metadata:

```text
_id
filename
length
chunkSize
uploadDate
metadata
```

`fs.chunks` contains binary data:

```text
_id
files_id
n
data
```

## Logging

The service uses SLF4J:

- `INFO` — uploads and deletions
- `DEBUG` — file lookups and resource retrieval
- `WARN` — invalid IDs and missing files
- `ERROR` — unexpected file-processing failures

Never log passwords, JWT tokens, secrets, or file contents.

## Exception Handling

### `InvalidInputException`

Used for invalid GridFS IDs or invalid file input.

### `ResourceNotFoundException`

Used when a requested GridFS file does not exist.

## Design Principle

`GridFsStorageService` follows **Separation of Concerns**.

`SongService` handles:

```text
- Song metadata
- Ownership
- Play count
- Recommendations
- Artist statistics
```

`GridFsStorageService` handles:

```text
- Store
- Find
- Retrieve
- Delete
```

This keeps GridFS-specific logic isolated from the application's business logic.

## Architecture

```text
                         Client
                           │
                           ▼
                    SongController
                           │
                           ▼
                      SongService
                           │
                           ▼
                 GridFsStorageService
                           │
                           ▼
                     GridFsTemplate
                           │
                           ▼
                       MongoDB
                           │
                  ┌────────┴────────┐
                  ▼                 ▼
              fs.files          fs.chunks
                  │                 │
                  └────────┬────────┘
                           │
                     Media Files
                  ┌────────┴────────┐
                  ▼                 ▼
                Audio             Cover
```

## Result

The MusicPlayer backend no longer depends on local directories such as:

```text
storage/songs/
storage/covers/
```

Instead:

```text
Song.fileId
      ↓
MongoDB GridFS
      ↓
Audio

Song.coverFileId
      ↓
MongoDB GridFS
      ↓
Cover Image
```

`GridFsStorageService` is the central file-storage component of the MusicPlayer backend.
