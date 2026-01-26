function openDeleteModal() {
    document.getElementById('deleteConfirmModal').classList.add('open');
}

function closeDeleteModal() {
    document.getElementById('deleteConfirmModal').classList.remove('open');
}

async function executeDelete() {
    if (!currentHerbDTO || !currentHerbDTO.rowNum) {
        alert("약재 정보를 찾을 수 없습니다.");
        return;
    }

    // [중요] 전송용 데이터 복사본 생성
    const payload = { ...currentHerbDTO };

    // [해결책] lastStoredDate가 객체라면 "yyyy-MM-dd" 문자열로 변환
    if (payload.lastStoredDate && typeof payload.lastStoredDate === 'object') {
        const year = payload.lastStoredDate.year;
        // 월/일이 한 자리수일 경우 앞에 0을 붙여야 함 (예: 1 -> 01)
        const month = String(payload.lastStoredDate.monthValue).padStart(2, '0');
        const day = String(payload.lastStoredDate.dayOfMonth).padStart(2, '0');

        payload.lastStoredDate = `${year}-${month}-${day}`;
    }

    try {
        // [수정] Query Parameter 대신 RequestBody 에 DTO 전체를 담아 전송
        const response = await fetch('/herb', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json' // JSON 전송 명시
            },
            body: JSON.stringify(payload) // DTO 객체 직렬화
        });

        if (response.ok) {
            alert('삭제되었습니다.');
            window.location.href = '/herb';
        } else {
            // 에러 메시지 처리 (낙관적 락 오류 등)
            const errorText = await response.text();

            // 만약 서버가 HTML 에러 페이지를 반환하는 경우 처리
            if (response.headers.get("content-type")?.includes("text/html")) {
                document.open();
                document.write(errorText);
                document.close();
                return;
            }

            alert('삭제 실패: ' + errorText);
        }
    } catch (e) {
        console.error(e);
        alert('서버 통신 중 오류가 발생했습니다.');
    }
}