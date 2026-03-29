document.addEventListener('DOMContentLoaded', () => {
    const tableBody   = document.getElementById('memberTableBody');
    const pagination  = document.getElementById('pagination');
    const searchInput = document.getElementById('searchInput');
    const searchBtn   = document.getElementById('searchBtn');
    const resetBtn    = document.getElementById('resetBtn');

    let currentPage    = 0;
    let currentKeyword = '';
    const PAGE_SIZE    = 20;

    async function loadMembers(page, keyword) {
        tableBody.innerHTML = '<tr><td colspan="8" class="center-cell">불러오는 중...</td></tr>';
        pagination.innerHTML = '';

        try {
            let res;
            if (keyword) {
                res = await axios.get('/api/admin/members/search', {
                    params: { keyword, page, size: PAGE_SIZE }
                });
            } else {
                res = await axios.get('/api/admin/members', {
                    params: { page, size: PAGE_SIZE }
                });
            }

            const data = res.data;
            renderTable(data.content);
            renderPagination(data.totalPages, data.number);
        } catch (err) {
            const status = err.response?.status;
            if (status === 401 || status === 403) {
                tableBody.innerHTML = '<tr><td colspan="8" class="center-cell">접근 권한이 없습니다.</td></tr>';
            } else {
                tableBody.innerHTML = '<tr><td colspan="8" class="center-cell">데이터를 불러오지 못했습니다.</td></tr>';
            }
        }
    }

    function renderTable(members) {
        if (!members || members.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="center-cell">조회된 유저가 없습니다.</td></tr>';
            return;
        }

        tableBody.innerHTML = members.map(m => {
            const isAdmin = (m.memberRole === 'ROLE_ADMIN' || m.memberRole === 'ADMIN');
            const roleBadge = isAdmin
                ? `<span class="role-badge role-admin">관리자</span>`
                : `<span class="role-badge role-user">일반</span>`;

            return `
                <tr>
                    <td>${m.memberNo}</td>
                    <td>${escape(m.memberId)}</td>
                    <td>${escape(m.memberName ?? '-')}</td>
                    <td>${escape(m.memberEmail ?? '-')}</td>
                    <td>${escape(m.memberPhone ?? '-')}</td>
                    <td>${escape(m.memberAddress ?? '-')}</td>
                    <td>${roleBadge}</td>
                    <td>${(m.memberPoint ?? 0).toLocaleString()} P</td>
                </tr>`;
        }).join('');
    }

    function renderPagination(totalPages, currentPageNum) {
        if (totalPages <= 1) return;

        const MAX_VISIBLE = 5;
        const half = Math.floor(MAX_VISIBLE / 2);
        let startPage = Math.max(0, currentPageNum - half);
        let endPage   = Math.min(totalPages - 1, startPage + MAX_VISIBLE - 1);
        if (endPage - startPage < MAX_VISIBLE - 1) {
            startPage = Math.max(0, endPage - MAX_VISIBLE + 1);
        }

        const buttons = [];

        const prevBtn = document.createElement('button');
        prevBtn.textContent = '이전';
        prevBtn.disabled = currentPageNum === 0;
        prevBtn.addEventListener('click', () => goToPage(currentPageNum - 1));
        buttons.push(prevBtn);

        for (let i = startPage; i <= endPage; i++) {
            const btn = document.createElement('button');
            btn.textContent = i + 1;
            if (i === currentPageNum) btn.classList.add('active');
            btn.addEventListener('click', () => goToPage(i));
            buttons.push(btn);
        }

        const nextBtn = document.createElement('button');
        nextBtn.textContent = '다음';
        nextBtn.disabled = currentPageNum >= totalPages - 1;
        nextBtn.addEventListener('click', () => goToPage(currentPageNum + 1));
        buttons.push(nextBtn);

        buttons.forEach(b => pagination.appendChild(b));
    }

    function goToPage(page) {
        currentPage = page;
        loadMembers(currentPage, currentKeyword);
    }

    function escape(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    searchBtn.addEventListener('click', () => {
        currentKeyword = searchInput.value.trim();
        currentPage    = 0;
        loadMembers(currentPage, currentKeyword);
    });

    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') searchBtn.click();
    });

    resetBtn.addEventListener('click', () => {
        searchInput.value = '';
        currentKeyword    = '';
        currentPage       = 0;
        loadMembers(currentPage, currentKeyword);
    });

    loadMembers(currentPage, currentKeyword);
});
