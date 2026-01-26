document.addEventListener('DOMContentLoaded', () => {
    // [추가] 페이지 로드 시 모든 입력 필드에 변경 감지 이벤트 연결
    const inputs = document.querySelectorAll('.date-input, .memo-input, .qty-input');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            const item = this.closest('.herb-item');
            checkModifiedState(item);
        });
        // 날짜 선택 등은 change 이벤트도 감지
        input.addEventListener('change', function() {
            const item = this.closest('.herb-item');
            checkModifiedState(item);
        });
    });
});

// [신규] 해당 행(Row)이 수정되었는지 확인하고 스타일 적용
function checkModifiedState(item) {
    const dateInput = item.querySelector('.date-input');
    const qtyInput = item.querySelector('.qty-input');
    const memoInput = item.querySelector('.memo-input');

    // 원본 값 가져오기
    const originalDate = dateInput.getAttribute('data-original-date') || '-';
    const originalAmount = parseInt(qtyInput.getAttribute('data-original-amount')) || 0;
    const originalMemo = memoInput.getAttribute('data-original-memo') || '';

    // 현재 값 가져오기
    const currDate = dateInput.value || '-';
    const currAmount = parseInt(qtyInput.value) || 0;
    const currMemo = memoInput.value || '';

    // 하나라도 다르면 수정된 것으로 간주
    const isChanged = (originalDate !== currDate) ||
        (originalAmount !== currAmount) ||
        (originalMemo !== currMemo);

    if (isChanged) {
        item.classList.add('modified');
    } else {
        item.classList.remove('modified');
    }
}

// 수량 변경 버튼 핸들러 (수정됨)
function updateQuantity(btn, change) {
    const wrapper = btn.closest('.quantity-control');
    const input = wrapper.querySelector('.qty-input');
    let currentValue = parseInt(input.value) || 0;
    let newValue = currentValue + change;
    if (newValue < 0) newValue = 0;
    input.value = newValue;

    // [추가] 수량 변경 후 상태 체크 트리거
    // 버튼은 quantity-control 안에 있으므로, herb-item을 찾아서 넘김
    const item = btn.closest('.herb-item');
    checkModifiedState(item);
}

// 변경사항 수집 및 모달 표시
function saveChanges() {
    // modal.js에 선언된 pendingChanges 초기화
    pendingChanges = [];

    const items = document.querySelectorAll('.herb-item');
    const listContainer = document.getElementById('changeList');
    listContainer.innerHTML = '';

    items.forEach(item => {
        const rowNum = item.querySelector('[type=hidden]').getAttribute('data-row-num');
        const nameInput = item.querySelector('.item-name');
        const name = nameInput.getAttribute('data-herb-name') || '';

        const dateInput = item.querySelector('.date-input');
        const originalDate = dateInput.getAttribute('data-original-date') || '-';
        const newDate = dateInput.value || '-';

        const qtyInput = item.querySelector('.qty-input');
        const originalAmount = parseInt(qtyInput.getAttribute('data-original-amount')) || 0;
        const newAmount = parseInt(qtyInput.value) || 0;

        const memoInput = item.querySelector('.memo-input');
        let originalMemo = memoInput.getAttribute('data-original-memo');
        if (!originalMemo || originalMemo.trim() === '') {
            originalMemo = null;
        }

        let newMemo = memoInput.value;
        if (!newMemo || newMemo.trim() === '') {
            newMemo = null;
        }

        // 비교 로직도 null 체크를 고려하여 수행
        const isDateChanged = originalDate !== newDate;
        const isQtyChanged = originalAmount !== newAmount;
        const isMemoChanged = originalMemo !== newMemo;

        if (isDateChanged || isQtyChanged || isMemoChanged) {
            const changeData = {
                rowNum: parseInt(rowNum),
                name: name,
                originalLastStoredDate: originalDate,
                newLastStoredDate: newDate,
                originalAmount: originalAmount,
                newAmount: newAmount,
                originalMemo: originalMemo,
                newMemo: newMemo
            };
            pendingChanges.push(changeData);

            // HTML 생성 (그리드 구조)
            const li = document.createElement('li');
            li.className = 'change-item';

            let htmlContent = `<div class="change-name">${name}</div>`;
            htmlContent += `
                <div class="change-grid">
                    <div class="grid-cell grid-header">항목</div>
                    <div class="grid-cell grid-header">변경 전</div>
                    <div class="grid-cell grid-header">변경 후</div>
            `;

            if (isDateChanged) {
                htmlContent += `
                    <div class="grid-cell grid-label">입고일</div>
                    <div class="grid-cell val-old">${originalDate}</div>
                    <div class="grid-cell"><span class="val-new">${newDate}</span></div>
                `;
            }

            if (isQtyChanged) {
                htmlContent += `
                    <div class="grid-cell grid-label">수량</div>
                    <div class="grid-cell val-old">${originalAmount}</div>
                    <div class="grid-cell"><span class="val-new">${newAmount}</span></div>
                `;
            }

            if (isMemoChanged) {
                const oldText = originalMemo ? originalMemo : '(없음)';
                const newText = newMemo ? newMemo : '(없음)';
                htmlContent += `
                    <div class="grid-cell grid-label">메모</div>
                    <div class="grid-cell val-old">${oldText}</div>
                    <div class="grid-cell"><span class="val-new">${newText}</span></div>
                `;
            }

            htmlContent += `</div>`;
            li.innerHTML = htmlContent;
            listContainer.appendChild(li);
        }
    });

    if (pendingChanges.length === 0) {
        alert("변경사항이 없습니다.");
        return;
    }

    document.getElementById('confirmModal').classList.add('open');
}

