// ===================== 工具函数 =====================

// 商品名称 → 图片提示词（用于生成商品图）
const PRODUCT_IMAGES = {
    '智能手机': 'smartphone',
    '蓝牙耳机': 'bluetooth earbuds',
    '笔记本电脑': 'laptop computer',
    '智能手表': 'smart watch',
    '机械键盘': 'mechanical keyboard',
    '无线鼠标': 'wireless mouse',
    '平板电脑': 'tablet computer',
    '智能音箱': 'smart speaker',
    '显示器': 'computer monitor',
    '充电宝': 'power bank',
    '高清摄像头': 'webcam',
    '无线路由器': 'wireless router'
};

function productImage(name) {
    const keyword = PRODUCT_IMAGES[name] || name;
    const prompt = encodeURIComponent(keyword + ' product photo white background');
    return 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' + prompt + '&image_size=square';
}

function formatPrice(p) {
    const n = Number(p);
    return Number.isInteger(n) ? n.toFixed(0) : n.toFixed(2);
}

function showToast(msg) {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.classList.add('show');
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => toast.classList.remove('show'), 2000);
}

// 当前登录用户（localStorage 缓存）
function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem('currentUser') || 'null');
    } catch (e) {
        return null;
    }
}

function setCurrentUser(user) {
    localStorage.setItem('currentUser', JSON.stringify(user));
}

// ===================== 商品加载与渲染 =====================

let allProducts = [];
let currentCategory = 'all';
let searchKeyword = '';

async function loadProducts() {
    try {
        const res = await fetch('/product/list');
        const data = await res.json();
        if (data.code === 0) {
            allProducts = data.dataMap.data || [];
            renderProducts();
        } else {
            showToast(data.msg || '商品加载失败');
        }
    } catch (e) {
        console.error('商品加载失败', e);
        showToast('商品加载失败，请稍后再试');
    }
}

function renderProducts() {
    const grid = document.getElementById('product-grid');
    const empty = document.getElementById('empty-tip');
    let list = currentCategory === 'all'
        ? allProducts
        : allProducts.filter(p => p.category === currentCategory);

    if (searchKeyword) {
        list = list.filter(p => p.name.indexOf(searchKeyword) !== -1);
    }

    grid.innerHTML = '';
    empty.style.display = list.length ? 'none' : 'block';

    list.forEach(p => {
        const card = document.createElement('div');
        card.className = 'product-card';
        card.innerHTML =
            '<div class="product-image">' +
                '<span class="product-sales">已售 ' + p.sales + '</span>' +
                '<img src="' + productImage(p.name) + '" alt="' + p.name + '" loading="lazy">' +
            '</div>' +
            '<div class="product-info">' +
                '<div class="product-name">' + p.name + '</div>' +
                '<div class="product-category">' + p.category + ' · 库存 ' + p.stock + '</div>' +
                '<div class="product-bottom">' +
                    '<span class="product-price">' + formatPrice(p.price) + '</span>' +
                    '<button class="add-to-cart" data-product-no="' + p.productNo + '">加入购物车</button>' +
                '</div>' +
            '</div>';
        grid.appendChild(card);
    });

    // 绑定加入购物车事件
    grid.querySelectorAll('.add-to-cart').forEach(btn => {
        btn.addEventListener('click', () => addToCart(btn.dataset.productNo));
    });
}

// ===================== 分类筛选 =====================

function initFilter() {
    document.getElementById('filter-bar').addEventListener('click', (e) => {
        const chip = e.target.closest('.filter-chip');
        if (!chip) return;
        document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        currentCategory = chip.dataset.category;
        renderProducts();
    });
}

// ===================== 搜索 =====================

function initSearch() {
    const input = document.getElementById('search-input');
    input.addEventListener('input', () => {
        searchKeyword = input.value.trim();
        renderProducts();
    });
}

// ===================== 热销榜 =====================

async function loadHotProducts() {
    try {
        const res = await fetch('/product/hot?limit=6');
        const data = await res.json();
        if (data.code === 0) {
            renderHotProducts(data.dataMap.data || []);
        }
    } catch (e) {
        console.error('热销商品加载失败', e);
    }
}

function renderHotProducts(items) {
    const grid = document.getElementById('hot-grid');
    grid.innerHTML = '';
    items.forEach((p, i) => {
        const rank = i + 1;
        const card = document.createElement('div');
        card.className = 'hot-card';
        card.innerHTML =
            '<div class="hot-rank' + (rank <= 3 ? ' rank-top' : '') + '">' + rank + '</div>' +
            '<img src="' + productImage(p.name) + '" alt="' + p.name + '" loading="lazy">' +
            '<div class="hot-info">' +
                '<div class="hot-name">' + p.name + '</div>' +
                '<div class="hot-price">¥' + formatPrice(p.price) + '</div>' +
                '<div class="hot-sales">已售 ' + p.sales + '</div>' +
            '</div>';
        grid.appendChild(card);
    });
}

