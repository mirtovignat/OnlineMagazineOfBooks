let _deleteMovieId = null;
let _ratingModalMode = 'add';

function handleResponse(response) {
    if (response.redirected && response.url.includes('/login')) {
        return Promise.reject({ status: 401, message: 'Авторизуйтесь!' });
    }
    if (response.status === 401) {
        return Promise.reject({ status: 401, message: 'Авторизуйтесь!' });
    }

    return response.text().then(text => {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/html')) {
            window.location.href = '/login';
            return new Promise(() => {});
        }
        let data = {};
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (e) {
                if (!response.ok) {
                    return Promise.reject({
                        status: response.status,
                        message: 'Ошибка сервера'
                    });
                }
                throw new Error('Некорректный ответ сервера');
            }
        }

        if (!response.ok) {
            return Promise.reject({
                status: response.status,
                message: data.message || 'Произошла ошибка'
            });
        }

        if (!data.message) data.message = '';
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

function saveFlashAndRedirect(message, type = 'success', redirectUrl = '/') {
    if (message) {
        sessionStorage.setItem('flashMessage', JSON.stringify({ message, type }));
    }
    window.location.href = redirectUrl;
}

function isAuthenticated() {
    const authStatus = document.getElementById('authStatus');
    return authStatus && authStatus.dataset.authenticated === 'true';
}

function getMovieId(element) {
    return element?.getAttribute('data-movie-id') || '';
}

function refreshCounts() {
    updateCartCount();
    updateFavouritesCount();
}

function requireAuth(event, url) {
    if (!isAuthenticated()) {
        event.preventDefault();
        showFlashMessage('Авторизуйтесь!', 'error');
        return false;
    }
    return true;
}

function setCartStatus(movieId, inCart) {
    sessionStorage.setItem('cart_' + movieId, inCart ? 'true' : 'false');
}

function getCartStatus(movieId) {
    const val = sessionStorage.getItem('cart_' + movieId);
    return val === 'true' ? true : (val === 'false' ? false : null);
}

function setFavouriteStatus(movieId, inFav) {
    sessionStorage.setItem('fav_' + movieId, inFav ? 'true' : 'false');
}

function getFavouriteStatus(movieId) {
    const val = sessionStorage.getItem('fav_' + movieId);
    return val === 'true' ? true : (val === 'false' ? false : null);
}

function clearAllStatuses() {
    const keys = Object.keys(sessionStorage);
    keys.forEach(key => {
        if (key.startsWith('cart_') || key.startsWith('fav_')) {
            sessionStorage.removeItem(key);
        }
    });
}

function applyStatusesFromSession() {
    document.querySelectorAll('.movie-card').forEach(card => {
        const movieId = card.getAttribute('data-movie-id');
        if (!movieId) return;

        const cartStatus = getCartStatus(movieId);
        if (cartStatus !== null) {
            const cartBtn = card.querySelector('.cart-button, .in-cart-btn-details, .to-cart-btn-details');
            if (cartBtn) {
                if (cartStatus) {
                    cartBtn.textContent = 'В корзине → Перейти';
                    cartBtn.className = 'cart-button in-cart-btn-details';
                    cartBtn.onclick = function() { window.location.href = '/cart'; };
                } else {
                    cartBtn.textContent = 'В корзину';
                    cartBtn.className = 'cart-button to-cart-btn-details';
                    cartBtn.setAttribute('onclick', 'addToCart(this)');
                    cartBtn.removeAttribute('href');
                }
            }
        }

        const favStatus = getFavouriteStatus(movieId);
        if (favStatus !== null) {
            const favBtn = card.querySelector('.favourite-btn');
            if (favBtn) {
                const span = favBtn.querySelector('span');
                if (span) {
                    span.textContent = favStatus ? '♥' : '♡';
                }
                favBtn.classList.toggle('active', favStatus);
                favBtn.setAttribute('data-action', favStatus ? 'remove' : 'add');
            }
        }
    });
    clearAllStatuses();
}

function addToCart(button) {
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        if (button && button.tagName && button.tagName.toLowerCase() === 'button') {
            button.disabled = false;
        }
        return;
    }

    const movieId = getMovieId(button);
    if (!movieId) {
        showFlashMessage('Ошибка: не удалось определить фильм', 'error');
        if (button && button.tagName && button.tagName.toLowerCase() === 'button') {
            button.disabled = false;
        }
        return;
    }

    if (button && button.tagName && button.tagName.toLowerCase() === 'button') {
        button.disabled = true;
    }

    fetch('/cart/add/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success');
        updateCartCount();
        setCartStatus(movieId, true);
        if (button) {
            button.textContent = 'В корзине → Перейти';
            button.className = 'cart-button in-cart-btn-details';
            button.onclick = function() {
                window.location.href = '/cart';
            };
            button.disabled = false;
        }
    })
    .catch(err => {
        if (err.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(err.message || 'Ошибка', 'error');
        }
        if (button && button.tagName && button.tagName.toLowerCase() === 'button') {
            button.disabled = false;
        }
    });
}

