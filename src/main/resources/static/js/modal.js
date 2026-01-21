// 전역 변수로 변경사항 임시 저장 (inventory.js에서 데이터 push)
let pendingChanges = [];

// 모달 닫기
function closeModal() {
    document.getElementById('confirmModal').classList.remove('open');
    pendingChanges = []; // 데이터 초기화
}

// UI 로딩 상태 제어 함수
function setLoadingState(isLoading) {
    const saveBtn = document.getElementById('modalSaveBtn');
    const cancelBtn = document.getElementById('modalCancelBtn');
    const btnText = saveBtn.querySelector('.btn-text');

    if (isLoading) {
        saveBtn.classList.add('loading');
        saveBtn.disabled = true;
        cancelBtn.disabled = true;
        btnText.innerText = "저장 중...";
    } else {
        saveBtn.classList.remove('loading');
        saveBtn.disabled = false;
        cancelBtn.disabled = false;
        btnText.innerText = "저장하기";
    }
}

// 실제 저장 실행 (API 호출)
async function executeSave() {
    if (pendingChanges.length === 0) return;

    setLoadingState(true);

    try {
        const response = await fetch('/herb', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(pendingChanges)
        });

        if (response.ok) {
            alert("성공적으로 저장되었습니다.");
            location.reload();
        } else {
            // 2. 실패 시 처리 로직 개선
            const contentType = response.headers.get("content-type");

            // (A) 응답이 HTML 페이지인 경우 (우리가 만든 에러 페이지가 온 경우)
            if (contentType && contentType.includes("text/html")) {
                const html = await response.text();

                // 현재 문서를 에러 페이지 HTML로 덮어쓰기
                document.open();
                document.write(html);
                document.close();
                return;
            }

            // (B) 그 외 일반 텍스트 에러인 경우
            const errorText = await response.text();
            throw new Error(errorText || "알 수 없는 오류");
        }
    } catch (error) {
        console.error('Save Error:', error);
        alert("저장 실패: " + error.message);
        setLoadingState(false);
    }
}