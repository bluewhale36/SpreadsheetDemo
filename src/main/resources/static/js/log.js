/**
 * 아코디언 토글 함수
 * @param {HTMLElement} headerElement - 클릭된 헤더 요소 (.log-date-summary)
 */
function toggleAccordion(headerElement) {
    // 헤더의 부모 요소(.log-date-details)를 찾음
    const container = headerElement.parentElement;

    // open 클래스를 토글 (있으면 제거, 없으면 추가)
    container.classList.toggle('open');
}