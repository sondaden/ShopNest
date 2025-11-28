/**
 * ShopNest - Main JavaScript
 */

(function() {
    'use strict';

    // ============================================
    // Configuration
    // ============================================
    const API_BASE = '/api';

    // ============================================
    // Utility Functions
    // ============================================
    
    /**
     * Format number as Vietnamese currency
     */
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
    }

    /**
     * Show toast notification
     */
    function showToast(message, type = 'success') {
        // Create toast container if not exists
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        // Create toast element
        const toastId = 'toast-' + Date.now();
        const iconClass = type === 'success' ? 'bi-check-circle-fill text-success' : 
                         type === 'error' ? 'bi-exclamation-circle-fill text-danger' :
                         'bi-info-circle-fill text-info';

        const toastHtml = `
            <div id="${toastId}" class="toast show fade-in" role="alert">
                <div class="toast-header">
                    <i class="bi ${iconClass} me-2"></i>
                    <strong class="me-auto">ShopNest</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
                </div>
                <div class="toast-body">${message}</div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', toastHtml);

        // Auto remove after 3 seconds
        setTimeout(() => {
            const toast = document.getElementById(toastId);
            if (toast) {
                toast.classList.remove('show');
                setTimeout(() => toast.remove(), 300);
            }
        }, 3000);
    }

    /**
     * Show loading spinner
     */
    function showLoading() {
        let spinner = document.querySelector('.spinner-overlay');
        if (!spinner) {
            spinner = document.createElement('div');
            spinner.className = 'spinner-overlay';
            spinner.innerHTML = `
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            `;
            document.body.appendChild(spinner);
        }
        setTimeout(() => spinner.classList.add('show'), 10);
    }

    /**
     * Hide loading spinner
     */
    function hideLoading() {
        const spinner = document.querySelector('.spinner-overlay');
        if (spinner) {
            spinner.classList.remove('show');
        }
    }

    /**
     * Make API request
     */
    async function apiRequest(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            },
        };

        // Add JWT token if exists
        const token = localStorage.getItem('jwt_token');
        if (token) {
            defaultOptions.headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, { ...defaultOptions, ...options });
            
            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: 'Đã có lỗi xảy ra' }));
                throw new Error(error.message || 'Request failed');
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // ============================================
    // Cart Functions
    // ============================================

    /**
     * Add product to cart
     */
    async function addToCart(productId, quantity = 1) {
        try {
            showLoading();
            
            // For now, use session-based cart (can be replaced with API call)
            const response = await fetch('/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `productId=${productId}&quantity=${quantity}`
            });

            if (response.ok) {
                showToast('Đã thêm vào giỏ hàng!', 'success');
                updateCartBadge();
            } else {
                throw new Error('Không thể thêm vào giỏ hàng');
            }
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            hideLoading();
        }
    }

    /**
     * Update cart item quantity
     */
    async function updateCartItem(itemId, quantity) {
        try {
            const response = await fetch('/cart/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `itemId=${itemId}&quantity=${quantity}`
            });

            if (response.ok) {
                location.reload();
            } else {
                throw new Error('Không thể cập nhật giỏ hàng');
            }
        } catch (error) {
            showToast(error.message, 'error');
        }
    }

    /**
     * Remove item from cart
     */
    async function removeFromCart(itemId) {
        if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
            return;
        }

        try {
            showLoading();
            const response = await fetch('/cart/remove', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `itemId=${itemId}`
            });

            if (response.ok) {
                showToast('Đã xóa sản phẩm khỏi giỏ hàng', 'success');
                location.reload();
            } else {
                throw new Error('Không thể xóa sản phẩm');
            }
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            hideLoading();
        }
    }

    /**
     * Update cart badge count
     */
    function updateCartBadge() {
        // This would typically fetch the cart count from server
        const badge = document.querySelector('.nav-link .badge');
        if (badge) {
            const currentCount = parseInt(badge.textContent) || 0;
            badge.textContent = currentCount + 1;
        }
    }

    // ============================================
    // Navbar Scroll Effect
    // ============================================
    
    function initNavbarScrollEffect() {
        const navbar = document.getElementById('mainNavbar');
        if (!navbar) return;

        let lastScrollY = window.scrollY;
        let ticking = false;

        function updateNavbar() {
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
            ticking = false;
        }

        window.addEventListener('scroll', function() {
            lastScrollY = window.scrollY;
            if (!ticking) {
                window.requestAnimationFrame(function() {
                    updateNavbar();
                    ticking = false;
                });
                ticking = true;
            }
        });

        // Initial check
        updateNavbar();
    }

    // ============================================
    // Event Listeners
    // ============================================

    document.addEventListener('DOMContentLoaded', function() {
        // Initialize navbar scroll effect
        initNavbarScrollEffect();

        // Add to cart buttons
        document.querySelectorAll('.btn-add-cart').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = this.dataset.productId;
                addToCart(productId);
            });
        });

        // Cart quantity increase
        document.querySelectorAll('.btn-increase').forEach(btn => {
            btn.addEventListener('click', function() {
                const itemId = this.dataset.itemId;
                const input = document.querySelector(`.quantity-input[data-item-id="${itemId}"]`);
                const newValue = parseInt(input.value) + 1;
                if (newValue <= 99) {
                    input.value = newValue;
                    updateCartItem(itemId, newValue);
                }
            });
        });

        // Cart quantity decrease
        document.querySelectorAll('.btn-decrease').forEach(btn => {
            btn.addEventListener('click', function() {
                const itemId = this.dataset.itemId;
                const input = document.querySelector(`.quantity-input[data-item-id="${itemId}"]`);
                const newValue = parseInt(input.value) - 1;
                if (newValue >= 1) {
                    input.value = newValue;
                    updateCartItem(itemId, newValue);
                }
            });
        });

        // Cart quantity input change
        document.querySelectorAll('.quantity-input').forEach(input => {
            input.addEventListener('change', function() {
                const itemId = this.dataset.itemId;
                let value = parseInt(this.value);
                
                if (isNaN(value) || value < 1) value = 1;
                if (value > 99) value = 99;
                
                this.value = value;
                updateCartItem(itemId, value);
            });
        });

        // Remove from cart buttons
        document.querySelectorAll('.btn-remove').forEach(btn => {
            btn.addEventListener('click', function() {
                const itemId = this.dataset.itemId;
                removeFromCart(itemId);
            });
        });

        // Apply coupon
        const applyCouponBtn = document.getElementById('applyCoupon');
        if (applyCouponBtn) {
            applyCouponBtn.addEventListener('click', function() {
                const couponCode = document.getElementById('couponCode').value.trim();
                if (!couponCode) {
                    showToast('Vui lòng nhập mã giảm giá', 'error');
                    return;
                }
                // TODO: Implement coupon logic
                showToast('Tính năng đang được phát triển', 'info');
            });
        }

        // Dismiss alerts automatically
        document.querySelectorAll('.alert-dismissible').forEach(alert => {
            setTimeout(() => {
                const closeBtn = alert.querySelector('.btn-close');
                if (closeBtn) closeBtn.click();
            }, 5000);
        });

        console.log('ShopNest initialized successfully!');
    });

    // ============================================
    // Expose functions globally if needed
    // ============================================
    window.ShopNest = {
        addToCart,
        removeFromCart,
        updateCartItem,
        showToast,
        formatCurrency
    };

    // Also expose showToast globally for product-detail page
    window.showToast = showToast;
    window.addToCart = addToCart;
    window.updateCartBadge = updateCartBadge;

})();
