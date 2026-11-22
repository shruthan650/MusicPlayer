const API_BASE_URL = "http://localhost:8080";

// =====================================================
// AUTHENTICATION
// =====================================================

const token = localStorage.getItem("melodiaToken");

// If there is no JWT, go back to login
if (!token) {
window.location.href = "index.html";

}

// Headers used for protected API requests
function authHeaders() {
return {
    "Authorization": `Bearer ${token}`
};
}

// =====================================================
// ELEMENTS
// =====================================================

const songList =
document.getElementById("songList");

const playlistList =
document.getElementById("playlistList");

const searchInput =
document.getElementById("searchInput");

const searchButton =
document.getElementById("searchButton");

const audioPlayer =
document.getElementById("audioPlayer");

const playPauseButton =
document.getElementById("playPauseButton");

const previousButton =
document.getElementById("previousButton");

const nextButton =
document.getElementById("nextButton");

const seekBar =
document.getElementById("seekBar");

const volumeBar =
document.getElementById("volumeBar");

const currentTitle =
document.getElementById("currentTitle");

const currentArtist =
document.getElementById("currentArtist");

const currentTime =
document.getElementById("currentTime");

const duration =
document.getElementById("duration");

const profileButton =
document.getElementById("profileButton");

const logoutButton =
document.getElementById("logoutButton");

const createPlaylistButton =
document.getElementById("createPlaylistButton");

// =====================================================
// GLOBAL DATA
// =====================================================

let songs = [];

let currentSongIndex = -1;

let currentAudioUrl = null;

let likedSongIds = new Set();
let playingSong = null;

// =====================================================
// LOAD SONGS
// =====================================================

async function loadSongs() {

try {

    songList.innerHTML =
        `<div class="empty-message">
            Loading songs...
         </div>`;


    const response = await fetch(
        `${API_BASE_URL}/api/songs?page=0&size=50`,
        {
            headers: authHeaders()
        }
    );


    if (response.status === 401) {

        logout();

        return;
    }


    if (!response.ok) {

        throw new Error(
            "Unable to load songs."
        );
    }


    const data = await response.json();


    // Spring Page<T> response
    songs = data.content || [];


    renderSongs();


} catch (error) {

    console.error(error);

    songList.innerHTML =
        `<div class="empty-message">
            ${error.message}
         </div>`;

}

}

// =====================================================
// DISPLAY SONGS
// =====================================================

function renderSongs(songArray = songs) {

songList.innerHTML = "";


if (!songArray.length) {

    songList.innerHTML =
        `<div class="empty-message">
            No songs found.
         </div>`;

    return;
}


songArray.forEach((song, index) => {

    const card =
        document.createElement("div");

    card.className = "song-card";


    const liked =
        likedSongIds.has(song.id);


    card.innerHTML = `

        <div class="song-cover">
            ♪
        </div>

        <div class="song-info">

            <div class="song-title">
                ${escapeHTML(song.title)}
            </div>

            <div class="song-artist">
                ${escapeHTML(song.artistName || "Unknown Artist")}
            </div>

        </div>

        <div class="song-actions">

            <button
                class="song-action-btn like-btn ${liked ? "liked" : ""}"
                data-id="${song.id}">

                ${liked ? "♥" : "♡"}

            </button>

            <button
                class="song-action-btn play-song-btn"
                data-id="${song.id}">

                ▶

            </button>

        </div>
    `;


    // Play button
    card
        .querySelector(".play-song-btn")
        .addEventListener("click", () => {

            const realIndex =
                songs.findIndex(
                    item => item.id === song.id
                );

            playSong(realIndex);

        });


    // Like button
    card
        .querySelector(".like-btn")
        .addEventListener("click", () => {

            toggleLike(song);

        });


    songList.appendChild(card);

});

}

// =====================================================
// PLAY SONG
// =====================================================
async function loadSongBlob(songId) {

    const CHUNK = 1024 * 1024;   // backend sends 1 MB at a time
    const parts = [];
    let type = "audio/mpeg";
    let start = 0;

    while (true) {

        const response = await fetch(
            `${API_BASE_URL}/api/songs/${songId}/stream`,
            {
                headers: {
                    ...authHeaders(),
                    "Range": `bytes=${start}-${start + CHUNK - 1}`
                }
            }
        );

        if (response.status === 416) break;   // no more data

        if (!response.ok) {
            throw new Error("Unable to load this song.");
        }

        type = response.headers.get("Content-Type") || type;

        const buffer = await response.arrayBuffer();

        if (!buffer.byteLength) break;

        parts.push(buffer);

        if (buffer.byteLength < CHUNK) break;   // last piece

        start += buffer.byteLength;
    }

    return new Blob(parts, { type });
}

const playerLikeButton = document.getElementById("playerLikeButton");

function updatePlayerLike() {

    if (!playingSong) return;

    const liked = likedSongIds.has(playingSong.id);

    playerLikeButton.hidden = false;
    playerLikeButton.textContent = liked ? "♥" : "♡";
    playerLikeButton.classList.toggle("liked", liked);
}

