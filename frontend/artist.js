const API_BASE_URL = (
    window.MUSICPLAYER_API_BASE_URL ||
    (["localhost", "127.0.0.1"].includes(window.location.hostname)
        ? `${window.location.protocol}//${window.location.hostname}:8080`
        : "")
).replace(/\/$/, "");

// =====================================================
// AUTHENTICATION
// =====================================================

const token =
localStorage.getItem("melodiaToken");

if (!token) {


window.location.href = "index.html";


}

// =====================================================
// ELEMENTS
// =====================================================

const uploadForm =
document.getElementById("uploadForm");

const uploadButton =
document.getElementById("uploadButton");

const uploadMessage =
document.getElementById("uploadMessage");

const artistSongList =
document.getElementById("artistSongList");

const refreshButton =
document.getElementById("refreshButton");

const profileButton =
document.getElementById("profileButton");

const logoutButton =
document.getElementById("logoutButton");

// =====================================================
// AUTH HEADERS
// =====================================================

function authHeaders() {


return {
    "Authorization": `Bearer ${token}`
};


}

// =====================================================
// LOAD ARTIST STATISTICS
// =====================================================

async function loadStatistics() {

try {

    const response = await fetch(
        `${API_BASE_URL}/api/songs/artist/statistics`,
        {
            method: "GET",

            headers: authHeaders()
        }
    );


    if (response.status === 401 ||
        response.status === 403) {

        handleLogout();

        return;
    }


    if (!response.ok) {

        throw new Error(
            "Unable to load statistics."
        );

    }


    const statistics =
        await response.json();


    document.getElementById(
        "totalSongs"
    ).textContent =
        statistics.totalSongs ?? 0;


    document.getElementById(
        "totalPlays"
    ).textContent =
        statistics.totalPlays ?? 0;


    document.getElementById(
        "mostPlayedSong"
    ).textContent =
        statistics.mostPlayedSong ||
        "--";


    document.getElementById(
        "mostPlayedCount"
    ).textContent =
        statistics.mostPlayedCount ?? 0;


} catch (error) {

    console.error(
        "Statistics error:",
        error
    );

}


}

// =====================================================
// LOAD MY SONGS
// =====================================================

async function loadMySongs() {

try {

    artistSongList.innerHTML =
        `<div class="empty-message">
            Loading your songs...
         </div>`;


    const response = await fetch(
        `${API_BASE_URL}/api/songs?page=0&size=100`,
        {
            method: "GET",

            headers: authHeaders()
        }
    );


    if (response.status === 401 ||
        response.status === 403) {

        handleLogout();

        return;
    }


    if (!response.ok) {

        throw new Error(
            "Unable to load songs."
        );

    }


    const data =
        await response.json();


    const allSongs =
        data.content || [];


    // ------------------------------------------------
    // Get current logged-in user
    // ------------------------------------------------

    const savedUser =
        localStorage.getItem("melodiaUser");


    let currentUser = null;


    if (savedUser) {

        currentUser =
            JSON.parse(savedUser);

    }


    const currentUserId =
        currentUser?.id;


    // ------------------------------------------------
    // Filter songs owned by this artist
    // ------------------------------------------------

    let mySongs = allSongs.filter(
        song =>
            song.ownerId === currentUserId
    );


    /*
     * If the backend's GET /api/songs already returns
     * only the artist's songs, this fallback prevents
     * the page from becoming empty when ownerId is not
     * included in the returned JSON.
     */

    if (
        mySongs.length === 0 &&
        allSongs.length > 0 &&
        currentUserId
    ) {

        const hasOwnerInformation =
            allSongs.some(
                song =>
                    song.ownerId !== undefined &&
                    song.ownerId !== null
            );


        if (!hasOwnerInformation) {

            mySongs = allSongs;

        }

    }


    renderMySongs(mySongs);


} catch (error) {

    console.error(
        "My songs error:",
        error
    );


    artistSongList.innerHTML =
        `<div class="empty-message">
            ${escapeHTML(error.message)}
         </div>`;

}


}

// =====================================================
// DISPLAY MY SONGS
// =====================================================

function renderMySongs(mySongs) {

artistSongList.innerHTML = "";


if (!mySongs.length) {

    artistSongList.innerHTML =
        `<div class="empty-message">
            You haven't uploaded any songs yet.
         </div>`;

    return;

}


mySongs.forEach(song => {

    const card =
        document.createElement("div");


    card.className =
        "artist-song-card";


    card.innerHTML = `

        <div class="artist-song-cover">
            ♪
        </div>

        <div class="artist-song-info">

            <div class="artist-song-title">
                ${escapeHTML(song.title)}
            </div>

            <div class="artist-song-details">

                ${escapeHTML(song.artistName || "Unknown Artist")}

                ${song.albumName
                    ? " • " + escapeHTML(song.albumName)
                    : ""}

                ${song.genre
                    ? " • " + escapeHTML(song.genre)
                    : ""}

                • ${song.playCount || 0} plays

            </div>

        </div>

        <button
            class="delete-song-button"
            data-id="${song.id}">

            Delete

        </button>

    `;


    card
        .querySelector(".delete-song-button")
        .addEventListener(
            "click",
            () => deleteSong(song.id)
        );


    artistSongList.appendChild(card);

});


}

// =====================================================
// UPLOAD SONG
// =====================================================