function removeFromCart(button) {
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

    const movieId = getMovieId(button);
    if (!movieId) return;
    if (button.tagName.toLowerCase() === 'button') button.disabled = true;

    fetch('/cart/remove/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success');
        updateCartCount();
        setCartStatus(movieId, false);
        const card = button.closest('.movie-card');
        if (card && window.location.pathname.includes('/cart')) {
            card.style.transform = 'scale(0.9)';
            card.style.opacity = '0';
            setTimeout(() => {
                card.remove();
                const rem = document.querySelectorAll('.movie-card').length;
                if (rem === 0) window.location.reload();
            }, 300);
        } else {
            const newBtn = document.createElement('button');
            newBtn.type = 'button';
            newBtn.className = 'cart-button to-cart-btn-details';
            newBtn.setAttribute('data-movie-id', movieId);
            newBtn.setAttribute('onclick', 'addToCart(this)');
            newBtn.textContent = 'В корзину';
            button.parentNode.replaceChild(newBtn, button);
        }
    })
    .catch(err => {
        if (err.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(err.message || 'Ошибка', 'error');
        }
        if (button.tagName.toLowerCase() === 'button') button.disabled = false;
    });
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
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

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
        setFavouriteStatus(movieId, newAction === 'add');
        button.setAttribute('data-action', newAction);
        const span = button.querySelector('span');
        if (span) span.textContent = newAction === 'add' ? '♡' : '♥';
        button.classList.toggle('active');
        if (window.location.pathname.includes('/favourites') && newAction === 'add') {
            const card = button.closest('.movie-card');
            if (card) {
                card.style.transform = 'scale(0.9)';
                card.style.opacity = '0';
                setTimeout(() => {
                    card.remove();
                    if (document.querySelectorAll('.movie-card').length === 0) {
                        showEmptyFavourites();
                    }
                }, 300);
            }
        }
        updateFavouritesCount();
    })
    .catch(err => {
        if (err.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(err.message || 'Ошибка', 'error');
        }
    })
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
    if (!isAuthenticated()) {
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
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

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

        const buyButtons = document.querySelectorAll(`.buy-now-btn[data-movie-id="${movieId}"]`);

        buyButtons.forEach(buyBtn => {
            const cartCard = buyBtn.closest('.movie-card');

            if (window.location.pathname.includes('/cart') && cartCard) {
                const priceElement = cartCard.querySelector('.price');
                let price = 0;
                if (priceElement) {
                    price = parseFloat(
                        priceElement.textContent.replace(/[^\d.,]/g, '').replace(',', '.')
                    ) || 0;
                }

                setCartStatus(movieId, false);

                cartCard.style.transition = 'all .3s ease';
                cartCard.style.opacity = '0';
                cartCard.style.transform = 'scale(0.9)';

                setTimeout(() => {
                    cartCard.remove();

                    const totalPriceElement = document.querySelector('.btn-price');
                    if (totalPriceElement) {
                        let total = parseFloat(
                            totalPriceElement.textContent.replace(/[^\d.,]/g, '').replace(',', '.')
                        ) || 0;
                        total -= price;
                        totalPriceElement.textContent = (total <= 0) ? '0 ₽' : total.toFixed(0) + ' ₽';
                    }

                    const cartCount = document.querySelector('.cart-count span');
                    if (cartCount) {
                        let count = parseInt(cartCount.textContent) || 0;
                        count = Math.max(0, count - 1);
                        cartCount.textContent = count;
                    }

                    const cards = document.querySelectorAll('.movie-card');
                    if (cards.length === 0) {
                        const checkoutForm = document.querySelector('.text-center');
                        if (checkoutForm) checkoutForm.remove();

                        const grid = document.querySelector('.movies-grid');
                        if (grid) grid.remove();
                        const cartHeader = document.querySelector('.cart-header');
                        if (cartHeader) cartHeader.remove();
                        const main = document.querySelector('main');
                        if (main) {
                            main.innerHTML = `
                                <div class="empty-cart-message">
                                    <h3>😔 Корзина пуста</h3>
                                    <p class="text-muted">Вы еще не добавили ни одного фильма.</p>
                                    <a href="/" class="browse-movies-btn">Перейти в каталог</a>
                                </div>
                            `;
                        }
                        const headerCount = document.getElementById('cart-count');
                        if (headerCount) {
                            headerCount.textContent = '0';
                            headerCount.style.display = 'none';
                        }
                    }
                }, 300);

            } else {
                const container = buyBtn.parentNode;
                const purchaseButtons = container.querySelectorAll(
                    '.cart-button, .buy-now-btn, .in-cart-btn-details, .to-cart-btn-details'
                );
                purchaseButtons.forEach(btn => btn.remove());

                const newBtn = document.createElement('button');
                newBtn.className = 'bought-btn';
                newBtn.disabled = true;
                newBtn.textContent = 'Куплено';
                newBtn.style.opacity = '0';
                newBtn.style.transform = 'translateY(5px)';
                container.appendChild(newBtn);
                requestAnimationFrame(() => {
                    newBtn.style.transition = 'all .25s ease';
                    newBtn.style.opacity = '1';
                    newBtn.style.transform = 'translateY(0)';
                });
            }
        });

        refreshCounts();
    })
    .catch(error => {
        hideBuyModal();
        if (error.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(error.message || 'Ошибка при покупке', 'error');
        }
        if (btn) { btn.disabled = false; btn.textContent = 'Подтвердить'; }
    });
}

function getDeclension(n) {
    n = Math.abs(n);
    const lastDigit = n % 10;
    const lastTwoDigits = n % 100;

    if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
        return 'отзывов';
    }
    if (lastDigit === 1) {
        return 'отзыв';
    }
    if (lastDigit >= 2 && lastDigit <= 4) {
        return 'отзыва';
    }
    return 'отзывов';
}

function updateReviewsCount(movieId) {
    if (!movieId) return;
    fetch('/rated/count/' + encodeURIComponent(movieId))
        .then(r => {
            if (!r.ok) throw new Error('Ошибка получения количества отзывов');
            return r.text();
        })
        .then(count => {
            const num = parseInt(count, 10) || 0;
            document.querySelectorAll(`.js-reviews-count[data-movie-id="${movieId}"]`).forEach(badge => {
                badge.textContent = num;
                badge.style.display = num > 0 ? '' : 'none';
            });
            const reviewsCounter = document.getElementById('reviewsCount');
            if (reviewsCounter) {
                reviewsCounter.textContent = num + ' ' + getDeclension(num);
            }
        })
        .catch(console.warn);
}

function updateRatingBadge(movieId, rating) {
    const hasRating = rating !== null
        && rating !== '-'
        && rating !== '—'
        && rating !== 'undefined'
        && !isNaN(parseFloat(rating));

    const numericRating = hasRating ? parseFloat(rating).toFixed(1) : null;

    document.querySelectorAll(`.js-rating-badge[data-movie-id="${movieId}"]`).forEach(el => {
        el.textContent = numericRating ? `★ ${numericRating} / 10` : '★ —';
    });

    document.querySelectorAll(`.rating-val[data-movie-id="${movieId}"]`).forEach(el => {
        el.textContent = numericRating ? `★ ${numericRating}` : '★ —';
    });

    const ratingValueEl = document.querySelector('.rating-value');
    if (ratingValueEl) {
        ratingValueEl.textContent = numericRating ? `${numericRating} / 10` : '—';
    }

    document.querySelectorAll(`.favourites-card[data-movie-id="${movieId}"] .favourites-rating-badge`).forEach(el => {
        const ratingValueEl2 = el.querySelector('.rating-value');
        if (ratingValueEl2) {
            if (numericRating) {
                ratingValueEl2.textContent = numericRating;
                el.style.display = 'flex';
            } else {
                el.style.display = 'none';
            }
        }
    });

    document.querySelectorAll(`.favourites-card[data-movie-id="${movieId}"] .rating-value`).forEach(el => {
        if (numericRating) {
            el.textContent = numericRating;
        } else {
            el.textContent = '—';
        }
    });
}

function updateMovieRating(movieId) {
    if (!movieId) return;
    fetch('/movies/' + encodeURIComponent(movieId) + '/rating?_=' + Date.now())
        .then(r => {
            if (!r.ok) throw new Error('Ошибка получения рейтинга');
            return r.json();
        })
        .then(data => {
            const rating = (data.rating === '-' || data.rating === '—' || data.rating === null)
                ? null
                : data.rating;
            updateRatingBadge(movieId, rating);
        })
        .catch(console.warn);
}

function applyRatingResponse(data, movieId) {
    const payload = data?.data ?? data;

    if (payload?.rating !== undefined) {
        const rating = (payload.rating === '-' || payload.rating === '—' || payload.rating === null)
            ? null
            : payload.rating;
        updateRatingBadge(movieId, rating);
    }

    if (payload?.reviewsCount !== undefined) {
        const count = Number(payload.reviewsCount) || 0;
        document.querySelectorAll(`.js-reviews-count[data-movie-id="${movieId}"]`).forEach(badge => {
            badge.textContent = count;
            badge.style.display = count > 0 ? '' : 'none';
        });
        const reviewsCounter = document.getElementById('reviewsCount');
        if (reviewsCounter) {
            reviewsCounter.textContent = count + ' ' + getDeclension(count);
        }
    }
}

