window.addEventListener("load", () => {
	// Check if user is logged
	if (sessionStorage.getItem("user") === null) {
		window.location.href = "login.html";
	} else {
		const usernameSpan = document.getElementById("username");
		usernameSpan.textContent = getUser().username;

		// Page elements
		const homeSection = document.getElementById("homeSection");
		const albumSection = document.getElementById("albumSection");
		const imageListSection = document.getElementById("imageListSection");
		const homeLink = document.getElementById("homeLink");
		const prevPageLink = document.getElementById("prevPageLink");
		const nextPageLink = document.getElementById("nextPageLink");
		const logoutLink = document.getElementById("logoutLink");
		const createAlbumSection = document.getElementById("createAlbumSection");
		const createAlbumTitle = document.getElementById("createAlbumTitle");
		const uploadImageSection = document.getElementById("uploadImageSection");
		const uploadImageTitle = document.getElementById("uploadImageTitle");
		const uploadImageFile = document.getElementById("uploadImageFile");
		const uploadImageDescription = document.getElementById("uploadImageDescription");
		const sortImageListSection = document.getElementById("sortImageListSection");
		const sortBtn = document.getElementById("sortBtn");
		const saveOrderBtn = document.getElementById("saveOrderBtn");
		const titleList = document.getElementById('titleList');
		const deleteImageBtn = document.getElementById("deleteImageBtn");

		// Modal elements
		const modal = document.getElementById("imageModal");
		const modalImage = document.getElementById("modalImage");
		const modalImageTitle = document.getElementById("modalImageTitle");
		const modalImageDescription = document.getElementById("modalImageDescription");
		const modalImageUser = document.getElementById("modalImageUser");
		const modalImageCreationDate = document.getElementById("modalImageCreationDate");
		const modalComments = document.getElementById("modalComments");
		const modalCommentForm = document.getElementById("modalCommentForm");
		const modalCommentText = document.getElementById("modalCommentText");
		const spanClose = document.getElementsByClassName("close")[0];

		// Global variables
		let currentAlbumId = null;
		let currentPage = 0;
		let totalPages = 0;
		let currentImageId = null;
		let images = [];
		const imagesPerPage = 5;
		let dragSrcEl = null; // Used for drag&drop

		// Load albums
		function loadAlbums() {
			makeCall('GET', 'ViewHome', null, (response) => {
				const data = JSON.parse(response.responseText);
				populateAlbums(data.myAlbums, data.otherAlbums);
				populateUserImages(data.userImages);
			});
		}

		function populateAlbums(myAlbums, otherAlbums) {
			const myAlbumsDiv = document.getElementById('myAlbums');
			const otherAlbumsDiv = document.getElementById('otherAlbums');

			myAlbumsDiv.innerHTML = '<h1>My albums</h1>';
			otherAlbumsDiv.innerHTML = '<h1>Other albums</h1>';

			// Load my albums if any
			if (myAlbums && myAlbums.length > 0) {
				myAlbums.forEach(album => {
					const albumDiv = document.createElement('div');
					albumDiv.setAttribute('class', 'album-container');

					const titleSpan = document.createElement('span');
					titleSpan.textContent = album.title;
					const ownerSpan = document.createElement('span');
					ownerSpan.textContent = "(by " + album.owner.username + ")";

					const directoryImg = document.createElement('img');
					directoryImg.setAttribute('src', 'icons/directory.png');
					directoryImg.setAttribute('class', 'directory');
					directoryImg.addEventListener('click', () => showImageListSection(album.id, 0));

					const dateSpan = document.createElement('span');
					dateSpan.textContent = album.date;

					albumDiv.appendChild(titleSpan);
					albumDiv.appendChild(ownerSpan);
					albumDiv.appendChild(directoryImg);
					albumDiv.appendChild(dateSpan);

					myAlbumsDiv.appendChild(albumDiv);
				});
			} else {
				// Display a message if no albums are available
				const noMyAlbumsMessage = document.createElement('p');
				noMyAlbumsMessage.textContent = 'There are no albums.';
				myAlbumsDiv.appendChild(noMyAlbumsMessage);
			}

			// Load other albums if any
			if (otherAlbums && otherAlbums.length > 0) {
				otherAlbums.forEach(album => {
					const albumDiv = document.createElement('div');
					albumDiv.setAttribute('class', 'album-container');

					const titleSpan = document.createElement('span');
					titleSpan.textContent = album.title;
					const ownerSpan = document.createElement('span');
					ownerSpan.textContent = "(by " + album.owner.username + ")";

					const directoryImg = document.createElement('img');
					directoryImg.setAttribute('src', 'icons/directory.png');
					directoryImg.setAttribute('class', 'directory');
					directoryImg.addEventListener('click', () => showImageListSection(album.id, 0));

					const dateSpan = document.createElement('span');
					dateSpan.textContent = album.date;

					albumDiv.appendChild(titleSpan);
					albumDiv.appendChild(ownerSpan);
					albumDiv.appendChild(directoryImg);
					albumDiv.appendChild(dateSpan);

					otherAlbumsDiv.appendChild(albumDiv);
				});
			} else {
				// Display a message if no albums are available
				const noOtherAlbumsMessage = document.createElement('p');
				noOtherAlbumsMessage.textContent = 'There are no albums.';
				otherAlbumsDiv.appendChild(noOtherAlbumsMessage);
			}
		}

		// Show albums
		function showHomeSection() {
			homeSection.style.display = 'block';
			imageListSection.style.display = 'none';
			sortImageListSection.style.display = 'none';
			homeLink.style.display = 'none';
			modal.style.display = 'none';
			loadAlbums();
		}

		// Show album images
		function showImageListSection(albumId, pageNumber) {
			currentAlbumId = albumId;
			currentPage = pageNumber;
			homeSection.style.display = 'none';
			imageListSection.style.display = 'flex';
			sortImageListSection.style.display = 'none';
			homeLink.style.display = 'inline';
			modal.style.display = 'none';

			// Remove all previous image event listeners
			const currentImageElements = document.querySelectorAll('.in-album');
			currentImageElements.forEach(img => {
				img.removeEventListener('mouseover', img.mouseOverHandler);
			});
			loadImagesForAlbum(albumId);
		}

		sortBtn.addEventListener('click', () => {
			showSortImageListSection();
		});

		// Show album images
		function showSortImageListSection() {
			homeSection.style.display = 'none';
			imageListSection.style.display = 'none';
			sortImageListSection.style.display = 'block';
			homeLink.style.display = 'inline';
			modal.style.display = 'none';
		}


		// Load album images and their comments
		function loadImagesForAlbum(albumId) {
			if (albumId >= 0) {
				makeCall('GET', `ViewAlbum?albumId=${albumId}`, null, (response) => {
					images = JSON.parse(response.responseText); // Salva i dati delle immagini globalmente
					displayImages();
				});
			} else {
				alert("Invalid album id.")
			}
		}

		function displayImages() {
			const imageRow = document.getElementById("imageRow");
			imageRow.innerHTML = "";

			if (images.length == 0) {
				// Nessuna immagine
				const noImagesMessage = document.createElement('tr');
				const noImagesCell = document.createElement('td');
				noImagesCell.colSpan = 5; // Adatta in base al numero di colonne nella tua tabella
				noImagesCell.textContent = 'There are no images in this album.';
				noImagesMessage.appendChild(noImagesCell);
				imageRow.appendChild(noImagesMessage);

				prevPageLink.style.display = 'none';
				nextPageLink.style.display = 'none';

				// Pulizia ordinamento
				titleList.innerHTML = '';
				sortBtn.style.display = 'none';
			} else {
				// Determina quali immagini mostrare in base alla paginazione
				const start = currentPage * imagesPerPage;
				const end = Math.min(start + imagesPerPage, images.length);

				// Aggiungi immagini alla vista
				for (let i = start; i < end; i++) {
					const image = images[i];
					const cell = createImageCell(image);
					imageRow.appendChild(cell);
				}

				// Aggiungi celle vuote se ci sono meno immagini di imagesPerPage
				const cellsToAdd = imagesPerPage - (end - start);
				for (let i = 0; i < cellsToAdd; i++) {
					const emptyCell = document.createElement('td');
					emptyCell.className = "fixed-cell";
					imageRow.appendChild(emptyCell);
				}

				// Aggiorna i controlli di paginazione
				totalPages = Math.ceil(images.length / imagesPerPage);
				updatePaginationControls();

				// Funzioni di ordinamento
				sortBtn.style.display = 'inline';
				generateSortingList(images);
				initializeDragAndDrop();
			}
		}

		// Drag & Drop
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Generate title list
		function generateSortingList(images) {
			titleList.innerHTML = "";

			images.forEach(image => {
				const li = document.createElement('li');
				li.className = 'sortingEle';
				li.draggable = true;
				li.setAttribute('data-id', image.id);
				li.textContent = image.title;
				titleList.appendChild(li);
			});
		}

		function handleDragStart(e) {
			dragSrcEl = this;
			e.dataTransfer.effectAllowed = 'move';
			e.dataTransfer.setData('text/html', this.innerHTML);
			e.dataTransfer.setData('text/plain', this.getAttribute('data-id'));
			this.classList.add('dragging');
		}

		function handleDragOver(e) {
			e.preventDefault();
			e.dataTransfer.dropEffect = 'move';
			return false;
		}

		function handleDrop(e) {
			e.stopPropagation();
			e.preventDefault();

			if (dragSrcEl !== this) {
				const dragSrcId = e.dataTransfer.getData('text/plain');
				const dropTargetId = this.getAttribute('data-id');

				// Swap HTML content
				const dragSrcHTML = dragSrcEl.innerHTML;
				dragSrcEl.innerHTML = this.innerHTML;
				this.innerHTML = dragSrcHTML;

				// Swap IDs
				dragSrcEl.setAttribute('data-id', dropTargetId);
				this.setAttribute('data-id', dragSrcId);

				// Enamble save button
				saveOrderBtn.disabled = false;
			}
			return false;
		}

		function handleDragEnd() {
			const items = document.querySelectorAll('.sortingEle');
			items.forEach(item => {
				item.classList.remove('over');
				item.classList.remove('dragging');
			});
		}

		function getCurrentOrder() {
			const listItems = document.querySelectorAll('#titleList li');
			return Array.from(listItems).map((item, index) => ({
				imageId: parseInt(item.getAttribute('data-id'), 10),  // Assicurati che sia un numero
				position: index
			}));
		}

		saveOrderBtn.addEventListener('click', () => {
			const order = getCurrentOrder();
			if (order.length === 0) {
				alert('No items to save.');
				return;
			}
			const data = JSON.stringify({
				albumId: currentAlbumId,  // Assicurati che currentAlbumId sia un numero
				order: order
			});

			makeJsonCall('POST', 'SaveOrder', data, () => {
				alert("Custom order saved");
				showImageListSection(currentAlbumId, 0);
			});
		});

		function initializeDragAndDrop() {
			const items = document.querySelectorAll('.sortingEle');
			items.forEach(item => {
				item.addEventListener('dragstart', handleDragStart);
				item.addEventListener('dragover', handleDragOver);
				item.addEventListener('drop', handleDrop);
				item.addEventListener('dragend', handleDragEnd);
			});
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		function createImageCell(image) {
			const cell = document.createElement('td');
			cell.className = "fixed-cell";

			const imageDiv = document.createElement('div');
			const imageTitle = document.createElement('span');
			imageTitle.textContent = image.title;

			const thumbnail = document.createElement('img');
			thumbnail.setAttribute('src', `/Gallery/imageGetter?imageName=${encodeURIComponent(image.path)}`);
			thumbnail.className = "in-album";

			// Define the mouseOverHandler function
			function mouseOverHandler() {
				showImageDetailInModal(image.id);
			}
			thumbnail.mouseOverHandler = mouseOverHandler; // Store the handler reference
			thumbnail.addEventListener('mouseover', mouseOverHandler);

			imageDiv.appendChild(imageTitle);
			imageDiv.appendChild(thumbnail);
			cell.appendChild(imageDiv);

			return cell;
		}

		// Show image details in modal
		function showImageDetailInModal(imageId) {
			currentImageId = imageId;
			const image = images.find(img => img.id === imageId);

			if (image) {
				modalImage.setAttribute('src', `/Gallery/imageGetter?imageName=${encodeURIComponent(image.path)}`);
				modalImageTitle.textContent = image.title;
				modalImageDescription.textContent = image.description;
				modalImageUser.textContent = 'Image uploaded by ' + image.user.username;
				modalImageCreationDate.textContent = '(' + image.creation_date + ')';

				// Show delete button
				if (image.user.id === getUser().id) {
					deleteImageBtn.style.display = 'inline';
				} else {
					deleteImageBtn.style.display = 'none';
				}

				// Populate comments
				modalComments.innerHTML = '';
				if (image.comments && image.comments.length > 0) {
					image.comments.forEach(comment => {
						const commentDiv = document.createElement('div');
						commentDiv.innerHTML = `<b>${comment.user.username}:</b> ${comment.text} (${comment.date})`;
						modalComments.appendChild(commentDiv);
					});
				} else {
					modalComments.innerHTML = '<p>There are no comments.</p>';
				}

				modal.style.display = 'block';
			}
		}

		// Close the modal
		spanClose.addEventListener('click', () => {
			modal.style.display = 'none';
		});

		window.addEventListener('click', (event) => {
			if (event.target === modal) {
				modal.style.display = 'none';
			}
		});

		// Update pagination controls
		function updatePaginationControls() {
			if (images.length === 0) {
				prevPageLink.style.display = 'none';
				nextPageLink.style.display = 'none';
			} else {
				prevPageLink.style.display = currentPage > 0 ? "inline" : "none";
				nextPageLink.style.display = currentPage < totalPages - 1 ? "inline" : "none";
			}
		}

		// Event listeners for pagination controls
		prevPageLink.addEventListener('click', () => {
			if (currentPage > 0) {
				currentPage--; // Vai alla pagina precedente
				displayImages(); // Mostra le immagini senza ricaricarle
			}
		});

		nextPageLink.addEventListener('click', () => {
			if (currentPage < totalPages - 1) {
				currentPage++; // Vai alla pagina successiva
				displayImages(); // Mostra le immagini senza ricaricarle
			}
		});

		// Write comment
		modalCommentForm.addEventListener('submit', function(event) {
			event.preventDefault();

			const text = modalCommentText.value.trim();
			if (text) {
				const formData = new FormData();
				formData.append('imageId', currentImageId);
				formData.append('text', text);

				makeCall('POST', 'WriteComment', formData, () => {
					// Add the new comment to the DOM
					const newCommentDiv = document.createElement('div');
					newCommentDiv.innerHTML = `<b>${getUser().username}:</b> ${text} (just now)`;
					modalComments.appendChild(newCommentDiv);
					modalCommentText.value = '';
					// Update for next interactions
					loadImagesForAlbum(currentAlbumId);
				});
			} else {
				alert('Comment cannot be empty');
			}
		});

		// Delete image
		deleteImageBtn.addEventListener('click', function(event) {
			event.preventDefault();

			makeCall('GET', `DeleteImage?imageId=${currentImageId}`, null, () => {
				loadImagesForAlbum(currentAlbumId);
				// Check if the current page is empty after deletion
				if (currentPage > 0 && (currentPage * imagesPerPage) >= images.length) {
					currentPage--; // Go back to the previous page if the current one is empty
				}
				alert("Image deleted.");
				showImageListSection(currentAlbumId, currentPage);
			});
		});

		// Navigate to home (albums section)
		homeLink.addEventListener('click', () => {
			showHomeSection();
		});

		// Populate the images in the Create Album section
		function populateUserImages(images) {
			const userImagesDiv = document.getElementById("userImages");
			userImagesDiv.innerHTML = "";

			if (images != null) {
				images.forEach(image => {
					const title = document.createElement('span');
					title.textContent = image.title;

					const imageDiv = document.createElement('div');
					imageDiv.className = "image-item";

					const thumbnail = document.createElement('img');
					thumbnail.setAttribute('src', `/Gallery/imageGetter?imageName=${encodeURIComponent(image.path)}`);
					thumbnail.className = "thumbnail";

					const checkbox = document.createElement('input');
					checkbox.setAttribute('type', 'checkbox');
					checkbox.setAttribute('name', 'selectedImages');
					checkbox.setAttribute('value', image.id);

					imageDiv.appendChild(title);
					imageDiv.appendChild(thumbnail);
					imageDiv.appendChild(checkbox);
					userImagesDiv.appendChild(imageDiv);
				});
			} else {
				userImagesDiv.innerHTML = "No images uploaded";
			}
		}

		// Submit Create Album Form
		createAlbumSection.querySelector('form').addEventListener('submit', function(event) {
			event.preventDefault();

			const title = createAlbumTitle.value.trim();
			// Validate the album title
			if (title) {
				const formData = new FormData(this);
				makeCall('POST', 'CreateAlbum', formData, () => {
					alert("Album created successfully!");
					createAlbumTitle.value = '';
					showHomeSection();
				});
			} else {
				alert("Album title cannot be empty.");
			}
		});

		// Submit Upload image form
		uploadImageSection.querySelector('form').addEventListener('submit', function(event) {
			event.preventDefault();

			// Controllo lato client per assicurarsi che i campi non siano vuoti e che solo un file venga caricato
			if (!uploadImageTitle.value.trim()) {
				alert("Name field cannot be empty.");
				return;
			}
			if (!uploadImageDescription.value.trim()) {
				alert("Description field cannot be empty.");
				return;
			}
			if (uploadImageFile.files.length !== 1) {
				alert("Please upload exactly one image.");
				return;
			}

			const formData = new FormData(this);
			makeCall('POST', 'UploadImage', formData, () => {
				//alert("Image uploaded successfully!");
				uploadImageTitle.value = '';
				uploadImageDescription.value = '';
				showHomeSection();
			});
		});

		// Logout event listener
		logoutLink.addEventListener('click', () => {
			makeCall('POST', 'Logout', null, () => {
				sessionStorage.clear();
				window.location.href = "login.html";
			});
		});
	}

	// Load and display albums on page load
	showHomeSection();
});
