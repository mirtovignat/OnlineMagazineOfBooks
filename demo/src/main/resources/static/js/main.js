let _deleteMovieId = null;
let _ratingModalMode = 'add';

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

function getMovieId(element) {
    return element?.getAttribute('data-movie-id') || '';
}

function refreshCounts() {
    updateCartCount();
    updateFavouritesCount();
}

function requireAuth(event, url) {
    var userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
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
                    cartBtn.textContent = 'В корзине →';
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
    const movieId = getMovieId(button);
    if (!movieId) return;
    if (button.tagName.toLowerCase() === 'button') button.disabled = true;
    fetch('/cart/add/' + encodeURIComponent(movieId), {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(handleResponse)
    .then(data => {
        showFlashMessage(data.message, 'success');
        updateCartCount();
        setCartStatus(movieId, true);
        button.textContent = 'В корзине →';
        button.className = 'cart-button in-cart-btn-details';
        button.onclick = function() {
            window.location.href = '/cart';
        };
        button.disabled = false;
    })
    .catch(err => {
        showFlashMessage(err.message || 'Ошибка', 'error');
        if (button.tagName.toLowerCase() === 'button') button.disabled = false;
    });
}

function removeFromCart(button) {
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
        showFlashMessage(err.message || 'Ошибка', 'error');
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
        showFlashMessage(error.message || 'Ошибка при покупке', 'error');
        if (btn) { btn.disabled = false; btn.textContent = 'Подтвердить'; }
    });
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
                reviewsCounter.textContent = `(${num} отзывов)`;
            }
        })
        .catch(console.warn);
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
    if (!movieId) {
        showFlashMessage('Ошибка: не удалось определить фильм', 'error');
        return;
    }

    let ratingInput = document.getElementById('ratingValue');
    let rating = ratingInput.value.replace(',', '.');
    const review = document.getElementById('ratingComment').value;

    const ratingRegex = /^(10(\.0)?|[0-9](\.[0-9])?)$/;
    if (!ratingRegex.test(rating)) {
        if (errorDiv) {
            errorDiv.innerText = 'Введите число от 0.0 до 10.0 (одна цифра после точки)';
            errorDiv.style.display = 'block';
        }
        return;
    }

    const ratingValue = parseFloat(rating);
    if (ratingValue < 0 || ratingValue > 10) {
        if (errorDiv) {
            errorDiv.innerText = 'Оценка должна быть от 0.0 до 10.0';
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
        updateReviewsCount(movieId);

        if (data.data && data.data.rating) {
            const newRating = data.data.rating;
            document.querySelectorAll(`.js-rating-badge[data-movie-id="${movieId}"]`).forEach(b => {
                b.textContent = newRating !== '-' ? `★ ${newRating} / 10` : '-';
            });
        } else {
            fetch('/rated/rating/' + encodeURIComponent(movieId) + '?_=' + Date.now())
                .then(r => r.json())
                .then(ratingData => {
                    const newRating = ratingData.rating;
                    document.querySelectorAll(`.js-rating-badge[data-movie-id="${movieId}"]`).forEach(b => {
                        b.textContent = newRating !== null && newRating !== '-' ? `★ ${parseFloat(newRating).toFixed(1)} / 10` : '-';
                    });
                })
                .catch(console.warn);
        }

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
                <div class="review-card" data-movie-id="${movieId}" data-own="true" style="animation-delay: 0s;">
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
        showFlashMessage(err.message || 'Ошибка сохранения', 'error');
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
        updateReviewsCount(movieId);
        if (data.data && data.data.rating) {
            const newRating = data.data.rating;
            document.querySelectorAll(`.js-rating-badge[data-movie-id="${movieId}"]`).forEach(b => {
                b.textContent = newRating !== '-' ? `★ ${newRating} / 10` : '-';
            });
        } else {
            fetch('/rated/rating/' + encodeURIComponent(movieId) + '?_=' + Date.now())
                .then(r => r.json())
                .then(ratingData => {
                    const newRating = ratingData.rating;
                    document.querySelectorAll(`.js-rating-badge[data-movie-id="${movieId}"]`).forEach(b => {
                        b.textContent = newRating !== null && newRating !== '-' ? `★ ${parseFloat(newRating).toFixed(1)} / 10` : '-';
                    });
                })
                .catch(console.warn);
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
    })
    .catch(err => {
        hideConfirmDeleteModal();
        showFlashMessage(err.message || 'Ошибка удаления', 'error');
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
            let val = Math.min(Math.max(num, 0), 10);
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
            if (num < 0) this.value = '0';
            if (Number.isInteger(num) && num >= 0 && num <= 10) {
                this.value = num.toString();
            }
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    setupRatingInput();
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
    var positiveInputs = document.querySelectorAll('.positive-int');
    positiveInputs.forEach(function(input) {
        input.addEventListener('keydown', function(e) {
            if (e.ctrlKey || e.metaKey) return;
            var allowedKeys = ['Backspace', 'Tab', 'ArrowLeft', 'ArrowRight', 'Delete', 'Enter', 'Escape'];
            if (allowedKeys.includes(e.key)) return;
            if (!/^[0-9]$/.test(e.key)) {
                e.preventDefault();
            }
        });
        input.addEventListener('input', function(e) {
            e.target.value = e.target.value.replace(/\D/g, '');
            calculateRangeMinutes();
        });
    });
    calculateRangeMinutes();
    var filterForm = document.getElementById('filterForm') || document.querySelector('form');
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
});

window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        applyStatusesFromSession();
        refreshCounts();

        document.querySelectorAll('.js-reviews-count[data-movie-id]').forEach(function(el) {
            var id = el.getAttribute('data-movie-id');
            if (id) updateReviewsCount(id);
        });
        document.querySelectorAll('.js-rating-badge[data-movie-id]').forEach(function(el) {
            var id = el.getAttribute('data-movie-id');
            if (id) {
                fetch('/rated/rating/' + encodeURIComponent(id) + '?_=' + Date.now())
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        var rating = data.rating;
                        el.textContent = rating !== null && rating !== '-' ? '★ ' + parseFloat(rating).toFixed(1) + ' / 10' : '-';
                    })
                    .catch(console.warn);
            }
        });
    }
});

document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') refreshCounts();
});

function openClearCartModal() {
    document.getElementById('clearCartModal')?.classList.add('show');
}

function closeClearCartModal() {
    document.getElementById('clearCartModal')?.classList.remove('show');
}

function confirmClearCart() {
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
        showFlashMessage(err.message || 'Ошибка очистки', 'error');
        if (button) {
            button.disabled = false;
            button.textContent = 'Очистить корзину';
        }
    });
}