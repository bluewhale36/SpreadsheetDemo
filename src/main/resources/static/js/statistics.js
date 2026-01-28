/* src/main/resources/static/js/statistics.js */

/**
 * 조회 기간 유효성 검사
 */
function validateDates() {
    const fromInput = document.getElementById('from');
    const toInput = document.getElementById('to');

    if (!fromInput.value || !toInput.value) {
        alert("시작일과 종료일을 모두 선택해주세요.");
        return false;
    }

    if (fromInput.value > toInput.value) {
        alert("시작일은 종료일보다 클 수 없습니다.");
        return false;
    }
    return true;
}

/**
 * 상세 내역 토글 (아코디언)
 * @param {HTMLButtonElement} btn
 */
function toggleDetails(btn) {
    // 버튼이 있는 행(tr)의 바로 다음 행이 상세 내역 행임
    const currentRow = btn.closest('tr');
    const detailsRow = currentRow.nextElementSibling;

    if (detailsRow && detailsRow.classList.contains('details-row')) {
        const isHidden = detailsRow.style.display === 'none';

        if (isHidden) {
            detailsRow.style.display = 'table-row';
            btn.innerText = '▲'; // 접기 아이콘
            btn.title = '접기';
            currentRow.style.backgroundColor = '#f0f7ff'; // 활성 행 강조
        } else {
            detailsRow.style.display = 'none';
            btn.innerText = '▼'; // 펼치기 아이콘
            btn.title = '상세 보기';
            currentRow.style.backgroundColor = ''; // 배경색 복원
        }
    }
}

/**
 * 변동 없는(입/출고 모두 0) 행 숨기기/보이기
 */
function toggleZeroRows() {
    const checkbox = document.getElementById('hideZero');
    const rows = document.querySelectorAll('.stats-row');

    rows.forEach(row => {
        const stored = parseInt(row.getAttribute('data-stored')) || 0;
        const delivered = parseInt(row.getAttribute('data-delivered')) || 0;

        // 상세 행도 같이 제어해야 함
        const detailsRow = row.nextElementSibling;
        const hasDetails = detailsRow && detailsRow.classList.contains('details-row');

        // 입고와 출고가 모두 0인 경우
        if (stored === 0 && delivered === 0) {
            if (checkbox.checked) {
                row.style.display = 'none';
                if (hasDetails) detailsRow.style.display = 'none';
            } else {
                row.style.display = 'table-row';
                // 상세 행은 원래 닫혀있어야 하므로 여기서 강제로 열지 않음 (사용자 클릭 상태 유지 불가하므로 닫힘 상태로 둠)
                if (hasDetails) detailsRow.style.display = 'none';
                // 만약 체크 해제 시 원래 열려있던 상태를 복구하려면 별도 상태 관리가 필요하지만,
                // 여기서는 닫힌 상태로 복구하는 것이 UX상 깔끔함.
            }
        }
    });
}