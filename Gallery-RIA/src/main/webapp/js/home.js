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
        const homeLink = document.getElementById("homeLink");
        const prevPageLink = document.getElementById("prevPageLink");
        const nextPageLink = document.getElementById("nextPageLink");
        const logoutLink = document.getElementById("logoutLink");
        const createAlbumSection = document.getElementById("createAlbumSection");
        const createAlbumBtn = document.getElementById("createAlbumBtn");
        const deleteImageBtn = document.getElementById("deleteImageBtn");
        const commentForm = document.getElementById("commentForm");
        const commentText = document.getElementById("commentText");

        // Modal elements
        const modal = document.getElementById("imageModal");
        const modalImage = document.getElementById("modalImage");
        const modalImageTitle = document.getElementById("modalImageTitle");
        const modalImageDescription = document.getElementById("modalImageDescription");
        const modalComments = document.getElementById("modalComments");
        const modalCommentForm = document.getElementById("modalCommentForm");
        const modalCommentText = document.getElementById("modalCommentText");
        const spanClose = document.getElementsByClassName("close")[0];

        // Global variables
        let currentAlbumId = null;
        let currentPage = 0;
        let totalPages = 0;
        let currentImageId = null;
        let images = []; // Contains all images and comments for the current album
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
                    albumDiv.setAttribute('class', 'album-container');

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

                // Load other albums
                data.otherAlbums.forEach(album => {
                    const albumDiv = document.createElement('div');
                    albumDiv.setAttribute('class', 'album-container');

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
            createAlbumSection.style.display = 'none';
            homeLink.style.display = 'inline'; // Ensure homeLink is visible
            modal.style.display = 'none'; // Hide modal
            loadAlbums();
        }

        // Show album images
        function showImageListSection(albumId, pageNumber) {
            currentAlbumId = albumId;
            currentPage = pageNumber;
            albumSection.style.display = 'none';
            imageListSection.style.display = 'block';
            createAlbumSection.style.display = 'none'; // Ensure createAlbumSection is hidden
            homeLink.style.display = 'inline'; // Show homeLink when viewing images
            modal.style.display = 'none'; // Hide modal
            loadImagesForAlbum(albumId);
        }

        // Load album images and their comments
        function loadImagesForAlbum(albumId) {
            makeCall('GET', `ViewAlbum?albumId=${albumId}`, null, null, (response) => {
                images = JSON.parse(response.responseText); // Save the images data globally
                const imageRow = document.getElementById("imageRow");
                imageRow.innerHTML = "";

                if (images.length === 0) {
                    // Display a message if no images are available
                    const noImagesMessage = document.createElement('tr');
                    const noImagesCell = document.createElement('td');
                    noImagesCell.colSpan = 5; // Adjust based on the number of columns in your table
                    noImagesCell.textContent = 'No images available.';
                    noImagesMessage.appendChild(noImagesCell);
                    imageRow.appendChild(noImagesMessage);

                    // Hide pagination controls if there are no images
                    prevPageLink.style.display = 'none';
                    nextPageLink.style.display = 'none';
                } else {
                    // Determine which images to show based on pagination
                    const start = currentPage * imagesPerPage;
                    const end = Math.min(start + imagesPerPage, images.length);

                    // Add images to the view
                    for (let i = start; i < end; i++) {
                        const image = images[i];
                        const cell = createImageCell(image);
                        imageRow.appendChild(cell);
                    }

                    // Add empty cells if there are less than imagesPerPage images
                    const cellsToAdd = imagesPerPage - (end - start);
                    for (let i = 0; i < cellsToAdd; i++) {
                        const emptyCell = document.createElement('td');
                        emptyCell.className = "fixed-cell";
                        imageRow.appendChild(emptyCell);
                    }

                    // Update pagination controls
                    totalPages = Math.ceil(images.length / imagesPerPage);
                    updatePaginationControls();
                }
            });
        }

        // Create image cell
        function createImageCell(image) {
            const cell = document.createElement('td');
            cell.className = "fixed-cell";

            const imageDiv = document.createElement('div');
            const imageTitle = document.createElement('span');
            imageTitle.textContent = image.title;

            const thumbnail = document.createElement('img');
            thumbnail.setAttribute('src', `images/${image.path}`);
            thumbnail.className = "in-album";
            thumbnail.addEventListener('mouseover', () => showImageDetailInModal(image.id)); // Show modal on hover

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
                modalImage.setAttribute('src', `images/${image.path}`);
                modalImageTitle.textContent = image.title;
                modalImageDescription.textContent = image.description;

                // Populate comments
                modalComments.innerHTML = '';
                if (image.comments && image.comments.length > 0) {
                    image.comments.forEach(comment => {
                        const commentDiv = document.createElement('div');
                        commentDiv.innerHTML = `<b>${comment.user.username}:</b> ${comment.text} (${comment.date})`;
                        modalComments.appendChild(commentDiv);
                    });
                } else {
                    modalComments.innerHTML = '<p>No comments available.</p>';
                }

                // Show the modal
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

        // Write comment
        modalCommentForm.addEventListener('submit', function(event) {
            event.preventDefault();

            const text = modalCommentText.value.trim();
            if (text) {
                const formData = new FormData();
                formData.append('imageId', currentImageId);
                formData.append('text', text);

                makeCall('POST', 'WriteComment', formData, null, () => {
                    loadImageDetails(currentImageId);
                    modalCommentText.value = '';
                });
            } else {
                alert('Comment cannot be empty');
            }
        });

        // Navigate to home (albums section)
        homeLink.addEventListener('click', () => {
            showAlbumSection();
        });

        // Show Create Album Section
        createAlbumBtn.addEventListener('click', () => {
            albumSection.style.display = 'none';
            createAlbumSection.style.display = 'block';
            homeLink.style.display = 'inline'; // Ensure homeLink is visible when creating album

            // Load user images when user clicks to create album
            makeCall('GET', 'ViewCreateAlbum', null, null, (response) => {
                const images = JSON.parse(response.responseText);
                populateUserImages(images);
            });
        });

        // Populate the images in the Create Album section
        function populateUserImages(images) {
            const userImagesDiv = document.getElementById("userImages");
            userImagesDiv.innerHTML = "";

            images.forEach(image => {
                const imageDiv = document.createElement('div');
                imageDiv.className = "image-item";

                const thumbnail = document.createElement('img');
                thumbnail.setAttribute('src', `images/${image.path}`);
                thumbnail.className = "thumbnail";

                const checkbox = document.createElement('input');
                checkbox.setAttribute('type', 'checkbox');
                checkbox.setAttribute('name', 'selectedImages');
                checkbox.setAttribute('value', image.id);

                imageDiv.appendChild(thumbnail);
                imageDiv.appendChild(checkbox);
                userImagesDiv.appendChild(imageDiv);
            });
        }

        // Submit Create Album Form
        createAlbumSection.querySelector('form').addEventListener('submit', function(event) {
            event.preventDefault();
            const formData = new FormData(this);
            makeCall('POST', 'CreateAlbum', formData, null, () => {
                alert("Album created successfully!");
                showAlbumSection();
            });
        });

        // Initialize showing albums
        showAlbumSection();
    }
});