function showRatingModal(movieId, movieTitle, mode = 'add', existingRating = 5.0, existingReview = '') {
    if (window.event) window.event.stopPropagation();

    if (!isAuthenticated()) {
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
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

    const modal = document.getElementById('ratingModal');
    if (!modal) return;

    let errorDiv = document.getElementById('ratingModalError');
    if (errorDiv) {
        errorDiv.style.display = 'none';
        errorDiv.innerText = '';
    }

    const movieId = document.getElementById('ratingFormTitle').value;
    if (!movieId) {
        showFlashMessage('Ошибка: не удалось определить фильм', 'error');
        return;
    }

    let ratingInput = document.getElementById('ratingValue');
    let rating = ratingInput.value.replace(',', '.');
    const review = document.getElementById('ratingComment').value;

    const ratingRegex = /^(10(\.0)?|([1-9](\.[0-9])?)|0\.[1-9])$/;
    if (!ratingRegex.test(rating)) {
        if (errorDiv) {
            errorDiv.innerText = 'Введите число от 0.1 до 10.0 (одна цифра после точки)';
            errorDiv.style.display = 'block';
        }
        return;
    }

    const ratingValue = parseFloat(rating);
    if (ratingValue < 0.1 || ratingValue > 10) {
        if (errorDiv) {
            errorDiv.innerText = 'Оценка должна быть от 0.1 до 10.0';
            errorDiv.style.display = 'block';
        }
        return;
    }

    const submitBtn = document.querySelector('#ratingModal .modal-confirm-btn');
    const cancelBtn = document.querySelector('#ratingModal .modal-cancel-btn');
    const originalText = submitBtn ? submitBtn.textContent : 'Отправить';

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = '⏳ Отправка...';
    }
    if (cancelBtn) {
        cancelBtn.disabled = true;
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
        showFlashMessage(data.message, 'success');

        updateMovieRating(movieId);
        updateReviewsCount(movieId);

        const rating = data.rating !== undefined ? data.rating : ratingValue;
        const reviewsCount = data.reviewsCount !== undefined ? data.reviewsCount : null;

        const ownCard = document.querySelector(`.review-card[data-movie-id="${movieId}"][data-own="true"]`);
        if (ownCard) {
            const ratingValEl = ownCard.querySelector('.review-rating-value');
            const textEl = ownCard.querySelector('.review-text');
            const btnEdit = ownCard.querySelector('.btn-edit');
            if (ratingValEl) ratingValEl.textContent = ratingValue.toFixed(1);
            if (textEl) textEl.textContent = review ? review.trim() : '';
            if (btnEdit) {
                btnEdit.setAttribute('data-review-rating', ratingValue.toFixed(1));
                btnEdit.setAttribute('data-review-text', review ? review.trim() : '');
            }
        } else {
            const addBtnContainer = document.getElementById('addReviewBtnContainer');
            if (addBtnContainer) addBtnContainer.style.display = 'none';
            const noReviewsBlock = document.getElementById('noReviewsBlock');
            if (noReviewsBlock) noReviewsBlock.style.display = 'none';
            const username = document.getElementById('currentUserData')?.dataset.username || 'Пользователь';
            const firstLetter = username.charAt(0).toUpperCase() || 'U';
            const newCardHtml = `
                <div class="review-card own-card" data-movie-id="${movieId}" data-own="true" style="animation-delay: 0s;">
                    <div class="review-header">
                        <div class="review-user">
                            <div class="avatar">${firstLetter}</div>
                            <span class="review-username">${username}</span>
                        </div>
                        <div class="review-meta">
                            <span class="review-rating">
                                <span>★</span>
                                <span class="review-rating-value">${ratingValue.toFixed(1)}</span>
                            </span>
                            <span class="review-date">Только что</span>
                        </div>
                    </div>
                    <p class="review-text">${review ? review.trim() : ''}</p>
                    <div class="review-actions">
                        <button class="btn-edit"
                                data-movie-id="${movieId}"
                                data-movie-title=""
                                data-review-rating="${ratingValue.toFixed(1)}"
                                data-review-text="${review ? review.trim() : ''}"
                                onclick="showRatingModal(this.getAttribute('data-movie-id'), this.getAttribute('data-movie-title'), 'edit', parseFloat(this.getAttribute('data-review-rating')), this.getAttribute('data-review-text') || '')">
                            ✏️ Редактировать
                        </button>
                        <button class="btn-delete"
                                data-movie-id="${movieId}"
                                onclick="deleteRating(this.getAttribute('data-movie-id'))">
                            🗑 Удалить
                        </button>
                    </div>
                </div>
            `;
            let reviewsList = document.getElementById('reviewsList');
            if (!reviewsList) {
                reviewsList = document.createElement('div');
                reviewsList.className = 'reviews-list';
                reviewsList.id = 'reviewsList';
                const header = document.querySelector('.reviews-header');
                if (header) header.insertAdjacentElement('afterend', reviewsList);
            }
            reviewsList.insertAdjacentHTML('afterbegin', newCardHtml);
        }
    })
    .catch(err => {
        hideRatingModal();
        if (err.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(err.message || 'Ошибка сохранения', 'error');
        }
    })
    .finally(() => {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
        if (cancelBtn) {
            cancelBtn.disabled = false;
        }
    });
}

function deleteRating(movieId) {
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }
    if (!movieId) {
        showFlashMessage('Ошибка: не удалось считать ID фильма', 'error');
        return;
    }
    _deleteMovieId = movieId;
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.classList.add('show');
}

function hideConfirmDeleteModal() {
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.classList.remove('show');
    _deleteMovieId = null;
}

function executeDelete() {
    if (!_deleteMovieId) return;
    const movieId = _deleteMovieId;
    fetch('/rated/remove/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        hideConfirmDeleteModal();
        showFlashMessage(data.message, 'success');
        let rating = data.rating;
        if (rating === '-' || rating === '—' || rating === null || rating === undefined) {
            rating = null;
        }
        updateRatingBadge(movieId, rating);
        const reviewsCount = data.reviewsCount !== undefined ? data.reviewsCount : 0;
        document.querySelectorAll(`.js-reviews-count[data-movie-id="${movieId}"]`).forEach(badge => {
            badge.textContent = reviewsCount;
            badge.style.display = reviewsCount > 0 ? '' : 'none';
        });
        const reviewsCounter = document.getElementById('reviewsCount');
        if (reviewsCounter) {
            reviewsCounter.textContent = reviewsCount + ' ' + getDeclension(reviewsCount);
        }
        const ownCard = document.querySelector(`.review-card[data-movie-id="${movieId}"][data-own="true"]`);
        if (ownCard) {
            ownCard.style.transition = 'all 0.3s ease';
            ownCard.style.opacity = '0';
            ownCard.style.transform = 'scale(0.9)';
            setTimeout(() => {
                ownCard.remove();
                const addBtnContainer = document.getElementById('addReviewBtnContainer');
                if (addBtnContainer) addBtnContainer.style.display = 'block';
                const remainingCards = document.querySelectorAll('.review-card');
                if (remainingCards.length === 0) {
                    const noReviewsBlock = document.getElementById('noReviewsBlock');
                    if (noReviewsBlock) noReviewsBlock.style.display = 'block';
                }
            }, 300);
        }
        updateMovieRating(movieId);
    })
    .catch(err => {
        hideConfirmDeleteModal();
        if (err.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(err.message || 'Ошибка удаления', 'error');
        }
    });
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
        if (e.target.id === 'confirmDeleteModal') _deleteMovieId = null;
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