uploadForm.addEventListener(
"submit",
async function(event) {

    event.preventDefault();


    const title =
        document.getElementById(
            "songTitle"
        ).value.trim();


    const artistName =
        document.getElementById(
            "artistName"
        ).value.trim();


    const albumName =
        document.getElementById(
            "albumName"
        ).value.trim();


    const audioFile =
        document.getElementById(
            "audioFile"
        ).files[0];


    const coverFile =
        document.getElementById(
            "coverFile"
        ).files[0];


    // ----------------------------------------------
    // VALIDATION
    // ----------------------------------------------

    if (!title || !artistName) {

        showUploadMessage(
            "Please enter the song title and artist name.",
            "error"
        );

        return;

    }


    if (!audioFile) {

        showUploadMessage(
            "Please select an audio file.",
            "error"
        );

        return;

    }


    uploadButton.disabled = true;

    uploadButton.textContent =
        "Creating Song...";


    try {

        // ------------------------------------------
        // STEP 1: CREATE SONG METADATA
        // ------------------------------------------
        const songData = {

    title: title,

    artistName: artistName,

    albumName:
        albumName || null,

    genre:
        genre || null

    };

        const createResponse =
            await fetch(
                `${API_BASE_URL}/api/songs`,
                {
                    method: "POST",

                    headers: {
                        ...authHeaders(),

                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(songData)
                }
            );


        if (
            createResponse.status === 401 ||
            createResponse.status === 403
        ) {

            handleLogout();

            return;

        }


        if (!createResponse.ok) {

            const errorText =
                await createResponse.text();

            throw new Error(
                errorText ||
                "Could not create the song."
            );

        }


        const createdSong =
            await createResponse.json();


        // ------------------------------------------
        // STEP 2: UPLOAD AUDIO
        // ------------------------------------------

        uploadButton.textContent =
            "Uploading Audio...";


        const audioFormData =
            new FormData();


        audioFormData.append(
            "file",
            audioFile
        );


        const audioResponse =
            await fetch(
                `${API_BASE_URL}/api/songs/${createdSong.id}/upload`,
                {
                    method: "POST",

                    headers:
                        authHeaders(),

                    body:
                        audioFormData
                }
            );


        if (
            audioResponse.status === 401 ||
            audioResponse.status === 403
        ) {

            handleLogout();

            return;

        }


        if (!audioResponse.ok) {

            const errorText =
                await audioResponse.text();


            throw new Error(
                errorText ||
                "Song created, but audio upload failed."
            );

        }


        // ------------------------------------------
        // STEP 3: COVER IMAGE
        // ------------------------------------------

        if (coverFile) {

            uploadButton.textContent =
                "Uploading Cover...";


            const coverFormData =
                new FormData();


            coverFormData.append(
                "file",
                coverFile
            );


            const coverResponse =
                await fetch(
                    `${API_BASE_URL}/api/songs/${createdSong.id}/cover`,
                    {
                        method: "POST",

                        headers:
                            authHeaders(),

                        body:
                            coverFormData
                    }
                );


            if (!coverResponse.ok) {

                console.warn(
                    "Cover upload failed."
                );

            }

        }


        // ------------------------------------------
        // SUCCESS
        // ------------------------------------------

        showUploadMessage(
            "Song uploaded successfully!",
            "success"
        );


        uploadForm.reset();


        await loadStatistics();

        await loadMySongs();


    } catch (error) {

        console.error(
            "Upload error:",
            error
        );


        showUploadMessage(
            error.message ||
            "Upload failed.",
            "error"
        );


    } finally {

        uploadButton.disabled = false;

        uploadButton.textContent =
            "Upload Song";

    }

}
);

// =====================================================
// DELETE SONG
// =====================================================

async function deleteSong(songId) {


const confirmed =
    confirm(
        "Are you sure you want to delete this song?"
    );


if (!confirmed) {
    return;
}


try {

    const response =
        await fetch(
            `${API_BASE_URL}/api/songs/${songId}`,
            {
                method: "DELETE",

                headers:
                    authHeaders()
            }
        );


    if (
        response.status === 401 ||
        response.status === 403
    ) {

        handleLogout();

        return;

    }


    if (!response.ok) {

        const errorText =
            await response.text();


        throw new Error(
            errorText ||
            "Could not delete the song."
        );

    }


    await loadStatistics();

    await loadMySongs();


} catch (error) {

    console.error(
        "Delete error:",
        error
    );


    alert(
        error.message ||
        "Could not delete song."
    );

}


}

// =====================================================
// REFRESH
// =====================================================

refreshButton.addEventListener(
"click",
async () => {


    await loadStatistics();

    await loadMySongs();

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
handleLogout
);

function handleLogout() {


localStorage.removeItem(
    "melodiaToken"
);

localStorage.removeItem(
    "melodiaUser"
);


window.location.href =
    "index.html";


}

// =====================================================
// UPLOAD MESSAGE
// =====================================================

function showUploadMessage(
text,
type
) {

uploadMessage.textContent =
    text;


if (type === "success") {

    uploadMessage.style.color =
        "#4ade80";

} else {

    uploadMessage.style.color =
        "#f87171";

}


}

// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHTML(value) {


if (
    value === null ||
    value === undefined
) {

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
// INITIALIZE
// =====================================================

async function initializeArtistDashboard() {

await loadStatistics();

await loadMySongs();


}

initializeArtistDashboard();