// ===================== 登录 / 注册 =====================

function updateUserUI() {
    const user = getCurrentUser();
    const guest = document.getElementById('user-actions');
    const logged = document.getElementById('user-logged');
    if (user) {
        guest.style.display = 'none';
        logged.style.display = 'flex';
        document.getElementById('user-name').textContent = user.nickname || user.username;
    } else {
        guest.style.display = 'flex';
        logged.style.display = 'none';
    }
    refreshCart();
}

function openModal(id) {
    document.getElementById(id).style.display = 'flex';
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

function initAuth() {
    document.getElementById('login-btn').addEventListener('click', () => openModal('login-modal'));
    document.getElementById('register-btn').addEventListener('click', () => openModal('register-modal'));

    document.querySelectorAll('[data-close]').forEach(btn => {
        btn.addEventListener('click', () => closeModal(btn.dataset.close));
    });

    // 点击遮罩关闭
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) overlay.style.display = 'none';
        });
    });

    document.getElementById('go-register').addEventListener('click', (e) => {
        e.preventDefault();
        closeModal('login-modal');
        openModal('register-modal');
    });
    document.getElementById('go-login').addEventListener('click', (e) => {
        e.preventDefault();
        closeModal('register-modal');
        openModal('login-modal');
    });

    // 登录
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('login-username').value.trim();
        const password = document.getElementById('login-password').value.trim();
        if (!username || !password) {
            showToast('请输入用户名和密码');
            return;
        }
        try {
            const res = await fetch('/user/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const data = await res.json();
            if (data.code === 0) {
                setCurrentUser({
                    id: data.dataMap.id,
                    username: data.dataMap.username,
                    nickname: data.dataMap.nickname
                });
                closeModal('login-modal');
                document.getElementById('login-password').value = '';
                updateUserUI();
                showToast('登录成功');
            } else {
                showToast(data.msg || '登录失败');
            }
        } catch (err) {
            showToast('登录请求失败');
        }
    });

    // 注册
    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('register-username').value.trim();
        const nickname = document.getElementById('register-nickname').value.trim();
        const password = document.getElementById('register-password').value.trim();
        if (!username || !password) {
            showToast('请输入用户名和密码');
            return;
        }
        try {
            const res = await fetch('/user/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password, nickname })
            });
            const data = await res.json();
            if (data.code === 0) {
                showToast('注册成功，请登录');
                closeModal('register-modal');
                document.getElementById('register-username').value = '';
                document.getElementById('register-nickname').value = '';
                document.getElementById('register-password').value = '';
                openModal('login-modal');
                document.getElementById('login-username').value = username;
            } else {
                showToast(data.msg || '注册失败');
            }
        } catch (err) {
            showToast('注册请求失败');
        }
    });

    // 退出
    document.getElementById('logout-btn').addEventListener('click', () => {
        localStorage.removeItem('currentUser');
        updateUserUI();
        showToast('已退出登录');
    });
}

// ===================== 购物车 =====================

async function addToCart(productNo) {
    const user = getCurrentUser();
    if (!user) {
        showToast('请先登录');
        openModal('login-modal');
        return;
    }
    try {
        const res = await fetch('/cart/add?userId=' + user.id + '&productNo=' + productNo + '&quantity=1', {
            method: 'POST'
        });
        const data = await res.json();
        if (data.code === 0) {
            showToast('已加入购物车');
            refreshCart();
        } else {
            showToast(data.msg || '加入失败');
        }
    } catch (e) {
        showToast('加入购物车失败');
    }
}

async function refreshCart() {
    const user = getCurrentUser();
    const badge = document.getElementById('cart-badge');
    if (!user) {
        badge.style.display = 'none';
        return;
    }
    try {
        const res = await fetch('/cart/list?userId=' + user.id);
        const data = await res.json();
        if (data.code === 0) {
            const items = data.dataMap.data || [];
            const count = items.reduce((s, it) => s + it.quantity, 0);
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
            renderCart(items);
        }
    } catch (e) {
        console.error('购物车加载失败', e);
    }
}