const commonDomains = [
    'gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com',
    'mail.ru', 'yandex.ru', 'rambler.ru', 'bk.ru', 'list.ru',
    'protonmail.com', 'proton.me', 'tutanota.com'
];

function validateEmail(email) {
    const domain = email.split('@')[1];
    if (!domain) return false;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return false;
    return commonDomains.includes(domain);
}

function initEmailValidation() {
    document.querySelectorAll('input[type="email"]').forEach(function(input) {
        input.addEventListener('blur', function() {
            var email = this.value.trim();
            if (email && !validateEmail(email)) {
                this.style.borderColor = '#f59e0b';
                this.style.boxShadow = '0 0 0 3px rgba(245, 158, 11, 0.2)';
                if (!this.dataset.warningShown) {
                    this.dataset.warningShown = 'true';
                    console.warn('Домен не из списка популярных: ' + email.split('@')[1]);
                }
            } else {
                this.style.borderColor = '';
                this.style.boxShadow = '';
                this.dataset.warningShown = '';
            }
        });

        input.addEventListener('input', function() {
            if (this.style.borderColor === 'rgb(245, 158, 11)' || this.style.borderColor === '#f59e0b') {
                var email = this.value.trim();
                if (!email || validateEmail(email)) {
                    this.style.borderColor = '';
                    this.style.boxShadow = '';
                    this.dataset.warningShown = '';
                }
            }
        });
    });
}

function setupRatingInput() {
    const input = document.getElementById('ratingValue');
    if (!input) return;

    let lastValue = '';

    input.addEventListener('input', function () {
        let value = this.value;

        if (value === '') {
            lastValue = '';
            return;
        }

        value = value.replace(',', '.');

        let parts = value.split('.');
        let intPart = parts[0].replace(/[^0-9]/g, '');
        let decPart = parts.length > 1 ? parts[1].replace(/[^0-9]/g, '') : '';

        if (intPart === '' && decPart !== '') {
            intPart = '0';
        }

        if (intPart.length > 1 && intPart.startsWith('0')) {
            intPart = intPart.replace(/^0+/, '');
            if (intPart === '') intPart = '0';
        }

        if (intPart.length > 2) {
            intPart = intPart.slice(0, 2);
        }

        let intNum = parseInt(intPart);
        if (intNum > 10) {
            intPart = '10';
        }

        if (decPart.length > 1) {
            decPart = decPart.slice(0, 1);
        }

        let newValue = intPart;
        if (decPart.length > 0 || (value.includes('.') && intPart !== '')) {
            newValue = intPart + '.' + decPart;
        }

        const num = parseFloat(newValue);
        if (!isNaN(num)) {
            if (num > 10) {
                newValue = '10.0';
            }
            if (num < 0.1 && num > 0) {
                newValue = '0.1';
            }
            if (num < 0) {
                newValue = '0';
            }
        }

        if (value === '.' || value === ',') {
            newValue = '0.';
        }

        if (value === '0.') {
            newValue = '0.';
        }

        if (this.value !== newValue) {
            this.value = newValue;
        }

        lastValue = this.value;
    });

    input.addEventListener('keydown', function (e) {
        const allowedKeys = ['Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Enter', 'Escape', '.', ','];
        if (allowedKeys.includes(e.key)) return;
        if (e.key >= '0' && e.key <= '9') return;
        e.preventDefault();
    });

    input.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text');
        const cleaned = pasted.replace(/[^0-9.,]/g, '').replace(',', '.');
        const num = parseFloat(cleaned);
        if (!isNaN(num)) {
            let val = Math.min(Math.max(num, 0.1), 10);
            if (Number.isInteger(val)) {
                this.value = val.toString();
            } else {
                this.value = val.toFixed(1);
            }
        } else if (cleaned === '.') {
            this.value = '0.';
        } else {
            this.value = '';
        }
    });

    input.addEventListener('blur', function () {
        if (this.value === '.' || this.value === '0.') {
            this.value = '0';
        }
        const num = parseFloat(this.value);
        if (!isNaN(num)) {
            if (num > 10) this.value = '10.0';
            if (num < 0.1 && num > 0) this.value = '0.1';
            if (num < 0) this.value = '0';
            if (Number.isInteger(num) && num >= 1 && num <= 10) {
                this.value = num.toString();
            }
        }
    });
}

function setupFilterRatingInput(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    input.addEventListener('input', function () {
        let value = this.value;
        if (value === '') return;
        value = value.replace(',', '.');
        let parts = value.split('.');
        let intPart = parts[0].replace(/[^0-9]/g, '');
        let decPart = parts.length > 1 ? parts[1].replace(/[^0-9]/g, '') : '';
        if (intPart === '' && decPart !== '') intPart = '0';
        if (intPart.length > 1 && intPart.startsWith('0')) {
            intPart = intPart.replace(/^0+/, '');
            if (intPart === '') intPart = '0';
        }
        if (intPart.length > 2) intPart = intPart.slice(0, 2);
        let intNum = parseInt(intPart);
        if (intNum > 10) intPart = '10';
        if (decPart.length > 1) decPart = decPart.slice(0, 1);
        let newValue = intPart;
        if (decPart.length > 0 || (value.includes('.') && intPart !== '')) {
            newValue = intPart + '.' + decPart;
        }
        const num = parseFloat(newValue);
        if (!isNaN(num)) {
            if (num > 10) newValue = '10.0';
            if (num < 0.1 && num > 0) newValue = '0.1';
            if (num < 0) newValue = '0';
        }
        if (value === '.' || value === ',') newValue = '0.';
        if (value === '0.') newValue = '0.';
        if (this.value !== newValue) this.value = newValue;
    });

    input.addEventListener('keydown', function (e) {
        const allowedKeys = ['Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Enter', 'Escape', '.', ','];
        if (allowedKeys.includes(e.key)) return;
        if (e.key >= '0' && e.key <= '9') return;
        e.preventDefault();
    });

    input.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text');
        const cleaned = pasted.replace(/[^0-9.,]/g, '').replace(',', '.');
        const num = parseFloat(cleaned);
        if (!isNaN(num)) {
            let val = Math.min(Math.max(num, 0.1), 10);
            if (Number.isInteger(val)) {
                this.value = val.toString();
            } else {
                this.value = val.toFixed(1);
            }
        } else if (cleaned === '.') {
            this.value = '0.';
        } else {
            this.value = '';
        }
    });

    input.addEventListener('blur', function () {
        if (this.value === '.' || this.value === '0.') {
            this.value = '0';
        }
        const num = parseFloat(this.value);
        if (!isNaN(num)) {
            if (num > 10) this.value = '10.0';
            if (num < 0.1 && num > 0) this.value = '0.1';
            if (num < 0) this.value = '0';
            if (Number.isInteger(num) && num >= 1 && num <= 10) {
                this.value = num.toString();
            }
        }
    });
}

