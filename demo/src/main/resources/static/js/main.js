function handleResponse(response) {
    return response.text().then(text => {
        let data;
        try { data = JSON.parse(text); } catch(e) {
            if (!response.ok) throw new Error('Ошибка сервера');
            throw new Error('Некорректный ответ сервера');
        }
        if (!response.ok) throw new Error(data.message || 'Ошибка');
        return data;
    });
}

function showFlashMessage(message, type, autoHide = true) {
    const container = document.getElementById('flash-messages-container');
    if (!container) return;
    const msgDiv = document.createElement('div');
    msgDiv.className = `flash-message ${type}`;
    msgDiv.innerText = message;
    container.appendChild(msgDiv);
    if (autoHide) setTimeout(() => msgDiv.remove(), 5000);
}

function addToCart(button) {
    const title = button.getAttribute('data-movie-title');
    if (!title) return;
    button.disabled = true;
    fetch('/user/cart/add/' + encodeURIComponent(title), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success', true);
        updateCartCount();
        toggleCartButton(button, true);
    })
    .catch(error => showFlashMessage(error.message || 'Ошибка', 'error', true))
    .finally(() => button.disabled = false);
}

function removeFromCart(button) {
    const title = button.getAttribute('data-movie-title');
    if (!title) return;
    button.disabled = true;
    fetch('/user/cart/remove/' + encodeURIComponent(title), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success', true);
        updateCartCount();
        toggleCartButton(button, false);
        const movieCard = button.closest('.movie-card');
        if (movieCard && window.location.pathname.includes('/cart')) {
            movieCard.remove();
            const remaining = document.querySelectorAll('.movie-card').length;
            const countSpan = document.querySelector('.cart-count span');
            if (countSpan) countSpan.innerText = remaining;
            if (remaining === 0) window.location.reload();
        }
    })
    .catch(error => showFlashMessage(error.message || 'Ошибка', 'error', true))
    .finally(() => button.disabled = false);
}

function toggleCartButton(button, inCart) {
    if (inCart) {
        button.classList.remove('to-cart-btn-details');
        button.classList.add('in-cart-btn-details');
        button.innerText = 'В корзине Перейти';
        button.onclick = function() { window.location.href = '/user/cart'; };
    } else {
        button.classList.remove('in-cart-btn-details');
        button.classList.add('to-cart-btn-details');
        button.innerText = 'В корзину';
        button.onclick = function() { addToCart(this); };
    }
}

function updateCartCount() {
    fetch('/user/cart/count')
        .then(response => response.text())
        .then(count => {
            const badge = document.getElementById('cart-count');
            if (badge) {
                badge.innerText = count;
                badge.style.display = (count && count !== '0') ? 'flex' : 'none';
            }
        })
        .catch(console.error);
}

function toggleFavourite(button) {
    const title = button.getAttribute('data-movie-title');
    const action = button.getAttribute('data-action');
    if (!title || !action) return;
    button.disabled = true;
    fetch('/user/favourites/' + action + '/' + encodeURIComponent(title), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success', true);
        const newAction = action === 'add' ? 'remove' : 'add';
        button.setAttribute('data-action', newAction);
        const span = button.querySelector('span');
        if (span) span.textContent = newAction === 'add' ? '♡' : '♥';
        button.classList.toggle('active');
        updateFavouritesCount();
    })
    .catch(error => showFlashMessage(error.message || 'Ошибка', 'error', true))
    .finally(() => button.disabled = false);
}

function updateFavouritesCount() {
    fetch('/user/favourites/count')
        .then(response => response.text())
        .then(count => {
            const badge = document.getElementById('favourites-count');
            if (badge) {
                badge.innerText = count;
                badge.style.display = (count && count !== '0') ? 'flex' : 'none';
            }
        })
        .catch(console.error);
}

function showBuyModal(button) {
    const title = button.getAttribute('data-movie-title');
    const price = button.getAttribute('data-movie-price');
    if (!title) return;
    document.getElementById('modalMovieTitle').innerText = title;
    document.getElementById('modalMoviePrice').innerHTML = price + ' ₽';
    document.getElementById('buyModal').style.display = 'flex';
}

function hideBuyModal() {
    document.getElementById('buyModal').style.display = 'none';
}

