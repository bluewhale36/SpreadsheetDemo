// 현재 페이지 상태 관리
let currentPage = 1;
let hasNextPage = false;
let isLoading = false;

document.addEventListener('DOMContentLoaded', () => {
    // Thymeleaf에서 전달받은 초기 데이터로 상태 초기화
    if (typeof initialData !== 'undefined' && initialData) {
        currentPage = initialData.pageNum;
        hasNextPage = initialData.hasNextPage;
        updateMoreButtonState();
    }
});

/**
 * 아코디언 토글 함수
 */
function toggleAccordion(headerElement) {
    const container = headerElement.parentElement;
    container.classList.toggle('open');
}

/**
 * 이력 더 불러오기 (Load More)
 */
async function loadMore() {
    if (isLoading || !hasNextPage) return;

    isLoading = true;
    const btn = document.getElementById('btn-load-more');
    const originalBtnText = btn.innerHTML;

    // 로딩 UI
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> 불러오는 중...';
    btn.classList.add('loading');

    try {
        const nextPage = currentPage + 1;
        // API 엔드포인트 호출
        const response = await fetch(`/api/herb/log/${nextPage}`);

        if (!response.ok) throw new Error('데이터 로드 실패');

        const paginationData = await response.json();

        // [핵심] 데이터 렌더링 (Merge 모드 = true)
        renderLogList(paginationData.data, true);

        // 상태 업데이트
        currentPage = paginationData.pageNum;
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
 * 로그 리스트 렌더링 (병합 및 정렬 로직 포함)
 */
function renderLogList(logList, isAppend = false) {
    const container = document.getElementById('log-list-container');

    if (!logList || logList.length === 0) {
        if (!isAppend) container.innerHTML = '<div class="empty-logs"><p>기록된 변경 이력이 없습니다.</p></div>';
        return;
    }

    // 1. 이어붙이기(Load More) 모드일 때의 병합 로직
    if (isAppend) {
        const lastElement = container.lastElementChild;

        // 마지막 요소가 로그 아코디언인지 확인
        if (lastElement && lastElement.classList.contains('log-date-details')) {
            const lastDateText = lastElement.querySelector('.log-date-summary span:last-child').innerText.trim();
            const newFirstItem = logList[0];

            // [Condition 1] 날짜가 동일하다면 -> 일자 병합 시작
            if (lastDateText === newFirstItem.loggedDate.toString()) {
                const contentInner = lastElement.querySelector('.log-content-inner');

                // 해당 일자 내의 약재 Map 순회
                for (const [herbName, logs] of Object.entries(newFirstItem.herbLogListMapByName)) {

                    // [Condition 2] 같은 약재 카드가 이미 있는지 찾기
                    const existingHerbCards = Array.from(contentInner.querySelectorAll('.log-herb-card'));
                    const targetCard = existingHerbCards.find(card => {
                        const nameEl = card.querySelector('.log-herb-name');
                        return nameEl && nameEl.innerText.trim() === herbName;
                    });

                    if (targetCard) {
                        // Case A: 약재 카드가 존재함 -> 그리드에 행 추가
                        const grid = targetCard.querySelector('.change-grid');
                        const newRowsHtml = renderLogRows(logs);
                        grid.insertAdjacentHTML('beforeend', newRowsHtml);

                        // [Condition 3] 시각 기준 내림차순 정렬 (데이터 섞임 방지)
                        sortGridByTimeDesc(grid);

                    } else {
                        // Case B: 약재 카드가 없음 -> 새 카드 생성 후 추가
                        const newCardHtml = createHerbCardHtml(herbName, logs);
                        contentInner.insertAdjacentHTML('beforeend', newCardHtml);
                    }
                }

                // 첫 번째 데이터는 병합 처리했으므로 리스트에서 제거
                logList.shift();
            }
        }
    }

    // 2. 남은 데이터(날짜가 다르거나, 병합되고 남은 것들) 렌더링
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

    if (isAppend) {
        container.insertAdjacentHTML('beforeend', html);
    } else {
        container.innerHTML = html;
    }
}

/**
 * 약재 카드 전체 HTML 생성
 */
function renderHerbCards(herbLogMap) {
    let html = '';
    for (const [name, logs] of Object.entries(herbLogMap)) {
        html += createHerbCardHtml(name, logs);
    }
    return html;
}

/**
 * 단일 약재 카드 HTML 생성
 */
function createHerbCardHtml(name, logs) {
    return `
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

/**
 * 그리드 내부 상세 Row(4개 div) HTML 생성
 */
function renderLogRows(logs) {
    return logs.map(log => {
        // 시간 포맷 (HH:mm)
        const dateObj = new Date(log.loggedDatetime);
        const timeStr = dateObj.toLocaleTimeString('ko-KR', {
            hour: '2-digit', minute: '2-digit', hour12: false
        });
        // 정렬용 타임스탬프
        const timestamp = dateObj.getTime();

        const diff = log.afterAmount - log.beforeAmount;
        const isIncreased = log.amountIncreased;
        const badgeClass = isIncreased ? 'val-inc' : 'val-dec';
        const sign = isIncreased ? '+' : '';
        const diffText = `(${sign}${diff})`;

        // data-timestamp 속성 추가
        return `
            <div class="grid-cell grid-label">수량</div>
            <div class="grid-cell val-time" data-timestamp="${timestamp}">${timeStr}</div>
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

/**
 * 그리드 내부 행 시각 내림차순 정렬 함수
 */
function sortGridByTimeDesc(gridElement) {
    // 1. 헤더 4개 제외하고 데이터 셀들만 가져오기
    const allChildren = Array.from(gridElement.children);
    const headers = allChildren.slice(0, 4);
    const dataCells = allChildren.slice(4);

    // 2. 4개 단위(Row)로 그룹핑
    const rows = [];
    for (let i = 0; i < dataCells.length; i += 4) {
        const chunk = dataCells.slice(i, i + 4);
        if (chunk.length === 4) {
            // 시각 정보를 가진 두 번째 셀(index 1)에서 timestamp 추출
            const timeCell = chunk[1];
            const timestamp = parseInt(timeCell.dataset.timestamp || '0');
            rows.push({
                timestamp: timestamp,
                elements: chunk
            });
        }
    }

    // 3. 내림차순 정렬 (최신 시간이 위로)
    rows.sort((a, b) => b.timestamp - a.timestamp);

    // 4. DOM 다시 그리기
    gridElement.innerHTML = '';
    headers.forEach(h => gridElement.appendChild(h));
    rows.forEach(row => {
        row.elements.forEach(el => gridElement.appendChild(el));
    });
}