function setupFilterPriceInput(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    input.addEventListener('input', function () {
        let value = this.value;
        if (value === '') return;
        value = value.replace(',', '.');
        let parts = value.split('.');
        let intPart = parts[0].replace(/[^0-9]/g, '');
        let decPart = parts.length > 1 ? parts[1].replace(/[^0-9]/g, '') : '';
        if (intPart.length > 1 && intPart.startsWith('0')) {
            intPart = intPart.replace(/^0+/, '');
            if (intPart === '') intPart = '0';
        }
        if (decPart.length > 2) decPart = decPart.slice(0, 2);
        let newValue = intPart;
        if (decPart.length > 0 || (value.includes('.') && intPart !== '')) {
            newValue = intPart + '.' + decPart;
        }
        if (value === '.' || value === ',') newValue = '0.';
        if (this.value !== newValue) this.value = newValue;
    });

    input.addEventListener('keydown', function (e) {
        const allowedKeys = ['Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Enter', 'Escape', '.', ','];
        if (allowedKeys.includes(e.key)) return;
        if (e.key >= '0' && e.key <= '9') return;
        e.preventDefault();
    });

    input.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text');
        const cleaned = pasted.replace(/[^0-9.,]/g, '').replace(',', '.');
        const parts = cleaned.split('.');
        const intPart = parts[0].replace(/[^0-9]/g, '');
        const decPart = parts.length > 1 ? parts[1].replace(/[^0-9]/g, '').slice(0, 2) : '';
        if (intPart !== '' || decPart !== '') {
            this.value = decPart.length > 0 ? intPart + '.' + decPart : intPart;
        } else if (cleaned === '.') {
            this.value = '0.';
        } else {
            this.value = '';
        }
    });

    input.addEventListener('blur', function () {
        if (this.value === '.' || this.value === '0.') {
            this.value = '0';
        }
    });
}

function openClearCartModal() {
    document.getElementById('clearCartModal')?.classList.add('show');
}

function closeClearCartModal() {
    document.getElementById('clearCartModal')?.classList.remove('show');
}

function confirmClearCart() {
    if (!isAuthenticated()) {
        showFlashMessage('Авторизуйтесь!', 'error');
        return;
    }

    var button = document.querySelector('#clearCartModal .modal-confirm-btn');
    if (button) {
        button.disabled = true;
        button.textContent = 'Очищаем...';
    }
    fetch('/cart/clear', {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'application/json'
        }
    })
    .then(handleResponse)
    .then(function(data) {
        closeClearCartModal();
        showFlashMessage(data.message || 'Корзина очищена', 'success');
        document.querySelectorAll('.movie-card').forEach(card => {
            const id = card.getAttribute('data-movie-id');
            if (id) setCartStatus(id, false);
        });
        updateCartCount();
        location.reload();
    })
    .catch(function(err) {
        closeClearCartModal();
        if (err.status === 401) {
            showFlashMessage('Авторизуйтесь!', 'error');
        } else {
            showFlashMessage(err.message || 'Ошибка очистки', 'error');
        }
        if (button) {
            button.disabled = false;
            button.textContent = 'Очистить корзину';
        }
    });
}

function setupDurationStrictFilter(id, maxVal) {
    const input = document.getElementById(id);
    if (!input) return;

    input.addEventListener('beforeinput', function (e) {
        if (e.data && !/^\d+$/.test(e.data)) {
            e.preventDefault();
            return;
        }
        if (e.data) {
            const selStart = input.selectionStart;
            const selEnd = input.selectionEnd;
            const current = input.value;
            const nextStr = current.slice(0, selStart) + e.data + current.slice(selEnd);
            const nextNum = parseInt(nextStr, 10);
            if (nextNum > maxVal) {
                e.preventDefault();
            }
        }
    });

    input.addEventListener('keydown', function (e) {
        const allowed = ['Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Enter', 'Escape'];
        if (allowed.includes(e.key) || e.ctrlKey || e.metaKey) return;
        if (!/^\d$/.test(e.key)) {
            e.preventDefault();
        }
    });

    input.addEventListener('input', function () {
        let valStr = this.value.replace(/\D/g, '');
        if (valStr !== '') {
            let valNum = parseInt(valStr, 10);
            if (valNum > maxVal) valNum = maxVal;
            valStr = valNum.toString();
        }
        if (this.value !== valStr) {
            this.value = valStr;
        }
        calculateRangeMinutes();
    });

    input.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text');
        const digits = pasted.replace(/\D/g, '');
        if (!digits) return;

        const selStart = input.selectionStart;
        const selEnd = input.selectionEnd;
        const current = input.value;
        const nextStr = current.slice(0, selStart) + digits + current.slice(selEnd);
        let nextNum = parseInt(nextStr, 10);

        if (!isNaN(nextNum)) {
            if (nextNum > maxVal) nextNum = maxVal;
            input.value = nextNum.toString();
            calculateRangeMinutes();
        }
    });
}

function normalizeNumberInput(value, {
    min = null,
    max = null,
    decimals = 0,
    emptyValues = []
} = {}) {
    if (value === null || value === undefined) return '';
    let str = String(value).trim().replace(',', '.');
    if (str === '') return '';
    str = str.replace(/[^\d.]/g, '');
    if (!str) return '';
    const firstDot = str.indexOf('.');
    if (firstDot !== -1) {
        str =
            str.substring(0, firstDot + 1) +
            str.substring(firstDot + 1).replace(/\./g, '');
    }
    let num = parseFloat(str);
    if (isNaN(num)) return '';
    if (emptyValues.some(v => num === v)) {
        return '';
    }
    if (min !== null && num < min) {
        return {
            error: `Значение должно быть не меньше ${min}`
        };
    }
    if (max !== null && num > max) {
        return {
            error: `Значение должно быть не больше ${max}`
        };
    }
    if (decimals === 0) {
        return String(Math.round(num));
    }
    return num.toFixed(decimals);
}

function normalizeDurationFilters() {
    const fromH = parseInt(
        document.getElementById('durationFromHours')?.value,
        10
    ) || 0;

    const fromM = parseInt(
        document.getElementById('durationFromMinutes')?.value,
        10
    ) || 0;

    const fromS = parseInt(
        document.getElementById('durationFromSeconds')?.value,
        10
    ) || 0;

    const toH = parseInt(
        document.getElementById('durationToHours')?.value,
        10
    ) || 0;

    const toM = parseInt(
        document.getElementById('durationToMinutes')?.value,
        10
    ) || 0;

    const toS = parseInt(
        document.getElementById('durationToSeconds')?.value,
        10
    ) || 0;
    if (fromH === 0 && fromM === 0 && fromS === 0) {
        document.getElementById('durationFromHours').value = '';
        document.getElementById('durationFromMinutes').value = '';
        document.getElementById('durationFromSeconds').value = '';
    }
    if (toH === 16 && toM === 59 && toS === 59) {
        document.getElementById('durationToHours').value = '';
        document.getElementById('durationToMinutes').value = '';
        document.getElementById('durationToSeconds').value = '';
    }

    calculateRangeMinutes();
}

