/* src/main/resources/static/js/details.js */

function openDeleteModal() {
    document.getElementById('deleteConfirmModal').classList.add('open');
}

function closeDeleteModal() {
    // 로딩 중일 때는 닫기 방지 (선택 사항)
    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn.disabled) return;

    document.getElementById('deleteConfirmModal').classList.remove('open');
}

async function executeDelete() {
    if (!currentHerbDTO || !currentHerbDTO.rowNum) {
        alert("약재 정보를 찾을 수 없습니다.");
        return;
    }

    // 1. 버튼 요소 가져오기
    const deleteBtn = document.getElementById('deleteBtn');
    const deleteBtnText = deleteBtn.querySelector('.btn-text'); // [추가] 텍스트 요소 선택
    const cancelBtn = document.getElementById('deleteCancelBtn');

    // 2. 로딩 상태 시작 (UI 잠금)
    deleteBtn.classList.add('loading'); // CSS에서 스피너 표시 처리됨
    deleteBtn.disabled = true;          // 중복 클릭 방지
    cancelBtn.disabled = true;          // 취소 방지

    // [추가] 텍스트 변경
    if (deleteBtnText) {
        deleteBtnText.innerText = '삭제 중...';
    }

    // 3. 전송 데이터 준비 (날짜 변환 포함)
    const payload = { ...currentHerbDTO };

    if (payload.lastStoredDate && typeof payload.lastStoredDate === 'object') {
        const year = payload.lastStoredDate.year;
        const month = String(payload.lastStoredDate.monthValue).padStart(2, '0');
        const day = String(payload.lastStoredDate.dayOfMonth).padStart(2, '0');
        payload.lastStoredDate = `${year}-${month}-${day}`;
    }

    try {
        const response = await fetch('/herb', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert('삭제되었습니다.');
            window.location.href = '/herb';
        } else {
            const errorText = await response.text();

            if (response.headers.get("content-type")?.includes("text/html")) {
                document.open();
                document.write(errorText);
                document.close();
                return;
            }

            alert('삭제 실패: ' + errorText);

            // 실패 시 UI 잠금 해제 (다시 시도 가능하도록)
            resetLoadingState();
        }
    } catch (e) {
        console.error(e);
        alert('서버 통신 중 오류가 발생했습니다.');

        // 에러 시 UI 잠금 해제
        resetLoadingState();
    }

    // 로딩 상태 초기화 헬퍼 함수
    function resetLoadingState() {
        deleteBtn.classList.remove('loading');
        deleteBtn.disabled = false;
        cancelBtn.disabled = false;

        // [추가] 텍스트 원복
        if (deleteBtnText) {
            deleteBtnText.innerText = '삭제하기';
        }
    }
}