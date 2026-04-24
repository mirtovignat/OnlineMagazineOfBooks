function toggleUserMenu() {
    const menu = document.getElementById('userMenu');
    if (menu) menu.classList.toggle('show');
}
document.addEventListener('click', function(event) {
    const menu = document.getElementById('userMenu');
    if (menu && !menu.contains(event.target)) menu.classList.remove('show');
});
function showFlashMessage(message, type, isJs = false) {
    if (!isJs) return;
    document.querySelectorAll('.js-flash').forEach(msg => msg.remove());
    const flash = document.createElement('div');
    flash.className = `flash-message ${type} js-flash`;
    flash.innerHTML = `<span>${message}</span>`;
    document.body.appendChild(flash);
    setTimeout(() => {
        flash.style.animation = 'fadeOut 0.3s ease forwards';
        setTimeout(() => flash.remove(), 300);
    }, 3000);
}
function updateCartCount() {
    fetch('/user/cart/count')
        .then(response => response.text())
        .then(count => {
            const badge = document.querySelector('.cart-btn .count-badge');
            if (badge) {
                const newCount = parseInt(count, 10) || 0;
                badge.textContent = newCount;
                badge.style.display = newCount > 0 ? 'flex' : 'none';
            }
        })
        .catch(() => {});
}
function updateFavouritesCount() {
    fetch('/user/favourites/count')
        .then(response => response.json())
        .then(data => {
            const badge = document.querySelector('.favourites-btn .count-badge');
            if (badge) {
                const count = data.count !== undefined ? data.count : data;
                badge.textContent = count;
                badge.style.display = count > 0 ? 'flex' : 'none';
            }
        })
        .catch(() => {});
}
let currentMovieTitle = '';
let currentMoviePrice = '';
function showBuyModal(button) {
    currentMovieTitle = button.getAttribute('data-movie-title');
    currentMoviePrice = button.getAttribute('data-movie-price');
    document.getElementById('modalMovieTitle').textContent = currentMovieTitle;
    document.getElementById('modalMoviePrice').textContent = currentMoviePrice + ' ₽';
    document.getElementById('buyModal').style.display = 'flex';
}
function hideBuyModal() {
    document.getElementById('buyModal').style.display = 'none';
}
function confirmPurchase() {
    window.location.href = '/user/purchases/add/' + encodeURIComponent(currentMovieTitle);
}
function showRatingModal(button) {
    const movieTitle = button.getAttribute('data-movie-title');
    document.getElementById('ratingModalMovieTitle').textContent = movieTitle;
    const form = document.getElementById('ratingForm');
    form.action = '/user/rated/add/' + encodeURIComponent(movieTitle);
    document.getElementById('ratingFormTitle').value = movieTitle;
    document.getElementById('ratingValue').value = '5.0';
    document.getElementById('ratingComment').value = '';
    document.getElementById('ratingModal').style.display = 'flex';
}
function hideRatingModal() {
    document.getElementById('ratingModal').style.display = 'none';
}
document.addEventListener('click', function(e) {
    if (e.target === document.getElementById('buyModal')) hideBuyModal();
    if (e.target === document.getElementById('ratingModal')) hideRatingModal();
});
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        hideBuyModal();
        hideRatingModal();
        const clearCart = document.getElementById('clearCartModal');
        if (clearCart) clearCart.style.display = 'none';
        const clearFav = document.getElementById('clearFavouritesModal');
        if (clearFav) clearFav.style.display = 'none';
    }
});
function toggleFavourite(button) {
    const movieTitle = button.getAttribute('data-movie-title');
    const action = button.getAttribute('data-action');
    if (!movieTitle) return;
    button.disabled = true;
    const url = action === 'add' ? '/user/favourites/add/' + encodeURIComponent(movieTitle) : '/user/favourites/remove/' + encodeURIComponent(movieTitle);
    fetch(url, { method: 'POST' })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка сервера');
            return response.json();
        })
        .then(data => {
            updateFavouritesCount();
            const newAction = action === 'add' ? 'remove' : 'add';
            button.setAttribute('data-action', newAction);
            const span = button.querySelector('span');
            span.textContent = newAction === 'add' ? '♡' : '♥';
            button.classList.toggle('active');
            showFlashMessage(data.message, 'success', true);
        })
        .catch(() => showFlashMessage('Ошибка при изменении избранного', 'error', true))
        .finally(() => button.disabled = false);
}

function addToCart(button) {
    const movieTitle = button.getAttribute('data-movie-title');
    if (!movieTitle) return;
    button.disabled = true;
    fetch('/user/cart/add/' + encodeURIComponent(movieTitle), { method: 'POST' })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка сервера');
            return response.json();
        })
        .then(data => {
            updateCartCount();
            showFlashMessage(data.message, 'success', true);
            button.outerHTML = '<button class="cart-button in-cart-btn-details" onclick="window.location.href=\'/user/cart\'">В корзине<br/><span class="go-to-cart">Перейти</span></button>';
        })
        .catch(() => showFlashMessage('Ошибка при добавлении в корзину', 'error', true))
        .finally(() => button.disabled = false);
}