function validateFilter() {
    normalizeDurationFilters();

    const errorEl = document.getElementById('ratingFilterError');

    function showError(message, input) {
        if (errorEl) {
            errorEl.innerText = '⚠️ ' + message;
            errorEl.style.display = 'block';
        }
        if (input) {
            input.focus();
            input.style.borderColor = '#f87171';
            input.style.boxShadow = '0 0 0 3px rgba(248, 113, 113, 0.3)';
            setTimeout(function() {
                input.style.borderColor = '';
                input.style.boxShadow = '';
            }, 3000);
        }
        return false;
    }

    function clearError() {
        if (errorEl) {
            errorEl.style.display = 'none';
            errorEl.innerText = '';
        }
        document.querySelectorAll('.filter-input').forEach(function(el) {
            el.style.borderColor = '';
            el.style.boxShadow = '';
        });
    }

    clearError();

    let hasMeaningfulFilter = false;
    let hasRestrictiveFilter = false;
    let hasPriceFilter = false;
    let hasRatingFilter = false;
    let hasDurationFilter = false;
    let hasDateFilter = false;

    const priceFrom = document.getElementById('priceFrom');
    if (priceFrom && priceFrom.value.trim() !== '') {
        const val = parseFloat(priceFrom.value.replace(',', '.'));
        if (isNaN(val) || val < 0) {
            return showError('Цена "от" должна быть ≥ 0', priceFrom);
        }
        if (val >= 100000) {
            return showError('Цена "от" не может быть ≥ 100 000 ₽. Максимум 99 999.99', priceFrom);
        }
        if (val > 99999.99) {
            return showError('Цена "от" не может быть больше 99 999.99 ₽', priceFrom);
        }
        priceFrom.value = val.toFixed(2);
        hasMeaningfulFilter = true;
        hasPriceFilter = true;
        if (val > 0) hasRestrictiveFilter = true;
    }

    const priceTo = document.getElementById('priceTo');
    if (priceTo && priceTo.value.trim() !== '') {
        const val = parseFloat(priceTo.value.replace(',', '.'));
        if (isNaN(val) || val < 0) {
            return showError('Цена "до" должна быть ≥ 0', priceTo);
        }
        if (val >= 100000) {
            return showError('Цена "до" не может быть ≥ 100 000 ₽. Максимум 99 999.99', priceTo);
        }
        if (val > 99999.99) {
            return showError('Цена "до" не может быть больше 99 999.99 ₽', priceTo);
        }
        priceTo.value = val.toFixed(2);
        hasMeaningfulFilter = true;
        hasPriceFilter = true;
        if (val < 99999.99) hasRestrictiveFilter = true;
    }

    if (priceFrom && priceFrom.value && priceTo && priceTo.value) {
        const from = parseFloat(priceFrom.value);
        const to = parseFloat(priceTo.value);
        if (from > to) {
            return showError('Цена "от" не может быть больше цены "до"', priceFrom);
        }
        if (from === 0 && to === 99999.99) {
            priceFrom.value = '';
            priceTo.value = '';
            hasPriceFilter = false;
            hasMeaningfulFilter = false;
            return showError('Диапазон цен 0 - 99 999.99 эквивалентен "все фильмы". Укажите конкретные границы', null);
        }
    }

    const ratingFrom = document.getElementById('ratingFrom');
    if (ratingFrom && ratingFrom.value.trim() !== '') {
        const val = parseFloat(ratingFrom.value.replace(',', '.'));
        if (isNaN(val)) {
            return showError('Рейтинг "от" должен быть числом', ratingFrom);
        }
        if (val < 0.1) {
            return showError('Рейтинг "от" не может быть меньше 0.1', ratingFrom);
        }
        if (val > 10) {
            return showError('Рейтинг "от" не может быть больше 10', ratingFrom);
        }
        ratingFrom.value = val.toFixed(1);
        hasMeaningfulFilter = true;
        hasRatingFilter = true;
        if (val > 0.1) hasRestrictiveFilter = true;
    }

    const ratingTo = document.getElementById('ratingTo');
    if (ratingTo && ratingTo.value.trim() !== '') {
        const val = parseFloat(ratingTo.value.replace(',', '.'));
        if (isNaN(val)) {
            return showError('Рейтинг "до" должен быть числом', ratingTo);
        }
        if (val < 0.1) {
            return showError('Рейтинг "до" не может быть меньше 0.1', ratingTo);
        }
        if (val > 10) {
            return showError('Рейтинг "до" не может быть больше 10', ratingTo);
        }
        ratingTo.value = val.toFixed(1);
        hasMeaningfulFilter = true;
        hasRatingFilter = true;
        if (val < 10) hasRestrictiveFilter = true;
    }

    if (ratingFrom && ratingFrom.value && ratingTo && ratingTo.value) {
        const from = parseFloat(ratingFrom.value);
        const to = parseFloat(ratingTo.value);
        if (from > to) {
            return showError('Рейтинг "от" не может быть больше рейтинга "до"', ratingFrom);
        }
        if (from === 0.1 && to === 10) {
            ratingFrom.value = '';
            ratingTo.value = '';
            hasRatingFilter = false;
            hasMeaningfulFilter = false;
            return showError('Диапазон рейтинга 0.1 - 10 эквивалентен "все фильмы". Укажите конкретные границы', null);
        }
    }

    const fromH = parseInt(document.getElementById('durationFromHours')?.value || '0', 10);
    const fromM = parseInt(document.getElementById('durationFromMinutes')?.value || '0', 10);
    const fromS = parseInt(document.getElementById('durationFromSeconds')?.value || '0', 10);
    const toH = parseInt(document.getElementById('durationToHours')?.value || '0', 10);
    const toM = parseInt(document.getElementById('durationToMinutes')?.value || '0', 10);
    const toS = parseInt(document.getElementById('durationToSeconds')?.value || '0', 10);

    const fromTotal = fromH * 3600 + fromM * 60 + fromS;
    const toTotal = toH * 3600 + toM * 60 + toS;

    if (fromTotal > 0) {
        if (fromTotal < 39) {
            return showError('Длительность "от" не может быть меньше 39 секунд (минимальная длительность фильма)', document.getElementById('durationFromHours'));
        }
        if (fromH > 16 || (fromH === 16 && (fromM > 0 || fromS > 0))) {
            return showError('Длительность "от" не может быть больше 16:00:00', document.getElementById('durationFromHours'));
        }
        hasMeaningfulFilter = true;
        hasDurationFilter = true;
        if (fromTotal > 39) hasRestrictiveFilter = true;
    }

    if (toTotal > 0) {
        if (toTotal < 39) {
            return showError('Длительность "до" не может быть меньше 39 секунд (минимальная длительность фильма)', document.getElementById('durationToHours'));
        }
        if (toH > 16 || (toH === 16 && toM > 58) || (toH === 16 && toM === 58 && toS > 58)) {
            return showError('Длительность "до" не может быть больше 16:58:58', document.getElementById('durationToHours'));
        }
        hasMeaningfulFilter = true;
        hasDurationFilter = true;
        if (toTotal < 61198) hasRestrictiveFilter = true;
    }

    if (fromTotal > 0 && toTotal > 0 && fromTotal > toTotal) {
        return showError('Длительность "от" не может быть больше длительности "до"', document.getElementById('durationFromHours'));
    }

    if (fromTotal === 39 && toTotal === 61198) {
        document.getElementById('durationFromHours').value = '';
        document.getElementById('durationFromMinutes').value = '';
        document.getElementById('durationFromSeconds').value = '';
        document.getElementById('durationToHours').value = '';
        document.getElementById('durationToMinutes').value = '';
        document.getElementById('durationToSeconds').value = '';
        hasDurationFilter = false;
        hasMeaningfulFilter = false;
        return showError('Диапазон длительности 39 сек - 16:58:58 эквивалентен "все фильмы". Укажите конкретные границы', null);
    }

    const dateFrom = document.getElementById('releaseDateFrom');
    const dateTo = document.getElementById('releaseDateTo');
    if (dateFrom && dateFrom.value) {
        const d = new Date(dateFrom.value);
        if (d.getFullYear() < 1895) {
            return showError('Год не может быть раньше 1895 (первые фильмы)', dateFrom);
        }
        if (d > new Date()) {
            return showError('Дата "от" не может быть в будущем', dateFrom);
        }
        hasMeaningfulFilter = true;
        hasDateFilter = true;
        hasRestrictiveFilter = true;
    }
    if (dateTo && dateTo.value) {
        const d = new Date(dateTo.value);
        if (d.getFullYear() < 1895) {
            return showError('Год не может быть раньше 1895 (первые фильмы)', dateTo);
        }
        if (d > new Date()) {
            return showError('Дата "до" не может быть в будущем', dateTo);
        }
        hasMeaningfulFilter = true;
        hasDateFilter = true;
        hasRestrictiveFilter = true;
    }
    if (dateFrom && dateFrom.value && dateTo && dateTo.value) {
        const from = new Date(dateFrom.value);
        const to = new Date(dateTo.value);
        if (from > to) {
            return showError('Дата "от" не может быть позже даты "до"', dateFrom);
        }
        if (from.getFullYear() === 1895 && to.getFullYear() === new Date().getFullYear()) {
            dateFrom.value = '';
            dateTo.value = '';
            hasDateFilter = false;
            hasMeaningfulFilter = false;
            return showError('Диапазон дат 1895 - ' + new Date().getFullYear() + ' эквивалентен "все фильмы". Укажите конкретные границы', null);
        }
    }

    const genresCheckboxes = document.querySelectorAll('input[name="genres"]:checked');
    const directorsCheckboxes = document.querySelectorAll('input[name="directors"]:checked');
    if (genresCheckboxes.length > 0) {
        hasMeaningfulFilter = true;
        hasRestrictiveFilter = true;
    }
    if (directorsCheckboxes.length > 0) {
        hasMeaningfulFilter = true;
        hasRestrictiveFilter = true;
    }

    const titleInput = document.getElementById('headerSearchInput');
    if (titleInput && titleInput.value && titleInput.value.trim() !== '') {
        const title = titleInput.value.trim();
        if (title.length < 2) {
            return showError('Поисковый запрос должен содержать минимум 2 символа', titleInput);
        }
        hasMeaningfulFilter = true;
        hasRestrictiveFilter = true;
    }

    if (!hasMeaningfulFilter) {
        return showError('❌ Укажите хотя бы один параметр поиска (название, жанр, режиссёр, цену, рейтинг, дату или длительность)', null);
    }

    if (!hasRestrictiveFilter) {
        let wideFilters = [];

        if (hasPriceFilter) {
            const from = priceFrom?.value ? parseFloat(priceFrom.value) : 0;
            const to = priceTo?.value ? parseFloat(priceTo.value) : 99999.99;
            if (from === 0 && to === 99999.99) {
                wideFilters.push('цена 0 - 99 999.99');
            }
        }

        if (hasRatingFilter) {
            const from = ratingFrom?.value ? parseFloat(ratingFrom.value) : 0.1;
            const to = ratingTo?.value ? parseFloat(ratingTo.value) : 10;
            if (from === 0.1 && to === 10) {
                wideFilters.push('рейтинг 0.1 - 10');
            }
        }

        if (hasDurationFilter) {
            if (fromTotal === 39 && toTotal === 61198) {
                wideFilters.push('длительность 39 сек - 16:58:58');
            }
        }

        if (hasDateFilter) {
            const from = dateFrom?.value ? new Date(dateFrom.value).getFullYear() : 1895;
            const to = dateTo?.value ? new Date(dateTo.value).getFullYear() : new Date().getFullYear();
            if (from === 1895 && to === new Date().getFullYear()) {
                wideFilters.push('даты 1895 - ' + new Date().getFullYear());
            }
        }

        if (wideFilters.length > 0) {
            return showError('❌ Вы указали только широкие фильтры (' + wideFilters.join(', ') + '), которые эквивалентны "показать все фильмы". Добавьте конкретный фильтр: название, жанр, режиссёра, цену > 0, узкий диапазон рейтинга/длительности/дат.', null);
        }
    }

    return true;
}

