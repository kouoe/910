// ===== 分页状态 =====
const PAGE_SIZE = 10;
let currentPage = 1;
let totalCount = 0;

// 分类映射（与 t_faq.category_id 语义一致）
const CATEGORY_MAP = { 1: '订单', 2: '支付', 3: '商品', 4: '账户', 5: '其他' };

// ===== DOM 引用 =====
const chatBox = document.getElementById('chat-box');
const chatInput = document.getElementById('chat-input');
const chatSend = document.getElementById('chat-send');

const filterCategory = document.getElementById('filter-category');
const filterKeyword = document.getElementById('filter-keyword');
const btnQuery = document.getElementById('btn-query');
const btnSync = document.getElementById('btn-sync');

const faqId = document.getElementById('faq-id');
const faqQuestion = document.getElementById('faq-question');
const faqAnswer = document.getElementById('faq-answer');
const faqCategory = document.getElementById('faq-category');
const faqStatus = document.getElementById('faq-status');
const formTitle = document.getElementById('form-title');
const btnSave = document.getElementById('btn-save');
const btnReset = document.getElementById('btn-reset');

const faqTbody = document.getElementById('faq-tbody');
const btnPrev = document.getElementById('btn-prev');
const btnNext = document.getElementById('btn-next');
const pageInfo = document.getElementById('page-info');

// ===== 工具函数 =====
function addBubble(sender, text) {
    const div = document.createElement('div');
    div.className = 'bubble ' + (sender === 'user' ? 'user' : 'agent');
    div.textContent = text;
    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
}

async function postJson(url, body) {
    const resp = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body || {})
    });
    return resp.json();
}

async function getJson(url) {
    const resp = await fetch(url);
    return resp.json();
}

function showError(data, fallback) {
    alert((data && data.msg) || fallback);
}

function categoryLabel(id) {
    return CATEGORY_MAP[id] || id;
}

// ===== AI 对话 =====
async function sendChat() {
    const message = chatInput.value.trim();
    if (!message) {
        alert('请输入问题');
        return;
    }
    addBubble('user', message);
    chatInput.value = '';

    try {
        const data = await postJson('/ai/agent/chat', { message });
        if (data.code !== 0) {
            addBubble('agent', data.msg || '请求失败');
        } else {
            const answer = (data.dataMap && data.dataMap.response) || data.response;
            addBubble('agent', answer || '抱歉，暂时无法回答。');
        }
    } catch (e) {
        console.error(e);
        addBubble('agent', '请求失败，请稍后再试。');
    }
}

// ===== FAQ 分页查询 =====
async function loadFaqs() {
    const categoryId = filterCategory.value;
    const keyword = filterKeyword.value.trim();
    const params = new URLSearchParams();
    if (categoryId) params.set('categoryId', categoryId);
    if (keyword) params.set('keyword', keyword);

    const url = `/getFaqByPage/${currentPage}/${PAGE_SIZE}?${params.toString()}`;
    const data = await getJson(url);
    if (data.code !== 0) {
        showError(data, '查询失败');
        return;
    }

    const list = data.dataMap.data || [];
    totalCount = data.dataMap.total || 0;
    renderTable(list);
    updatePagination();
}

function renderTable(list) {
    if (!list || list.length === 0) {
        faqTbody.innerHTML = '<tr><td colspan="7" class="empty">暂无数据</td></tr>';
        return;
    }
    faqTbody.innerHTML = list.map(f => `
        <tr>
            <td title="${f.id}">${(f.id || '').substring(0, 8)}…</td>
            <td>${categoryLabel(f.categoryId)}</td>
            <td>${escapeHtml(f.question)}</td>
            <td class="cell-answer" title="${escapeHtml(f.answer)}">${escapeHtml(f.answer)}</td>
            <td>${f.status === 1 ? '启用' : '禁用'}</td>
            <td>${f.useCount || 0}</td>
            <td>
                <div class="op-btns">
                    <button onclick='fillForm(${JSON.stringify(f)})'>编辑</button>
                    <button class="danger" onclick="deleteFaq('${f.id}')">删除</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function updatePagination() {
    const totalPages = Math.max(1, Math.ceil(totalCount / PAGE_SIZE));
    pageInfo.textContent = `第 ${currentPage} / ${totalPages} 页（共 ${totalCount} 条）`;
    btnPrev.disabled = currentPage <= 1;
    btnNext.disabled = currentPage >= totalPages;
}

function escapeHtml(str) {
    return String(str == null ? '' : str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ===== 新增/编辑二合一表单 =====
function fillForm(f) {
    faqId.value = f.id || '';
    faqQuestion.value = f.question || '';
    faqAnswer.value = f.answer || '';
    faqCategory.value = f.categoryId || 1;
    faqStatus.value = f.status == null ? 1 : f.status;
    formTitle.textContent = f.id ? `编辑 FAQ（ID: ${f.id}）` : '新增 FAQ';
}

function resetForm() {
    faqId.value = '';
    faqQuestion.value = '';
    faqAnswer.value = '';
    faqCategory.value = '1';
    faqStatus.value = '1';
    formTitle.textContent = '新增 FAQ';
}

async function saveFaq() {
    const question = faqQuestion.value.trim();
    const answer = faqAnswer.value.trim();
    if (!question || !answer) {
        alert('问题和答案不能为空');
        return;
    }

    const body = {
        id: faqId.value || null,
        categoryId: Number(faqCategory.value),
        question,
        answer,
        status: Number(faqStatus.value)
    };

    const url = faqId.value ? '/update' : '/add';
    const data = await postJson(url, body);
    if (data.code !== 0) {
        showError(data, '保存失败');
        return;
    }
    alert('保存成功');
    resetForm();
    loadFaqs();
}

async function deleteFaq(id) {
    if (!confirm('确认删除该 FAQ？')) return;
    const data = await getJson(`/deleteFaq?faqId=${id}`);
    if (data.code !== 0) {
        showError(data, '删除失败');
        return;
    }
    // 删除当前页最后一条时自动回退上一页
    if ((totalCount - 1) <= (currentPage - 1) * PAGE_SIZE && currentPage > 1) {
        currentPage--;
    }
    loadFaqs();
}

// ===== 全量同步 =====
async function syncToMilvus() {
    if (!confirm('确认全量同步 MySQL → Milvus？')) return;
    btnSync.textContent = '同步中...';
    btnSync.disabled = true;
    try {
        const data = await getJson('/syncToMilvus');
        if (data.code !== 0) {
            showError(data, '同步失败');
        } else {
            alert('同步完成');
        }
    } catch (e) {
        console.error(e);
        alert('同步请求失败');
    } finally {
        btnSync.textContent = '全量同步 MySQL → Milvus';
        btnSync.disabled = false;
    }
}

// ===== 事件绑定 =====
chatSend.addEventListener('click', sendChat);
chatInput.addEventListener('keypress', e => { if (e.key === 'Enter') sendChat(); });

btnQuery.addEventListener('click', () => { currentPage = 1; loadFaqs(); });
filterKeyword.addEventListener('keypress', e => { if (e.key === 'Enter') { currentPage = 1; loadFaqs(); } });
btnSync.addEventListener('click', syncToMilvus);

btnSave.addEventListener('click', saveFaq);
btnReset.addEventListener('click', resetForm);

btnPrev.addEventListener('click', () => { if (currentPage > 1) { currentPage--; loadFaqs(); } });
btnNext.addEventListener('click', () => { currentPage++; loadFaqs(); });

// 初始化
loadFaqs();
