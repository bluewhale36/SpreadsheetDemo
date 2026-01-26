document.addEventListener('DOMContentLoaded', () => {
    // 초기화 로직 (필요시)
});

/**
 * 아코디언 토글 기능
 */
function toggleAccordion(headerElement) {
    const container = headerElement.parentElement;
    container.classList.toggle('open');
}