document.addEventListener('DOMContentLoaded', function() {
    setupRatingInput();
    setupFilterRatingInput('ratingFrom');
    setupFilterRatingInput('ratingTo');
    setupFilterPriceInput('priceFrom');
    setupFilterPriceInput('priceTo');

    setupDurationStrictFilter('durationFromHours', 16);
    setupDurationStrictFilter('durationToHours', 16);
    setupDurationStrictFilter('durationFromMinutes', 59);
    setupDurationStrictFilter('durationToMinutes', 59);
    setupDurationStrictFilter('durationFromSeconds', 59);
    setupDurationStrictFilter('durationToSeconds', 59);

    clearAllStatuses();
    initEmailValidation();

    var flash = sessionStorage.getItem('flashMessage');
    if (flash) {
        try {
            var data = JSON.parse(flash);
            showFlashMessage(data.message, data.type);
            sessionStorage.removeItem('flashMessage');
        } catch (e) {}
    }

    calculateRangeMinutes();

    var filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('reset', function() {
            setTimeout(calculateRangeMinutes, 0);
        });
    }

    document.querySelectorAll('.js-reviews-count[data-movie-id]').forEach(function(el) {
        var id = el.getAttribute('data-movie-id');
        if (id) updateReviewsCount(id);
    });

    var reviewsContainer = document.querySelector('.reviews-container');
    if (reviewsContainer) {
        var movieId = reviewsContainer.getAttribute('data-movie-id');
        if (movieId) updateReviewsCount(movieId);
    }

    var searchInput = document.getElementById('headerSearchInput');
    var suggestionsBox = document.getElementById('headerSuggestions');
    if (searchInput && suggestionsBox) {
        var debounceTimer;

        function fetchSuggestions(query) {
            if (!query) {
                suggestionsBox.style.display = 'none';
                suggestionsBox.innerHTML = '';
                return;
            }
            fetch('/search-suggestions?query=' + encodeURIComponent(query))
                .then(function(resp) { return resp.json(); })
                .then(function(data) {
                    if (!data || data.length === 0) {
                        suggestionsBox.style.display = 'none';
                        suggestionsBox.innerHTML = '';
                        return;
                    }
                    var html = '';
                    data.forEach(function(item) {
                        var title = item.title || '';
                        var year = item.releaseDate ? new Date(item.releaseDate).getFullYear() : '';
                        html += '<div class="suggestion-item" data-id="' + (item.id || '') + '">';
                        html += '<span class="suggestion-title">' + title + '</span>';
                        if (year) html += '<span class="suggestion-meta">' + year + '</span>';
                        html += '</div>';
                    });
                    suggestionsBox.innerHTML = html;
                    suggestionsBox.style.display = 'flex';
                })
                .catch(function() {
                    suggestionsBox.style.display = 'none';
                });
        }

        searchInput.addEventListener('input', function() {
            var val = this.value.trim();
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(function() {
                fetchSuggestions(val);
            }, 300);
        });

        searchInput.addEventListener('blur', function() {
            setTimeout(function() {
                suggestionsBox.style.display = 'none';
            }, 200);
        });

        suggestionsBox.addEventListener('mousedown', function(e) {
            e.preventDefault();
            var item = e.target.closest('.suggestion-item');
            if (!item) return;
            var movieId = item.getAttribute('data-id');
            if (movieId && movieId !== 'null' && movieId !== 'undefined') {
                window.location.href = '/movies/' + movieId;
            } else {
                var titleEl = item.querySelector('.suggestion-title');
                if (titleEl) {
                    searchInput.value = titleEl.textContent;
                    searchForm.submit();
                }
            }
        });
    }

    function loadFilterOptions(url, dropdownId) {
        var dropdown = document.getElementById(dropdownId);
        if (!dropdown) return;
        fetch(url)
            .then(function(resp) { return resp.json(); })
            .then(function(items) {
                if (!items || items.length === 0) {
                    dropdown.innerHTML = '<div class="accordion-item" style="color:#94a3b8;">Нет данных</div>';
                    return;
                }
                var nameAttr = (dropdownId === 'genresDropdown') ? 'genres' : 'directors';
                var html = '';
                items.forEach(function(name) {
                    html += '<label class="accordion-item">';
                    html += '<input type="checkbox" class="accordion-checkbox" name="' + nameAttr + '" value="' + name.replace(/"/g, '&quot;') + '">';
                    html += ' ' + name;
                    html += '</label>';
                });
                dropdown.innerHTML = html;
                if (typeof initMultiSelects === 'function') {
                    initMultiSelects();
                }
            })
            .catch(function() {
                dropdown.innerHTML = '<div class="accordion-item" style="color:#f87171;">Ошибка загрузки</div>';
            });
    }

    var filterOverlay = document.getElementById('filtersModalOverlay');
    if (filterOverlay) {
        var observer = new MutationObserver(function() {
            if (filterOverlay.classList.contains('show')) {
                loadFilterOptions('/genres', 'genresDropdown');
                loadFilterOptions('/directors', 'directorsDropdown');
                observer.disconnect();
            }
        });
        observer.observe(filterOverlay, { attributes: true, attributeFilter: ['class'] });
    }

    document.addEventListener('click', function(e) {
        var trigger = e.target.closest('.accordion-trigger');
        if (!trigger) return;
        var group = trigger.closest('.accordion-group');
        if (!group) return;
        group.classList.toggle('active');
    });
});

