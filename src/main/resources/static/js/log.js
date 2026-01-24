// 페이징 상태 관리
let nextCursorDate = null; // 다음 요청 시 보낼 기준 날짜 (fromExclude)
let hasNextPage = false;
let isLoading = false;

document.addEventListener('DOMContentLoaded', () => {
    // 초기 데이터 세팅
    if (typeof initialData !== 'undefined' && initialData) {
        // HerbLogPagination 필드명에 맞춰서 매핑해야 함
        // 서비스 로직 상: toInclude(이번 요청 기준), fromExclude(다음 요청 기준)
        // DTO에 fromExclude 필드가 있다고 가정합니다.
        let endDate = initialData.endDate;
        nextCursorDate = `${endDate.year}-${endDate.monthValue}-${endDate.dayOfMonth}`;
        hasNextPage = initialData.hasNextPage;

        updateMoreButtonState();
    }
});

function toggleAccordion(headerElement) {
    const container = headerElement.parentElement;
    container.classList.toggle('open');
}

/**
 * 더 불러오기
 */
async function loadMore() {
    console.log("loadMore executed");
    console.log(hasNextPage);
    console.log(nextCursorDate);
    if (isLoading || !hasNextPage || !nextCursorDate) return;

    isLoading = true;
    const btn = document.getElementById('btn-load-more');
    const originalBtnText = btn.innerHTML;

    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> 불러오는 중...';
    btn.classList.add('loading');

    try {
        // 날짜 기반 요청 (/api/herb/log/2025-12-23)
        const response = await fetch(`/api/herb/log/${nextCursorDate}`);

        if (!response.ok) throw new Error('데이터 로드 실패');

        const paginationData = await response.json();

        // 데이터 렌더링 (단순 Append)
        renderLogList(paginationData.data);

        // 상태 업데이트
        nextCursorDate = paginationData.endDate; // 다음 기준일 갱신
        hasNextPage = paginationData.hasNextPage;

    } catch (error) {
        console.error('Load More Error:', error);
        alert('추가 데이터를 불러오지 못했습니다.');
    } finally {
        isLoading = false;
        btn.innerHTML = originalBtnText;
        btn.classList.remove('loading');
        updateMoreButtonState();
    }
}

function updateMoreButtonState() {
    const container = document.getElementById('more-btn-container');
    if (container) {
        container.style.display = hasNextPage ? 'flex' : 'none';
    }
}

/**
 * 로그 리스트 렌더링 (단순 추가 방식)
 * 백엔드에서 날짜 단위로 끊어주므로 복잡한 병합 로직 불필요
 */
function renderLogList(logList) {
    const container = document.getElementById('log-list-container');

    if (!logList || logList.length === 0) return;

    let html = '';
    logList.forEach((viewDTO) => {
        html += `
        <div class="log-date-details">
            <div class="log-date-summary" onclick="toggleAccordion(this)">
                <div>
                    <span class="log-date-badge">DATE</span>
                    <span>${viewDTO.loggedDate}</span>
                </div>
            </div>
            <div class="log-content-wrapper">
                <div class="log-content-inner">
                    ${renderHerbCards(viewDTO.herbLogListMapByName)}
                </div>
            </div>
        </div>
        `;
    });

    // 기존 리스트 뒤에 추가
    container.insertAdjacentHTML('beforeend', html);
}

function renderHerbCards(herbLogMap) {
    let html = '';
    for (const [name, logs] of Object.entries(herbLogMap)) {
        html += `
        <div class="log-herb-card">
            <div class="log-herb-name">${name}</div>
            <div class="change-grid">
                <div class="grid-cell grid-header">항목</div>
                <div class="grid-cell grid-header">시각</div>
                <div class="grid-cell grid-header">변경 전</div>
                <div class="grid-cell grid-header">변경 후</div>
                ${renderLogRows(logs)}
            </div>
        </div>
        `;
    }
    return html;
}

function renderLogRows(logs) {
    // 최신 시간순 정렬
    logs.sort((a, b) => new Date(b.loggedDateTime) - new Date(a.loggedDateTime));

    return logs.map(log => {
        const dateObj = new Date(log.loggedDateTime);
        const timeStr = dateObj.toLocaleTimeString('ko-KR', {
            hour: '2-digit', minute: '2-digit', hour12: false
        });

        const diff = log.afterAmount - log.beforeAmount;
        const isIncreased = log.amountIncreased;
        const badgeClass = isIncreased ? 'val-inc' : 'val-dec';
        const sign = isIncreased ? '+' : '';
        const diffText = `(${sign}${diff})`;

        return `
            <div class="grid-cell grid-label">수량</div>
            <div class="grid-cell val-time">${timeStr}</div>
            <div class="grid-cell val-old">${log.beforeAmount}</div>
            <div class="grid-cell">
                <span class="val-badge ${badgeClass}">
                    <span>${log.afterAmount}</span>
                    <span class="val-diff">${diffText}</span>
                </span>
            </div>
        `;
    }).join('');
}