playerLikeButton.addEventListener("click", () => {
    if (playingSong) toggleLike(playingSong);
});


async function playSong(index) {

if (
    index < 0 ||
    index >= songs.length
) {
    return;
}


currentSongIndex = index;

const song = songs[index];


currentTitle.textContent =
    song.title || "Unknown Song";

currentArtist.textContent =
    song.artistName || "Unknown Artist";

playingSong = song;
updatePlayerLike();


playPauseButton.textContent = "⏳";


try {

    // ---------------------------------------------
    // GET PROTECTED AUDIO
    // ---------------------------------------------

    const response = await fetch(
        `${API_BASE_URL}/api/songs/${song.id}/stream`,
        {
            headers: authHeaders()
        }
    );


    if (!response.ok) {

        throw new Error(
            "Unable to load this song."
        );

    }


        const blob = await loadSongBlob(song.id);

    // Remove old blob URL
    if (currentAudioUrl) {
        URL.revokeObjectURL(currentAudioUrl);
    }

    currentAudioUrl = URL.createObjectURL(blob);

    audioPlayer.src = currentAudioUrl;



    // ---------------------------------------------
    // INCREASE PLAY COUNT
    // ---------------------------------------------

    await fetch(
        `${API_BASE_URL}/api/songs/${song.id}/play`,
        {
            method: "POST",
            headers: authHeaders()
        }
    );


    // ---------------------------------------------
    // ADD HISTORY
    // ---------------------------------------------

    await fetch(
        `${API_BASE_URL}/api/user/history/${song.id}`,
        {
            method: "POST",
            headers: authHeaders()
        }
    );


    await audioPlayer.play();


    playPauseButton.textContent = "❚❚";


} catch (error) {

    console.error(error);

    playPauseButton.textContent = "▶";

    alert(error.message);

}

}

// =====================================================
// PLAY / PAUSE
// =====================================================

playPauseButton.addEventListener(
"click",
() => {

    if (!audioPlayer.src) {

        if (songs.length > 0) {
            playSong(0);
        }

        return;
    }


    if (audioPlayer.paused) {

        audioPlayer.play();

    } else {

        audioPlayer.pause();

    }

}


);

// =====================================================
// AUDIO EVENTS
// =====================================================

audioPlayer.addEventListener(
"play",
() => {

    playPauseButton.textContent = "❚❚";

}

);

audioPlayer.addEventListener(
"pause",
() => {

    playPauseButton.textContent = "▶";

}

);

audioPlayer.addEventListener(
"loadedmetadata",
() => {

    seekBar.max =
        audioPlayer.duration;

    duration.textContent =
        formatTime(audioPlayer.duration);

}

);

audioPlayer.addEventListener(
"timeupdate",
() => {

    seekBar.value =
        audioPlayer.currentTime;

    currentTime.textContent =
        formatTime(
            audioPlayer.currentTime
        );

}


);

// Automatically play next song
audioPlayer.addEventListener(
"ended",
() => {

    playNextSong();

}

);

// =====================================================
// SEEK
// =====================================================

seekBar.addEventListener(
"input",
() => {

    audioPlayer.currentTime =
        seekBar.value;

}

);

// =====================================================
// VOLUME
// =====================================================

volumeBar.addEventListener(
"input",
() => {
    audioPlayer.volume =
        volumeBar.value;

}

);

// =====================================================
// NEXT
// =====================================================

function playNextSong() {


if (!songs.length) {
    return;
}


let nextIndex =
    currentSongIndex + 1;


if (nextIndex >= songs.length) {

    nextIndex = 0;

}


playSong(nextIndex);

}

nextButton.addEventListener(
"click",
playNextSong
);

// =====================================================
// PREVIOUS
// =====================================================

function playPreviousSong() {


if (!songs.length) {
    return;
}


let previousIndex =
    currentSongIndex - 1;


if (previousIndex < 0) {

    previousIndex =
        songs.length - 1;

}


playSong(previousIndex);


}

previousButton.addEventListener(
"click",
playPreviousSong
);

// =====================================================
// SEARCH
// =====================================================

async function searchSongs() {

const query =
    searchInput.value.trim();


if (!query) {

    loadSongs();

    return;

}


try {

    songList.innerHTML =
        `<div class="empty-message">
            Searching...
         </div>`;


    const response = await fetch(
        `${API_BASE_URL}/api/songs/search?query=${encodeURIComponent(query)}&page=0&size=50`,
        {
            headers: authHeaders()
        }
    );


    if (!response.ok) {

        throw new Error(
            "Search failed."
        );

    }


    const data =
        await response.json();


    songs =
        data.content || [];


    currentSongIndex = -1;


    renderSongs();


} catch (error) {

    console.error(error);

    songList.innerHTML =
        `<div class="empty-message">
            ${error.message}
         </div>`;

}


}

searchButton.addEventListener(
"click",
searchSongs
);