// --- 신규 등록 모달 관련 로직 ---

// 1. 모달 열기 (초기화 로직에 에러 스타일 제거 추가)
function openRegisterModal() {
    const modal = document.getElementById('registerModal');
    const dateInput = document.getElementById('regDate');

    document.getElementById('registerForm').reset();

    // [추가] 이전에 떠있던 에러 메시지 및 붉은 테두리 초기화
    document.querySelectorAll('.form-input').forEach(input => input.classList.remove('input-error'));
    document.querySelectorAll('.error-msg').forEach(msg => msg.classList.remove('show'));

    const today = new Date().toISOString().split('T')[0];
    dateInput.value = today;

    modal.classList.add('open');
}

// [신규] 등록 모달 로딩 상태 제어 함수
function setRegisterLoadingState(isLoading) {
    const submitBtn = document.getElementById('regSubmitBtn');
    const cancelBtn = document.getElementById('regCancelBtn');
    const btnText = submitBtn.querySelector('.btn-text');

    if (isLoading) {
        // 로딩 시작: 버튼 비활성화, 스피너 표시, 텍스트 변경
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
        cancelBtn.disabled = true;
        btnText.innerText = "등록 중...";

        // (선택사항) 입력창들도 비활성화하여 수정 방지
        document.querySelectorAll('#registerForm .form-input').forEach(input => input.disabled = true);
    } else {
        // 로딩 종료: 상태 원복
        submitBtn.classList.remove('loading');
        submitBtn.disabled = false;
        cancelBtn.disabled = false;
        btnText.innerText = "등록하기";

        // 입력창 비활성화 해제
        document.querySelectorAll('#registerForm .form-input').forEach(input => input.disabled = false);
    }
}

// 2. 등록 요청 전송 (유효성 검사 로직 강화)
async function submitRegistration() {
    const nameInput = document.getElementById('regName');
    const dateInput = document.getElementById('regDate');
    const amountInput = document.getElementById('regAmount');
    const memoInput = document.getElementById('regMemo');

    const name = nameInput.value.trim();
    const date = dateInput.value;
    const amount = amountInput.value;
    const memo = memoInput.value.trim();

    let isValid = true;

    // --- [수정됨] 유효성 검사 헬퍼 함수 ---
    function setError(input, msgId, show) {
        const msgEl = document.getElementById(msgId);
        if (show) {
            // 1. 이미 에러 클래스가 있다면 제거하여 애니메이션 상태 초기화
            if (input.classList.contains('input-error')) {
                input.classList.remove('input-error');

                // 2. Reflow 강제 (브라우저가 스타일 변경을 인지하고 다시 그리도록 함)
                // 이 줄이 없으면 브라우저 최적화 때문에 제거->추가가 하나의 동작으로 합쳐져 애니메이션이 안 뜹니다.
                void input.offsetWidth;
            }

            // 3. 클래스 다시 추가 (애니메이션 재시작)
            input.classList.add('input-error');
            msgEl.classList.add('show');
        } else {
            input.classList.remove('input-error');
            msgEl.classList.remove('show');
        }
    }

    // 1. 이름 검사
    if (!name) {
        setError(nameInput, 'msg-name', true);
        isValid = false;
    } else {
        setError(nameInput, 'msg-name', false);
    }

    // 2. 날짜 검사
    if (!date) {
        setError(dateInput, 'msg-date', true);
        isValid = false;
    } else {
        setError(dateInput, 'msg-date', false);
    }

    // 3. 수량 검사
    if (amount === '' || amount === null) {
        setError(amountInput, 'msg-amount', true);
        isValid = false;
    } else {
        setError(amountInput, 'msg-amount', false);
    }

    if (!isValid) {
        return;
    }

    // --- 데이터 전송 시작 ---

    // 1. 로딩 상태 활성화 (버튼 잠금, 스피너 표시)
    setRegisterLoadingState(true);

    const registerData = {
        name: nameInput.value.trim(),
        amount: parseInt(amountInput.value),
        lastStoredDate: dateInput.value,
        memo: memoInput.value.trim()
    };

    try {
        const response = await fetch('/herb', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
        });

        if (response.ok) {
            alert("신규 약재가 등록되었습니다.");
            location.reload(); // 성공 시 새로고침하므로 로딩 해제 불필요
        } else {
            // [추가된 부분] 에러 페이지 렌더링 처리
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("text/html")) {
                const html = await response.text();
                document.open();
                document.write(html);
                document.close();
                return;
            }

            const errorText = await response.text();
            alert("등록 실패: " + errorText);
            setRegisterLoadingState(false);
        }
    } catch (error) {
        console.error('Registration Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
        // 에러 발생 시 로딩 해제
        setRegisterLoadingState(false);
    }
}

// 2. 모달 닫기
function closeRegisterModal() {
    document.getElementById('registerModal').classList.remove('open');
}

