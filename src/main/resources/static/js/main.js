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
        console.log('showToast called:', message, type);
        
        // Create toast container if not exists
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            container.style.cssText = 'position:fixed;top:80px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:10px;';
            document.body.appendChild(container);
        }

        // Define icons and colors for each type
        const icons = {
            success: '<i class="bi bi-check-circle-fill" style="font-size:1.25rem;margin-right:12px;"></i>',
            error: '<i class="bi bi-x-circle-fill" style="font-size:1.25rem;margin-right:12px;"></i>',
            info: '<i class="bi bi-info-circle-fill" style="font-size:1.25rem;margin-right:12px;"></i>',
            warning: '<i class="bi bi-exclamation-triangle-fill" style="font-size:1.25rem;margin-right:12px;"></i>'
        };
        
        const colors = {
            success: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            error: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
            info: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
            warning: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
        };

        // Create toast element with inline styles
        const toast = document.createElement('div');
        toast.style.cssText = `
            display:flex;align-items:center;padding:14px 20px;border-radius:12px;
            background:${colors[type] || colors.info};color:white;font-weight:500;
            box-shadow:0 8px 30px rgba(0,0,0,0.12);min-width:280px;max-width:400px;
            animation:slideInRight 0.4s ease-out;
        `;
        toast.innerHTML = `
            ${icons[type] || icons.info}
            <span style="flex:1;">${message}</span>
            <button onclick="this.parentElement.remove()" style="background:none;border:none;color:white;opacity:0.7;cursor:pointer;font-size:1.25rem;padding:0;margin-left:12px;">
                <i class="bi bi-x"></i>
            </button>
        `;
        
        // Add animation keyframes if not exists
        if (!document.getElementById('toast-animation-style')) {
            const style = document.createElement('style');
            style.id = 'toast-animation-style';
            style.textContent = '@keyframes slideInRight{from{transform:translateX(100%);opacity:0;}to{transform:translateX(0);opacity:1;}}';
            document.head.appendChild(style);
        }

        container.appendChild(toast);

        // Auto remove after 3 seconds
        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, 3000);
    }

    /**
     * Show custom confirm dialog
     */
    function showConfirm(message, options = {}) {
        return new Promise((resolve) => {
            const {
                title = 'Xác nhận',
                confirmText = 'Xác nhận',
                cancelText = 'Hủy',
                type = 'warning' // warning, danger, info
            } = options;
            
            const colors = {
                warning: { bg: '#f59e0b', hover: '#d97706' },
                danger: { bg: '#ef4444', hover: '#dc2626' },
                info: { bg: '#3b82f6', hover: '#2563eb' }
            };
            
            const color = colors[type] || colors.warning;
            
            // Create overlay
            const overlay = document.createElement('div');
            overlay.style.cssText = `
                position:fixed;top:0;left:0;right:0;bottom:0;
                background:rgba(0,0,0,0.5);z-index:10000;
                display:flex;align-items:center;justify-content:center;
                animation:fadeIn 0.2s ease-out;
            `;
            
            // Create dialog
            overlay.innerHTML = `
                <div style="
                    background:white;border-radius:16px;padding:24px;
                    max-width:400px;width:90%;box-shadow:0 20px 60px rgba(0,0,0,0.3);
                    animation:scaleIn 0.2s ease-out;
                ">
                    <div style="text-align:center;margin-bottom:20px;">
                        <div style="
                            width:60px;height:60px;border-radius:50%;
                            background:${color.bg}15;margin:0 auto 16px;
                            display:flex;align-items:center;justify-content:center;
                        ">
                            <i class="bi bi-exclamation-triangle-fill" style="font-size:28px;color:${color.bg};"></i>
                        </div>
                        <h5 style="margin:0 0 8px;font-weight:600;color:#1f2937;">${title}</h5>
                        <p style="margin:0;color:#6b7280;font-size:0.95rem;">${message}</p>
                    </div>
                    <div style="display:flex;gap:12px;">
                        <button id="confirmCancel" style="
                            flex:1;padding:12px 20px;border-radius:10px;
                            border:1px solid #e5e7eb;background:white;
                            color:#374151;font-weight:500;cursor:pointer;
                            transition:all 0.2s;
                        ">${cancelText}</button>
                        <button id="confirmOk" style="
                            flex:1;padding:12px 20px;border-radius:10px;
                            border:none;background:${color.bg};
                            color:white;font-weight:500;cursor:pointer;
                            transition:all 0.2s;
                        ">${confirmText}</button>
                    </div>
                </div>
            `;
            
            // Add animation keyframes
            if (!document.getElementById('confirm-animation-style')) {
                const style = document.createElement('style');
                style.id = 'confirm-animation-style';
                style.textContent = `
                    @keyframes fadeIn{from{opacity:0;}to{opacity:1;}}
                    @keyframes scaleIn{from{transform:scale(0.9);opacity:0;}to{transform:scale(1);opacity:1;}}
                `;
                document.head.appendChild(style);
            }
            
            document.body.appendChild(overlay);
            
            // Handle buttons
            const okBtn = overlay.querySelector('#confirmOk');
            const cancelBtn = overlay.querySelector('#confirmCancel');
            
            okBtn.onmouseover = () => okBtn.style.background = color.hover;
            okBtn.onmouseout = () => okBtn.style.background = color.bg;
            cancelBtn.onmouseover = () => cancelBtn.style.background = '#f3f4f6';
            cancelBtn.onmouseout = () => cancelBtn.style.background = 'white';
            
            okBtn.onclick = () => {
                overlay.remove();
                resolve(true);
            };
            
            cancelBtn.onclick = () => {
                overlay.remove();
                resolve(false);
            };
            
            // Close on overlay click
            overlay.onclick = (e) => {
                if (e.target === overlay) {
                    overlay.remove();
                    resolve(false);
                }
            };
            
            // Focus cancel button
            cancelBtn.focus();
        });
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
    // Wishlist Functions
    // ============================================

    let wishlistProductIds = [];

    /**
     * Load wishlist product IDs
     */
    async function loadWishlistIds() {
        try {
            const response = await fetch('/api/wishlist/products');
            if (response.ok) {
                wishlistProductIds = await response.json();
                updateWishlistButtons();
                updateWishlistBadge();
            }
        } catch (error) {
            console.log('Error loading wishlist:', error);
        }
    }

    /**
     * Toggle wishlist for a product
     */
    async function toggleWishlist(productId, button) {
        try {
            const response = await fetch(`/api/wishlist/${productId}/toggle`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.status === 401) {
                showToast('Vui lòng đăng nhập để sử dụng danh sách yêu thích', 'warning');
                setTimeout(() => window.location.href = '/login', 1500);
                return;
            }

            const result = await response.json();

            if (result.success) {
                if (result.added) {
                    wishlistProductIds.push(parseInt(productId));
                    button.classList.add('active');
                    button.querySelector('i')?.classList.replace('bi-heart', 'bi-heart-fill');
                } else {
                    wishlistProductIds = wishlistProductIds.filter(id => id !== parseInt(productId));
                    button.classList.remove('active');
                    button.querySelector('i')?.classList.replace('bi-heart-fill', 'bi-heart');
                }
                updateWishlistBadge();
                showToast(result.message, 'success');
            } else {
                showToast(result.error || 'Có lỗi xảy ra', 'error');
            }
        } catch (error) {
            console.error('Toggle wishlist error:', error);
            showToast('Không thể cập nhật danh sách yêu thích', 'error');
        }
    }

    /**
     * Update wishlist buttons state based on loaded IDs
     */
    function updateWishlistButtons() {
        document.querySelectorAll('.btn-wishlist').forEach(btn => {
            const productId = parseInt(btn.dataset.productId);
            if (wishlistProductIds.includes(productId)) {
                btn.classList.add('active');
                const icon = btn.querySelector('i');
                if (icon) {
                    icon.classList.remove('bi-heart');
                    icon.classList.add('bi-heart-fill');
                }
            }
        });
    }

    /**
     * Update wishlist badge count
     */
    function updateWishlistBadge() {
        const badge = document.querySelector('.wishlist-badge');
        if (badge) {
            badge.textContent = wishlistProductIds.length;
            badge.style.display = wishlistProductIds.length > 0 ? 'inline-block' : 'none';
        }
    }

    // ============================================
    // Cart Functions
    // ============================================

    /**
     * Add product to cart
     */
    async function addToCart(productId, quantity = 1) {
        console.log('addToCart called with productId:', productId, 'quantity:', quantity);
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

            console.log('Response status:', response.status);
            const data = await response.json();
            console.log('Response data:', data);
            
            if (response.ok && data.success) {
                console.log('Calling showToast success');
                showToast('Đã thêm vào giỏ hàng!', 'success');
                updateCartBadge(data.cartCount);
            } else if (response.status === 401) {
                // Chưa đăng nhập
                showToast('Vui lòng đăng nhập để thêm vào giỏ hàng', 'error');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            } else {
                throw new Error(data.error || 'Không thể thêm vào giỏ hàng');
            }
        } catch (error) {
            console.error('Add to cart error:', error);
            showToast(error.message || 'Có lỗi xảy ra', 'error');
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
        const confirmed = await showConfirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?', {
            title: 'Xóa sản phẩm',
            confirmText: 'Xóa',
            cancelText: 'Hủy',
            type: 'danger'
        });
        
        if (!confirmed) return;

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
    function updateCartBadge(count) {
        const badge = document.querySelector('.cart-badge');
        if (badge && count !== undefined) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
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

    function initEventListeners() {
        console.log('initEventListeners called');
        
        // Initialize navbar scroll effect
        initNavbarScrollEffect();
        
        // Load wishlist IDs if logged in
        loadWishlistIds();

        // Wishlist toggle buttons
        document.querySelectorAll('.btn-wishlist').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                const productId = this.dataset.productId;
                if (productId) {
                    toggleWishlist(productId, this);
                }
            });
        });

        // Add to cart buttons
        const addToCartButtons = document.querySelectorAll('.btn-add-cart');
        console.log('Found add-to-cart buttons:', addToCartButtons.length);
        
        addToCartButtons.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                const productId = this.dataset.productId;
                console.log('Add to cart clicked, productId:', productId);
                if (productId) {
                    addToCart(productId);
                }
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
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initEventListeners);
    } else {
        // DOM already loaded
        initEventListeners();
    }

    // ============================================
    // Expose functions globally if needed
    // ============================================
    window.ShopNest = {
        addToCart,
        removeFromCart,
        updateCartItem,
        showToast,
        showConfirm,
        formatCurrency,
        toggleWishlist,
        loadWishlistIds
    };

    // Also expose functions globally
    window.showToast = showToast;
    window.showConfirm = showConfirm;
    window.addToCart = addToCart;
    window.updateCartBadge = updateCartBadge;
    window.toggleWishlist = toggleWishlist;

})();