window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        if (!isAuthenticated()) {
            return;
        }
        applyStatusesFromSession();
        refreshCounts();
        document.querySelectorAll('.js-reviews-count[data-movie-id]').forEach(function(el) {
            var id = el.getAttribute('data-movie-id');
            if (id) updateReviewsCount(id);
        });
        document.querySelectorAll('.js-rating-badge[data-movie-id]').forEach(function(el) {
            var id = el.getAttribute('data-movie-id');
            if (id) updateMovieRating(id);
        });
    }
});

document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        if (isAuthenticated()) {
            refreshCounts();
        }
    }
});

let _stepInterval = null;
let _stepTimeout = null;

function stepRating(delta) {
    const input = document.getElementById('ratingValue');
    if (!input) return;
    let current = parseFloat(input.value.replace(',', '.'));
    if (isNaN(current)) current = 0;
    let newValue = current + delta;
    newValue = Math.round(newValue * 10) / 10;
    if (newValue < 0.1) newValue = 0.1;
    if (newValue > 10.0) newValue = 10.0;
    input.value = newValue.toFixed(1);
    const event = new Event('input', { bubbles: true });
    input.dispatchEvent(event);
}

function startStepping(delta, button) {
    stopStepping();
    if (button) button.classList.add('holding');
    stepRating(delta);
    _stepTimeout = setTimeout(function() {
        _stepInterval = setInterval(function() {
            stepRating(delta);
        }, 80);
    }, 250);
}

function stopStepping(button) {
    if (button) button.classList.remove('holding');
    if (_stepTimeout) {
        clearTimeout(_stepTimeout);
        _stepTimeout = null;
    }
    if (_stepInterval) {
        clearInterval(_stepInterval);
        _stepInterval = null;
    }
}
let _amountStepInterval = null;
let _amountStepTimeout = null;

function stepAmount(delta) {
    const input = document.getElementById('amount');
    if (!input) return;
    let current = parseFloat(input.value.replace(',', '.'));
    if (isNaN(current)) current = 0;
    let newValue = current + delta;
    newValue = Math.round(newValue * 100) / 100;
    if (newValue < 0) newValue = 0;
    if (newValue > 1000000) newValue = 1000000;
    input.value = newValue.toFixed(2);
    const event = new Event('input', { bubbles: true });
    input.dispatchEvent(event);
    document.querySelectorAll('.quick-amount-btn').forEach(b => b.classList.remove('active'));
}

function startSteppingAmount(delta, button) {
    stopSteppingAmount(button);
    if (button) button.classList.add('holding');

    stepAmount(delta);

    _amountStepTimeout = setTimeout(function() {
        _amountStepInterval = setInterval(function() {
            stepAmount(delta);
        }, 80);
    }, 250);
}

function stopSteppingAmount(button) {
    if (button) button.classList.remove('holding');
    if (_amountStepTimeout) {
        clearTimeout(_amountStepTimeout);
        _amountStepTimeout = null;
    }
    if (_amountStepInterval) {
        clearInterval(_amountStepInterval);
        _amountStepInterval = null;
    }
}
let _filterStepInterval = null;
let _filterStepTimeout = null;

function stepFilterValue(inputId, delta, min, max, decimals) {
    const input = document.getElementById(inputId);
    if (!input) return;
    let current = parseFloat(input.value.replace(',', '.'));
    if (isNaN(current)) current = 0;
    let newValue = current + delta;
    const factor = Math.pow(10, decimals);
    newValue = Math.round(newValue * factor) / factor;
    if (min !== null && newValue < min) newValue = min;
    if (max !== null && newValue > max) newValue = max;
    input.value = newValue.toFixed(decimals);
    const event = new Event('input', { bubbles: true });
    input.dispatchEvent(event);
    document.querySelectorAll('.quick-amount-btn').forEach(b => b.classList.remove('active'));
}

function startSteppingFilter(inputId, delta, min, max, decimals, button) {
    stopSteppingFilter(button);
    if (button) button.classList.add('holding');
    stepFilterValue(inputId, delta, min, max, decimals);
    _filterStepTimeout = setTimeout(function() {
        _filterStepInterval = setInterval(function() {
            stepFilterValue(inputId, delta, min, max, decimals);
        }, 10);
    }, 250);
}

function stopSteppingFilter(button) {
    if (button) button.classList.remove('holding');
    if (_filterStepTimeout) {
        clearTimeout(_filterStepTimeout);
        _filterStepTimeout = null;
    }
    if (_filterStepInterval) {
        clearInterval(_filterStepInterval);
        _filterStepInterval = null;
    }
}