function confirmPurchase() {
    const title = document.getElementById('modalMovieTitle').innerText;
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/user/purchases/add/' + encodeURIComponent(title);
    document.body.appendChild(form);
    form.submit();
}

let _ratingModalMode = 'add';

function showRatingModal(button, mode = 'add', existingRating = 5.0, existingReview = '') {
    const title = button.getAttribute('data-movie-title');
    if (!title) return;
    _ratingModalMode = mode;
    document.getElementById('ratingFormTitle').value = title;
    document.getElementById('ratingModalMovieTitle').innerText = title;
    document.getElementById('ratingValue').value = existingRating;
    document.getElementById('ratingComment').value = existingReview;
    document.getElementById('ratingModal').style.display = 'flex';
}

function hideRatingModal() {
    document.getElementById('ratingModal').style.display = 'none';
}

function updateMovieRating(title) {
    fetch('/user/movies/rating/' + encodeURIComponent(title) + '?_=' + Date.now())
        .then(response => response.json())
        .then(data => {
            const newRating = data.rating;
            document.querySelectorAll(`.badge.rating[data-movie-title="${title}"]`).forEach(badge => {
                badge.textContent = newRating;
            });
        })
        .catch(err => console.warn('Rating update failed', err));
}

function submitRating() {
    const title = document.getElementById('ratingFormTitle').value;
    const rating = document.getElementById('ratingValue').value;
    const review = document.getElementById('ratingComment').value;
    const url = _ratingModalMode === 'add' ? '/user/rated/add' : '/user/rated/edit';

    const params = new URLSearchParams();
    params.append('title', title);
    params.append('rating', rating);
    if (review && review.trim() !== '') params.append('review', review);

    fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
        body: params.toString()
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success', true);
        hideRatingModal();
        updateMovieRating(title);
        if (window.location.pathname.includes('/reviews')) {
            sessionStorage.setItem('flashMessage', JSON.stringify({ message: data.message, type: 'success' }));
            window.location.reload();
        }
    })
    .catch(error => showFlashMessage(error.message || 'Ошибка оценки', 'error', true));
}

function executeDelete() {
    if (!_deleteMovieTitle) return;
    const title = _deleteMovieTitle;
    fetch('/user/rated/remove/' + encodeURIComponent(title), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success', true);
        updateMovieRating(title);
        if (window.location.pathname.includes('/reviews')) {
            sessionStorage.setItem('flashMessage', JSON.stringify({ message: data.message, type: 'success' }));
            window.location.reload();
        }
    })
    .catch(error => showFlashMessage(error.message || 'Ошибка удаления', 'error', true))
    .finally(() => hideConfirmDeleteModal());
}

let _deleteMovieTitle = null;

function showConfirmDeleteModal(title) {
    _deleteMovieTitle = title;
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.style.display = 'flex';
}

function hideConfirmDeleteModal() {
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.style.display = 'none';
    _deleteMovieTitle = null;
}

function deleteRating(title) {
    showConfirmDeleteModal(title);
}

function toggleUserMenu() {
    const userMenu = document.querySelector('.user-menu');
    if (userMenu) userMenu.classList.toggle('show');
}

document.addEventListener('click', function(e) {
    const userMenu = document.querySelector('.user-menu');
    if (userMenu && !userMenu.contains(e.target)) {
        userMenu.classList.remove('show');
    }
    if (e.target.classList && e.target.classList.contains('modal')) {
        e.target.style.display = 'none';
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const ratingForm = document.getElementById('ratingForm');
    if (ratingForm) {
        ratingForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitRating();
        });
    }
    updateCartCount();
    updateFavouritesCount();
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (confirmBtn) confirmBtn.onclick = executeDelete;
    const stored = sessionStorage.getItem('flashMessage');
    if (stored) {
        try {
            const { message, type } = JSON.parse(stored);
            showFlashMessage(message, type, true);
            sessionStorage.removeItem('flashMessage');
        } catch(e) {}
    }
});

window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        updateCartCount();
        updateFavouritesCount();
    }
});

function refreshBadgesOnBack() {
    updateCartCount();
    updateFavouritesCount();
}

window.addEventListener('popstate', refreshBadgesOnBack);
window.addEventListener('pageshow', function(e) {
    if (e.persisted) refreshBadgesOnBack();
});