function renderCart(items) {
    const body = document.getElementById('cart-body');
    const foot = document.getElementById('cart-foot');
    body.innerHTML = '';

    if (!items || items.length === 0) {
        body.innerHTML = '<div class="cart-empty">购物车还是空的</div>';
        foot.style.display = 'none';
        return;
    }

    let total = 0;
    items.forEach(it => {
        total += it.price * it.quantity;
        const row = document.createElement('div');
        row.className = 'cart-item';
        row.innerHTML =
            '<img src="' + productImage(it.productName) + '" alt="' + it.productName + '">' +
            '<div class="cart-item-info">' +
                '<div class="cart-item-name">' + it.productName + '</div>' +
                '<div class="cart-item-price">¥' + formatPrice(it.price) + '</div>' +
                '<div class="cart-item-ops">' +
                    '<button class="qty-btn" data-op="minus" data-no="' + it.productNo + '">-</button>' +
                    '<span class="qty-num">' + it.quantity + '</span>' +
                    '<button class="qty-btn" data-op="plus" data-no="' + it.productNo + '">+</button>' +
                    '<button class="remove-btn" data-no="' + it.productNo + '">删除</button>' +
                '</div>' +
            '</div>';
        body.appendChild(row);
    });

    foot.style.display = 'block';
    document.getElementById('cart-total').textContent = '¥' + formatPrice(total);

    // 绑定加减与删除
    body.querySelectorAll('.qty-btn').forEach(btn => {
        btn.addEventListener('click', () => changeQty(btn.dataset.no, btn.dataset.op));
    });
    body.querySelectorAll('.remove-btn').forEach(btn => {
        btn.addEventListener('click', () => removeItem(btn.dataset.no));
    });
}

async function changeQty(productNo, op) {
    const user = getCurrentUser();
    if (!user) return;
    const item = (await fetchCart()).find(it => it.productNo === productNo);
    if (!item) return;
    let qty = item.quantity + (op === 'plus' ? 1 : -1);
    if (qty < 1) qty = 1;
    await fetch('/cart/update?userId=' + user.id + '&productNo=' + productNo + '&quantity=' + qty, { method: 'POST' });
    refreshCart();
}

async function removeItem(productNo) {
    const user = getCurrentUser();
    if (!user) return;
    await fetch('/cart/remove?userId=' + user.id + '&productNo=' + productNo, { method: 'POST' });
    refreshCart();
}

async function fetchCart() {
    const user = getCurrentUser();
    const res = await fetch('/cart/list?userId=' + user.id);
    const data = await res.json();
    return data.code === 0 ? (data.dataMap.data || []) : [];
}

function initCart() {
    const drawer = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-overlay');
    document.getElementById('cart-icon').addEventListener('click', () => {
        const user = getCurrentUser();
        if (!user) {
            showToast('请先登录');
            openModal('login-modal');
            return;
        }
        refreshCart();
        drawer.classList.add('open');
        overlay.style.display = 'block';
    });
    document.getElementById('cart-close').addEventListener('click', () => {
        drawer.classList.remove('open');
        overlay.style.display = 'none';
    });
    overlay.addEventListener('click', () => {
        drawer.classList.remove('open');
        overlay.style.display = 'none';
    });
    document.getElementById('cart-checkout').addEventListener('click', () => {
        showToast('结算功能演示中，暂未开通');
    });
}

// ===================== 客服聊天 =====================

function initChat() {
    const chatContainer = document.getElementById('chat-container');
    const chatBody = document.getElementById('chat-body');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const closeChatButton = document.getElementById('close-chat');
    const customerServiceIcon = document.getElementById('customer-service-icon');

    customerServiceIcon.addEventListener('click', () => {
        chatContainer.classList.toggle('open');
    });
    closeChatButton.addEventListener('click', () => {
        chatContainer.classList.remove('open');
    });

    function addMessage(sender, content) {
        const div = document.createElement('div');
        div.className = 'message ' + (sender === 'user' ? 'user-message' : 'agent-message');
        div.textContent = content;
        chatBody.appendChild(div);
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    function sendMessage() {
        const message = messageInput.value.trim();
        if (!message) return;
        addMessage('user', message);
        messageInput.value = '';
        fetch('/ai/agent/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: message })
        })
            .then(response => response.json())
            .then(data => {
                const answer = (data.dataMap && data.dataMap.response) || data.response;
                addMessage('agent', answer || '抱歉，暂时无法回答您的问题。');
            })
            .catch(() => {
                addMessage('agent', '抱歉，请求失败，请稍后再试。');
            });
    }

    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    // 欢迎语
    addMessage('agent', '你好！我是智能客服，可以问我商品价格、热销排行、订单查询等问题。');
}

// ===================== 初始化 =====================

window.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    loadHotProducts();
    initFilter();
    initSearch();
    initAuth();
    initCart();
    initChat();
    updateUserUI();
});