searchInput.addEventListener(
"keydown",
event => {

    if (event.key === "Enter") {

        searchSongs();

    }

}


);

// =====================================================
// LOAD LIKED SONGS
// =====================================================

async function loadLikedSongs() {


try {

    const response = await fetch(
        `${API_BASE_URL}/api/liked`,
        {
            headers: authHeaders()
        }
    );


    if (!response.ok) {
        return;
    }


    const data =
        await response.json();


    // Depending on backend response,
    // this may be an array of song IDs
    // or song objects.

    if (Array.isArray(data)) {

        data.forEach(item => {

            if (typeof item === "string") {

                likedSongIds.add(item);

            } else if (item && item.id) {

                likedSongIds.add(item.id);

            }

        });

    }


    renderSongs();

} catch (error) {

    console.error(
        "Could not load likes:",
        error
    );

}


}

// =====================================================
// LIKE / UNLIKE
// =====================================================

async function toggleLike(song) {


const alreadyLiked =
    likedSongIds.has(song.id);


try {

    const response = await fetch(
        `${API_BASE_URL}/api/like/${song.id}`,
        {
            method:
                alreadyLiked
                    ? "DELETE"
                    : "POST",

            headers:
                authHeaders()
        }
    );


    if (!response.ok) {

        throw new Error(
            "Unable to update like."
        );

    }


    if (alreadyLiked) {

        likedSongIds.delete(song.id);

    } else {

        likedSongIds.add(song.id);

    }


    renderSongs();
    updatePlayerLike();


} catch (error) {

    console.error(error);

    alert(error.message);

}}

// =====================================================
// PLAYLISTS
// =====================================================

async function loadPlaylists() {


try {

    const response = await fetch(
        `${API_BASE_URL}/api/playlist?page=0&size=50`,
        {
            headers: authHeaders()
        }
    );


    if (!response.ok) {

        throw new Error(
            "Unable to load playlists."
        );

    }


    const data =
        await response.json();


    const playlists =
        data.content || [];


    renderPlaylists(playlists);


} catch (error) {

    console.error(error);

    playlistList.innerHTML =
        `<div class="empty-message">
            ${error.message}
         </div>`;

}


}

// =====================================================
// DISPLAY PLAYLISTS
// =====================================================

function renderPlaylists(playlists) {

playlistList.innerHTML = "";


if (!playlists.length) {

    playlistList.innerHTML =
        `<div class="empty-message">
            You don't have any playlists yet.
         </div>`;

    return;

}


playlists.forEach(playlist => {

    const card =
        document.createElement("div");

    card.className =
        "playlist-card";


    const count =
        playlist.songIds
            ? playlist.songIds.length
            : 0;


    card.innerHTML = `

        <div class="playlist-icon">
            ♫
        </div>

        <div class="playlist-name">
            ${escapeHTML(playlist.name)}
        </div>

        <div class="playlist-count">
            ${count} song${count === 1 ? "" : "s"}
        </div>

    `;


    playlistList.appendChild(card);

});


}

// =====================================================
// CREATE PLAYLIST
// =====================================================

createPlaylistButton.addEventListener(
"click",
async () => {


    const name =
        prompt("Enter playlist name:");


    if (!name || !name.trim()) {
        return;
    }


    try {

        const response = await fetch(
            `${API_BASE_URL}/api/playlist`,
            {
                method: "POST",

                headers: {
                    ...authHeaders(),
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({
                    name: name.trim(),
                    songIds: []
                })
            }
        );


        if (!response.ok) {

            throw new Error(
                "Could not create playlist."
            );

        }


        loadPlaylists();


    } catch (error) {

        console.error(error);

        alert(error.message);

    }

}


);

// =====================================================
// PROFILE
// =====================================================

profileButton.addEventListener(
"click",
() => {
    window.location.href =
        "profile.html";

}


);

// =====================================================
// LOGOUT
// =====================================================

logoutButton.addEventListener(
"click",
logout
);

function logout() {


localStorage.removeItem(
    "melodiaToken"
);

localStorage.removeItem(
    "melodiaUser"
);


if (currentAudioUrl) {

    URL.revokeObjectURL(
        currentAudioUrl
    );

}


audioPlayer.pause();

audioPlayer.src = "";


window.location.href =
    "index.html";


}

// =====================================================
// UTILITY FUNCTIONS
// =====================================================

function formatTime(seconds) {


if (!seconds || isNaN(seconds)) {
    return "0:00";
}


const minutes =
    Math.floor(seconds / 60);

const remainingSeconds =
    Math.floor(seconds % 60);


return `${minutes}:${String(
    remainingSeconds
).padStart(2, "0")}`;


}

function escapeHTML(value) {


if (value === null ||
    value === undefined) {

    return "";

}


return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");


}

// =====================================================
// START DASHBOARD
// =====================================================

async function initializeDashboard() {


await loadLikedSongs();

await loadSongs();

await loadPlaylists();


}

initializeDashboard();
