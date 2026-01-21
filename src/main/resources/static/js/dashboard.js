// dashboard.js

document.addEventListener('DOMContentLoaded', () => {
    // 추후 서버에서 데이터를 가져올 때 사용할 함수 예시
    loadDashboardStats();
});

function loadDashboardStats() {
    console.log("Dashboard loaded. Waiting for API integration...");

    // 예: 재고 부족 알림이 0개일 경우 '비어있음' 메시지 표시 로직
    const alerts = document.querySelectorAll('.alert-item');
    const emptyMsg = document.querySelector('.alert-empty');

    if (alerts.length === 0 && emptyMsg) {
        emptyMsg.style.display = 'block';
    }
}