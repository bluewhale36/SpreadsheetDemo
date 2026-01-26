// [추가] 중복 검사를 위한 기존 약재 이름 목록 저장용 Set
let existingHerbNames = new Set();
// [추가] 이름 유효성 상태 플래그
let isNameValid = false;

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

    // [추가] 약재명 입력 시 실시간 유효성 검사 연결
    const regNameInput = document.getElementById('regName');
    if (regNameInput) {
        regNameInput.addEventListener('input', validateRegName);
    }
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

// 수량 변경 버튼 핸들러
function updateQuantity(btn, change) {
    const wrapper = btn.closest('.quantity-control');
    const input = wrapper.querySelector('.qty-input');
    let currentValue = parseInt(input.value) || 0;
    let newValue = currentValue + change;
    if (newValue < 0) newValue = 0;
    input.value = newValue;

    // [추가] 수량 변경 후 상태 체크 트리거
    const item = btn.closest('.herb-item');
    checkModifiedState(item);
}

// 변경사항 수집 및 모달 표시
function saveChanges() {
    pendingChanges = [];

    const items = document.querySelectorAll('.herb-item');
    const listContainer = document.getElementById('changeList');
    listContainer.innerHTML = '';

    items.forEach(item => {
        const rowNum = item.querySelector('[type=hidden]').getAttribute('data-row-num');
        const nameInput = item.querySelector('.item-name');

        // item-name 내부의 a 태그나 텍스트에서 이름을 가져오도록 수정 (HTML 구조에 따라 조정 필요)
        // 기존 코드: const name = nameInput.getAttribute('data-herb-name') || '';
        // details 링크가 추가된 구조라면 innerText 등을 사용해야 할 수 있음. 
        // 여기서는 안전하게 innerText를 trim해서 사용하거나 기존 로직 유지.
        const name = nameInput.getAttribute('data-herb-name').trim() || null;

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

/**
 * 신규 등록 모달 열기
 */
async function openRegisterModal() {
    // 1. 폼 초기화
    document.getElementById('regName').value = '';
    document.getElementById('regAmount').value = '';
    document.getElementById('regDate').value = new Date().toISOString().substring(0, 10);
    document.getElementById('regMemo').value = '';

    // 에러 메시지 초기화
    clearErrorMsg('name');
    clearErrorMsg('amount');
    clearErrorMsg('date');

    // 입력창 스타일 초기화
    document.querySelectorAll('#registerForm .form-input').forEach(input => {
        input.classList.remove('input-error');
    });
    isNameValid = false;

    // 2. 기존 약재 이름 목록 비동기 조회
    try {
        const response = await fetch('/api/herb/all/name'); // [주의] Controller 매핑 주소 확인 필요
        if (response.ok) {
            const names = await response.json();
            existingHerbNames = new Set(names);
            console.log("Loaded herb names:", existingHerbNames);
        } else {
            console.error("Failed to fetch herb names");
        }
    } catch (e) {
        console.error("Error fetching herb names:", e);
    }

    // 3. 모달 표시
    document.getElementById('registerModal').classList.add('open');
}

/**
 * 약재명 유효성 검사 (중복 및 빈 값 체크)
 */
function validateRegName() {
    const input = document.getElementById('regName');
    const msgBox = document.getElementById('msg-name');
    const name = input.value.trim();

    // 1. 빈 값 체크
    if (name === '') {
        msgBox.innerText = '약재 이름을 입력해주세요.';
        msgBox.style.display = 'block'; // [통일] display 속성 직접 제어
        input.classList.add('input-error');
        isNameValid = false;
        return;
    }

    // 2. 중복 체크
    if (existingHerbNames.has(name)) {
        msgBox.innerText = '이미 존재하는 약재 이름입니다.';
        msgBox.style.display = 'block'; // [통일] display 속성 직접 제어
        input.classList.add('input-error');
        isNameValid = false;
        return;
    }

    // 3. 유효함
    msgBox.style.display = 'none';
    input.classList.remove('input-error');
    isNameValid = true;
}

// 등록 모달 로딩 상태 제어 함수
function setRegisterLoadingState(isLoading) {
    const submitBtn = document.getElementById('regSubmitBtn');
    const cancelBtn = document.getElementById('regCancelBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const inputs = document.querySelectorAll('#registerForm .form-input');

    if (isLoading) {
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
        cancelBtn.disabled = true;
        btnText.innerText = "등록 중...";
        inputs.forEach(input => input.disabled = true);
    } else {
        submitBtn.classList.remove('loading');
        submitBtn.disabled = false;
        cancelBtn.disabled = false;
        btnText.innerText = "등록하기";
        inputs.forEach(input => input.disabled = false);
    }
}

// 2. 등록 요청 전송 (유효성 검사 로직 수정됨)
async function submitRegistration() {
// 1. 이름 유효성 재확인
    validateRegName();
    // (여기서 return 하지 않음)

    const nameInput = document.getElementById('regName');
    const dateInput = document.getElementById('regDate');
    const amountInput = document.getElementById('regAmount');
    const memoInput = document.getElementById('regMemo');

    // [핵심 수정] 애니메이션 재시작을 위한 setError 함수 개선
    function setError(input, msgId, isError) {
        const msgEl = document.getElementById(msgId);

        if (isError) {
            // 1. 기존 에러 클래스 제거 (애니메이션 리셋 준비)
            if (input.classList.contains('input-error')) {
                input.classList.remove('input-error');

                // 2. [중요] 강제 리플로우(Reflow) 발생
                // offsetWidth를 읽으면 브라우저는 현재 스타일을 계산하기 위해
                // 렌더링 큐를 비우고 화면을 다시 그리려 합니다.
                void input.offsetWidth;
            }

            // 3. 클래스 다시 추가 (애니메이션 시작)
            input.classList.add('input-error');
            msgEl.style.display = 'block';
        } else {
            input.classList.remove('input-error');
            msgEl.style.display = 'none';
        }
    }

    // --- 유효성 검사 실행 ---
    let isNameValidFinal = true;
    let isDateValid = true;
    let isAmountValid = true;

    // 1. 이름 검사 (전역변수 isNameValid 활용)
    // validateRegName()이 실행되었으므로 UI는 업데이트되었지만,
    // submit 버튼을 눌렀을 때도 흔들림 효과를 주기 위해 강제로 setError 호출
    if (!nameInput.value.trim() || !isNameValid) {
        setError(nameInput, 'msg-name', true);
        isNameValidFinal = false;
    } else {
        setError(nameInput, 'msg-name', false);
    }

    // 2. 날짜 검사
    if (!dateInput.value) {
        setError(dateInput, 'msg-date', true);
        isDateValid = false;
    } else {
        setError(dateInput, 'msg-date', false);
    }

    // 3. 수량 검사
    const amountVal = parseInt(amountInput.value);
    if (amountInput.value === '' || isNaN(amountVal) || amountVal < 0) {
        setError(amountInput, 'msg-amount', true);
        isAmountValid = false;
    } else {
        setError(amountInput, 'msg-amount', false);
    }

    // 종합 판단
    if (!isNameValidFinal || !isDateValid || !isAmountValid) {
        return;
    }

    // --- 데이터 전송 시작 ---
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
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(registerData)
        });

        if (response.ok) {
            alert("신규 약재가 등록되었습니다.");
            location.reload();
        } else {
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
        setRegisterLoadingState(false);
    }
}

// 2. 모달 닫기
function closeRegisterModal() {
    document.getElementById('registerModal').classList.remove('open');
}

// 헬퍼 함수: 에러 메시지 숨김
function clearErrorMsg(type) {
    const el = document.getElementById(`msg-${type}`);
    if (el) el.style.display = 'none';
}