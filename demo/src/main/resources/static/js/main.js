function handleResponse(response) {
    if (response.status === 401) {
        return response.json().then(data => {
            throw new Error(data.message || 'Авторизуйтесь!');
        }).catch(err => {
            throw new Error(err.message || 'Авторизуйтесь!');
        });
    }

    return response.text().then(text => {
        let data = {};
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (e) {
                if (!response.ok) throw new Error('Ошибка сервера');
                throw new Error('Некорректный ответ сервера');
            }
        }

        if (!response.ok) throw new Error(data.message || 'Произошла ошибка');

        if (!data.message) {
            data.message = '';
        }

        return data;
    });
}

function showFlashMessage(message, type, autoHide = true) {
    if (!message || message === 'undefined') return;
    const container = document.getElementById('flash-messages-container');
    if (!container) {
        console.log(`[${type.toUpperCase()}]: ${message}`);
        return;
    }
    const div = document.createElement('div');
    div.className = `flash-message ${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';
    if (type === 'warning') icon = '⚠️';

    div.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(div);

    if (autoHide) {
        setTimeout(() => {
            div.style.animation = 'slideIn 0.3s ease reverse forwards';
            div.addEventListener('animationend', () => div.remove());
        }, 4000);
    }
}

function getMovieId(element) {
    return element?.getAttribute('data-movie-id') || '';
}

function refreshCounts() {
    updateCartCount();
    updateFavouritesCount();
}

function addToCart(button) {
    const movieId = getMovieId(button);
    if (!movieId) return;
    button.disabled = true;

    fetch('/cart/add/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success');
        updateCartCount();
        window.location.reload();
    })
    .catch(err => showFlashMessage(err.message || 'Ошибка', 'error'))
    .finally(() => {});
}

function removeFromCart(button) {
    const movieId = getMovieId(button);
    if (!movieId) return;
    button.disabled = true;

    fetch('/cart/remove/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success');
        updateCartCount();
        const parent = button.parentNode;
        const btn = document.createElement('button');
        btn.className = 'cart-button to-cart-btn-details';
        btn.textContent = 'В корзину';
        btn.setAttribute('data-movie-id', movieId);
        btn.onclick = function() { addToCart(this); };
        parent.replaceChild(btn, button);

        const card = button.closest('.movie-card');
        if (card && window.location.pathname.includes('/cart')) {
            card.style.transform = 'scale(0.9)';
            card.style.opacity = '0';
            setTimeout(() => {
                card.remove();
                const rem = document.querySelectorAll('.movie-card').length;
                const span = document.querySelector('.cart-count span');
                if (span) span.innerText = rem;
                if (rem === 0) window.location.reload();
            }, 300);
        }
    })
    .catch(err => showFlashMessage(err.message || 'Ошибка', 'error'))
    .finally(() => button.disabled = false);
}

function toggleCartButton(button, inCart) {
    const parent = button.parentNode;
    if (inCart) {
        const link = document.createElement('a');
        link.className = 'cart-button in-cart-btn-details';
        link.href = '/cart';
        link.textContent = 'В корзине →';
        parent.replaceChild(link, button);
    } else {
        const btn = document.createElement('button');
        btn.className = 'cart-button to-cart-btn-details';
        btn.textContent = 'В корзину';
        btn.setAttribute('data-movie-id', button.getAttribute('data-movie-id') || '');
        btn.onclick = function() { addToCart(this); };
        parent.replaceChild(btn, button);
    }
}

function updateCartCount() {
    fetch('/cart/count')
        .then(r => r.text())
        .then(count => {
            const b = document.getElementById('cart-count');
            if (b) {
                b.innerText = count;
                b.style.display = (count && count !== '0') ? 'flex' : 'none';
            }
        })
        .catch(console.error);
}

function toggleFavourite(button) {
    const movieId = getMovieId(button);
    const action = button.getAttribute('data-action');
    if (!movieId || !action) return;
    button.disabled = true;

    fetch('/favourites/' + action + '/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success');
        const newAction = action === 'add' ? 'remove' : 'add';
        button.setAttribute('data-action', newAction);

        const span = button.querySelector('span');
        if (span) span.textContent = newAction === 'add' ? '♡' : '♥';
        button.classList.toggle('active');

        if (window.location.pathname.includes('/favourites') && newAction === 'add') {
            const card = button.closest('.movie-card');
            if (card) {
                card.style.transform = 'scale(0.9)';
                card.style.opacity = '0';
                setTimeout(() => card.remove(), 300);
            }
        }
        updateFavouritesCount();
    })
    .catch(err => showFlashMessage(err.message || 'Ошибка', 'error'))
    .finally(() => button.disabled = false);
}

function updateFavouritesCount() {
    fetch('/favourites/count')
        .then(r => r.text())
        .then(count => {
            const b = document.getElementById('favourites-count');
            if (b) {
                b.innerText = count;
                b.style.display = (count && count !== '0') ? 'flex' : 'none';
            }
        })
        .catch(console.error);
}

function showBuyModal(button) {
    const userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

    const movieId = getMovieId(button);
    if (!movieId) {
        showFlashMessage('Ошибка: не удалось определить фильм', 'error');
        return;
    }
    const title = button.getAttribute('data-movie-title') || movieId;
    const price = button.getAttribute('data-movie-price');
    const modalTitle = document.getElementById('modalMovieTitle');
    const modalPrice = document.getElementById('modalMoviePrice');
    const modal = document.getElementById('buyModal');

    if (!modal || !modalTitle || !modalPrice) return;

    modalTitle.innerText = title;
    modalTitle.dataset.movieId = movieId;
    modalPrice.innerHTML = price ? price + ' ₽' : '';

    modal.classList.add('show');
}

function hideBuyModal() {
    const modal = document.getElementById('buyModal');
    if (modal) modal.classList.remove('show');
}

function confirmPurchase() {
    const modalTitle = document.getElementById('modalMovieTitle');
    if (!modalTitle) return;
    const movieId = modalTitle.dataset.movieId || modalTitle.innerText;

    const btn = document.querySelector('#buyModal .modal-confirm-btn');
    if (btn) { btn.disabled = true; btn.textContent = 'Покупаем...'; }

    fetch('/orders/add/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        hideBuyModal();
        showFlashMessage(data.message, 'success');
        if (btn) { btn.disabled = false; btn.textContent = 'Подтвердить'; }
        const card = document.querySelector('.movie-card .bought-btn') || document.querySelector('.bought-btn-large');
        if (card) {
            const parent = card.parentNode;
            const newBtn = document.createElement('button');
            newBtn.className = 'bought-btn' + (card.classList.contains('bought-btn-large') ? '-large' : '');
            newBtn.disabled = true;
            newBtn.textContent = 'Куплено';
            parent.replaceChild(newBtn, card);
        }
        refreshCounts();
    })
    .catch(error => {
        hideBuyModal();
        showFlashMessage(error.message || 'Ошибка при покупке', 'error');
        if (btn) { btn.disabled = false; btn.textContent = 'Подтвердить'; }
    });
}

function showRatingModal(movieId, movieTitle, mode = 'add', existingRating = 5.0, existingReview = '') {
    if (window.event) window.event.stopPropagation();

    const userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

    if (!movieId) {
        showFlashMessage('Ошибка: ID фильма не передан', 'error');
        return;
    }
    _ratingModalMode = mode;

    const modal = document.getElementById('ratingModal');
    const keyInput = document.getElementById('ratingFormTitle');
    const titleEl = document.getElementById('ratingModalMovieTitle');
    const ratingInput = document.getElementById('ratingValue');
    const reviewInput = document.getElementById('ratingComment');
    const heading = modal?.querySelector('h2');

    const errorDiv = document.getElementById('ratingModalError');
    if (errorDiv) {
        errorDiv.innerText = '';
        errorDiv.style.display = 'none';
    }

    if (keyInput) keyInput.value = movieId;
    if (titleEl) {
        titleEl.innerText = movieTitle || "Рейтинг фильма";
        titleEl.dataset.movieId = movieId;
    }
    if (ratingInput) ratingInput.value = existingRating;
    if (reviewInput) reviewInput.value = existingReview;

    if (heading) {
        heading.innerText = (mode === 'edit') ? 'Редактировать отзыв' : 'Оценить фильм';
    }

    if (modal) modal.classList.add('show');
}

function hideRatingModal() {
    const modal = document.getElementById('ratingModal');
    if (modal) modal.classList.remove('show');

    const errorDiv = document.getElementById('ratingModalError');
    if (errorDiv) {
        errorDiv.innerText = '';
        errorDiv.style.display = 'none';
    }
}

function submitRating() {
    const modal = document.getElementById('ratingModal');
    if (!modal) return;

    let errorDiv = document.getElementById('ratingModalError');
    if (errorDiv) {
        errorDiv.style.display = 'none';
        errorDiv.innerText = '';
    }

    const movieId = document.getElementById('ratingFormTitle').value;
    let rating = document.getElementById('ratingValue').value;
    const review = document.getElementById('ratingComment').value;

    if (!movieId) {
        if (errorDiv) { errorDiv.innerText = 'Ошибка: не определён фильм'; errorDiv.style.display = 'block'; }
        return;
    }

    const ratingValue = parseFloat(rating);
    if (isNaN(ratingValue) || ratingValue < 0 || ratingValue > 10) {
        if (errorDiv) { errorDiv.innerText = 'Оценка должна быть от 0.0 до 10.0'; errorDiv.style.display = 'block'; }
        return;
    }

    const url = _ratingModalMode === 'add' ? '/rated/add' : '/rated/edit';

    const params = new URLSearchParams();
    params.append('id', movieId);
    params.append('rating', ratingValue.toFixed(1));
    params.append('review', review ? review.trim() : '');

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: params.toString()
    })
    .then(handleResponse)
    .then(data => {
        hideRatingModal();
        if (window.location.pathname.includes('/reviews')) {
            sessionStorage.setItem('flashMessage', JSON.stringify({ message: data.message, type: 'success' }));
            setTimeout(() => window.location.reload(), 600);
        } else {
            showFlashMessage(data.message, 'success');
            updateMovieRating(movieId);
        }
    })
    .catch(err => {
        hideRatingModal();
        showFlashMessage(err.message || 'Ошибка при отправке оценки', 'error');
    });
}

function updateMovieRating(movieId) {
    fetch('/rated/rating/' + encodeURIComponent(movieId) + '?_=' + Date.now())
        .then(r => r.json())
        .then(data => {
            const newRating = data.rating;
            document.querySelectorAll('.badge.rating').forEach(b => {
                const id = b.getAttribute('data-movie-id');
                if (String(id) === String(movieId)) {
                    if (newRating !== null && newRating !== '-') {
                        const formatted = parseFloat(newRating).toFixed(1);
                        const currentText = b.textContent.trim();
                        if (currentText.includes('★')) {
                            b.textContent = currentText.includes('/ 10') ? '★ ' + formatted + ' / 10' : '★ ' + formatted;
                        } else {
                            b.textContent = formatted;
                        }
                    } else {
                        b.textContent = '-';
                    }
                }
            });
        })
        .catch(console.warn);
}

function deleteRating(movieId) {
    if (!movieId) {
        showFlashMessage('Ошибка: не удалось считать ID фильма', 'error');
        return;
    }
    _deletemovieId = movieId;
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.classList.add('show');
}

function hideConfirmDeleteModal() {
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.classList.remove('show');
    _deletemovieId = null;
}

function executeDelete() {
    if (!_deletemovieId) return;
    const movieId = _deletemovieId;

    fetch('/rated/remove/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        hideConfirmDeleteModal();
        if (window.location.pathname.includes('/reviews')) {
            sessionStorage.setItem('flashMessage', JSON.stringify({ message: data.message, type: 'success' }));
            setTimeout(() => window.location.reload(), 600);
        } else {
            showFlashMessage(data.message, 'success');
            updateMovieRating(movieId);
        }
    })
    .catch(err => showFlashMessage(err.message || 'Ошибка удаления', 'error'));
}

function toggleUserMenu() {
    document.querySelector('.user-menu')?.classList.toggle('show');
}

function toggleFiltersModal(show) {
    const overlay = document.getElementById('filtersModalOverlay');

    if (overlay) {
        if (typeof show === 'boolean') {
            if (show) {
                overlay.classList.add('show');
                document.body.style.overflow = 'hidden';
            } else {
                overlay.classList.remove('show');
                document.body.style.overflow = '';
                document.getElementById('directorsDropdown')?.classList.remove('active');
                document.getElementById('genresDropdown')?.classList.remove('active');
            }
        } else {
            overlay.classList.toggle('show');
            if (overlay.classList.contains('show')) {
                document.body.style.overflow = 'hidden';
            } else {
                document.body.style.overflow = '';
            }
        }
    } else {
        console.warn("Панель фильтров (#filtersModalOverlay) не найдена в DOM.");
    }
}

document.addEventListener('click', function(e) {
    const menu = document.querySelector('.user-menu');
    if (menu && !menu.contains(e.target)) menu.classList.remove('show');

    if (e.target.classList && e.target.classList.contains('modal')) {
        e.target.classList.remove('show');
        if (e.target.id === 'confirmDeleteModal') _deletemovieId = null;
    }
});

function calculateRangeMinutes() {
        const getVal = (id) => parseInt(document.getElementById(id)?.value, 10) || 0;

        const fromH = getVal('durationFromHours');
        const fromM = getVal('durationFromMinutes');
        const fromS = getVal('durationFromSeconds');
        const totalFrom = (fromH * 60) + fromM + Math.round(fromS / 60);

        const toH = getVal('durationToHours');
        const toM = getVal('durationToMinutes');
        const toS = getVal('durationToSeconds');
        const totalTo = (toH * 60) + toM + Math.round(toS / 60);

        const minEl = document.getElementById('minDuration');
        const maxEl = document.getElementById('maxDuration');

        if (minEl) minEl.value = totalFrom > 0 ? totalFrom : '';
        if (maxEl) maxEl.value = totalTo > 0 ? totalTo : '';
}

document.addEventListener('DOMContentLoaded', () => {
    const positiveInputs = document.querySelectorAll('.positive-int');

    positiveInputs.forEach(input => {
        input.addEventListener('keydown', (e) => {
            if (e.ctrlKey || e.metaKey) return;

            const allowedKeys = ['Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Enter', 'Escape'];
            if (allowedKeys.includes(e.key)) return;

            if (!/^[0-9]$/.test(e.key)) {
                e.preventDefault();
            }
        });

        input.addEventListener('input', (e) => {
            e.target.value = e.target.value.replace(/\D/g, '');
            calculateRangeMinutes();
        });
    });

    calculateRangeMinutes();

    const filterForm = document.getElementById('filterForm') || document.querySelector('form');
    if (filterForm) {
        filterForm.addEventListener('reset', () => {
            setTimeout(calculateRangeMinutes, 0);
        });
    }
});

document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') refreshCounts();
});

function requireAuth(event, url) {
    var userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
        event.preventDefault();
        showFlashMessage('Авторизуйтесь!', 'error');
        return false;
    }
    return true;
}