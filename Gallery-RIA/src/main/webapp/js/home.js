window.addEventListener("load", () => {
	// Check if user is logged
	if (sessionStorage.getItem("user") === null) {
		window.location.href = "login.html";
	} else {
		const usernameSpan = document.getElementById("username");
		usernameSpan.textContent = getUser().username;

		// Page elements
		const albumSection = document.getElementById("albumSection");
		const imageListSection = document.getElementById("imageListSection");
		const imageDetailSection = document.getElementById("imageDetailSection");
		const homeLink = document.getElementById("homeLink");
		const backLink = document.getElementById("backLink");
		const prevPageLink = document.getElementById("prevPageLink");
		const nextPageLink = document.getElementById("nextPageLink");
		const logoutLink = document.getElementById("logoutLink");
		const createAlbumBtn = document.getElementById("createAlbumBtn");
		const deleteImageBtn = document.getElementById("deleteImageBtn");
		const commentForm = document.getElementById("commentForm");
		const commentText = document.getElementById("commentText");

		// Global variables
		let currentAlbumId = null;
		let currentPage = 0;
		let totalPages = 0;
		let currentImageId = null;
		const imagesPerPage = 5;

		// Load albums
		function loadAlbums() {
			makeCall('GET', 'ViewHome', null, null, (response) => {
				const data = JSON.parse(response.responseText);
				const myAlbumsDiv = document.getElementById('myAlbums');
				const otherAlbumsDiv = document.getElementById('otherAlbums');

				// Cleaning
				myAlbumsDiv.innerHTML = '<h1>My albums</h1>';
				otherAlbumsDiv.innerHTML = '<h1>Other albums</h1>';

				// Load my albums
				data.myAlbums.forEach(album => {
					const albumDiv = document.createElement('div');
					albumDiv.setAttribute('class', 'album-container')

					const titleSpan = document.createElement('span');
					titleSpan.textContent = album.title;

					const directoryImg = document.createElement('img');
					directoryImg.setAttribute('src', 'icons/directory.png');
					directoryImg.setAttribute('class', 'directory');
					directoryImg.addEventListener('click', () => showImageListSection(album.id, 0));

					const dateSpan = document.createElement('span');
					dateSpan.textContent = album.date;

					albumDiv.appendChild(titleSpan);
					albumDiv.appendChild(directoryImg);
					albumDiv.appendChild(dateSpan);

					myAlbumsDiv.appendChild(albumDiv);
				});

				// load other albums
				data.otherAlbums.forEach(album => {
					const albumDiv = document.createElement('div');
					albumDiv.setAttribute('class', 'album-container')

					const titleSpan = document.createElement('span');
					titleSpan.textContent = album.title;

					const directoryImg = document.createElement('img');
					directoryImg.setAttribute('src', 'icons/directory.png');
					directoryImg.setAttribute('class', 'directory');
					directoryImg.addEventListener('click', () => showImageListSection(album.id, 0));

					const dateSpan = document.createElement('span');
					dateSpan.textContent = album.date;

					albumDiv.appendChild(titleSpan);
					albumDiv.appendChild(directoryImg);
					albumDiv.appendChild(dateSpan);

					otherAlbumsDiv.appendChild(albumDiv);
				});
			});
		}

		// Show albums
		function showAlbumSection() {
			albumSection.style.display = 'block';
			imageListSection.style.display = 'none';
			imageDetailSection.style.display = 'none';
			loadAlbums();
		}

		// Show album images
		function showImageListSection(albumId, pageNumber) {
			currentAlbumId = albumId;
			currentPage = pageNumber;
			albumSection.style.display = 'none';
			imageListSection.style.display = 'block';
			imageDetailSection.style.display = 'none';
			loadImagesForAlbum(albumId, pageNumber);
		}

		// Show image details
		function showImageDetailSection(imageId) {
			currentImageId = imageId;
			albumSection.style.display = 'none';
			imageListSection.style.display = 'none';
			imageDetailSection.style.display = 'block';
			loadImageDetails(imageId);
		}

		// Logout
		logoutLink.addEventListener('click', () => {
			makeCall('GET', 'Logout', null, null, () => {
				sessionStorage.removeItem("user");
				window.location.href = 'login.html';
			});
		});

		// Load album images
		// Carica le immagini di un album
		function loadImagesForAlbum(albumId) {
    makeCall('GET', `ViewAlbum?albumId=${albumId}`, null, null, (response) => {
        const data = JSON.parse(response.responseText);
        const imageRow = document.getElementById("imageRow");

        // Pulisci la tabella delle immagini
        imageRow.innerHTML = "";

        // Calcola l'inizio e la fine della pagina corrente
        const start = currentPage * imagesPerPage;
        const end = Math.min(start + imagesPerPage, data.length);

        // Aggiungi le immagini alla tabella
        for (let i = start; i < end; i++) {
            const image = data[i];
            const cell = createImageCell(image);
            imageRow.appendChild(cell);
        }

        // Aggiungi celle vuote se necessario per riempire la pagina
        const cellsToAdd = imagesPerPage - (end - start);
        for (let i = 0; i < cellsToAdd; i++) {
            const emptyCell = document.createElement('td');
            emptyCell.className = "fixed-cell";
            imageRow.appendChild(emptyCell);
        }

        // Aggiorna i controlli di paginazione
        totalPages = Math.ceil(data.length / imagesPerPage);
        updatePaginationControls();
    });
}


		// Crea una cella per l'immagine
		function createImageCell(image) {
			const cell = document.createElement('td');
			cell.className = "fixed-cell";

			const imageDiv = document.createElement('div');
			const imageTitle = document.createElement('span');
			imageTitle.textContent = image.title;

			const thumbnail = document.createElement('img');
			thumbnail.setAttribute('src', `images/${image.path}`);
			thumbnail.className = "in-album";
			thumbnail.addEventListener('click', () => showImageDetailSection(image.id));

			imageDiv.appendChild(imageTitle);
			imageDiv.appendChild(thumbnail);
			cell.appendChild(imageDiv);

			return cell;
		}

		// Aggiorna i controlli di paginazione
        function updatePaginationControls() {
            prevPageLink.style.display = currentPage > 0 ? "inline" : "none";
            nextPageLink.style.display = currentPage < totalPages - 1 ? "inline" : "none";

            prevPageLink.onclick = () => showImageListSection(currentAlbumId, currentPage - 1);
            nextPageLink.onclick = () => showImageListSection(currentAlbumId, currentPage + 1);
        }


		// Write comment
		commentForm.addEventListener('submit', function(event) {
			event.preventDefault();

			const text = commentText.value.trim();
			if (text) {
				const formData = new FormData();
				formData.append('imageId', currentImageId);
				formData.append('text', text);

				makeCall('POST', 'WriteComment', formData, null, () => {
					loadImageDetails(currentImageId);
					commentText.value = '';
				});
			} else {
				alert('Comment cannot be empty');
			}
		});

		// Initialize showing albums
		showAlbumSection();
	}
});
